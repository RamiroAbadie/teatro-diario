package io.github.ramiroabadie.backend.catalogo.internal.sala;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso del CRUD de salas. El mapeo entidad↔DTO ocurre dentro de la transacción
 * ({@code open-in-view: false}). La autorización de admin (D7) es un concern transversal
 * de la capa de aplicación y llega con Spring Security en la Fase 2 (D52).
 *
 * <p>Es {@code public} solo para que Producción pueda relacionarse con salas dentro del
 * mismo módulo: el paquete sigue siendo {@code internal} y {@code ModulithArchitectureTest}
 * impide que otro módulo lo use.</p>
 */
@Service
public class SalaService {

	private final SalaRepository repositorio;

	SalaService(SalaRepository repositorio) {
		this.repositorio = repositorio;
	}

	@Transactional
	public SalaResponse crear(SalaRequest req) {
		Sala sala = repositorio.save(new Sala(req.nombre(), req.complejo()));
		return SalaResponse.desde(sala);
	}

	@Transactional(readOnly = true)
	public List<SalaResponse> listar() {
		return repositorio.findAll().stream().map(SalaResponse::desde).toList();
	}

	@Transactional(readOnly = true)
	public SalaResponse obtener(Long id) {
		return SalaResponse.desde(buscar(id));
	}

	@Transactional
	public SalaResponse actualizar(Long id, SalaRequest req) {
		Sala sala = buscar(id);
		sala.actualizar(req.nombre(), req.complejo());
		return SalaResponse.desde(sala);
	}

	@Transactional
	public void borrar(Long id) {
		if (!repositorio.existsById(id)) {
			throw new SalaNoEncontradaException(id);
		}
		repositorio.deleteById(id);
	}

	/**
	 * Devuelve la entidad para que otra parte del módulo Catálogo (hoy Producción) la
	 * referencie. Requiere transacción abierta del llamador.
	 */
	public Sala obtenerEntidad(Long id) {
		return buscar(id);
	}

	private Sala buscar(Long id) {
		return repositorio.findById(id).orElseThrow(() -> new SalaNoEncontradaException(id));
	}
}
