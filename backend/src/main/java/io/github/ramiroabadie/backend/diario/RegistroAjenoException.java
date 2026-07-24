package io.github.ramiroabadie.backend.diario;

/**
 * Alguien quiso editar o borrar un registro que no es suyo (HU-11). Se responde 403 y no 404: el
 * registro existe y es público como todo lo demás (D21), lo que no se puede es tocarlo.
 */
public class RegistroAjenoException extends RuntimeException {

	public RegistroAjenoException(Long id) {
		super("El registro " + id + " es de otra persona");
	}
}
