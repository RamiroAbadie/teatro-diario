package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import io.github.ramiroabadie.backend.catalogo.internal.persona.PersonaResponse;

/**
 * Una línea del elenco o del equipo, con la persona resuelta (el front necesita el id para
 * linkear a la página de artista, HU-05).
 */
record ParticipacionResponse(Long id, PersonaResponse persona, RolParticipacion rol) {

	static ParticipacionResponse desde(Participacion participacion) {
		return new ParticipacionResponse(
				participacion.getId(),
				PersonaResponse.desde(participacion.getPersona()),
				participacion.getRol());
	}
}
