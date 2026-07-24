package io.github.ramiroabadie.backend.diario;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Una línea del diario: una vez que alguien vio algo. Trae la producción resuelta porque el
 * diario se lee como una lista de títulos, no de ids — es el único dato que Diario le pide a
 * Catálogo (MODULE_MAP).
 *
 * <p>Aunque el admin borre la ficha, la línea sigue diciendo qué se vio: ver
 * {@link ProduccionRegistrada}.</p>
 */
public record RegistroDeDiario(
		Long id,
		ProduccionRegistrada produccion,
		LocalDate fecha,
		GranularidadFecha granularidad,
		Integer rating,
		String resenia,
		Instant creadoEn
) {
}
