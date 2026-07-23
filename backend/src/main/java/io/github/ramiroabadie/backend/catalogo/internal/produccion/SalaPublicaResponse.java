package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.List;

import io.github.ramiroabadie.backend.catalogo.internal.sala.SalaResponse;

/**
 * Página de sala (pantalla 5 de USER_FLOWS.md): nombre, complejo y qué hay en cartel ahí.
 * No tiene historia propia — es el destino del "sala con link" de la ficha (HU-04) y por eso
 * su contenido es el mínimo que fija ese hueco, sin dirección ni mapa (D15, MODO ESENCIAL).
 */
record SalaPublicaResponse(Long id, String nombre, String complejo, List<ProduccionResumenResponse> enCartel) {

	static SalaPublicaResponse desde(SalaResponse sala, List<Produccion> enCartel) {
		return new SalaPublicaResponse(
				sala.id(),
				sala.nombre(),
				sala.complejo(),
				enCartel.stream().map(ProduccionResumenResponse::desde).toList());
	}
}
