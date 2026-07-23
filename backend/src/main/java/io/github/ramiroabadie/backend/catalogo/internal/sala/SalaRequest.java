package io.github.ramiroabadie.backend.catalogo.internal.sala;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de alta y edición de una sala. El nombre es obligatorio; el complejo es opcional.
 */
record SalaRequest(
		@NotBlank(message = "El nombre de la sala es obligatorio")
		@Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
		String nombre,

		@Size(max = 200, message = "El complejo no puede superar los 200 caracteres")
		String complejo
) {
}
