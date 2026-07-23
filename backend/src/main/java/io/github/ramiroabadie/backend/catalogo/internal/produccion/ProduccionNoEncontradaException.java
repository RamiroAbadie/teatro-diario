package io.github.ramiroabadie.backend.catalogo.internal.produccion;

/**
 * Se lanza cuando se opera sobre una producción que no existe. El controlador la mapea a 404.
 */
class ProduccionNoEncontradaException extends RuntimeException {

	ProduccionNoEncontradaException(Long id) {
		super("No existe una producción con id " + id);
	}
}
