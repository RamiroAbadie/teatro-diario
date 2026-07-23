package io.github.ramiroabadie.backend.catalogo.internal.persona;

/**
 * Se lanza cuando se opera sobre una persona que no existe. El controlador la mapea a 404.
 */
public class PersonaNoEncontradaException extends RuntimeException {

	public PersonaNoEncontradaException(Long id) {
		super("No existe una persona con id " + id);
	}
}
