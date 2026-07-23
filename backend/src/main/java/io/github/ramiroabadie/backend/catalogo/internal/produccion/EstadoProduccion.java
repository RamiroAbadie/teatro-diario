package io.github.ramiroabadie.backend.catalogo.internal.produccion;

/**
 * Máximo nivel de "vigencia" que mantiene el sistema (D8, P6): no hay agenda de funciones,
 * así que el estado es lo único que dice si algo se puede ir a ver. El admin lo barre a mano
 * una vez por semana, por eso HU-20 pide cambiarlo en un clic.
 */
enum EstadoProduccion {

	EN_CARTEL,
	CERRADA,
	PROXIMAMENTE
}
