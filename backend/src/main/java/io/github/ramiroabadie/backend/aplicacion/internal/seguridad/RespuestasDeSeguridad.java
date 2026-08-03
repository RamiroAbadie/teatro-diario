package io.github.ramiroabadie.backend.aplicacion.internal.seguridad;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;

/**
 * El {@code 401} y el {@code 403} que decide la cadena de filtros, con la misma forma que el
 * resto de los errores de la API ({@code ErroresDeApi}). Son los dos casos que ningún
 * {@code @ControllerAdvice} puede tocar: pasan antes de que exista un controlador al que
 * despachar, así que hasta acá salían con el cuerpo que armaba el contenedor o directamente sin
 * ninguno.
 *
 * <p>Los dos mensajes del {@code 403} están separados a propósito: el de permisos es definitivo
 * —esta cuenta no puede hacer eso— y el de CSRF se arregla solo reintentando con un token nuevo,
 * que es lo que el frontend hace una vez y en silencio (`FRONTEND_ARCHITECTURE.md`).</p>
 */
class RespuestasDeSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final ObjectMapper json;

	RespuestasDeSeguridad(ObjectMapper json) {
		this.json = json;
	}

	/**
	 * Sin sesión. No es un error a mostrar y el frontend lo sabe: en {@code /api/auth/yo} es el
	 * estado normal de la mayoría de las visitas y en una acción protegida es el desvío al login.
	 * El cuerpo está igual porque una respuesta de esta API no se distingue por tener o no tener
	 * cuerpo, y porque el que lo necesite no tiene de dónde sacarlo.
	 */
	@Override
	public void commence(HttpServletRequest peticion, HttpServletResponse respuesta,
			AuthenticationException ex) throws IOException {
		escribir(respuesta, HttpStatus.UNAUTHORIZED, "No hay una sesión iniciada");
	}

	@Override
	public void handle(HttpServletRequest peticion, HttpServletResponse respuesta,
			AccessDeniedException ex) throws IOException {
		escribir(respuesta, HttpStatus.FORBIDDEN, ex instanceof CsrfException
				? "El token de seguridad venció. Probá de nuevo"
				: "No tenés permiso para hacer eso");
	}

	/**
	 * Escribe el cuerpo a mano porque acá no hay conversor de mensajes: esto corre en un filtro.
	 * Lo que no se toca son los headers ya emitidos —la cookie del token CSRF viaja en respuestas
	 * que terminan en {@code 401}, y de eso depende que la primera mutación de un visitante
	 * anónimo no se coma un {@code 403} (D78/D82)—: se fija el estado y se escribe, sin
	 * {@code sendError} ni {@code reset}.
	 */
	private void escribir(HttpServletResponse respuesta, HttpStatus estado, String detalle) throws IOException {
		if (respuesta.isCommitted()) {
			return;
		}
		respuesta.setStatus(estado.value());
		respuesta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
		this.json.writeValue(respuesta.getOutputStream(), ProblemDetail.forStatusAndDetail(estado, detalle));
	}
}
