package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.List;

/**
 * Respuesta de "en cartel" (HU-06). La historia admite sección o filtro para
 * {@code próximamente}: van las dos listas en una sola respuesta, así la página se arma con
 * un solo pedido y sin que el cliente tenga que conocer los estados (D8).
 */
record EnCartelResponse(List<ProduccionResumenResponse> enCartel, List<ProduccionResumenResponse> proximamente) {
}
