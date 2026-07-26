package io.github.ramiroabadie.backend.social.internal.reporte;

/**
 * Dónde está un reporte en la cola. No sale del módulo, igual que el estado de una sugerencia
 * (D69): afuera solo se pregunta qué falta resolver, y eso es una lista de pendientes.
 */
enum EstadoReporte {

	/** En la cola del admin, esperando (HU-22). */
	PENDIENTE,

	/** El reporte tenía razón: el texto de esa reseña se borró (D40). */
	RESENIA_BORRADA,

	/** El reporte no tenía razón: la reseña se quedó donde estaba. */
	DESESTIMADO
}
