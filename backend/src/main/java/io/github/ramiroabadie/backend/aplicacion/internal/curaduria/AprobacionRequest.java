package io.github.ramiroabadie.backend.aplicacion.internal.curaduria;

import jakarta.validation.constraints.NotNull;

/**
 * En qué ficha terminó la sugerencia. Se aprueba con la producción ya creada —el admin la carga
 * con el formulario de HU-20, precargado con lo sugerido— y no antes: si saliera de la cola al
 * abrir el formulario, abandonarlo a mitad de camino perdería la propuesta para siempre.
 */
record AprobacionRequest(
		@NotNull(message = "Decí en qué producción terminó la sugerencia")
		Long produccionId
) {
}
