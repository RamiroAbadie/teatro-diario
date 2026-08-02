package io.github.ramiroabadie.backend.aplicacion.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * El manejo global de errores, que le faltaba a la API: hasta acá cada controlador declaraba los
 * suyos y todo lo demás lo armaba el framework, así que las respuestas de error salían en tres
 * formas distintas (`API.md`). Con esto la forma es una sola —{@code ProblemDetail} con un
 * {@code detail} en castellano, y el mapa {@code errores} cuando el problema es de campos— y el
 * frontend deja de tener que tolerar la tercera.
 *
 * <p>Es transversal por definición, así que vive en la capa de aplicación (MODULE_MAP) y no
 * importa una sola clase de un módulo: los errores de dominio siguen donde estaban. Eso no es una
 * concesión, es la regla de siempre: la mitad de esas excepciones son internas de su módulo y la
 * capa de aplicación no las puede nombrar (ADR-001), y la otra mitad no tiene un estado HTTP fijo
 * —{@code DataIntegrityViolationException} es un 409 al borrar una sala y un 204 al dar dos veces
 * el mismo like—. Qué significa un error de dominio lo decide el endpoint; lo que se unifica acá
 * es la forma en que sale.</p>
 *
 * <p>Un {@code @ExceptionHandler} declarado en un controlador le gana a este, que es justo lo que
 * hace falta donde el mensaje es una decisión: el {@code 401} del login dice "Email/usuario o
 * contraseña incorrectos" a propósito, sin revelar cuál de los dos falló (HU-02).</p>
 */
@RestControllerAdvice
class ErroresDeApi extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ErroresDeApi.class);

	/**
	 * Los mensajes que ve el usuario cuando el error lo detectó el framework y no el dominio. Van
	 * en castellano y en voz del producto (D79/D84) porque el frontend los muestra tal cual: los
	 * que trae Spring —"Invalid request content."— son correctos y están en otro idioma.
	 */
	private static final Map<HttpStatus, String> MENSAJES = Map.of(
			HttpStatus.BAD_REQUEST, "Revisá los datos ingresados",
			HttpStatus.UNAUTHORIZED, "No hay una sesión iniciada",
			HttpStatus.FORBIDDEN, "No tenés permiso para hacer eso",
			HttpStatus.NOT_FOUND, "Eso no existe o ya no está",
			HttpStatus.METHOD_NOT_ALLOWED, "Esa acción no se puede hacer así",
			HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Ese tipo de contenido no se acepta");

	/** Lo único que se le puede decir a alguien sobre un error nuestro, sin mentirle. */
	private static final String FALLA_INTERNA = "Algo falló de nuestro lado. Probá de nuevo en un rato";

	/** Para el estado que la tabla no nombre: mejor esto que un mensaje en inglés o ninguno. */
	private static final String GENERICO = "No pudimos procesar el pedido";

	/**
	 * La validación por anotaciones, para TODOS los formularios y no para cuatro: es lo que
	 * permite pintar el error al lado de cada input, que es lo que al panel admin le faltaba
	 * (`API.md`). Un campo con dos violaciones informa la primera: mostrar dos mensajes en un
	 * input es peor que mostrar uno.
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> porCampo = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			porCampo.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problema.setProperty("errores", porCampo);
		return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
	}

	/**
	 * La sesión sigue viva pero la cuenta ya no existe, que es el {@code 401} que tiraban cinco
	 * controladores por su cuenta. El mensaje es el de la excepción porque las de esta familia
	 * las tiramos nosotros y ya están escritas para leerse; las de Spring no llegan hasta acá,
	 * las atajan los controladores que autentican.
	 */
	@ExceptionHandler(AuthenticationException.class)
	ResponseEntity<Object> sinSesion(AuthenticationException ex, WebRequest request) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
		return handleExceptionInternal(ex, problema, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
	}

	/**
	 * La red de contención: lo que no atajó nadie sale como un 500 con forma y sin tripas. Al
	 * usuario no se le puede decir nada útil, así que lo importante de este método es la otra
	 * mitad — que el error quede en el log del servidor, que es el único lugar donde se ve.
	 */
	@ExceptionHandler(Exception.class)
	ResponseEntity<Object> falloInesperado(Exception ex, WebRequest request) throws Exception {
		log.error("Error no manejado en la API", ex);
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, FALLA_INTERNA);
		return handleExceptionInternal(ex, problema, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
	}

	/**
	 * Los errores de Spring MVC que traen su propio cuerpo —el {@code 404} de una ruta que no
	 * existe, el {@code 405}, el {@code 415}—: la clase base ya los devuelve como
	 * {@code ProblemDetail} y lo que les falta es el idioma. Se traducen acá y no en un método
	 * por excepción para que no se pueda olvidar ninguna: un error de Spring MVC que hoy no
	 * ocurre entra ya traducido el día que ocurra.
	 *
	 * <p>Las respuestas de los dos manejadores de arriba pasan por acá con su mensaje ya puesto y
	 * no se tocan: lo que las distingue es que su excepción no es un {@code ErrorResponse} de
	 * Spring. La única que es las dos cosas es la validación, y ahí la traducción es justamente
	 * lo que se busca.</p>
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		ResponseEntity<Object> respuesta = super.handleExceptionInternal(ex, body, headers, status, request);
		if (ex instanceof ErrorResponse && respuesta != null
				&& respuesta.getBody() instanceof ProblemDetail problema) {
			problema.setDetail(mensajeDe(status));
		}
		return respuesta;
	}

	/** La otra mitad: los errores de Spring MVC a los que el cuerpo se lo arma la clase base. */
	@Override
	protected ProblemDetail createProblemDetail(Exception ex, HttpStatusCode status, String defaultDetail,
			String detailMessageCode, Object[] detailMessageArguments, WebRequest request) {
		ProblemDetail problema = super.createProblemDetail(ex, status, defaultDetail, detailMessageCode,
				detailMessageArguments, request);
		problema.setDetail(mensajeDe(status));
		return problema;
	}

	private static String mensajeDe(HttpStatusCode status) {
		if (status.is5xxServerError()) {
			return FALLA_INTERNA;
		}
		HttpStatus estado = HttpStatus.resolve(status.value());
		return estado == null ? GENERICO : MENSAJES.getOrDefault(estado, GENERICO);
	}
}
