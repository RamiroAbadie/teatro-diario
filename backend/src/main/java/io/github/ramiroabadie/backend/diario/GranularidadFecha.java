package io.github.ramiroabadie.backend.diario;

/**
 * Los cuatro niveles de precisión con los que se puede fechar un registro (MD-1). Existen para
 * que cargar historial viejo no necesite un camino aparte (D18): nadie se acuerda del día en
 * que vio algo en 2017, pero sí del año.
 *
 * <p>La fecha se guarda normalizada al comienzo del período que nombra —marzo de 2023 es el
 * 2023-03-01, y 2023 a secas es el 2023-01-01— y la granularidad dice hasta dónde leerla. Ese
 * par resuelve de una el orden que pide MD-2: en un diario descendente, "2023" cae después de
 * todos los días con fecha exacta de 2023, que es justo donde tiene que estar.</p>
 */
public enum GranularidadFecha {

	DIA,
	MES,
	ANIO,
	SIN_FECHA
}
