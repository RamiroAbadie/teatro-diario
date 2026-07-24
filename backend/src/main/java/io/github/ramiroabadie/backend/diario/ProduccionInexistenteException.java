package io.github.ramiroabadie.backend.diario;

/**
 * Se quiso registrar una producción que no está en el catálogo, que es cerrado (D7). No es un
 * caso raro: es la mitad triste del flujo 3 de USER_FLOWS.md, la de quien viene con historial
 * viejo, y el camino que sigue es sugerir la obra (HU-08).
 */
public class ProduccionInexistenteException extends RuntimeException {

	public ProduccionInexistenteException(Long id) {
		super("No existe una producción con id " + id);
	}
}
