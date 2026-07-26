package io.github.ramiroabadie.backend.aplicacion.internal.moderacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.ramiroabadie.backend.diario.Diario;
import io.github.ramiroabadie.backend.social.ReportesDeResenias;

/**
 * Las dos salidas de la cola de reportes (HU-22), que son las dos mitades de un mismo veredicto y
 * viven acá por lo mismo que la fusión de fichas duplicadas (D63): componen dos módulos que no se
 * conocen. Social anota qué se decidió y Diario borra el texto, en una sola transacción —una base,
 * un transaction manager, llamadas síncronas in-process (ADR-002)—, así que no queda una reseña
 * borrada con su reporte todavía en la cola ni al revés.
 *
 * <p>El orden importa: primero Social, que es el que bloquea las filas y decide si este reporte
 * todavía se podía resolver. Recién cuando eso pasa se toca el contenido. Al revés, un segundo
 * clic que va a terminar en 409 igual habría borrado la reseña.</p>
 */
@Service
class ResolucionDeReportes {

	private final ReportesDeResenias reportes;

	private final Diario diario;

	ResolucionDeReportes(ReportesDeResenias reportes, Diario diario) {
		this.reportes = reportes;
		this.diario = diario;
	}

	/**
	 * El reporte tenía razón: se va el texto y se queda el registro (D70). Social devuelve de qué
	 * reseña se trataba porque el id que llega es el del reporte y quien lo sabe es él.
	 */
	@Transactional
	void borrarResenia(Long reporteId) {
		diario.borrarResenia(reportes.confirmar(reporteId));
	}

	/** El reporte no tenía razón: la reseña se queda y la cola se vacía igual. */
	@Transactional
	void desestimar(Long reporteId) {
		reportes.desestimar(reporteId);
	}
}
