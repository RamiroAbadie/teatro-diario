package io.github.ramiroabadie.backend.diario;

/**
 * La producción tal como la recuerda el diario. No es la ficha de Catálogo: es lo que hace falta
 * para leer una línea del historial y, si todavía se puede, ir hasta ella.
 *
 * <p>El título sale del catálogo mientras la producción exista —así una corrección de la ficha se
 * ve en todos los diarios— y de la copia que el registro guardó cuando ya no existe (D62). Eso es
 * lo que dice {@code enCatalogo}: con {@code false} el título sigue estando pero el link no lleva
 * a ningún lado.</p>
 */
public record ProduccionRegistrada(Long id, String titulo, boolean enCatalogo) {
}
