package io.github.ramiroabadie.backend.diario;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Una reseña como la muestra la ficha (HU-14): el texto, el rating de ese registro y cuándo la
 * persona vio la obra. El autor viene como {@code usuarioId} opaco (D30) — el username lo pone
 * la capa de aplicación, que es la que puede preguntarle a Identidad.
 *
 * <p>Los likes (D11) no vienen acá: son del módulo Social, que los cuenta por su cuenta a partir
 * del {@code registroId} (HU-17). Diario no sabe que existen.</p>
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
