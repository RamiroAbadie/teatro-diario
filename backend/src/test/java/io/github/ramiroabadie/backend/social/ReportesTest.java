package io.github.ramiroabadie.backend.social;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.jayway.jsonpath.JsonPath;
import io.github.ramiroabadie.backend.TestcontainersConfiguration;
import io.github.ramiroabadie.backend.diario.Diario;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los criterios de aceptación de HU-18 y HU-22, ejecutables: el botón de avisar y la cola que lo
 * resuelve (D40). El que vigila la regla que se rompe sola es
 * {@link #borrarLaReseniaSeLlevaElTextoYDejaLaSalidaAlTeatro()}: moderar borra lo que alguien
 * escribió, no que haya ido al teatro, así que el registro y su puntaje quedan y el promedio de
 * D20 no se mueve (D70).
 *
 * <p>La base es la misma para toda la clase y para las demás, así que cada test usa sus propias
 * cuentas y sus propias producciones, y mira la cola filtrando por la reseña que él mismo reportó
 * en vez de contar cuántos ítems hay.</p>
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ReportesTest {

	private static final String TOKEN_CSRF = "un-token-de-prueba";

	/** Cuánto se queda abierta la transacción que gana, para que la otra tenga con qué chocar. */
	private static final Duration SOLAPE = Duration.ofMillis(500);

	@Autowired
	private MockMvc mockMvc;

	/** Los dos únicos que se usan sin pasar por HTTP: el solape hay que provocarlo desde adentro. */
	@Autowired
	private ReportesDeResenias reportes;

	@Autowired
	private TransactionTemplate transacciones;

	/**
	 * Espía, no reemplazo, igual que en {@code FusionDeProduccionesTest}: el bean real atiende a
	 * todos los tests de la clase —un spy sin stubear delega en el original— y solo el del rollback
	 * le pide que falle.
	 */
	@MockitoSpyBean
	private Diario diario;

	/**
	 * HU-18 y HU-22 de punta a punta: el aviso entra sin motivo si hace falta, y la cola le muestra
	 * al admin todo lo que necesita para decidir sin abrir otra pantalla — el texto, quién lo
	 * escribió, en qué obra fue y quién avisó.
	 */
	@Test
	void reportarUnaReseniaAjenaLaMandaALaColaConSuContexto() throws Exception {
		MockHttpSession octavio = cuenta("octavio");
		MockHttpSession malena = cuenta("malena");
		Long obra = crearProduccion("La obra que alguien reseñó de más");
		Long resenia = registrar(octavio, obra, 8, "Una reseña que a Malena le pareció ofensiva");

		reportar(malena, resenia, "\"Se pasó de la raya con el elenco\"");

		mockMvc.perform(get("/api/admin/reportes").with(user("jefa").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath(porResenia(resenia) + ".texto")
						.value("Una reseña que a Malena le pareció ofensiva"))
				.andExpect(jsonPath(porResenia(resenia) + ".autor").value("octavio"))
				.andExpect(jsonPath(porResenia(resenia) + ".reportante").value("malena"))
				.andExpect(jsonPath(porResenia(resenia) + ".motivo").value("Se pasó de la raya con el elenco"))
				.andExpect(jsonPath(porResenia(resenia) + ".rating").value(8))
				.andExpect(jsonPath(porResenia(resenia) + ".produccion.titulo")
						.value("La obra que alguien reseñó de más"));

		// El motivo es opcional: el botón es un clic, no un formulario (HU-18).
		MockHttpSession leopolda = cuenta("leopolda");
		Long otra = registrar(octavio, obra, 7, "La otra reseña de Octavio");
		mockMvc.perform(conCsrf(post("/api/resenias/" + otra + "/reporte")).session(leopolda))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/admin/reportes").with(user("jefa").roles("ADMIN")))
				.andExpect(jsonPath("$[?(@.reseniaId == " + otra + " && @.motivo == null)]").exists())
				.andExpect(jsonPath(porResenia(otra) + ".reportante").value("leopolda"));
	}

	/**
	 * La regla que sostiene todo lo demás (D70): lo ofensivo es el texto, no haber ido al teatro.
	 * Después de borrar, la reseña no está en la ficha, pero el registro sigue en el diario de su
	 * autor con su puntaje, y el promedio de D20 —que se calcula al leer— no se movió.
	 */
	@Test
	void borrarLaReseniaSeLlevaElTextoYDejaLaSalidaAlTeatro() throws Exception {
		MockHttpSession teo = cuenta("teo");
		MockHttpSession trinidad = cuenta("trinidad");
		Long obra = crearProduccion("La obra del reporte que prospera");
		Long resenia = registrar(teo, obra, 9, "El texto que el admin va a borrar");

		reportar(trinidad, resenia, "null");
		Long reporte = enLaCola(resenia);

		mockMvc.perform(conCsrf(post("/api/admin/reportes/" + reporte + "/borrar-resenia"))
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isNoContent());

		// El texto ya no está en la ficha, pero el puntaje sigue contando para el promedio.
		mockMvc.perform(get("/api/producciones/" + obra + "/opiniones"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.promedio").value(9.0))
				.andExpect(jsonPath("$.cantidadRatings").value(1))
				.andExpect(jsonPath("$.resenias[?(@.registroId == " + resenia + ")]").isEmpty());

		// Y la salida al teatro sigue en el diario de Teo, con su fecha y su puntaje.
		mockMvc.perform(get("/api/usuarios/teo"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.registros[?(@.id == " + resenia + ")].rating").value(9))
				.andExpect(jsonPath("$.registros[?(@.id == " + resenia + ")].fecha").value("2025-05-05"));

		// Sin texto ya no es una reseña: no se puede destacar ni volver a reportar (HU-17, HU-18).
		mockMvc.perform(conCsrf(post("/api/resenias/" + resenia + "/like")).session(trinidad))
				.andExpect(status().isNotFound());
		mockMvc.perform(conCsrf(post("/api/resenias/" + resenia + "/reporte")).session(trinidad))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/admin/reportes").with(user("jefa").roles("ADMIN")))
				.andExpect(jsonPath(porResenia(resenia)).isEmpty());
	}

	/** La otra salida (HU-22): la reseña se queda donde estaba y la cola se vacía igual. */
	@Test
	void desestimarDejaLaReseniaYVaciaLaCola() throws Exception {
		MockHttpSession ulises = cuenta("ulises");
		MockHttpSession aurelia = cuenta("aurelia");
		Long obra = crearProduccion("La obra del reporte que no prospera");
		Long resenia = registrar(ulises, obra, 6, "Una reseña dura pero no ofensiva");

		reportar(aurelia, resenia, "\"No me gustó lo que dijo\"");
		Long reporte = enLaCola(resenia);

		mockMvc.perform(conCsrf(post("/api/admin/reportes/" + reporte + "/desestimar"))
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/producciones/" + obra + "/opiniones"))
				.andExpect(jsonPath("$.resenias[?(@.registroId == " + resenia + ")].texto")
						.value("Una reseña dura pero no ofensiva"));
		mockMvc.perform(get("/api/admin/reportes").with(user("jefa").roles("ADMIN")))
				.andExpect(jsonPath(porResenia(resenia)).isEmpty());

		// La cola abierta en dos pestañas: el segundo intento llega tarde, no está roto.
		mockMvc.perform(conCsrf(post("/api/admin/reportes/" + reporte + "/desestimar"))
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isConflict());
		mockMvc.perform(conCsrf(post("/api/admin/reportes/" + reporte + "/borrar-resenia"))
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isConflict());
		mockMvc.perform(conCsrf(post("/api/admin/reportes/999999/desestimar"))
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isNotFound());

		// Y ese 409 no se llevó el texto por delante: primero se resuelve el reporte y recién
		// entonces se toca el contenido. Al revés, el segundo clic borraría igual.
		mockMvc.perform(get("/api/producciones/" + obra + "/opiniones"))
				.andExpect(jsonPath("$.resenias[?(@.registroId == " + resenia + ")].texto")
						.value("Una reseña dura pero no ofensiva"));

		// Resuelto el primero, la misma persona puede volver a avisar: la reseña pudo cambiar.
		reportar(aurelia, resenia, "\"La editó y quedó peor\"");
		assertThat(enLaCola(resenia)).isNotEqualTo(reporte);
	}

	/**
	 * El veredicto es sobre el texto, no sobre cada aviso (D70): si tres personas reportaron la
	 * misma reseña, resolverla saca los tres de la cola. Sin esto, el admin lee lo mismo tres veces
	 * y los dos que sobran quedan apuntando a un texto que ya borró.
	 */
	@Test
	void resolverUnReporteSacaTodosLosDeEsaMismaResenia() throws Exception {
		MockHttpSession casimiro = cuenta("casimiro");
		Long obra = crearProduccion("La obra que reportaron entre varios");
		Long resenia = registrar(casimiro, obra, 5, "La reseña que le molestó a tres personas");

		MockHttpSession primera = cuenta("wenceslao");
		MockHttpSession segunda = cuenta("eusebio");
		reportar(primera, resenia, "\"Es una barbaridad\"");
		reportar(segunda, resenia, "null");
		Long unoDeLosDos = enLaCola(resenia);

		// Reportar de nuevo mientras el aviso sigue pendiente no agrega una fila más a la cola.
		reportar(primera, resenia, "\"Sigo pensando lo mismo\"");
		assertThat(cuantosEnLaCola(resenia)).isEqualTo(2);

		mockMvc.perform(conCsrf(post("/api/admin/reportes/" + unoDeLosDos + "/borrar-resenia"))
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isNoContent());

		assertThat(cuantosEnLaCola(resenia)).isZero();
	}

	/** HU-18: el botón está en las reseñas ajenas, y solo en las que son reseñas. */
	@Test
	void noSeReportaLaPropiaReseniaNiUnRegistroSinTexto() throws Exception {
		MockHttpSession petrona = cuenta("petrona");
		Long obra = crearProduccion("La obra de los reportes que no entran");
		Long propia = registrar(petrona, obra, 10, "Mi propia reseña");
		Long sinTexto = registrar(petrona, obra, 4, null);

		mockMvc.perform(conCsrf(post("/api/resenias/" + propia + "/reporte")).session(petrona))
				.andExpect(status().isBadRequest());

		MockHttpSession joaco = cuenta("joaco");
		mockMvc.perform(conCsrf(post("/api/resenias/" + sinTexto + "/reporte")).session(joaco))
				.andExpect(status().isNotFound());
		mockMvc.perform(conCsrf(post("/api/resenias/999999/reporte")).session(joaco))
				.andExpect(status().isNotFound());

		mockMvc.perform(json(post("/api/resenias/" + propia + "/reporte"), """
				{"motivo":"%s"}""".formatted("x".repeat(501))).session(joaco))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.motivo").isNotEmpty());
	}

	/**
	 * Reportar es de quien tiene cuenta (HU-18) y la cola es del admin, como todo el panel: el
	 * candado lo pone el prefijo {@code /api/admin} y no una regla nueva.
	 */
	@Test
	void reportarPideSesionYLaColaEsSoloDelAdmin() throws Exception {
		MockHttpSession milagros = cuenta("milagros");
		Long obra = crearProduccion("La obra del reporte anónimo");
		Long resenia = registrar(milagros, obra, 7, "Una reseña cualquiera");

		mockMvc.perform(conCsrf(post("/api/resenias/" + resenia + "/reporte")))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/admin/reportes"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/admin/reportes").session(milagros))
				.andExpect(status().isForbidden());
	}

	/**
	 * Las dos pestañas que el reintento secuencial no cubre: los dos clics se solapan y las dos
	 * transacciones leen el reporte todavía pendiente. Sin nada que las serialice, las dos pasan el
	 * chequeo y las dos escriben, y una reseña puede terminar borrada con su reporte marcado como
	 * desestimado — dos veredictos opuestos sobre la misma cosa. Lo que lo sostiene es el
	 * {@code select ... for update} de leer los pendientes: el segundo espera, vuelve a leer, se
	 * encuentra con que ya no queda nada pendiente y corta.
	 *
	 * <p>El solape se fuerza a mano y no con dos pedidos HTTP a la vez, por lo mismo que en
	 * {@code SugerenciasTest}: la ventana real dura lo que tarda un {@code update} y un test así
	 * pasaría por suerte. Se llama al caso de uso y no al endpoint porque la transacción tiene que
	 * ser del test; la traducción de la excepción a un 409 la cubre
	 * {@link #desestimarDejaLaReseniaYVaciaLaCola()}.</p>
	 */
	@Test
	void dosResolucionesSimultaneasNoDanDosVeredictos() throws Exception {
		MockHttpSession amparo = cuenta("amparo");
		MockHttpSession beltran = cuenta("beltran");
		Long obra = crearProduccion("La obra de las dos pestañas");
		Long resenia = registrar(amparo, obra, 3, "La reseña que dos pestañas resuelven a la vez");
		reportar(beltran, resenia, "\"Que lo vea el admin\"");
		Long reporte = enLaCola(resenia);

		CountDownLatch laPrimeraEmpezo = new CountDownLatch(1);
		ExecutorService hilos = Executors.newFixedThreadPool(2);
		try {
			Future<?> primera = hilos.submit(() -> transacciones.executeWithoutResult(estado -> {
				reportes.confirmar(reporte);
				laPrimeraEmpezo.countDown();
				dormir(SOLAPE);
			}));
			Future<Class<?>> segunda = hilos.submit(() -> {
				laPrimeraEmpezo.await();
				try {
					reportes.desestimar(reporte);
					return null;
				}
				catch (RuntimeException ex) {
					return ex.getClass();
				}
			});

			primera.get();
			assertThat(segunda.get()).isEqualTo(ReporteResueltoException.class);
		}
		finally {
			hilos.shutdownNow();
		}

		mockMvc.perform(get("/api/admin/reportes").with(user("jefa").roles("ADMIN")))
				.andExpect(jsonPath(porResenia(resenia)).isEmpty());
	}

	/**
	 * El "todo o nada" que promete D70, con el fallo puesto a mano en el peor lugar posible: entre
	 * que Social marca los reportes resueltos y que Diario borra el texto. Lo que se verifica no es
	 * que Spring sepa hacer rollback, sino que las dos escrituras —de dos módulos distintos— caen
	 * adentro de la misma transacción. Sin eso, un fallo ahí vacía la cola y deja la reseña
	 * ofensiva publicada, que es el peor de los dos resultados posibles: el admin ya no la ve.
	 *
	 * <p>Es el mismo molde que {@code FusionDeProduccionesTest}, y por lo mismo un espía y no un
	 * mock: el bean real sigue atendiendo al resto de la clase y solo este test le pide que
	 * falle.</p>
	 */
	@Test
	void siFallaElBorradoDelTextoElReporteVuelveALaCola() throws Exception {
		MockHttpSession olegario = cuenta("olegario");
		MockHttpSession herminia = cuenta("herminia");
		Long obra = crearProduccion("La obra del borrado que falla");
		Long resenia = registrar(olegario, obra, 4, "El texto que no se va a poder borrar");
		reportar(herminia, resenia, "\"Que lo resuelva el admin\"");
		Long reporte = enLaCola(resenia);
		willThrow(new IllegalStateException("el borrado falla a propósito"))
				.given(diario).borrarResenia(resenia);

		assertThatThrownBy(() -> mockMvc.perform(conCsrf(post("/api/admin/reportes/" + reporte + "/borrar-resenia"))
				.with(user("jefa").roles("ADMIN"))))
				.hasRootCauseMessage("el borrado falla a propósito");

		// Ni media resolución: la reseña sigue publicada y el reporte sigue esperando en la cola.
		mockMvc.perform(get("/api/producciones/" + obra + "/opiniones"))
				.andExpect(jsonPath("$.resenias[?(@.registroId == " + resenia + ")].texto")
						.value("El texto que no se va a poder borrar"));
		assertThat(enLaCola(resenia)).isEqualTo(reporte);
	}

	/** Un filtro de JsonPath: la cola tiene lo de todos los tests, y a cada uno le importa el suyo. */
	private static String porResenia(Long reseniaId) {
		return "$[?(@.reseniaId == " + reseniaId + ")]";
	}

	/** El id del primer reporte pendiente de esa reseña, que es por donde el admin la resuelve. */
	private Long enLaCola(Long reseniaId) throws Exception {
		List<Integer> ids = idsEnLaCola(reseniaId);
		assertThat(ids).isNotEmpty();
		return ids.get(0).longValue();
	}

	private int cuantosEnLaCola(Long reseniaId) throws Exception {
		return idsEnLaCola(reseniaId).size();
	}

	private List<Integer> idsEnLaCola(Long reseniaId) throws Exception {
		MvcResult cola = mockMvc.perform(get("/api/admin/reportes").with(user("jefa").roles("ADMIN")))
				.andExpect(status().isOk())
				.andReturn();
		return JsonPath.read(cola.getResponse().getContentAsString(), porResenia(reseniaId) + ".id");
	}

	/** @param motivo ya en JSON: entre comillas, o {@code null} para reportar sin decir nada */
	private void reportar(MockHttpSession sesion, Long reseniaId, String motivo) throws Exception {
		mockMvc.perform(json(post("/api/resenias/" + reseniaId + "/reporte"), """
				{"motivo":%s}""".formatted(motivo)).session(sesion))
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

	private MockHttpSession cuenta(String username) throws Exception {
		MvcResult resultado = mockMvc.perform(json(post("/api/auth/registro"), """
				{"username":"%s","email":"%s@example.com","password":"unaClaveLarga"}"""
				.formatted(username, username)))
				.andExpect(status().isCreated())
				.andReturn();
		return (MockHttpSession) resultado.getRequest().getSession(false);
	}

	private static String comillas(String valor) {
		return valor == null ? "null" : "\"" + valor + "\"";
	}

	private static Long id(MvcResult resultado) throws Exception {
		return ((Number) JsonPath.read(resultado.getResponse().getContentAsString(), "$.id")).longValue();
	}

	private static void dormir(Duration cuanto) {
		try {
			Thread.sleep(cuanto);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String cuerpo) {
		return conCsrf(peticion).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
	}

	/** Doble envío, igual que en el resto de los tests: cookie con el token y header con él. */
	private MockHttpServletRequestBuilder conCsrf(MockHttpServletRequestBuilder peticion) {
		return peticion.cookie(new Cookie("XSRF-TOKEN", TOKEN_CSRF)).header("X-XSRF-TOKEN", TOKEN_CSRF);
	}
}
