package io.github.ramiroabadie.backend.diario;

import java.time.Instant;

/**
 * Dónde quedó la página anterior del feed: el último registro que se entregó. La página siguiente
 * son los que se cargaron antes que él.
 *
 * <p>Es un cursor y no un número de página a propósito (D66). El feed se ordena por fecha de
 * carga descendente y le entran registros nuevos por arriba mientras alguien lo scrollea: con
 * {@code offset}, cada registro nuevo empuja una fila de la página que sigue y la muestra dos
 * veces. Preguntando "lo anterior a este" eso no puede pasar.</p>
 *
 * <p>El id desempata: dos registros pueden compartir el instante de carga, y sin él la página
 * siguiente se saltearía uno o repetiría el otro.</p>
 */
public record CursorDeActividad(Instant creadoEn, Long registroId) {
}
