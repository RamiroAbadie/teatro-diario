package io.github.ramiroabadie.backend.diario;

/**
 * No existe ese registro. Es pública porque quien traduce esto a un 404 es la capa de
 * aplicación, que es la que habla HTTP.
 */
public class RegistroNoEncontradoException extends RuntimeException {

	public RegistroNoEncontradoException(Long id) {
		super("No existe el registro " + id);
	}
}
