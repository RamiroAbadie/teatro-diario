package io.github.ramiroabadie.backend.aplicacion.internal.moderacion;

import jakarta.validation.constraints.Size;

/**
 * Lo que se manda al reportar una reseña (HU-18): un motivo, y es opcional. El cuerpo entero
 * también lo es —reportar sin decir nada es un {@code POST} sin body—, porque el botón tiene que
 * costar un clic: quien se topa con algo ofensivo avisa, no redacta un descargo.
 */
record ReporteRequest(
		@Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
		String motivo
) {
}
