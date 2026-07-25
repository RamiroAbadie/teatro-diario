package io.github.ramiroabadie.backend.catalogo;

/**
 * Lo que un usuario propone cuando la obra que quiere registrar no está (HU-08). Solo el título
 * es obligatorio: es la válvula del historial viejo (D24) y el que sugiere está a mitad del gesto
 * de registro, no cargando una ficha.
 *
 * <p>La sala y el elenco son texto libre y no ids del catálogo a propósito: quien sugiere no sabe
 * —ni tiene por qué— si esa sala o esa persona ya existen acá adentro. Cruzar eso con el catálogo
 * es trabajo de la curaduría, que es la que decide la identidad de las entidades (MODULE_MAP).</p>
 */
public record NuevaSugerencia(String titulo, String sala, Integer anio, String elenco, String comentario) {
}
