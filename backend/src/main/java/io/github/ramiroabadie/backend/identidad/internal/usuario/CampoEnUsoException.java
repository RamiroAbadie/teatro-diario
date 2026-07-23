package io.github.ramiroabadie.backend.identidad.internal.usuario;

/**
 * Username o email ya tomados. El controlador la mapea a 409 diciendo cuál de los dos es:
 * en el alta los errores van por campo (HU-01). En el login pasa lo contrario — el mensaje es
 * genérico a propósito (HU-02) — y por eso ahí no se usa esta excepción.
 */
class CampoEnUsoException extends RuntimeException {

	private final String campo;

	CampoEnUsoException(String campo, String mensaje) {
		super(mensaje);
		this.campo = campo;
	}

	String getCampo() {
		return campo;
	}
}
