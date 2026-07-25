package io.github.ramiroabadie.backend.aplicacion.internal.social;

/** El cursor que mandó el cliente no es uno de los que emite el feed. */
class CursorInvalidoException extends RuntimeException {

	CursorInvalidoException() {
		super("El cursor no es válido: pedí la primera página sin él");
	}
}
