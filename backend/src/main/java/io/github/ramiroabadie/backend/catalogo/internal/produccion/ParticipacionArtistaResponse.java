package io.github.ramiroabadie.backend.catalogo.internal.produccion;

/**
 * Una línea de la página de artista: en qué rol participó y en qué producción (HU-05).
 * Es el espejo de {@code ParticipacionResponse}, que mira desde la ficha: allá se resuelve la
 * persona, acá la producción.
 */
record ParticipacionArtistaResponse(Long id, RolParticipacion rol, ProduccionResumenResponse produccion) {

	static ParticipacionArtistaResponse desde(Participacion participacion) {
		return new ParticipacionArtistaResponse(
				participacion.getId(),
				participacion.getRol(),
				ProduccionResumenResponse.desde(participacion.getProduccion()));
	}
}
