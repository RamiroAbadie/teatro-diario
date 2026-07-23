package io.github.ramiroabadie.backend.catalogo.internal.sala;

/**
 * Se lanza cuando se opera sobre una sala que no existe. El controlador la mapea a 404.
 */
class SalaNoEncontradaException extends RuntimeException {

	SalaNoEncontradaException(Long id) {
		super("No existe una sala con id " + id);
	}
}
