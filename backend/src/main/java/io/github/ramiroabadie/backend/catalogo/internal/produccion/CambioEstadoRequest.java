package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo del cambio de estado suelto: el barrido semanal del admin no debería obligar a
 * mandar la ficha entera (HU-20, "cambio de estado en un clic desde el listado").
 */
record CambioEstadoRequest(

		@NotNull(message = "El estado es obligatorio")
		EstadoProduccion estado
) {
}
