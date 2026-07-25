package io.github.ramiroabadie.backend.aplicacion.internal.curaduria;

import java.time.Instant;
import java.util.Map;

import io.github.ramiroabadie.backend.catalogo.SugerenciaPropuesta;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;

/**
 * Un ítem de la cola del admin (HU-21). Es la sugerencia más el nombre de quien la mandó: el
 * {@code user_id} que Catálogo guarda es opaco (D30) y un número no le dice nada a nadie a las
 * once de la noche resolviendo la cola.
 *
 * <p>{@code sugerente} viaja nulo si esa cuenta ya no existe. La propuesta sigue en la cola igual:
 * lo que el admin tiene que resolver es si la obra entra al catálogo, y eso no cambia porque el
 * que la pidió se haya ido.</p>
 */
record SugerenciaEnColaResponse(Long id, String titulo, String sala, Integer anio, String elenco,
		String comentario, String sugerente, Instant creadoEn) {

	static SugerenciaEnColaResponse desde(SugerenciaPropuesta propuesta, Map<Long, UsuarioPublico> sugerentes) {
		UsuarioPublico sugerente = sugerentes.get(propuesta.sugerenteId());
		return new SugerenciaEnColaResponse(propuesta.id(), propuesta.titulo(), propuesta.sala(),
				propuesta.anio(), propuesta.elenco(), propuesta.comentario(),
				sugerente == null ? null : sugerente.username(), propuesta.creadoEn());
	}
}
