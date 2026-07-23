package io.github.ramiroabadie.backend.catalogo.internal.sala;

/**
 * Representación de salida de una sala.
 */
record SalaResponse(Long id, String nombre, String complejo) {

	static SalaResponse desde(Sala sala) {
		return new SalaResponse(sala.getId(), sala.getNombre(), sala.getComplejo());
	}
}
