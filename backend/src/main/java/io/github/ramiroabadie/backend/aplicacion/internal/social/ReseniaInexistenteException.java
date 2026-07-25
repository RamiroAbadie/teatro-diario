package io.github.ramiroabadie.backend.aplicacion.internal.social;

/**
 * No hay ninguna reseña con ese id: o el registro no existe, o existe pero nadie escribió nada en
 * él. Las dos cosas son lo mismo para quien quiere destacar una reseña (HU-17), y distinguirlas
 * sería contarle a cualquiera qué ids de registro están ocupados.
 */
class ReseniaInexistenteException extends RuntimeException {

	ReseniaInexistenteException(Long reseniaId) {
		super("No existe la reseña " + reseniaId);
	}
}
