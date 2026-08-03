package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.List;

import io.github.ramiroabadie.backend.catalogo.internal.sala.SalaResponse;

/**
 * Ficha completa de una producción, con sala y participaciones resueltas. Se arma dentro de
 * la transacción ({@code open-in-view: false}).
 *
 * <p>{@code aficheUrl} es {@code null} cuando la ficha no tiene afiche, que es el caso normal y
 * no un error: la pantalla cambia de forma en vez de dejar un hueco (D71/D79).</p>
 */
record ProduccionResponse(
		Long id,
		String titulo,
		String sinopsis,
		String obraOriginal,
		String autorOriginal,
		EstadoProduccion estado,
		String aficheUrl,
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
				Afiches.url(produccion.getId(), produccion.getAficheActual()),
				produccion.getSala() == null ? null : SalaResponse.desde(produccion.getSala()),
				produccion.getParticipaciones().stream().map(ParticipacionResponse::desde).toList());
	}
}
