package io.github.ramiroabadie.backend.catalogo;

/**
 * No hay ninguna sugerencia con ese id. Es pública —al revés que las excepciones de las entidades
 * del catálogo, que son internas del módulo— porque quien resuelve la cola es un controlador de la
 * capa de aplicación y tiene que poder traducirla a un 404.
 */
public class SugerenciaNoEncontradaException extends RuntimeException {

	public SugerenciaNoEncontradaException(Long id) {
		super("No existe una sugerencia con id " + id);
	}
}
