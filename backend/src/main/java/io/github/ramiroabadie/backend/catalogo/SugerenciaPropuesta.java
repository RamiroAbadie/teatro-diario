package io.github.ramiroabadie.backend.catalogo;

import java.time.Instant;

/**
 * Una sugerencia esperando en la cola del admin (HU-21), tal como la guardó Catálogo.
 *
 * <p>{@code sugerenteId} es una referencia opaca (D30): Catálogo no depende de Identidad y no
 * sabe cómo se llama esa persona. Ponerle nombre es trabajo de la capa de aplicación, igual que
 * con el autor de una reseña (D60).</p>
 */
public record SugerenciaPropuesta(Long id, String titulo, String sala, Integer anio, String elenco,
		String comentario, Long sugerenteId, Instant creadoEn) {
}
