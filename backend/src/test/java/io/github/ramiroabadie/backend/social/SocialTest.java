package io.github.ramiroabadie.backend.social;

import com.jayway.jsonpath.JsonPath;
import io.github.ramiroabadie.backend.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los criterios de aceptación de HU-15, HU-16 y HU-17, ejecutables. El que vigila la regla difícil
 * es {@link #elFeedTraeLoDeLosSeguidosYNadaMas()}: el feed es una composición de tres módulos que no
 * se conocen (D29), y la primera "optimización" que se le ocurre a cualquiera —una tabla de feed,
 * o un join entre registros y seguimientos— rompe el límite que sostiene toda la arquitectura.
 *
 * <p>La base es la misma para toda la clase y para las demás, así que cada test usa sus propias
 * cuentas y sus propias producciones en vez de limpiar entre medio. Por eso el test del feed
 * global mira quién está primero y no cuántos hay: lo global incluye lo que dejaron los otros
 * tests.</p>
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class SocialTest {

	private static final String TOKEN_CSRF = "un-token-de-prueba";

	@Autowired
	private MockMvc mockMvc;

	/** Solo para fabricar la colisión de instantes que la API no deja provocar a mano. */
	@Autowired
	private JdbcTemplate jdbc;

	/** HU-15: el botón, los dos contadores y la vuelta atrás. Sin aprobación de por medio (D21). */
	@Test
	void seguirYDejarDeSeguirSeVeEnLosContadoresYEnElBoton() throws Exception {
		MockHttpSession marina = cuenta("marina");
		cuenta("dario");

		// Sin sesión no hay botón que mostrar, así que no hay estado que devolver.
		mockMvc.perform(get("/api/usuarios/dario"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.seguidores").value(0))
				.andExpect(jsonPath("$.seguidos").value(0))
				.andExpect(jsonPath("$.loSigo").isEmpty());

		mockMvc.perform(conCsrf(post("/api/usuarios/dario/seguir")).session(marina))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/usuarios/dario").session(marina))
				.andExpect(jsonPath("$.seguidores").value(1))
				.andExpect(jsonPath("$.loSigo").value(true));
		// En el perfil propio tampoco hay botón, y el contador que se mueve es el otro.
		mockMvc.perform(get("/api/usuarios/marina").session(marina))
				.andExpect(jsonPath("$.seguidos").value(1))
				.andExpect(jsonPath("$.seguidores").value(0))
				.andExpect(jsonPath("$.loSigo").isEmpty());

		mockMvc.perform(conCsrf(delete("/api/usuarios/dario/seguir")).session(marina))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/usuarios/dario").session(marina))
				.andExpect(jsonPath("$.seguidores").value(0))
				.andExpect(jsonPath("$.loSigo").value(false));
	}

	/** El toggle es idempotente: el doble clic es lo normal, no un error. Y nadie se sigue solo. */
	@Test
	void seguirDosVecesEsSeguirUnaYNadieSeSigueASiMismo() throws Exception {
		MockHttpSession valeria = cuenta("valeria");
		cuenta("nestor");

		mockMvc.perform(conCsrf(post("/api/usuarios/valeria/seguir")).session(valeria))
				.andExpect(status().isBadRequest());

		mockMvc.perform(conCsrf(post("/api/usuarios/nestor/seguir")).session(valeria))
				.andExpect(status().isNoContent());
		mockMvc.perform(conCsrf(post("/api/usuarios/nestor/seguir")).session(valeria))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/usuarios/nestor"))
				.andExpect(jsonPath("$.seguidores").value(1));

		mockMvc.perform(conCsrf(delete("/api/usuarios/nestor/seguir")).session(valeria))
				.andExpect(status().isNoContent());
		mockMvc.perform(conCsrf(delete("/api/usuarios/nestor/seguir")).session(valeria))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/usuarios/nestor"))
				.andExpect(jsonPath("$.seguidores").value(0));
	}

	/**
	 * HU-16: el feed es lo que registraron los que sigo. Ni lo de los desconocidos ni lo mío
	 * propio —para eso está mi diario—, y con el autor y la obra puestos, que es lo que hace que
	 * una línea del feed se pueda leer sin pedir nada más.
	 */
	@Test
	void elFeedTraeLoDeLosSeguidosYNadaMas() throws Exception {
		Long laDeCiro = crearProduccion("La que registró quien sigo");
		Long laDeOtros = crearProduccion("La que registró quien no sigo");
		MockHttpSession paula = cuenta("paula");
		MockHttpSession ciro = cuenta("ciro");
		MockHttpSession lucrecia = cuenta("lucrecia");

		registrar(ciro, laDeCiro, 8, "Lo mejor que vi en el año");
		registrar(lucrecia, laDeOtros, 3, null);
		registrar(paula, laDeOtros, 5, null);
		seguir(paula, "ciro");

		mockMvc.perform(get("/api/feed").session(paula))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.global").value(false))
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].autor").value("ciro"))
				.andExpect(jsonPath("$.items[0].produccion.titulo").value("La que registró quien sigo"))
				.andExpect(jsonPath("$.items[0].produccion.enCatalogo").value(true))
				.andExpect(jsonPath("$.items[0].rating").value(8))
				.andExpect(jsonPath("$.items[0].resenia").value("Lo mejor que vi en el año"))
				.andExpect(jsonPath("$.siguienteCursor").isEmpty());
	}

	/**
	 * HU-16 y D22: quien todavía no sigue a nadie no se encuentra una pantalla vacía, sino la
	 * actividad de toda la plataforma — y la respuesta lo avisa, porque la pantalla tiene que
	 * poder explicar qué está mostrando (USER_FLOWS, flujo 2).
	 */
	@Test
	void sinSeguidosElFeedEsGlobalYLoAvisa() throws Exception {
		MockHttpSession ariel = cuenta("ariel");
		MockHttpSession felipe = cuenta("felipe");
		Long produccion = crearProduccion("La que ve todo el mundo");
		registrar(felipe, produccion, 7, null);

		mockMvc.perform(get("/api/feed").session(ariel))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.global").value(true))
				.andExpect(jsonPath("$.items[0].autor").value("felipe"))
				.andExpect(jsonPath("$.items[0].produccion.titulo").value("La que ve todo el mundo"));

		// Con un solo seguido el feed ya es suyo, aunque tenga menos cosas que el global.
		seguir(ariel, "felipe");
		mockMvc.perform(get("/api/feed").session(ariel))
				.andExpect(jsonPath("$.global").value(false))
				.andExpect(jsonPath("$.items.length()").value(1));
	}

	/**
	 * El otro lado de la regla del fallback (D22/D66): seguir gente que no registró nada da un
	 * feed vacío, no el global. Un feed sin nada de los tuyos es información honesta; rellenarlo
	 * con desconocidos sin avisar sería mentir sobre de quién es lo que se lee.
	 */
	@Test
	void seguirGenteCalladaDaUnFeedVacioYNoElGlobal() throws Exception {
		MockHttpSession bruno = cuenta("brunilda");
		cuenta("delia");
		registrar(cuenta("emilse"), crearProduccion("La que registró una desconocida"), 6, null);

		seguir(bruno, "delia");

		mockMvc.perform(get("/api/feed").session(bruno))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.global").value(false))
				.andExpect(jsonPath("$.items").isEmpty())
				.andExpect(jsonPath("$.siguienteCursor").isEmpty());
	}

	/**
	 * Por qué el cursor lleva el id además del instante (D66): dos registros pueden compartir la
	 * fecha de carga, y con un corte por instante solo el segundo se perdería para siempre —la
	 * página siguiente pide lo anterior a ese instante, y él no lo es—.
	 *
	 * <p>El empate se fuerza con un {@code UPDATE}: la carga la sella el propio registro con la
	 * hora del momento, así que por HTTP no hay forma de provocar una colisión de microsegundos
	 * a pedido. Es la condición que la base sí puede producir sola, escrita a mano.</p>
	 */
	@Test
	void dosRegistrosCargadosEnElMismoInstanteNoSePisanAlPaginar() throws Exception {
		MockHttpSession noelia = cuenta("noelia");
		MockHttpSession benito = cuenta("benito");
		Long primero = registrar(benito, crearProduccion("La empatada de abajo"), null, null);
		Long segundo = registrar(benito, crearProduccion("La empatada de arriba"), null, null);
		jdbc.update("update registro set creado_en = (select creado_en from registro where id = ?) "
				+ "where id = ?", primero, segundo);
		seguir(noelia, "benito");

		MvcResult pagina = mockMvc.perform(get("/api/feed").param("tamanio", "1").session(noelia))
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].registroId").value(segundo))
				.andReturn();
		String cursor = JsonPath.read(pagina.getResponse().getContentAsString(), "$.siguienteCursor");

		mockMvc.perform(get("/api/feed").param("tamanio", "1").param("cursor", cursor).session(noelia))
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].registroId").value(primero));
	}

	/**
	 * HU-16: la paginación. El cursor es el último ítem entregado y no un número de página (D66),
	 * así que lo que se cargó mientras tanto —acá, un registro nuevo entre las dos páginas— entra
	 * arriba de todo la próxima vez que se pida la primera, sin empujar nada ni repetirse.
	 */
	@Test
	void elFeedPaginaConCursorSinRepetirNiSaltear() throws Exception {
		MockHttpSession silvina = cuenta("silvina");
		MockHttpSession teresa = cuenta("teresa");
		Long primera = registrar(teresa, crearProduccion("Primera de la noche"), null, null);
		Long segunda = registrar(teresa, crearProduccion("Segunda de la noche"), null, null);
		Long tercera = registrar(teresa, crearProduccion("Tercera de la noche"), null, null);
		seguir(silvina, "teresa");

		MvcResult pagina = mockMvc.perform(get("/api/feed").param("tamanio", "2").session(silvina))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].registroId").value(tercera))
				.andExpect(jsonPath("$.items[1].registroId").value(segunda))
				.andExpect(jsonPath("$.siguienteCursor").isNotEmpty())
				.andReturn();
		String cursor = JsonPath.read(pagina.getResponse().getContentAsString(), "$.siguienteCursor");

		// Entre las dos páginas alguien registra: no tiene que aparecer en la segunda ni correr
		// a la que ya se leyó una fila más abajo, que es lo que haría un offset.
		Long recien = registrar(teresa, crearProduccion("La que llegó tarde"), null, null);

		mockMvc.perform(get("/api/feed").param("tamanio", "2").param("cursor", cursor).session(silvina))
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].registroId").value(primera))
				.andExpect(jsonPath("$.siguienteCursor").isEmpty());

		mockMvc.perform(get("/api/feed").param("tamanio", "2").session(silvina))
				.andExpect(jsonPath("$.items[0].registroId").value(recien));

		mockMvc.perform(get("/api/feed").param("cursor", "cualquier-cosa").session(silvina))
				.andExpect(status().isBadRequest());
	}

	/** El feed y el botón de seguir son de quien tiene cuenta; a quien no está no se lo sigue. */
	@Test
	void elFeedYElBotonPidenSesionYUnaCuentaQueExista() throws Exception {
		MockHttpSession ramona = cuenta("ramona");

		mockMvc.perform(get("/api/feed"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(conCsrf(post("/api/usuarios/ramona/seguir")))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(conCsrf(post("/api/usuarios/nadie-con-ese-nombre/seguir")).session(ramona))
				.andExpect(status().isNotFound());
	}

	/**
	 * HU-17 y lo que a HU-14 le faltaba en la ficha: el toggle, el contador y el estado del botón.
	 * El contador se ve sin cuenta, como todo (D21); el botón no, porque sin sesión no hay quién lo
	 * toque, y eso es lo que distingue {@code leDiLike} nulo de {@code false}.
	 */
	@Test
	void elLikeEsUnToggleConContadorEnLaFicha() throws Exception {
		Long produccion = crearProduccion("La que se llenó de likes");
		MockHttpSession alma = cuenta("alma");
		MockHttpSession corina = cuenta("corina");
		Long resenia = registrar(alma, produccion, 9, "Hora y media que se pasa volando");

		mockMvc.perform(get("/api/producciones/" + produccion + "/opiniones"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resenias[0].likes").value(0))
				.andExpect(jsonPath("$.resenias[0].leDiLike").isEmpty());

		// El doble clic es lo normal, no un error: el segundo like no suma otro.
		darLike(corina, resenia);
		darLike(corina, resenia);

		mockMvc.perform(get("/api/producciones/" + produccion + "/opiniones").session(corina))
				.andExpect(jsonPath("$.resenias[0].likes").value(1))
				.andExpect(jsonPath("$.resenias[0].leDiLike").value(true));
		// Quien no lo dio ve el mismo contador con el botón apagado.
		mockMvc.perform(get("/api/producciones/" + produccion + "/opiniones").session(alma))
				.andExpect(jsonPath("$.resenias[0].likes").value(1))
				.andExpect(jsonPath("$.resenias[0].leDiLike").value(false));

		// Darse like a la propia reseña está permitido: no es seguirse a uno mismo, es un número.
		darLike(alma, resenia);
		mockMvc.perform(get("/api/producciones/" + produccion + "/opiniones").session(alma))
				.andExpect(jsonPath("$.resenias[0].likes").value(2))
				.andExpect(jsonPath("$.resenias[0].leDiLike").value(true));

		mockMvc.perform(conCsrf(delete("/api/resenias/" + resenia + "/like")).session(corina))
				.andExpect(status().isNoContent());
		mockMvc.perform(conCsrf(delete("/api/resenias/" + resenia + "/like")).session(corina))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/producciones/" + produccion + "/opiniones").session(corina))
				.andExpect(jsonPath("$.resenias[0].likes").value(1))
				.andExpect(jsonPath("$.resenias[0].leDiLike").value(false));
	}

	/**
	 * Los likes también viajan en el feed, que es donde más se leen reseñas (HU-16 + HU-17). Un
	 * registro sin texto no es una reseña: viaja igual en el feed, pero sin botón —{@code leDiLike}
	 * nulo— y no se le puede dar like ni a mano.
	 */
	@Test
	void elLikeViajaEnElFeedYSoloSobreLoQueEsResenia() throws Exception {
		MockHttpSession genaro = cuenta("genaro");
		MockHttpSession hebe = cuenta("hebe");
		Long conTexto = registrar(hebe, crearProduccion("La que Hebe reseñó"), 8, "Salí pensando");
		Long sinTexto = registrar(hebe, crearProduccion("La que Hebe solo puntuó"), 7, null);
		seguir(genaro, "hebe");
		darLike(genaro, conTexto);

		mockMvc.perform(get("/api/feed").session(genaro))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].registroId").value(sinTexto))
				.andExpect(jsonPath("$.items[0].likes").value(0))
				.andExpect(jsonPath("$.items[0].leDiLike").isEmpty())
				.andExpect(jsonPath("$.items[1].registroId").value(conTexto))
				.andExpect(jsonPath("$.items[1].likes").value(1))
				.andExpect(jsonPath("$.items[1].leDiLike").value(true));

		mockMvc.perform(conCsrf(post("/api/resenias/" + sinTexto + "/like")).session(genaro))
				.andExpect(status().isNotFound());
	}

	/** Destacar es de quien tiene cuenta, y lo que no es una reseña no está: 404 en los dos sentidos. */
	@Test
	void elLikePideSesionYUnaReseniaQueExista() throws Exception {
		MockHttpSession ignacio = cuenta("ignacio");
		Long resenia = registrar(ignacio, crearProduccion("La de la sala chica"), null, "Corta e intensa");

		mockMvc.perform(conCsrf(post("/api/resenias/" + resenia + "/like")))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(conCsrf(post("/api/resenias/999999/like")).session(ignacio))
				.andExpect(status().isNotFound());
		mockMvc.perform(conCsrf(delete("/api/resenias/999999/like")).session(ignacio))
				.andExpect(status().isNotFound());
	}

	private void darLike(MockHttpSession sesion, Long reseniaId) throws Exception {
		mockMvc.perform(conCsrf(post("/api/resenias/" + reseniaId + "/like")).session(sesion))
				.andExpect(status().isNoContent());
	}

	private void seguir(MockHttpSession sesion, String username) throws Exception {
		mockMvc.perform(conCsrf(post("/api/usuarios/" + username + "/seguir")).session(sesion))
				.andExpect(status().isNoContent());
	}

	private Long crearProduccion(String titulo) throws Exception {
		MvcResult resultado = mockMvc.perform(json(post("/api/admin/producciones"), """
				{"titulo":"%s","estado":"EN_CARTEL"}""".formatted(titulo))
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isCreated())
				.andReturn();
		return id(resultado);
	}

	private MockHttpSession cuenta(String username) throws Exception {
		MvcResult resultado = mockMvc.perform(json(post("/api/auth/registro"), """
				{"username":"%s","email":"%s@example.com","password":"unaClaveLarga"}"""
				.formatted(username, username)))
				.andExpect(status().isCreated())
				.andReturn();
		return (MockHttpSession) resultado.getRequest().getSession(false);
	}

	private Long registrar(MockHttpSession sesion, Long produccionId, Integer rating, String resenia)
			throws Exception {
		MvcResult resultado = mockMvc.perform(json(post("/api/registros"), """
				{"produccionId":%d,"fecha":"2025-05-05","granularidad":"DIA","rating":%s,"resenia":%s}"""
				.formatted(produccionId, rating, comillas(resenia)))
				.session(sesion))
				.andExpect(status().isCreated())
				.andReturn();
		return id(resultado);
	}

	private static String comillas(String valor) {
		return valor == null ? "null" : "\"" + valor + "\"";
	}

	private static Long id(MvcResult resultado) throws Exception {
		return ((Number) JsonPath.read(resultado.getResponse().getContentAsString(), "$.id")).longValue();
	}

	private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String cuerpo) {
		return conCsrf(peticion).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
	}

	/** Doble envío, igual que en {@code AutenticacionTest}: cookie con el token y header con él. */
	private MockHttpServletRequestBuilder conCsrf(MockHttpServletRequestBuilder peticion) {
		return peticion.cookie(new Cookie("XSRF-TOKEN", TOKEN_CSRF)).header("X-XSRF-TOKEN", TOKEN_CSRF);
	}
}
