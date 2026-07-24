package io.github.ramiroabadie.backend.aplicacion;

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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La fusión de fichas duplicadas (D63, ampliación de HU-20). Lo que estos tests cuidan no es la
 * operación en sí —mover una columna es fácil— sino que el historial de la gente salga entero del
 * otro lado: los registros, sus puntajes, sus reseñas y el promedio de D20 calculado ahora sobre
 * el conjunto unido.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class FusionDeProduccionesTest {

	private static final String TOKEN_CSRF = "un-token-de-prueba";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void losRegistrosDeLaFichaDuplicadaPasanALaCanonica() throws Exception {
		Long duplicada = crearProduccion("Un tranvía llamado deseo (duplicada)");
		Long canonica = crearProduccion("Un tranvía llamado deseo");
		MockHttpSession sesion = cuenta("valeria");
		registrar(sesion, duplicada, "2024-04-04", 8, "La vi en la sala chica");

		fusionar(duplicada, canonica)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.destinoId").value(canonica))
				.andExpect(jsonPath("$.registrosReasignados").value(1));

		mockMvc.perform(get("/api/usuarios/valeria"))
				.andExpect(jsonPath("$.registros.length()").value(1))
				.andExpect(jsonPath("$.registros[0].produccion.id").value(canonica))
				.andExpect(jsonPath("$.registros[0].produccion.titulo").value("Un tranvía llamado deseo"))
				.andExpect(jsonPath("$.registros[0].produccion.enCatalogo").value(true))
				.andExpect(jsonPath("$.registros[0].rating").value(8))
				.andExpect(jsonPath("$.registros[0].resenia").value("La vi en la sala chica"));

		mockMvc.perform(get("/api/producciones/" + duplicada)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/producciones/" + canonica)).andExpect(status().isOk());
	}

	/** Las dos mitades de la conversación quedan en la misma ficha, que es el punto de fusionar. */
	@Test
	void lasResenasDeLasDosFichasQuedanJuntas() throws Exception {
		Long duplicada = crearProduccion("Los días felices (duplicada)");
		Long canonica = crearProduccion("Los días felices");
		registrar(cuenta("marina"), duplicada, "2025-02-02", 7, "Winnie enterrada hasta la cintura");
		registrar(cuenta("nicolas"), canonica, "2025-02-03", 9, "Me arruinó el resto del año");

		fusionar(duplicada, canonica).andExpect(status().isOk());

		mockMvc.perform(get("/api/producciones/" + canonica + "/opiniones"))
				.andExpect(jsonPath("$.resenias.length()").value(2))
				.andExpect(jsonPath("$.cantidadRatings").value(2))
				.andExpect(jsonPath("$.promedio").value(8.0));
	}

	/**
	 * El caso que hace falta pensar: alguien que registró las dos fichas. Después de fusionar son
	 * re-vistos de la misma obra (D19) y D20 se queda con el de la función más reciente, no con
	 * los dos.
	 */
	@Test
	void elPromedioSigueSiendoElUltimoRatingDeCadaUsuario() throws Exception {
		Long duplicada = crearProduccion("La vida es sueño (duplicada)");
		Long canonica = crearProduccion("La vida es sueño");
		MockHttpSession repetidora = cuenta("paula");
		registrar(repetidora, duplicada, "2023-05-05", 3, null);
		registrar(repetidora, canonica, "2025-05-05", 9, null);
		registrar(cuenta("ariel"), duplicada, "2024-05-05", 5, null);

		fusionar(duplicada, canonica).andExpect(status().isOk())
				.andExpect(jsonPath("$.registrosReasignados").value(2));

		// (9 de Paula + 5 de Ariel) / 2, y no el promedio de los tres registros, que daría 5.7.
		mockMvc.perform(get("/api/producciones/" + canonica + "/opiniones"))
				.andExpect(jsonPath("$.promedio").value(7.0))
				.andExpect(jsonPath("$.cantidadRatings").value(2));

		mockMvc.perform(get("/api/usuarios/paula"))
				.andExpect(jsonPath("$.estadisticas.totalRegistros").value(2))
				.andExpect(jsonPath("$.estadisticas.totalProducciones").value(1));
	}

	@Test
	void fusionarConsigoMismaOConUnaFichaQueNoExisteNoTocaNada() throws Exception {
		Long duplicada = crearProduccion("Esperando la carroza (duplicada)");
		MockHttpSession sesion = cuenta("bruna");
		registrar(sesion, duplicada, "2024-10-10", 6, null);

		fusionar(duplicada, duplicada).andExpect(status().isBadRequest());
		fusionar(duplicada, 999999L).andExpect(status().isNotFound());
		fusionar(999999L, duplicada).andExpect(status().isNotFound());

		mockMvc.perform(get("/api/usuarios/bruna"))
				.andExpect(jsonPath("$.registros[0].produccion.id").value(duplicada))
				.andExpect(jsonPath("$.registros[0].produccion.enCatalogo").value(true));
		mockMvc.perform(get("/api/producciones/" + duplicada)).andExpect(status().isOk());
	}

	/** Fusionar es escribir el catálogo: solo admin (D7), y con token como toda escritura (D57). */
	@Test
	void fusionarEsCosaDeAdmin() throws Exception {
		Long duplicada = crearProduccion("Doña Rosita (duplicada)");
		Long canonica = crearProduccion("Doña Rosita");
		String cuerpo = """
				{"destinoId":%d}""".formatted(canonica);
		String url = "/api/admin/producciones/" + duplicada + "/fusionar";

		mockMvc.perform(json(post(url), cuerpo)).andExpect(status().isUnauthorized());
		mockMvc.perform(json(post(url), cuerpo).with(user("comun").roles("USUARIO")))
				.andExpect(status().isForbidden());
		mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(cuerpo)
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isForbidden());
	}

	private ResultActions fusionar(Long origenId, Long destinoId) throws Exception {
		return mockMvc.perform(json(post("/api/admin/producciones/" + origenId + "/fusionar"), """
				{"destinoId":%d}""".formatted(destinoId))
				.with(user("jefa").roles("ADMIN")));
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

	private void registrar(MockHttpSession sesion, Long produccionId, String fecha, Integer rating,
			String resenia) throws Exception {
		mockMvc.perform(json(post("/api/registros"), """
				{"produccionId":%d,"fecha":"%s","granularidad":"DIA","rating":%s,"resenia":%s}"""
				.formatted(produccionId, fecha, rating, resenia == null ? "null" : "\"" + resenia + "\""))
				.session(sesion))
				.andExpect(status().isCreated());
	}

	private static Long id(MvcResult resultado) throws Exception {
		return ((Number) JsonPath.read(resultado.getResponse().getContentAsString(), "$.id")).longValue();
	}

	private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String cuerpo) {
		return conCsrf(peticion).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
	}

	private MockHttpServletRequestBuilder conCsrf(MockHttpServletRequestBuilder peticion) {
		return peticion.cookie(new Cookie("XSRF-TOKEN", TOKEN_CSRF)).header("X-XSRF-TOKEN", TOKEN_CSRF);
	}
}
