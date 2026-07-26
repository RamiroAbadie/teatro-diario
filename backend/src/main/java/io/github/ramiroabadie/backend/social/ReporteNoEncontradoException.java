package io.github.ramiroabadie.backend.social;

/** No hay ningún reporte con ese id. */
public class ReporteNoEncontradoException extends RuntimeException {

	public ReporteNoEncontradoException(Long reporteId) {
		super("No existe el reporte " + reporteId);
	}
}
