package io.github.ramiroabadie.backend.social;

import java.time.Instant;

/**
 * Un reporte esperando en la cola del admin (HU-22), tal como lo guardó Social.
 *
 * <p>Los dos ids son opacos (D30): {@code reseniaId} es un registro de Diario y
 * {@code reportanteId} una cuenta de Identidad, y Social no depende de ninguno de los dos
 * (MODULE_MAP). Traer el texto reportado y ponerle nombre al que avisó es trabajo de la capa de
 * aplicación, igual que con el autor de una reseña (D60) o el sugerente de una obra (D69).</p>
 *
 * @param motivo lo que escribió quien reportó, o {@code null}: es opcional (HU-18) porque el
 * botón tiene que costar un clic — quien se topa con algo ofensivo avisa, no redacta
 */
public record ReporteRecibido(Long id, Long reseniaId, Long reportanteId, String motivo, Instant creadoEn) {
}
