package io.github.ramiroabadie.backend.aplicacion.internal.diario;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import io.github.ramiroabadie.backend.diario.GranularidadFecha;
import io.github.ramiroabadie.backend.diario.OpinionesDeProduccion;
import io.github.ramiroabadie.backend.diario.ReseniaDeProduccion;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;

/**
 * Lo que opina la gente de una producción, como lo muestra la ficha (HU-14): el promedio de D20
 * con su cantidad de votos y las reseñas firmadas.
 *
 * <p>La firma es el trabajo de composición: Diario guarda ids opacos de usuario (D30) y acá se
 * cambian por el username que Identidad resuelve. Si la cuenta se borró, la reseña queda sin
 * autor antes que desaparecer.</p>
 */
record OpinionesResponse(Double promedio, long cantidadRatings, List<Resenia> resenias) {

	static OpinionesResponse desde(OpinionesDeProduccion opiniones, Map<Long, UsuarioPublico> autores) {
		return new OpinionesResponse(opiniones.promedio(), opiniones.cantidadRatings(),
				opiniones.resenias().stream()
						.map(resenia -> Resenia.desde(resenia, autores.get(resenia.usuarioId())))
						.toList());
	}

	/** Autor, texto, fecha y el rating de ese registro (HU-14). Los likes llegan con HU-17. */
	record Resenia(
			Long registroId,
			String autor,
			String texto,
			Integer rating,
			LocalDate fecha,
			GranularidadFecha granularidad,
			Instant creadoEn
	) {

		static Resenia desde(ReseniaDeProduccion resenia, UsuarioPublico autor) {
			return new Resenia(resenia.registroId(), autor == null ? null : autor.username(),
					resenia.texto(), resenia.rating(), resenia.fecha(), resenia.granularidad(),
					resenia.creadoEn());
		}
	}
}
