package io.github.ramiroabadie.backend.aplicacion.internal.diario;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.ramiroabadie.backend.diario.GranularidadFecha;
import io.github.ramiroabadie.backend.diario.OpinionesDeProduccion;
import io.github.ramiroabadie.backend.diario.ReseniaDeProduccion;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;

/**
 * Lo que opina la gente de una producción, como lo muestra la ficha (HU-14): el promedio de D20
 * con su cantidad de votos y las reseñas firmadas y con sus likes.
 *
 * <p>La firma y los likes son el trabajo de composición: Diario guarda ids opacos de usuario (D30)
 * y acá se cambian por el username que Identidad resuelve, mientras Social cuenta los likes de esa
 * misma lista de reseñas. Si la cuenta se borró, la reseña queda sin autor antes que
 * desaparecer.</p>
 */
record OpinionesResponse(Double promedio, long cantidadRatings, List<Resenia> resenias) {

	/**
	 * @param likes cuántos likes tiene cada reseña; las que no tienen ninguno no vienen en el mapa
	 * @param mios las reseñas que ya tienen el like de quien mira, o {@code null} si no hay sesión
	 */
	static OpinionesResponse desde(OpinionesDeProduccion opiniones, Map<Long, UsuarioPublico> autores,
			Map<Long, Long> likes, Set<Long> mios) {
		return new OpinionesResponse(opiniones.promedio(), opiniones.cantidadRatings(),
				opiniones.resenias().stream()
						.map(resenia -> Resenia.desde(resenia, autores.get(resenia.usuarioId()),
								likes.getOrDefault(resenia.registroId(), 0L),
								mios == null ? null : mios.contains(resenia.registroId())))
						.toList());
	}

	/**
	 * Autor, texto, fecha, el rating de ese registro y los likes (HU-14 + HU-17).
	 *
	 * @param leDiLike el estado del botón, {@code null} cuando no hay botón porque nadie está
	 * logueado — la misma convención que {@code loSigo} en el perfil (D67). Darle like a la propia
	 * reseña está permitido, así que en la reseña de uno el botón se dibuja igual
	 */
	record Resenia(
			Long registroId,
			String autor,
			String texto,
			Integer rating,
			LocalDate fecha,
			GranularidadFecha granularidad,
			long likes,
			Boolean leDiLike,
			Instant creadoEn
	) {

		static Resenia desde(ReseniaDeProduccion resenia, UsuarioPublico autor, long likes, Boolean leDiLike) {
			return new Resenia(resenia.registroId(), autor == null ? null : autor.username(),
					resenia.texto(), resenia.rating(), resenia.fecha(), resenia.granularidad(),
					likes, leDiLike, resenia.creadoEn());
		}
	}
}
