package io.github.ramiroabadie.backend.diario;

import java.time.LocalDate;

/**
 * El gesto de registro entero, en un objeto (D18): qué vi, cuándo, qué le pongo y qué escribo.
 * Todo menos la producción es opcional — un registro sin rating ni reseña es un registro válido
 * y es el caso más frecuente del camino de menos de un minuto (P8).
 *
 * <p>Sirve igual para crear (HU-09) y para editar (HU-11): editar es reemplazar el gesto.</p>
 *
 * @param fecha comienzo del período que se está nombrando; {@code null} con {@code SIN_FECHA}
 * @param rating entero de 1 a 10 (D9), o {@code null}
 * @param resenia texto libre, o {@code null}
 */
public record NuevoRegistro(
		Long produccionId,
		LocalDate fecha,
		GranularidadFecha granularidad,
		Integer rating,
		String resenia
) {
}
