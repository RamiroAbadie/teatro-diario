package io.github.ramiroabadie.backend.identidad.internal.usuario;

/**
 * Un campo del alta que las anotaciones de validación no alcanzan a cubrir. El controlador la
 * mapea a 400 con el mismo formato que los errores de bean validation, porque para quien se
 * registra es el mismo problema: un campo está mal (HU-01).
 */
class CampoInvalidoException extends RuntimeException {

	private final String campo;

	CampoInvalidoException(String campo, String mensaje) {
		super(mensaje);
		this.campo = campo;
	}

	String getCampo() {
		return campo;
	}
}
