package io.github.ramiroabadie.backend.catalogo;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.jayway.jsonpath.JsonPath;
import io.github.ramiroabadie.backend.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los criterios de aceptación de HU-08 y HU-21, ejecutables: la válvula del catálogo cerrado (D7)
 * y la cola que la vacía. El que vigila la regla que se rompe sola es
 * {@link #aprobarPideLaFichaYaCreadaYSacaLaSugerenciaDeLaCola()}: aprobar no crea producciones —eso
 * es el formulario de HU-20— y una sugerencia sale de la cola una sola vez.
 *
 * <p>La base es la misma para toda la clase y para las demás, así que cada test usa sus propias
 * cuentas y sus propios títulos, y mira la cola filtrando por el id de lo que él mismo mandó en
 * vez de contar cuántas hay.</p>
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class SugerenciasTest {

	private static final String TOKEN_CSRF = "un-token-de-prueba";

	/** Cuánto se queda abierta la transacción que gana, para que la otra tenga con qué chocar. */
	private static final Duration SOLAPE = Duration.ofMillis(500);

	@Autowired
	private MockMvc mockMvc;

	/** Los dos únicos que se usan sin pasar por HTTP: el solape hay que provocarlo desde adentro. */
	@Autowired
	private Sugerencias sugerencias;

	@Autowired
	private TransactionTemplate transacciones;

	/**
	 * HU-08: el formulario mínimo. Solo el título es obligatorio, y la respuesta devuelve lo
	 * propuesto — es la confirmación de recibido; no hay estado que consultar después (MD-3).
	 */
	@Test
	void sugerirUnaObraQueNoEstaPideSoloElTitulo() throws Exception {
		MockHttpSession lisandra = cuenta("lisandra");

		mockMvc.perform(json(post("/api/sugerencias"), """
				{"titulo":"Una obra que vi en 2014 y no está"}""").session(lisandra))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.titulo").value("Una obra que vi en 2014 y no está"))
				.andExpect(jsonPath("$.sala").isEmpty());

		// Y con todo lo opcional puesto, que es la sugerencia que al admin le sirve de verdad.
		mockMvc.perform(json(post("/api/sugerencias"), """
				{"titulo":"La otra que vi en 2014","sala":"Un sótano de Almagro","anio":2014,
				"elenco":"Una actriz y un actor de los que no me acuerdo el nombre",
				"comentario":"Estuvo dos temporadas"}""").session(lisandra))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.sala").value("Un sótano de Almagro"))
				.andExpect(jsonPath("$.anio").value(2014))
				.andExpect(jsonPath("$.comentario").value("Estuvo dos temporadas"));
	}

	/** Sin título no hay nada que sugerir, y el 400 dice qué campo falta (como el resto de los formularios). */
	@Test
	void sugerirSinTituloEsUnErrorDeCampo() throws Exception {
		MockHttpSession hernan = cuenta("hernan");

		mockMvc.perform(json(post("/api/sugerencias"), """
				{"titulo":"   ","anio":2024}""").session(hernan))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.titulo").isNotEmpty());

		mockMvc.perform(json(post("/api/sugerencias"), """
				{"titulo":"Con un año imposible","anio":12345}""").session(hernan))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.anio").isNotEmpty());
	}

	/**
	 * HU-21, el camino de la rutina semanal (D37): la cola muestra quién sugirió —el username, no
	 * el id opaco que guarda Catálogo—, el admin carga la ficha con el CRUD de HU-20 y recién ahí
	 * aprueba, diciendo en qué producción terminó. La sugerencia sale de la cola y no vuelve.
	 */
	@Test
	void aprobarPideLaFichaYaCreadaYSacaLaSugerenciaDeLaCola() throws Exception {
		MockHttpSession dolores = cuenta("dolores");
		Long sugerencia = sugerir(dolores, "La que el admin va a cargar");

		mockMvc.perform(get("/api/admin/sugerencias").with(user("jefa").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath(enLaCola(sugerencia) + ".titulo").value("La que el admin va a cargar"))
				.andExpect(jsonPath(enLaCola(sugerencia) + ".sugerente").value("dolores"))
				.andExpect(jsonPath(enLaCola(sugerencia) + ".creadoEn").isNotEmpty());

		// Aprobar no crea la ficha: la ficha ya tiene que existir, con el id que se manda.
		mockMvc.perform(json(post("/api/admin/sugerencias/" + sugerencia + "/aprobar"), """
				{"produccionId":999999}""").with(user("jefa").roles("ADMIN")))
				.andExpect(status().isNotFound());

		Long produccion = crearProduccion("La que el admin cargó");
		aprobar(sugerencia, produccion);

		mockMvc.perform(get("/api/admin/sugerencias").with(user("jefa").roles("ADMIN")))
				.andExpect(jsonPath(enLaCola(sugerencia)).isEmpty());

		// La cola abierta en dos pestañas: el segundo intento llega tarde, no está roto.
		mockMvc.perform(json(post("/api/admin/sugerencias/" + sugerencia + "/aprobar"), """
				{"produccionId":%d}""".formatted(produccion)).with(user("jefa").roles("ADMIN")))
				.andExpect(status().isConflict());
		mockMvc.perform(json(post("/api/admin/sugerencias/" + sugerencia + "/rechazar"), """
				{"motivo":"Llegué tarde"}""").with(user("jefa").roles("ADMIN")))
				.andExpect(status().isConflict());
	}

	/**
	 * La otra salida de la cola (HU-21): rechazar con un motivo que solo lee el admin. Es
	 * obligatorio — sin él, la misma propuesta vuelve en un mes y nadie se acuerda por qué no
	 * entró.
	 */
	@Test
	void rechazarPideMotivoYTambienVaciaLaCola() throws Exception {
		MockHttpSession efrain = cuenta("efrain");
		Long sugerencia = sugerir(efrain, "La que ya estaba cargada con otro nombre");

		mockMvc.perform(json(post("/api/admin/sugerencias/" + sugerencia + "/rechazar"), """
				{"motivo":""}""").with(user("jefa").roles("ADMIN")))
				.andExpect(status().isBadRequest());

		mockMvc.perform(json(post("/api/admin/sugerencias/" + sugerencia + "/rechazar"), """
				{"motivo":"Ya está en el catálogo como 'Los días felices'"}""")
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/admin/sugerencias").with(user("jefa").roles("ADMIN")))
				.andExpect(jsonPath(enLaCola(sugerencia)).isEmpty());

		mockMvc.perform(json(post("/api/admin/sugerencias/999999/rechazar"), """
				{"motivo":"Esta no existe"}""").with(user("jefa").roles("ADMIN")))
				.andExpect(status().isNotFound());
	}

	/** La cola es una fila: se resuelve lo más viejo primero. */
	@Test
	void laColaSaleDeLaMasViejaALaMasNueva() throws Exception {
		MockHttpSession gervasio = cuenta("gervasio");
		Long primera = sugerir(gervasio, "La primera que mandó Gervasio");
		Long segunda = sugerir(gervasio, "La segunda que mandó Gervasio");

		MvcResult cola = mockMvc.perform(get("/api/admin/sugerencias").with(user("jefa").roles("ADMIN")))
				.andExpect(status().isOk())
				.andReturn();
		List<Integer> ids = JsonPath.read(cola.getResponse().getContentAsString(), "$[*].id");

		assertThat(ids.indexOf(primera.intValue())).isLessThan(ids.indexOf(segunda.intValue()));
	}

	/**
	 * La otra forma de las dos pestañas, la que el reintento secuencial no cubre: los dos clics se
	 * solapan y las dos transacciones leen la sugerencia todavía pendiente. Sin nada que las
	 * serialice, las dos pasan el chequeo y las dos escriben —la segunda pisa a la primera— y la
	 * sugerencia sale de la cola dos veces, que es exactamente lo que D69 dice que no puede pasar.
	 * Lo que lo sostiene es el {@code select ... for update} de leerla para resolverla: la segunda
	 * espera a que la primera termine, la encuentra resuelta y corta.
	 *
	 * <p>El solape se fuerza a mano y no con dos pedidos HTTP a la vez: la ventana real dura lo que
	 * tarda un {@code update}, así que dos clientes simultáneos casi nunca la pisan y un test así
	 * pasa por suerte, no por estar bien. Acá la primera transacción se queda abierta a propósito
	 * mientras la segunda intenta, que es la condición que la base sí puede producir siempre. Se
	 * llama al caso de uso y no al endpoint por lo mismo: la transacción tiene que ser del test.
	 * La traducción de esa excepción a un 409 la cubre {@link #aprobarPideLaFichaYaCreadaYSacaLaSugerenciaDeLaCola()}.</p>
	 */
	@Test
	void dosResolucionesSimultaneasNoSacanLaSugerenciaDosVeces() throws Exception {
		MockHttpSession jazmin = cuenta("jazmin");
		Long sugerencia = sugerir(jazmin, "La que dos pestañas resuelven a la vez");
		Long produccion = crearProduccion("La ficha de las dos pestañas");

		CountDownLatch laPrimeraEmpezo = new CountDownLatch(1);
		ExecutorService hilos = Executors.newFixedThreadPool(2);
		try {
			Future<?> primera = hilos.submit(() -> transacciones.executeWithoutResult(estado -> {
				sugerencias.aprobar(sugerencia, produccion);
				laPrimeraEmpezo.countDown();
				dormir(SOLAPE);
			}));
			Future<Class<?>> segunda = hilos.submit(() -> {
				laPrimeraEmpezo.await();
				try {
					sugerencias.rechazar(sugerencia, "La rechacé sin ver que ya la habían aprobado");
					return null;
				}
				catch (RuntimeException ex) {
					return ex.getClass();
				}
			});

			primera.get();
			assertThat(segunda.get()).isEqualTo(SugerenciaResueltaException.class);
		}
		finally {
			hilos.shutdownNow();
		}

		mockMvc.perform(get("/api/admin/sugerencias").with(user("jefa").roles("ADMIN")))
				.andExpect(jsonPath(enLaCola(sugerencia)).isEmpty());
	}

	/**
	 * Sugerir es de quien tiene cuenta (HU-08) y la cola es del admin, como todo el panel: el
	 * candado lo pone el prefijo {@code /api/admin} y no una regla nueva.
	 */
	@Test
	void sugerirPideSesionYLaColaEsSoloDelAdmin() throws Exception {
		MockHttpSession irina = cuenta("irina");

		mockMvc.perform(json(post("/api/sugerencias"), """
				{"titulo":"La que sugiere un anónimo"}"""))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/admin/sugerencias"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/admin/sugerencias").session(irina))
				.andExpect(status().isForbidden());
	}

	/** Un filtro de JsonPath: la cola tiene lo de todos los tests, y a cada uno le importa el suyo. */
	private static String enLaCola(Long sugerenciaId) {
		return "$[?(@.id == " + sugerenciaId + ")]";
	}

	private Long sugerir(MockHttpSession sesion, String titulo) throws Exception {
		MvcResult resultado = mockMvc.perform(json(post("/api/sugerencias"), """
				{"titulo":"%s"}""".formatted(titulo)).session(sesion))
				.andExpect(status().isCreated())
				.andReturn();
		return id(resultado);
	}

	private static void dormir(Duration cuanto) {
		try {
			Thread.sleep(cuanto);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	private void aprobar(Long sugerenciaId, Long produccionId) throws Exception {
		mockMvc.perform(json(post("/api/admin/sugerencias/" + sugerenciaId + "/aprobar"), """
				{"produccionId":%d}""".formatted(produccionId)).with(user("jefa").roles("ADMIN")))
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

	private static Long id(MvcResult resultado) throws Exception {
		return ((Number) JsonPath.read(resultado.getResponse().getContentAsString(), "$.id")).longValue();
	}

	private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String cuerpo) {
		return conCsrf(peticion).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
	}

	/** Doble envío, igual que en el resto de los tests: cookie con el token y header con él. */
	private MockHttpServletRequestBuilder conCsrf(MockHttpServletRequestBuilder peticion) {
		return peticion.cookie(new Cookie("XSRF-TOKEN", TOKEN_CSRF)).header("X-XSRF-TOKEN", TOKEN_CSRF);
	}
}
