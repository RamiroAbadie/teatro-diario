package io.github.ramiroabadie.backend.social;

/**
 * Un seguimiento que el grafo no admite. Hoy hay una sola forma de llegar acá: intentar seguirse
 * a uno mismo, que no está prohibido por gusto sino porque un usuario en su propio feed lo
 * convierte en un espejo, y el feed es para ver a los demás (HU-16).
 */
public class SeguimientoInvalidoException extends RuntimeException {

	public SeguimientoInvalidoException(String mensaje) {
		super(mensaje);
	}
}
