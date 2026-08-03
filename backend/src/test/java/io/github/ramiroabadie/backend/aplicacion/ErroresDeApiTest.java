package io.github.ramiroabadie.backend.aplicacion;

import io.github.ramiroabadie.backend.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El manejo global de errores: que la API responda con una sola forma de error y no con tres.
 * Cada test de acá es una de las tres familias que `API.md` describía como algo que el frontend
 * tenía que tolerar, y lo que se comprueba es que ya no se distinguen.
 *
 * <p>Lo que este archivo NO comprueba, a propósito: los errores de dominio de cada módulo —el 404
 * de una ficha, el 409 de una cola ya resuelta, el 403 de un registro ajeno—. Esos siguen donde
 * estaban, ya salían con esta forma y los prueban los tests de su historia.</p>
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ErroresDeApiTest {

	private static final String TOKEN_CSRF = "un-token-de-prueba";

	@Autowired
	private MockMvc mockMvc;

	/**
	 * El hueco concreto que esto vino a tapar: los formularios del admin validaban igual que los
	 * demás, pero el 400 salía sin decir qué campo estaba mal, así que la pantalla solo podía
	 * decir "revisá el formulario". Ahora informan campo por campo como el alta de cuenta.
	 */
	@Test
	void losFormulariosDelAdminInformanElErrorDeCadaCampo() throws Exception {
		mockMvc.perform(json(post("/api/admin/salas"), """
				{"nombre":"   "}""").with(user("jefa").roles("ADMIN")))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.detail").value("Revisá los datos ingresados"))
				.andExpect(jsonPath("$.errores.nombre").isNotEmpty());

		mockMvc.perform(json(post("/api/admin/producciones"), """
				{"sinopsis":"Sin título ni estado"}""").with(user("jefa").roles("ADMIN")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.titulo").isNotEmpty())
				.andExpect(jsonPath("$.errores.estado").isNotEmpty());
	}

	/**
	 * La familia que armaba el framework: hasta acá esto salía con el cuerpo que Boot le pone a
	 * un error genérico, sin {@code detail} garantizado y en inglés cuando lo había.
	 */
	@Test
	void losErroresDeSpringSalenComoProblemDetailYEnCastellano() throws Exception {
		mockMvc.perform(get("/api/esto-no-existe"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.detail").value("Eso no existe o ya no está"));

		mockMvc.perform(json(post("/api/auth/registro"), "{roto"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Revisá los datos ingresados"));

		// Con sesión: sin ella, el candado de escritura responde antes de que exista un método
		// que no coincida (SecurityConfig abre los GET de /api/** y nada más).
		mockMvc.perform(conCsrf(post("/api/en-cartel")).with(user("comun").roles("USUARIO")))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.detail").value("Esa acción no se puede hacer así"));
	}

	/**
	 * El 401 y el 403 de la cadena de filtros, que no pasan por ningún controlador. Los dos 403
	 * dicen cosas distintas porque son cosas distintas: el de permisos no se arregla reintentando
	 * y el de CSRF sí.
	 */
	@Test
	void elCandadoDeSeguridadResponderConLaMismaForma() throws Exception {
		mockMvc.perform(json(post("/api/admin/salas"), """
				{"nombre":"Sala sin permiso"}"""))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.detail").value("No hay una sesión iniciada"));

		mockMvc.perform(json(post("/api/admin/salas"), """
				{"nombre":"Sala sin permiso"}""").with(user("comun").roles("USUARIO")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.detail").value("No tenés permiso para hacer eso"));

		mockMvc.perform(post("/api/admin/salas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nombre":"Sala sin token"}""")
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.detail").value("El token de seguridad venció. Probá de nuevo"));
	}

	/**
	 * El 401 sigue trayendo la cookie del token CSRF, que es de lo que depende que un visitante
	 * anónimo pueda crearse una cuenta sin comerse un 403 (D78/D82). Lo que cambió es el cuerpo,
	 * y el cuerpo no toca los headers ya emitidos — que es justo lo que había que comprobar.
	 */
	@Test
	void elCuerpoDelUnauthorizedNoSeLlevaPuestaLaCookieDelToken() throws Exception {
		mockMvc.perform(get("/api/auth/yo"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").isNotEmpty())
				.andExpect(result -> {
					Cookie cookie = result.getResponse().getCookie("XSRF-TOKEN");
					if (cookie == null || cookie.getValue().isBlank()) {
						throw new AssertionError("El 401 de /api/auth/yo tiene que sembrar el token CSRF");
					}
				});
	}

	private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String cuerpo) {
		return conCsrf(peticion).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
	}

	private MockHttpServletRequestBuilder conCsrf(MockHttpServletRequestBuilder peticion) {
		return peticion.cookie(new Cookie("XSRF-TOKEN", TOKEN_CSRF)).header("X-XSRF-TOKEN", TOKEN_CSRF);
	}
}
