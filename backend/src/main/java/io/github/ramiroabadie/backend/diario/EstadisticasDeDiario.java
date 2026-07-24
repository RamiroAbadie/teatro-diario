package io.github.ramiroabadie.backend.diario;

import java.util.List;

/**
 * Los números del perfil (HU-13), con el alcance quirúrgico que fija D26: cuánto vi, cuánto vi
 * cada año y qué puntajes pongo. Nada más — "tu año en teatro" es post-MVP (X1).
 *
 * <p>{@code promedioPropio} es el promedio plano de los ratings propios y no tiene nada que ver
 * con el promedio público de una producción (D20): acá no hay varios usuarios de los que elegir
 * un último rating, hay uno solo y todo lo que puso cuenta.</p>
 *
 * @param totalRegistros veces que fue al teatro
 * @param totalProducciones obras distintas, que es menos cuando repitió (D19)
 * @param promedioPropio con un decimal, o {@code null} si nunca puntuó
 * @param registrosSinFecha los que no entran en ningún año
 */
public record EstadisticasDeDiario(
		long totalRegistros,
		long totalProducciones,
		Double promedioPropio,
		long registrosSinFecha,
		List<ObrasPorAnio> porAnio
) {
}
