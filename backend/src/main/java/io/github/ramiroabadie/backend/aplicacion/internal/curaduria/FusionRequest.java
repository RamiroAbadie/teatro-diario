package io.github.ramiroabadie.backend.aplicacion.internal.curaduria;

import jakarta.validation.constraints.NotNull;

/**
 * A qué ficha se muda todo. La que se fusiona va en la URL: lo que la operación hace es sacar
 * una del medio, así que es ella la que nombra el recurso.
 */
record FusionRequest(
		@NotNull(message = "Elegí a qué producción se fusiona")
		Long destinoId
) {
}
