package io.github.ramiroabadie.backend.catalogo;

/**
 * Lo mínimo que otro módulo necesita saber de una producción: que existe y cómo se llama.
 * Es a propósito más pobre que la ficha (HU-04) — quien quiera el elenco, la sinopsis o el
 * estado va por la API de lectura del catálogo, que es HTTP y pública.
 */
public record ProduccionBasica(Long id, String titulo) {
}
