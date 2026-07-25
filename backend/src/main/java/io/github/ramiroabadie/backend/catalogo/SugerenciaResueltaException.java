package io.github.ramiroabadie.backend.catalogo;

/**
 * Esa sugerencia ya salió de la cola. Pasa cuando el admin tiene la lista abierta en dos pestañas
 * o vuelve atrás con el navegador: no es un error de programa, es que el estado ya cambió, así que
 * sale como conflicto y no como cosa rota.
 */
public class SugerenciaResueltaException extends RuntimeException {

	public SugerenciaResueltaException(Long id) {
		super("La sugerencia " + id + " ya fue resuelta");
	}
}
