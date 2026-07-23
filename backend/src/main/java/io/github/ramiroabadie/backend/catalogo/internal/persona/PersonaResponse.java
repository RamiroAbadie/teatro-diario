package io.github.ramiroabadie.backend.catalogo.internal.persona;

/**
 * Representación de salida de una persona.
 */
record PersonaResponse(Long id, String nombre) {

	static PersonaResponse desde(Persona persona) {
		return new PersonaResponse(persona.getId(), persona.getNombre());
	}
}
