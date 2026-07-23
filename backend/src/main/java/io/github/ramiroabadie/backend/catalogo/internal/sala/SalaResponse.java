package io.github.ramiroabadie.backend.catalogo.internal.sala;

/**
 * Representación de salida de una sala.
 */
public record SalaResponse(Long id, String nombre, String complejo) {

	public static SalaResponse desde(Sala sala) {
		return new SalaResponse(sala.getId(), sala.getNombre(), sala.getComplejo());
	}
}
