package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import io.github.ramiroabadie.backend.catalogo.internal.sala.SalaResponse;

/**
 * Fila del listado del panel: lo justo para reconocer la ficha y cambiarle el estado sin
 * traer el elenco entero de cada producción.
 */
record ProduccionResumenResponse(Long id, String titulo, EstadoProduccion estado, String aficheUrl,
		SalaResponse sala) {

	static ProduccionResumenResponse desde(Produccion produccion) {
		return new ProduccionResumenResponse(
				produccion.getId(),
				produccion.getTitulo(),
				produccion.getEstado(),
				Afiches.url(produccion.getId(), produccion.getAficheActual()),
				produccion.getSala() == null ? null : SalaResponse.desde(produccion.getSala()));
	}
}
