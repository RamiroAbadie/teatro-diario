package io.github.ramiroabadie.backend.catalogo.internal.produccion;

/**
 * Lista cerrada inicial de roles (D17). Los roles técnicos (iluminación, escenografía,
 * música) quedan post-MVP: agregarlos es sumar una constante, no cambiar el modelo.
 */
enum RolParticipacion {

	/** Actor / actriz. */
	ACTUACION,
	DIRECCION,
	DRAMATURGIA
}
