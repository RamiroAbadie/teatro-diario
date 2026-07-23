package io.github.ramiroabadie.backend.catalogo.internal.persona;

/**
 * Se lanza cuando se opera sobre una persona que no existe. El controlador la mapea a 404.
 */
class PersonaNoEncontradaException extends RuntimeException {

	PersonaNoEncontradaException(Long id) {
		super("No existe una persona con id " + id);
	}
}
