package io.github.ramiroabadie.backend.catalogo.internal.produccion;

/**
 * Lo que subieron no sirve como afiche: está vacío, no es una de las tres imágenes que se
 * aceptan, o declara más píxeles de los que estamos dispuestos a decodificar (D77/D88).
 *
 * <p>Es un {@code 400} y no un {@code 413}: el {@code 413} es del tamaño del archivo y lo decide
 * el contenedor antes de que esto corra.</p>
 */
class AficheInvalidoException extends RuntimeException {

	AficheInvalidoException(String mensaje) {
		super(mensaje);
	}
}
