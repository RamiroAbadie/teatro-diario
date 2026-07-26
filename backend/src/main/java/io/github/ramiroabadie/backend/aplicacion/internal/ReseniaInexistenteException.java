package io.github.ramiroabadie.backend.aplicacion.internal;

/**
 * No hay ninguna reseña con ese id: o el registro no existe, o existe pero nadie escribió nada en
 * él. Las dos cosas son lo mismo para quien quiere destacar una reseña (HU-17) o reportarla
 * (HU-18), y distinguirlas sería contarle a cualquiera qué ids de registro están ocupados.
 *
 * <p>Vive en la raíz de la capa y no en uno de sus paquetes porque desde D70 la levantan los dos
 * botones que cuelgan de una reseña, y la definición de "esa reseña no está" tiene que ser una
 * sola — el mismo motivo por el que {@link SesionActual} está acá.</p>
 */
public class ReseniaInexistenteException extends RuntimeException {

	public ReseniaInexistenteException(Long reseniaId) {
		super("No existe la reseña " + reseniaId);
	}
}
