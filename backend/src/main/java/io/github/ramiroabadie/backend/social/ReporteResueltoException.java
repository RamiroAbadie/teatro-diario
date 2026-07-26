package io.github.ramiroabadie.backend.social;

/**
 * Ese reporte ya salió de la cola, sea porque se resolvió solo o porque se resolvió junto con
 * otro sobre la misma reseña. Quien llega segundo llega tarde, no llega a algo roto.
 */
public class ReporteResueltoException extends RuntimeException {

	public ReporteResueltoException(Long reporteId) {
		super("El reporte " + reporteId + " ya estaba resuelto");
	}
}
