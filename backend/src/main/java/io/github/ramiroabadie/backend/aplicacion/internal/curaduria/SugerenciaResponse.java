package io.github.ramiroabadie.backend.aplicacion.internal.curaduria;

import io.github.ramiroabadie.backend.catalogo.SugerenciaPropuesta;

/**
 * La confirmación de recibido (HU-08): lo que se propuso, devuelto tal cual entró. Sin estado
 * adentro, y no es un olvido — la sugerencia no se sigue: si se aprueba, la producción
 * simplemente aparece en el catálogo, porque no hay notificaciones en el MVP (MD-3). La
 * expectativa honesta la pone el texto de la pantalla, no un campo.
 */
record SugerenciaResponse(Long id, String titulo, String sala, Integer anio, String elenco, String comentario) {

	static SugerenciaResponse desde(SugerenciaPropuesta propuesta) {
		return new SugerenciaResponse(propuesta.id(), propuesta.titulo(), propuesta.sala(),
				propuesta.anio(), propuesta.elenco(), propuesta.comentario());
	}
}
