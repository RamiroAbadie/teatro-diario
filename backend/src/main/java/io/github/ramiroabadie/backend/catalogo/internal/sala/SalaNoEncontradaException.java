package io.github.ramiroabadie.backend.catalogo.internal.sala;

/**
 * Se lanza cuando se opera sobre una sala que no existe. El controlador la mapea a 404.
 */
public class SalaNoEncontradaException extends RuntimeException {

	public SalaNoEncontradaException(Long id) {
		super("No existe una sala con id " + id);
	}
}
