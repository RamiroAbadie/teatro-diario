package io.github.ramiroabadie.backend.identidad;

import java.util.List;
import java.util.UUID;

import io.github.ramiroabadie.backend.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los criterios de aceptación de HU-01 y HU-02, ejecutables. Primeros tests del proyecto:
 * la deuda de testing vuelve progresivamente desde la Fase 2 y arranca por lo que rompe
 * silenciosamente — la autenticación.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AutenticacionTest {

	private static final String TOKEN_CSRF = "un-token-de-prueba";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void elAltaDejaLaSesionIniciada() throws Exception {
		MockHttpSession sesion = registrar("candela", "candela@example.com", "unaClaveLarga");

		mockMvc.perform(get("/api/auth/yo").session(sesion))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("candela"))
				.andExpect(jsonPath("$.rol").value("USUARIO"));
	}

	@Test
	void elAltaNormalizaYRechazaRepetidos() throws Exception {
		registrar("bruno", "bruno@example.com", "unaClaveLarga");

		mockMvc.perform(json(post("/api/auth/registro"), """
				{"username":"BRUNO","email":"otro@example.com","password":"unaClaveLarga"}"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.errores.username").exists());

		mockMvc.perform(json(post("/api/auth/registro"), """
				{"username":"otro","email":"Bruno@Example.com","password":"unaClaveLarga"}"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.errores.email").exists());
	}

	@Test
	void elAltaInformaQueCampoEstaMal() throws Exception {
		mockMvc.perform(json(post("/api/auth/registro"), """
				{"username":"con espacios","email":"no-es-un-email","password":"corta"}"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.username").exists())
				.andExpect(jsonPath("$.errores.email").exists())
				.andExpect(jsonPath("$.errores.password").exists());
	}

	@Test
	void seEntraConEmailOConUsername() throws Exception {
		registrar("delfina", "delfina@example.com", "unaClaveLarga");

		mockMvc.perform(json(post("/api/auth/login"), """
				{"identificador":"delfina","password":"unaClaveLarga"}"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("delfina"));

		mockMvc.perform(json(post("/api/auth/login"), """
				{"identificador":"DELFINA@example.com","password":"unaClaveLarga"}"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("delfina"));
	}

	@Test
	void elErrorDeLoginNoDelataQueCampoFallo() throws Exception {
		registrar("elena", "elena@example.com", "unaClaveLarga");

		String claveMala = mensajeDeError("""
				{"identificador":"elena","password":"otraClaveLarga"}""");
		String usuarioInexistente = mensajeDeError("""
				{"identificador":"nadie","password":"otraClaveLarga"}""");

		assertThat(claveMala).isEqualTo(usuarioInexistente);
	}

	@Test
	void elLogoutInvalidaLaSesion() throws Exception {
		MockHttpSession sesion = registrar("federico", "federico@example.com", "unaClaveLarga");

		mockMvc.perform(conCsrf(post("/api/auth/logout").session(sesion)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/auth/yo").session(sesion))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void escribirElCatalogoExigeAdminYLeerloNoPideNada() throws Exception {
		String sala = """
				{"nombre":"Sala de prueba"}""";

		mockMvc.perform(json(post("/api/admin/salas"), sala))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(json(post("/api/admin/salas"), sala).with(user("comun").roles("USUARIO")))
				.andExpect(status().isForbidden());
		mockMvc.perform(json(post("/api/admin/salas"), sala).with(user("jefe").roles("ADMIN")))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/en-cartel")).andExpect(status().isOk());
	}

	/**
	 * Entrar rota el token CSRF, así que la respuesta del login tiene que traer el nuevo: si no,
	 * la primera escritura después de entrar se come un 403 y el cliente no entiende por qué.
	 */
	@Test
	void elLoginDevuelveElTokenCsrfNuevo() throws Exception {
		registrar("gaston", "gaston@example.com", "unaClaveLarga");
		String tokenPrevio = UUID.randomUUID().toString();

		MvcResult resultado = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"identificador":"gaston","password":"unaClaveLarga"}""")
				.cookie(new Cookie("XSRF-TOKEN", tokenPrevio))
				.header("X-XSRF-TOKEN", tokenPrevio))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(tokenCsrfEmitido(resultado)).isNotBlank().isNotEqualTo(tokenPrevio);
	}

	@Test
	void sinTokenCsrfNoSeEscribe() throws Exception {
		mockMvc.perform(get("/api/en-cartel"))
				.andExpect(cookie().exists("XSRF-TOKEN"));

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"identificador":"quien","password":"unaClaveLarga"}"""))
				.andExpect(status().isForbidden());
	}

	private MockHttpSession registrar(String username, String email, String password) throws Exception {
		MvcResult resultado = mockMvc.perform(json(post("/api/auth/registro"), """
				{"username":"%s","email":"%s","password":"%s"}""".formatted(username, email, password)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value(username))
				.andReturn();
		return (MockHttpSession) resultado.getRequest().getSession(false);
	}

	private String mensajeDeError(String cuerpo) throws Exception {
		return mockMvc.perform(json(post("/api/auth/login"), cuerpo))
				.andExpect(status().isUnauthorized())
				.andReturn().getResponse().getContentAsString();
	}

	/**
	 * La respuesta del login trae dos Set-Cookie del token: primero el borrado del anterior y
	 * después el nuevo, que es el que queda en el cliente.
	 */
	private String tokenCsrfEmitido(MvcResult resultado) {
		List<String> emitidas = resultado.getResponse().getHeaders(HttpHeaders.SET_COOKIE).stream()
				.filter(cookie -> cookie.startsWith("XSRF-TOKEN="))
				.toList();
		String ultima = emitidas.get(emitidas.size() - 1);
		return ultima.substring("XSRF-TOKEN=".length(), ultima.indexOf(';'));
	}

	private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String cuerpo) {
		return conCsrf(peticion).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
	}

	/**
	 * Doble envío: la cookie lleva el token esperado y el header el enviado, que es exactamente lo
	 * que hace un cliente. No se usa el post-processor {@code csrf()} de spring-security-test a
	 * propósito: ese le cambia el repositorio al CsrfFilter del contexto — que es compartido por
	 * toda la clase — y a partir de ahí las cookies reales dejan de valer.
	 */
	private MockHttpServletRequestBuilder conCsrf(MockHttpServletRequestBuilder peticion) {
		return peticion.cookie(new Cookie("XSRF-TOKEN", TOKEN_CSRF)).header("X-XSRF-TOKEN", TOKEN_CSRF);
	}
}
