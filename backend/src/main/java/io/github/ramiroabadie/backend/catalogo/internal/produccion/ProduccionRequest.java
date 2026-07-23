package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de alta y edición de una ficha. Solo el título y el estado son obligatorios: la
 * curaduría tiene un presupuesto de 15 minutos por ficha (D37/D38) y una ficha incompleta
 * cargada hoy vale más que una perfecta que nunca se carga.
 *
 * <p>En la edición, {@code participaciones} se interpreta como la lista completa: lo que no
 * viene, se borra.</p>
 */
record ProduccionRequest(
		@NotBlank(message = "El título de la producción es obligatorio")
		@Size(max = 250, message = "El título no puede superar los 250 caracteres")
		String titulo,

		@Size(max = 5000, message = "La sinopsis no puede superar los 5000 caracteres")
		String sinopsis,

		@Size(max = 250, message = "La obra original no puede superar los 250 caracteres")
		String obraOriginal,

		@Size(max = 250, message = "El autor original no puede superar los 250 caracteres")
		String autorOriginal,

		@NotNull(message = "El estado de la producción es obligatorio")
		EstadoProduccion estado,

		Long salaId,

		List<@NotNull(message = "La participación no puede ser nula") @Valid ParticipacionRequest> participaciones
) {

	List<ParticipacionRequest> participacionesOVacio() {
		return participaciones == null ? List.of() : participaciones;
	}
}
