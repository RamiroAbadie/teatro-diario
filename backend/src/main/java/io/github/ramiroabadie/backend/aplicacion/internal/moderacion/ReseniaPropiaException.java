package io.github.ramiroabadie.backend.aplicacion.internal.moderacion;

/**
 * Alguien intentó reportar su propia reseña. HU-18 dice "botón en cada reseña ajena", y quien
 * quiere que la suya no esté más la edita o la borra (HU-11): mandarla a la cola del admin es
 * hacerle perder el tiempo a la única persona que la vacía.
 *
 * <p>La comprobación vive acá y no en Social por lo mismo que el resto de este paquete (D60):
 * hace falta la sesión, que la sabe Identidad, y el autor de la reseña, que lo sabe Diario.</p>
 */
class ReseniaPropiaException extends RuntimeException {

	ReseniaPropiaException() {
		super("No se reporta la propia reseña: si no te gusta cómo quedó, editala o borrala");
	}
}
