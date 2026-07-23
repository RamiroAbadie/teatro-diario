package io.github.ramiroabadie.backend.catalogo.internal.persona;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de alta y edición de una persona. El nombre es el único atributo (D14).
 */
record PersonaRequest(
		@NotBlank(message = "El nombre de la persona es obligatorio")
		@Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
		String nombre
) {
}
