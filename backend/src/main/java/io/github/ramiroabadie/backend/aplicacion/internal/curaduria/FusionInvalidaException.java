package io.github.ramiroabadie.backend.aplicacion.internal.curaduria;

/**
 * La fusión pedida no tiene sentido: hoy, fusionar una ficha consigo misma. Es un 400 y no un
 * 409 porque el problema está en lo que se pidió, no en el estado del catálogo.
 */
class FusionInvalidaException extends RuntimeException {

	FusionInvalidaException(String mensaje) {
		super(mensaje);
	}
}
