package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.List;

import io.github.ramiroabadie.backend.catalogo.internal.sala.SalaResponse;

/**
 * Ficha completa de una producción, con sala y participaciones resueltas. Se arma dentro de
 * la transacción ({@code open-in-view: false}).
 */
record ProduccionResponse(
		Long id,
		String titulo,
		String sinopsis,
		String obraOriginal,
		String autorOriginal,
		EstadoProduccion estado,
		SalaResponse sala,
		List<ParticipacionResponse> participaciones
) {

	static ProduccionResponse desde(Produccion produccion) {
		return new ProduccionResponse(
				produccion.getId(),
				produccion.getTitulo(),
				produccion.getSinopsis(),
				produccion.getObraOriginal(),
				produccion.getAutorOriginal(),
				produccion.getEstado(),
				produccion.getSala() == null ? null : SalaResponse.desde(produccion.getSala()),
				produccion.getParticipaciones().stream().map(ParticipacionResponse::desde).toList());
	}
}
