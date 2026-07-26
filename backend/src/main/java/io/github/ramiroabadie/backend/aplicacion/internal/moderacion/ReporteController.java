package io.github.ramiroabadie.backend.aplicacion.internal.moderacion;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.aplicacion.internal.ReseniaInexistenteException;
import io.github.ramiroabadie.backend.aplicacion.internal.SesionActual;
import io.github.ramiroabadie.backend.diario.Diario;
import io.github.ramiroabadie.backend.social.ReportesDeResenias;

/**
 * El botón de reportar una reseña (HU-18, D40). Cuelga de la reseña como el like y por la misma
 * razón de nombre (D68): el id es el de un registro, pero un registro sin texto no es una reseña y
 * no hay nada que reportar, así que el 404 es el de un recurso que no está.
 *
 * <p>Vive en la capa de aplicación porque compone tres módulos que no se conocen (D60): Identidad
 * dice quién está del otro lado, Diario dice si eso es una reseña y de quién, y Social guarda el
 * aviso. Las dos comprobaciones salen de una sola pregunta a Diario (D70).</p>
 *
 * <p>Responde 204 sin cuerpo. No hay nada que devolver ni que consultar después: el reportante no
 * recibe respuesta a su aviso —no hay notificaciones en el MVP (MD-3)— y la cola es del admin.</p>
 */
@RestController
@RequestMapping("/api/resenias/{reseniaId}/reporte")
class ReporteController {

	private final ReportesDeResenias reportes;

	private final Diario diario;

	private final SesionActual sesion;

	ReporteController(ReportesDeResenias reportes, Diario diario, SesionActual sesion) {
		this.reportes = reportes;
		this.diario = diario;
		this.sesion = sesion;
	}

	/**
	 * El cuerpo es opcional: reportar sin motivo es un {@code POST} vacío, que es lo que manda un
	 * botón sin formulario detrás.
	 */
	@PostMapping
	public ResponseEntity<Void> reportar(@PathVariable Long reseniaId,
			@Valid @RequestBody(required = false) ReporteRequest req, Authentication autenticado) {
		Long yo = sesion.id(autenticado);
		Long autor = diario.autorDeResenia(reseniaId)
				.orElseThrow(() -> new ReseniaInexistenteException(reseniaId));
		if (autor.equals(yo)) {
			throw new ReseniaPropiaException();
		}
		reportes.reportar(yo, reseniaId, req == null ? null : req.motivo());
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(ReseniaInexistenteException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrada(ReseniaInexistenteException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	/** Como seguirse a uno mismo: el pedido está mal formado, no es un permiso que falte. */
	@ExceptionHandler(ReseniaPropiaException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProblemDetail propia(ReseniaPropiaException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	/** Campo y mensaje, como el resto de los formularios que llena una persona. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProblemDetail datosInvalidos(MethodArgumentNotValidException ex) {
		Map<String, String> porCampo = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			porCampo.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Revisá los datos del reporte");
		problema.setProperty("errores", porCampo);
		return problema;
	}

	/** La sesión sigue viva pero la cuenta se borró: para quien escribe es no estar logueado. */
	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ProblemDetail sesionSinCuenta(AuthenticationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}
}
