package io.github.ramiroabadie.backend.catalogo.internal.persona;

/**
 * Representación de salida de una persona.
 */
public record PersonaResponse(Long id, String nombre) {

	public static PersonaResponse desde(Persona persona) {
		return new PersonaResponse(persona.getId(), persona.getNombre());
	}
}
