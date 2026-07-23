package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Una línea del elenco o del equipo. Sostiene el buscar-o-crear inline de HU-20: el admin
 * manda {@code personaId} si la persona ya está en el catálogo, o {@code nombrePersona} si
 * la está tipeando por primera vez — uno u otro, nunca los dos, para que no haya ambigüedad
 * sobre cuál gana.
 */
record ParticipacionRequest(
		Long personaId,

		@Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
		String nombrePersona,

		@NotNull(message = "El rol de la participación es obligatorio")
		RolParticipacion rol
) {

	@AssertTrue(message = "Indicá personaId o nombrePersona, no ambos")
	boolean isPersonaIndicada() {
		boolean tieneNombre = nombrePersona != null && !nombrePersona.isBlank();
		return (personaId != null) ^ tieneNombre;
	}
}
