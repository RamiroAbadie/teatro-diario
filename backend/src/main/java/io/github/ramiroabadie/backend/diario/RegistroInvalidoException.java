package io.github.ramiroabadie.backend.diario;

/**
 * Un campo del registro que las anotaciones de validación no alcanzan a cubrir, porque la regla
 * es del dominio y no del formato: una fecha que no corresponde con su granularidad, o una
 * función vista en el futuro. Se responde 400 con el campo señalado, igual que en el alta de
 * cuenta.
 */
public class RegistroInvalidoException extends RuntimeException {

	private final String campo;

	public RegistroInvalidoException(String campo, String mensaje) {
		super(mensaje);
		this.campo = campo;
	}

	public String getCampo() {
		return campo;
	}
}
