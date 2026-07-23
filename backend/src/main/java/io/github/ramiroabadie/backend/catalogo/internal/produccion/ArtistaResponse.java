package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.List;

import io.github.ramiroabadie.backend.catalogo.internal.persona.PersonaResponse;

/**
 * Página de artista (HU-05): nombre y participaciones, nada más. Sin foto ni bio a propósito
 * (D14). Una persona recién creada y todavía sin participaciones cargadas responde con la
 * lista vacía, no con 404: existe en el catálogo.
 */
record ArtistaResponse(Long id, String nombre, List<ParticipacionArtistaResponse> participaciones) {

	static ArtistaResponse desde(PersonaResponse persona, List<Participacion> participaciones) {
		return new ArtistaResponse(
				persona.id(),
				persona.nombre(),
				participaciones.stream().map(ParticipacionArtistaResponse::desde).toList());
	}
}
