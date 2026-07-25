package io.github.ramiroabadie.backend.catalogo.internal.sugerencia;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.ramiroabadie.backend.catalogo.NuevaSugerencia;
import io.github.ramiroabadie.backend.catalogo.SugerenciaNoEncontradaException;
import io.github.ramiroabadie.backend.catalogo.SugerenciaPropuesta;
import io.github.ramiroabadie.backend.catalogo.Sugerencias;

/**
 * Los casos de uso de las sugerencias (HU-08 y HU-21) y la única implementación de
 * {@link Sugerencias}. Es chico y tiene que seguir siéndolo: la sugerencia entra, espera, y sale
 * por una de las dos puertas. Todo lo que parezca "armar la ficha desde lo sugerido" es del
 * formulario de HU-20, que ya existe.
 *
 * <p>No valida que la cuenta del sugerente exista ni que la producción de la aprobación exista,
 * por el mismo motivo que Social no valida cuentas (D67): Catálogo no depende de Identidad, y la
 * ficha la comprueba quien llama —la capa de aplicación— antes de escribir, como con el like
 * (D68).</p>
 */
@Service
class SugerenciaService implements Sugerencias {

	private final SugerenciaRepository repositorio;

	SugerenciaService(SugerenciaRepository repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	@Transactional
	public SugerenciaPropuesta recibir(Long sugerenteId, NuevaSugerencia nueva) {
		return repositorio.save(new Sugerencia(sugerenteId, nueva)).aPropuesta();
	}

	@Override
	@Transactional(readOnly = true)
	public List<SugerenciaPropuesta> pendientes() {
		return repositorio.findByEstadoOrderByCreadoEnAscIdAsc(EstadoSugerencia.PENDIENTE).stream()
				.map(Sugerencia::aPropuesta)
				.toList();
	}

	@Override
	@Transactional
	public void aprobar(Long sugerenciaId, Long produccionId) {
		buscar(sugerenciaId).aprobar(produccionId);
	}

	@Override
	@Transactional
	public void rechazar(Long sugerenciaId, String motivo) {
		buscar(sugerenciaId).rechazar(motivo);
	}

	/**
	 * Las dos salidas de la cola leen la fila bloqueada: comprobar el estado y escribirlo tiene que
	 * ser un solo paso, o dos resoluciones que se solapan sacan la misma sugerencia dos veces.
	 */
	private Sugerencia buscar(Long id) {
		return repositorio.paraResolver(id).orElseThrow(() -> new SugerenciaNoEncontradaException(id));
	}
}
