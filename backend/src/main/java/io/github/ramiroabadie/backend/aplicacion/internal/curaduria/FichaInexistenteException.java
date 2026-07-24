package io.github.ramiroabadie.backend.aplicacion.internal.curaduria;

/**
 * Una de las dos fichas de la fusión no existe. Vive acá y no en Catálogo porque la excepción de
 * Catálogo es interna del módulo: afuera no se la puede ni nombrar, así que la capa de aplicación
 * pregunta antes de operar y tiene la suya.
 */
class FichaInexistenteException extends RuntimeException {

	FichaInexistenteException(Long id) {
		super("No existe una producción con id " + id);
	}
}
