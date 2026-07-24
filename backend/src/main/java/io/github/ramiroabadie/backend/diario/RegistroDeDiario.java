package io.github.ramiroabadie.backend.diario;

import java.time.Instant;
import java.time.LocalDate;

import io.github.ramiroabadie.backend.catalogo.ProduccionBasica;

/**
 * Una línea del diario: una vez que alguien vio algo. Trae la producción resuelta porque el
 * diario se lee como una lista de títulos, no de ids — es el único dato que Diario le pide a
 * Catálogo (MODULE_MAP).
 *
 * <p>Si el admin borró la producción, {@code produccion} viene en {@code null}: el registro es
 * del usuario y no se pierde porque el catálogo haya cambiado de opinión.</p>
 */
public record RegistroDeDiario(
		Long id,
		ProduccionBasica produccion,
		LocalDate fecha,
		GranularidadFecha granularidad,
		Integer rating,
		String resenia,
		Instant creadoEn
) {
}
