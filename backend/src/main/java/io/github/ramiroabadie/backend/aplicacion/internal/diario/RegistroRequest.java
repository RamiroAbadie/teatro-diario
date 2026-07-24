package io.github.ramiroabadie.backend.aplicacion.internal.diario;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.github.ramiroabadie.backend.diario.GranularidadFecha;
import io.github.ramiroabadie.backend.diario.NuevoRegistro;

/**
 * El cuerpo del gesto de registro (HU-09), igual para crear que para editar. Lo que las
 * anotaciones cubren es formato; que la fecha corresponda con su granularidad y que la función
 * ya haya pasado son reglas del dominio y las decide el módulo Diario.
 *
 * <p>La fecha se manda como el comienzo del período que se nombra: el día exacto, el primero del
 * mes, o el primero del año. El módulo la normaliza igual, así que mandar el 15 de marzo con
 * granularidad {@code MES} es "marzo de 2023" y no se pierde nada.</p>
 */
record RegistroRequest(
		@NotNull(message = "Elegí qué producción viste")
		Long produccionId,

		LocalDate fecha,

		@NotNull(message = "Elegí con qué precisión sabés la fecha")
		GranularidadFecha granularidad,

		@Min(value = 1, message = "El puntaje va de 1 a 10")
		@Max(value = 10, message = "El puntaje va de 1 a 10")
		Integer rating,

		@Size(max = 5000, message = "La reseña es demasiado larga")
		String resenia
) {

	NuevoRegistro aNuevoRegistro() {
		return new NuevoRegistro(produccionId, fecha, granularidad, rating, resenia);
	}
}
