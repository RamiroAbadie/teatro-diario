package io.github.ramiroabadie.backend.catalogo.internal.sugerencia;

/**
 * Dónde está una sugerencia en la cola. No sale del módulo: afuera solo se pregunta qué falta
 * resolver, y eso es una lista de pendientes, no un estado que alguien tenga que interpretar.
 */
enum EstadoSugerencia {

	/** En la cola, esperando el barrido semanal del admin (D37). */
	PENDIENTE,

	/** Terminó siendo una ficha del catálogo, y queda anotado cuál. */
	APROBADA,

	/** Descartada, con un motivo que solo lee el admin. */
	RECHAZADA
}
