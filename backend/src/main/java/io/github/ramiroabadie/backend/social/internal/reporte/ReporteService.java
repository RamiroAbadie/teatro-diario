package io.github.ramiroabadie.backend.social.internal.reporte;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.ramiroabadie.backend.social.ReporteNoEncontradoException;
import io.github.ramiroabadie.backend.social.ReporteRecibido;
import io.github.ramiroabadie.backend.social.ReporteResueltoException;
import io.github.ramiroabadie.backend.social.ReportesDeResenias;

/**
 * Los casos de uso de los reportes (HU-18 y HU-22) y la única implementación de
 * {@link ReportesDeResenias}. Es la misma forma que la cola de sugerencias (D69) —algo entra,
 * espera, y sale por una de dos puertas— con una diferencia: el veredicto es sobre la reseña, así
 * que sale de la cola con todos los demás reportes que apuntaban a ella.
 *
 * <p>No valida que la reseña exista ni que la cuenta exista, por el mismo motivo que
 * {@code LikeService} (D67/D68): Social no depende ni de Diario ni de Identidad (MODULE_MAP). Y no
 * borra el texto reportado: eso es de Diario, y lo ordena la capa de aplicación.</p>
 */
@Service
class ReporteService implements ReportesDeResenias {

	private final ReporteRepository repositorio;

	ReporteService(ReporteRepository repositorio) {
		this.repositorio = repositorio;
	}

	/**
	 * El chequeo previo es el mismo que el del like: el botón se toca dos veces y eso no es un
	 * error. Acá no hay índice único detrás —ver {@link ReporteDeResenia}—, así que dos avisos
	 * simultáneos de la misma persona pueden dejar dos filas pendientes; el admin ve una repetida y
	 * las dos salen juntas de la cola, que es todo el daño posible.
	 */
	@Override
	@Transactional
	public void reportar(Long usuarioId, Long reseniaId, String motivo) {
		if (repositorio.existsByReseniaIdAndReportanteIdAndEstado(reseniaId, usuarioId, EstadoReporte.PENDIENTE)) {
			return;
		}
		repositorio.save(new ReporteDeResenia(reseniaId, usuarioId, motivo));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReporteRecibido> pendientes() {
		return repositorio.findByEstadoOrderByCreadoEnAscIdAsc(EstadoReporte.PENDIENTE).stream()
				.map(ReporteDeResenia::aRecibido)
				.toList();
	}

	@Override
	@Transactional
	public Long confirmar(Long reporteId) {
		return resolver(reporteId, EstadoReporte.RESENIA_BORRADA);
	}

	@Override
	@Transactional
	public void desestimar(Long reporteId) {
		resolver(reporteId, EstadoReporte.DESESTIMADO);
	}

	/**
	 * La única salida de la cola, con las filas bloqueadas. Que el reporte pedido esté en el
	 * conjunto es lo que separa "ya se resolvió" (409) de "no existe" (404): la lista puede tener
	 * pendientes de la misma reseña llegados después de que este saliera.
	 */
	private Long resolver(Long reporteId, EstadoReporte veredicto) {
		List<ReporteDeResenia> pendientes = repositorio.paraResolver(reporteId, EstadoReporte.PENDIENTE);
		if (pendientes.stream().noneMatch(reporte -> reporte.getId().equals(reporteId))) {
			if (repositorio.existsById(reporteId)) {
				throw new ReporteResueltoException(reporteId);
			}
			throw new ReporteNoEncontradoException(reporteId);
		}
		pendientes.forEach(reporte -> reporte.resolver(veredicto));
		return pendientes.get(0).getReseniaId();
	}
}
