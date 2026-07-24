package io.github.ramiroabadie.backend.identidad;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import io.github.ramiroabadie.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lo mismo que {@code AutenticacionTest} pero contra Tomcat de verdad, porque hay una familia
 * de errores que MockMvc no puede ver: cuando el servlet responde con {@code sendError}, el
 * contenedor hace un segundo despacho hacia {@code /error} que también pasa por la cadena de
 * seguridad. MockMvc no hace ese segundo despacho, así que un 404, un 400 y un 403 se veían
 * bien en los tests y llegaban al cliente convertidos en 401.
 *
 * <p>Cliente HTTP del JDK con su propio frasco de cookies: las cookies son justamente lo que
 * está bajo prueba, así que conviene que no las maneje nadie más.</p>
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AutenticacionHttpTest {

	@LocalServerPort
	private int puerto;

	private CookieManager cookies;

	private HttpClient cliente;

	@BeforeEach
	void abrirCliente() {
		this.cookies = new CookieManager();
		this.cliente = HttpClient.newBuilder().cookieHandler(this.cookies).build();
	}

	@Test
	void unaRutaInexistenteResponde404YNo401() throws Exception {
		assertThat(get("/api/esto-no-existe").statusCode()).isEqualTo(404);
	}

	@Test
	void unJsonRotoResponde400YNo401() throws Exception {
		get("/api/en-cartel");

		assertThat(post("/api/auth/registro", "{roto", true).statusCode()).isEqualTo(400);
	}

	@Test
	void sinTokenCsrfResponde403YNo401() throws Exception {
		get("/api/en-cartel");

		HttpResponse<String> respuesta = post("/api/auth/login", """
				{"identificador":"quien","password":"unaClaveLarga"}""", false);

		assertThat(respuesta.statusCode()).isEqualTo(403);
	}

	/**
	 * BCrypt mide bytes, no caracteres: 40 letras con tilde son 80 bytes. Antes pasaba la
	 * validación y explotaba al hashear.
	 */
	@Test
	void unaPasswordDeMuchosBytesEsUnErrorDeCampo() throws Exception {
		get("/api/en-cartel");

		HttpResponse<String> respuesta = post("/api/auth/registro", """
				{"username":"multibyte","email":"multibyte@example.com","password":"%s"}"""
				.formatted("á".repeat(40)), true);

		assertThat(respuesta.statusCode()).isEqualTo(400);
		assertThat(respuesta.body()).contains("\"password\"");
	}

	@Test
	void un401AnonimoNoAbreSesion() throws Exception {
		assertThat(get("/api/auth/yo").statusCode()).isEqualTo(401);
		assertThat(cookie("JSESSIONID")).isEmpty();
	}

	/** Salir y volver a entrar es un movimiento normal: no puede pedir un GET en el medio. */
	@Test
	void despuesDelLogoutSePuedeVolverAEntrarDeUnaSolaVez() throws Exception {
		get("/api/en-cartel");
		post("/api/auth/registro", """
				{"username":"ines","email":"ines@example.com","password":"unaClaveLarga"}""", true);

		assertThat(post("/api/auth/logout", null, true).statusCode()).isEqualTo(204);
		assertThat(cookie("JSESSIONID")).isEmpty();

		HttpResponse<String> reingreso = post("/api/auth/login", """
				{"identificador":"ines","password":"unaClaveLarga"}""", true);

		assertThat(reingreso.statusCode()).isEqualTo(200);
	}

	private HttpResponse<String> get(String ruta) throws Exception {
		return enviar(peticion(ruta).GET(), false);
	}

	private HttpResponse<String> post(String ruta, String cuerpo, boolean conCsrf) throws Exception {
		HttpRequest.Builder peticion = peticion(ruta)
				.POST(cuerpo == null ? HttpRequest.BodyPublishers.noBody()
						: HttpRequest.BodyPublishers.ofString(cuerpo));
		if (cuerpo != null) {
			peticion.header("Content-Type", "application/json");
		}
		return enviar(peticion, conCsrf);
	}

	private HttpRequest.Builder peticion(String ruta) {
		return HttpRequest.newBuilder(URI.create("http://localhost:" + this.puerto + ruta));
	}

	private HttpResponse<String> enviar(HttpRequest.Builder peticion, boolean conCsrf) throws Exception {
		if (conCsrf) {
			peticion.header("X-XSRF-TOKEN", cookie("XSRF-TOKEN")
					.orElseThrow(() -> new IllegalStateException("no hay token CSRF todavía")));
		}
		return this.cliente.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
	}

	private Optional<String> cookie(String nombre) {
		return this.cookies.getCookieStore().getCookies().stream()
				.filter(cookie -> cookie.getName().equals(nombre))
				.map(HttpCookie::getValue)
				.filter(valor -> !valor.isEmpty())
				.findFirst();
	}
}
