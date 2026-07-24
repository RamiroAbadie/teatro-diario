package io.github.ramiroabadie.backend.aplicacion.internal.curaduria;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Las operaciones de curaduría que ningún módulo puede resolver solo. Por ahora hay una: fusionar
 * una producción duplicada en la canónica (D63, ampliación de HU-20).
 *
 * <p>Cuelga de {@code /api/admin} como el resto del panel, así que el candado de rol lo pone
 * {@code SecurityConfig} sin ninguna regla nueva. El botón y la confirmación son de la pantalla
 * del panel, que llega con el frontend.</p>
 */
@RestController
@RequestMapping("/api/admin/producciones")
class CuraduriaController {

	private final FusionDeProducciones fusion;

	CuraduriaController(FusionDeProducciones fusion) {
		this.fusion = fusion;
	}

	/**
	 * La ficha de la URL es la que desaparece. Devuelve 200 y no 204 porque el admin necesita ver
	 * cuántos registros se movieron.
	 */
	@PostMapping("/{origenId}/fusionar")
	public FusionResponse fusionar(@PathVariable Long origenId, @Valid @RequestBody FusionRequest req) {
		return fusion.fusionar(origenId, req.destinoId());
	}

	@ExceptionHandler(FichaInexistenteException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrada(FichaInexistenteException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(FusionInvalidaException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProblemDetail invalida(FusionInvalidaException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
	}
}
