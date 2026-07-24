package io.github.ramiroabadie.backend.diario;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Una reseña como la muestra la ficha (HU-14): el texto, el rating de ese registro y cuándo la
 * persona vio la obra. El autor viene como {@code usuarioId} opaco (D30) — el username lo pone
 * la capa de aplicación, que es la que puede preguntarle a Identidad.
 *
 * <p>Los likes (D11) todavía no existen: son del módulo Social y llegan en la Fase 3 con
 * HU-17.</p>
 */
public record ReseniaDeProduccion(
		Long registroId,
		Long usuarioId,
		String texto,
		Integer rating,
		LocalDate fecha,
		GranularidadFecha granularidad,
		Instant creadoEn
) {
}
