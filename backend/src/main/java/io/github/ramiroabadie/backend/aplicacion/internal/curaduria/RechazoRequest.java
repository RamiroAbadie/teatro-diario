package io.github.ramiroabadie.backend.aplicacion.internal.curaduria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * El motivo del rechazo, que lo lee solo el admin (HU-21): no hay notificación al que sugirió
 * (MD-3). Es obligatorio igual, y sirve para el mismo que lo escribe — "ya está cargada", "no es
 * teatro", "no se entiende qué obra es"— cuando la misma propuesta vuelve a aparecer en un mes.
 */
record RechazoRequest(
		@NotBlank(message = "El motivo del rechazo es obligatorio")
		@Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
		String motivo
) {
}
