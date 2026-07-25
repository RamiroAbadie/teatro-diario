package io.github.ramiroabadie.backend.aplicacion.internal.curaduria;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.github.ramiroabadie.backend.catalogo.NuevaSugerencia;

/**
 * El formulario de HU-08: mínimo a propósito. Obligatorio, el título y nada más — el que sugiere
 * está a mitad del gesto de registro, a la salida del teatro (P8), y cada campo obligatorio de más
 * es gente que abandona con la obra sin registrar.
 *
 * <p>El año se acota nada más que para atajar el dedazo: el historial viejo que D24 quiere
 * rescatar puede ser de cualquier década, así que el rango es ancho.</p>
 */
record SugerenciaRequest(
		@NotBlank(message = "El título de la obra es obligatorio")
		@Size(max = 250, message = "El título no puede superar los 250 caracteres")
		String titulo,

		@Size(max = 250, message = "La sala no puede superar los 250 caracteres")
		String sala,

		@Min(value = 1800, message = "El año tiene que estar entre 1800 y 2100")
		@Max(value = 2100, message = "El año tiene que estar entre 1800 y 2100")
		Integer anio,

		@Size(max = 1000, message = "El elenco no puede superar los 1000 caracteres")
		String elenco,

		@Size(max = 1000, message = "El comentario no puede superar los 1000 caracteres")
		String comentario
) {

	NuevaSugerencia aNuevaSugerencia() {
		return new NuevaSugerencia(titulo, sala, anio, elenco, comentario);
	}
}
