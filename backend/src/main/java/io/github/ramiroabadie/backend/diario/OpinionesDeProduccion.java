package io.github.ramiroabadie.backend.diario;

import java.util.List;

/**
 * Lo que la gente opina de una producción (HU-14): el promedio con su cantidad de votos y las
 * reseñas.
 *
 * <p>⚠️ {@code promedio} NO es el promedio de todos los ratings: es el promedio del último
 * rating de cada usuario (D20). Con re-visto (D19) una misma persona puede haber puntuado la
 * misma obra tres veces y solo cuenta la última; {@code cantidadRatings} son personas, no
 * registros.</p>
 *
 * @param promedio con un decimal (D9), o {@code null} si nadie puntuó todavía
 */
public record OpinionesDeProduccion(
		Double promedio,
		long cantidadRatings,
		List<ReseniaDeProduccion> resenias
) {
}
