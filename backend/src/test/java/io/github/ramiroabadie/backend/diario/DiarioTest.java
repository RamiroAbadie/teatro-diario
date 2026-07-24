package io.github.ramiroabadie.backend.diario;

import java.time.LocalDate;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los criterios de aceptación de HU-09 a HU-14, ejecutables. El que importa más que todos es
 * {@link #elPromedioEsElUltimoRatingDeCadaUsuario()}: D20 es la regla que cualquiera —persona o
 * IA— "simplifica" a un {@code AVG()} en la primera refactorización distraída, y este test es lo
 * único que se interpone.
 *
 * <p>La base es la misma para toda la clase, así que cada test usa sus propias cuentas y sus
 * propias producciones en vez de limpiar entre medio.</p>
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class DiarioTest {

	private static final String TOKEN_CSRF = "un-token-de-prueba";

	@Autowired
	private MockMvc mockMvc;

	/** HU-09: producción, fecha, rating y reseña en un solo gesto, y aparece en el diario. */
	@Test
	void elGestoCompletoQuedaEnElDiario() throws Exception {
		Long produccion = crearProduccion("Terrenal");
		MockHttpSession sesion = cuenta("juana");

		mockMvc.perform(json(post("/api/registros"), """
				{"produccionId":%d,"fecha":"2024-03-15","granularidad":"DIA","rating":9,
				 "resenia":"Salí flotando"}""".formatted(produccion)).session(sesion))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.produccion.titulo").value("Terrenal"))
				.andExpect(jsonPath("$.rating").value(9));

		mockMvc.perform(get("/api/usuarios/juana"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.usuario.username").value("juana"))
				.andExpect(jsonPath("$.registros[0].produccion.titulo").value("Terrenal"))
				.andExpect(jsonPath("$.registros[0].fecha").value("2024-03-15"))
				.andExpect(jsonPath("$.registros[0].resenia").value("Salí flotando"))
				.andExpect(jsonPath("$.estadisticas.totalRegistros").value(1));
	}

	/** HU-09: registrar sin fecha, sin rating y sin reseña también es registrar. */
	@Test
	void elGestoMinimoEsSoloLaObra() throws Exception {
		Long produccion = crearProduccion("Petróleo");
		MockHttpSession sesion = cuenta("tomas");

		mockMvc.perform(json(post("/api/registros"), """
				{"produccionId":%d,"granularidad":"SIN_FECHA"}""".formatted(produccion)).session(sesion))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/usuarios/tomas"))
				.andExpect(jsonPath("$.registros").isEmpty())
				.andExpect(jsonPath("$.sinFecha[0].produccion.titulo").value("Petróleo"))
				.andExpect(jsonPath("$.estadisticas.promedioPropio").isEmpty());
	}

	/** HU-10: la misma obra, dos veces, cada una con su rating. */
	@Test
	void laMismaObraSeRegistraVariasVeces() throws Exception {
		Long produccion = crearProduccion("Un enemigo del pueblo");
		MockHttpSession sesion = cuenta("bautista");

		registrar(sesion, produccion, "2023-08-01", "DIA", 7, null);
		registrar(sesion, produccion, "2025-08-01", "DIA", 9, null);

		mockMvc.perform(get("/api/usuarios/bautista"))
				.andExpect(jsonPath("$.registros.length()").value(2))
				.andExpect(jsonPath("$.registros[0].rating").value(9))
				.andExpect(jsonPath("$.registros[1].rating").value(7))
				.andExpect(jsonPath("$.estadisticas.totalRegistros").value(2))
				.andExpect(jsonPath("$.estadisticas.totalProducciones").value(1));
	}

	/** HU-11: la autorización de dueño (D30). El registro ajeno se ve, pero no se toca. */
	@Test
	void soloElDuenioEditaYBorra() throws Exception {
		Long produccion = crearProduccion("La omisión de la familia Coleman");
		MockHttpSession duenia = cuenta("lucia");
		MockHttpSession ajeno = cuenta("matias");
		Long registro = registrar(duenia, produccion, "2024-05-02", "DIA", 6, null);
		String cambio = """
				{"produccionId":%d,"fecha":"2024-05-02","granularidad":"DIA","rating":10,
				 "resenia":"Mejor de lo que me acordaba"}""".formatted(produccion);

		mockMvc.perform(json(put("/api/registros/" + registro), cambio).session(ajeno))
				.andExpect(status().isForbidden());
		mockMvc.perform(conCsrf(delete("/api/registros/" + registro)).session(ajeno))
				.andExpect(status().isForbidden());

		mockMvc.perform(json(put("/api/registros/" + registro), cambio).session(duenia))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rating").value(10))
				.andExpect(jsonPath("$.resenia").value("Mejor de lo que me acordaba"));
		mockMvc.perform(conCsrf(delete("/api/registros/" + registro)).session(duenia))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/usuarios/lucia"))
				.andExpect(jsonPath("$.registros").isEmpty());
	}

	/**
	 * ⚠️ D20. El promedio es el del último rating de cada usuario, y "último" es el de la función
	 * más reciente: quien sube después una salida vieja no pisa con eso lo que opina hoy.
	 *
	 * <p>Un {@code AVG()} plano de los cuatro registros daría 4.75. El promedio correcto es 8.0:
	 * el 10 de Vera (su función más reciente) y el 6 de Hugo.</p>
	 */
	@Test
	void elPromedioEsElUltimoRatingDeCadaUsuario() throws Exception {
		Long produccion = crearProduccion("Hamlet, versión de prueba");
		MockHttpSession vera = cuenta("vera");
		MockHttpSession hugo = cuenta("hugo");

		registrar(vera, produccion, "2023-04-10", "DIA", 2, null);
		registrar(vera, produccion, "2025-09-01", "DIA", 10, null);
		registrar(hugo, produccion, "2024-06-01", "DIA", 6, null);

		mockMvc.perform(get("/api/producciones/" + produccion + "/opiniones"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.promedio").value(8.0))
				.andExpect(jsonPath("$.cantidadRatings").value(2));

		// Vera carga hoy una función de 2019: es lo último que cargó, no lo último que vio.
		registrar(vera, produccion, "2019-01-05", "DIA", 1, null);

		mockMvc.perform(get("/api/producciones/" + produccion + "/opiniones"))
				.andExpect(jsonPath("$.promedio").value(8.0))
				.andExpect(jsonPath("$.cantidadRatings").value(2));
	}

	/** HU-11: borrar recalcula el promedio, porque el promedio se calcula al leer. */
	@Test
	void borrarUnRegistroRecalculaElPromedio() throws Exception {
		Long produccion = crearProduccion("Casa Valentina");
		MockHttpSession sesion = cuenta("ines");
		Long vieja = registrar(sesion, produccion, "2023-03-03", "DIA", 4, null);
		Long reciente = registrar(sesion, produccion, "2025-03-03", "DIA", 10, null);

		mockMvc.perform(get("/api/producciones/" + produccion + "/opiniones"))
				.andExpect(jsonPath("$.promedio").value(10.0));

		mockMvc.perform(conCsrf(delete("/api/registros/" + reciente)).session(sesion))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/producciones/" + produccion + "/opiniones"))
				.andExpect(jsonPath("$.promedio").value(4.0))
				.andExpect(jsonPath("$.cantidadRatings").value(1));

		mockMvc.perform(conCsrf(delete("/api/registros/" + vieja)).session(sesion))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/producciones/" + produccion + "/opiniones"))
				.andExpect(jsonPath("$.promedio").isEmpty())
				.andExpect(jsonPath("$.cantidadRatings").value(0));
	}

	/**
	 * HU-12 y MD-2: las fechas difusas se normalizan al comienzo de su período y ordenan por ahí,
	 * así un "2023" a secas cae al final de 2023. Los sin fecha van en su sección aparte.
	 */
	@Test
	void elDiarioOrdenaLasFechasDifusasPorSuGranularidad() throws Exception {
		Long dia = crearProduccion("La del día exacto");
		Long mes = crearProduccion("La del mes");
		Long anio = crearProduccion("La del año");
		Long nunca = crearProduccion("La que no sé cuándo fue");
		MockHttpSession sesion = cuenta("nadia");

		registrar(sesion, anio, "2023-11-02", "ANIO", null, null);
		registrar(sesion, dia, "2023-06-15", "DIA", null, null);
		registrar(sesion, nunca, null, "SIN_FECHA", null, null);
		registrar(sesion, mes, "2023-06-20", "MES", null, null);

		mockMvc.perform(get("/api/usuarios/nadia"))
				.andExpect(jsonPath("$.registros.length()").value(3))
				.andExpect(jsonPath("$.registros[0].produccion.id").value(dia))
				.andExpect(jsonPath("$.registros[0].fecha").value("2023-06-15"))
				.andExpect(jsonPath("$.registros[1].produccion.id").value(mes))
				.andExpect(jsonPath("$.registros[1].fecha").value("2023-06-01"))
				.andExpect(jsonPath("$.registros[2].produccion.id").value(anio))
				.andExpect(jsonPath("$.registros[2].fecha").value("2023-01-01"))
				.andExpect(jsonPath("$.sinFecha.length()").value(1))
				.andExpect(jsonPath("$.sinFecha[0].produccion.id").value(nunca))
				.andExpect(jsonPath("$.sinFecha[0].fecha").isEmpty());
	}

	/** HU-13 y D26: los números son solo sobre registros propios. */
	@Test
	void lasEstadisticasSonSoloDeLoPropio() throws Exception {
		Long primera = crearProduccion("Tercer cuerpo");
		Long segunda = crearProduccion("El pasado es un animal grotesco");
		MockHttpSession propia = cuenta("omar");
		MockHttpSession ajena = cuenta("pedro");

		registrar(propia, primera, "2023-07-07", "DIA", 8, null);
		registrar(propia, primera, "2024-07-07", "DIA", 6, null);
		registrar(propia, segunda, null, "SIN_FECHA", null, null);
		registrar(ajena, primera, "2024-07-08", "DIA", 1, null);

		mockMvc.perform(get("/api/usuarios/omar"))
				.andExpect(jsonPath("$.estadisticas.totalRegistros").value(3))
				.andExpect(jsonPath("$.estadisticas.totalProducciones").value(2))
				.andExpect(jsonPath("$.estadisticas.promedioPropio").value(7.0))
				.andExpect(jsonPath("$.estadisticas.registrosSinFecha").value(1))
				.andExpect(jsonPath("$.estadisticas.porAnio[0].anio").value(2024))
				.andExpect(jsonPath("$.estadisticas.porAnio[0].cantidad").value(1))
				.andExpect(jsonPath("$.estadisticas.porAnio[1].anio").value(2023));
	}

	/** HU-14: las reseñas van firmadas y con el rating de ese registro; las vacías no van. */
	@Test
	void lasResenasDeLaFichaVienenFirmadas() throws Exception {
		Long produccion = crearProduccion("Mi hijo solo camina un poco más lento");
		MockHttpSession sofia = cuenta("sofia");
		MockHttpSession callado = cuenta("ramon");

		registrar(sofia, produccion, "2025-04-04", "DIA", 9, "Me dejó pensando una semana");
		registrar(callado, produccion, "2025-04-05", "DIA", 5, null);

		mockMvc.perform(get("/api/producciones/" + produccion + "/opiniones"))
				.andExpect(jsonPath("$.promedio").value(7.0))
				.andExpect(jsonPath("$.cantidadRatings").value(2))
				.andExpect(jsonPath("$.resenias.length()").value(1))
				.andExpect(jsonPath("$.resenias[0].autor").value("sofia"))
				.andExpect(jsonPath("$.resenias[0].texto").value("Me dejó pensando una semana"))
				.andExpect(jsonPath("$.resenias[0].rating").value(9))
				.andExpect(jsonPath("$.resenias[0].fecha").value("2025-04-04"));
	}

	/** Una ficha que nadie registró todavía se muestra igual; una que no existe es 404. */
	@Test
	void unaFichaSinRegistrosNoTieneOpinionesPeroResponde() throws Exception {
		Long produccion = crearProduccion("Recién estrenada");

		mockMvc.perform(get("/api/producciones/" + produccion + "/opiniones"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.promedio").isEmpty())
				.andExpect(jsonPath("$.cantidadRatings").value(0))
				.andExpect(jsonPath("$.resenias").isEmpty());

		mockMvc.perform(get("/api/producciones/999999/opiniones"))
				.andExpect(status().isNotFound());
	}

	/** El catálogo es cerrado (D7): no se puede registrar algo que no está. Y registrar pide cuenta. */
	@Test
	void elRegistroPideSesionYUnaProduccionDelCatalogo() throws Exception {
		Long produccion = crearProduccion("Todo verde");
		String cuerpo = """
				{"produccionId":%d,"granularidad":"SIN_FECHA"}""".formatted(produccion);

		mockMvc.perform(json(post("/api/registros"), cuerpo))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(json(post("/api/registros"), """
				{"produccionId":999999,"granularidad":"SIN_FECHA"}""").session(cuenta("hilda")))
				.andExpect(status().isNotFound());
	}

	/** Las reglas de MD-1: la fecha y su granularidad tienen que cerrar, y el futuro no se vio. */
	@Test
	void laFechaTieneQueCerrarConSuGranularidad() throws Exception {
		Long produccion = crearProduccion("La fecha imposible");
		MockHttpSession sesion = cuenta("elsa");

		mockMvc.perform(json(post("/api/registros"), """
				{"produccionId":%d,"fecha":"2024-01-01","granularidad":"SIN_FECHA"}"""
				.formatted(produccion)).session(sesion))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.fecha").exists());

		mockMvc.perform(json(post("/api/registros"), """
				{"produccionId":%d,"granularidad":"DIA"}""".formatted(produccion)).session(sesion))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.fecha").exists());

		mockMvc.perform(json(post("/api/registros"), """
				{"produccionId":%d,"fecha":"%s","granularidad":"DIA"}"""
				.formatted(produccion, LocalDate.now().plusDays(1))).session(sesion))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.fecha").exists());

		mockMvc.perform(json(post("/api/registros"), """
				{"produccionId":%d,"granularidad":"SIN_FECHA","rating":11}"""
				.formatted(produccion)).session(sesion))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.rating").exists());
	}

	/**
	 * El registro es de la persona, no del catálogo: si el admin borra la ficha, la línea del
	 * diario sigue estando (sin título, porque ya no hay a qué linkear).
	 */
	@Test
	void borrarLaProduccionNoBorraElRegistroDeNadie() throws Exception {
		Long produccion = crearProduccion("La que se va a borrar");
		MockHttpSession sesion = cuenta("gustavo");
		registrar(sesion, produccion, "2024-02-02", "DIA", 7, null);

		mockMvc.perform(conCsrf(delete("/api/admin/producciones/" + produccion))
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/usuarios/gustavo"))
				.andExpect(jsonPath("$.registros.length()").value(1))
				.andExpect(jsonPath("$.registros[0].produccion").isEmpty())
				.andExpect(jsonPath("$.registros[0].rating").value(7));
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

	private Long registrar(MockHttpSession sesion, Long produccionId, String fecha, String granularidad,
			Integer rating, String resenia) throws Exception {
		MvcResult resultado = mockMvc.perform(json(post("/api/registros"), """
				{"produccionId":%d,"fecha":%s,"granularidad":"%s","rating":%s,"resenia":%s}"""
				.formatted(produccionId, comillas(fecha), granularidad, rating, comillas(resenia)))
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
