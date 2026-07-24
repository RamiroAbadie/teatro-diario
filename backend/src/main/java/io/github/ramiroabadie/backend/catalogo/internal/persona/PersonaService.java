package io.github.ramiroabadie.backend.catalogo.internal.persona;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso del CRUD de personas. El mapeo entidad↔DTO ocurre dentro de la transacción
 * ({@code open-in-view: false}). La autorización de admin (D7) es un concern transversal
 * de la capa de aplicación y llega con Spring Security en la Fase 2 (D52).
 *
 * <p>Es {@code public} solo para que Producción pueda resolver participaciones dentro del
 * mismo módulo: el paquete sigue siendo {@code internal} y {@code ModulithArchitectureTest}
 * impide que otro módulo lo use.</p>
 */
@Service
public class PersonaService {

	/** Lo mismo que en la búsqueda de producciones: una pantalla de resultados, sin paginar. */
	private static final int LIMITE_DE_RESULTADOS = 10;

	private final PersonaRepository repositorio;

	PersonaService(PersonaRepository repositorio) {
		this.repositorio = repositorio;
	}

	@Transactional
	public PersonaResponse crear(PersonaRequest req) {
		Persona persona = repositorio.save(new Persona(req.nombre()));
		return PersonaResponse.desde(persona);
	}

	@Transactional(readOnly = true)
	public List<PersonaResponse> listar() {
		return repositorio.findAll().stream().map(PersonaResponse::desde).toList();
	}

	@Transactional(readOnly = true)
	public PersonaResponse obtener(Long id) {
		return PersonaResponse.desde(buscar(id));
	}

	/**
	 * Búsqueda de personas (HU-07): el camino a la página de artista cuando no se llegó a ella
	 * desde una ficha. Consulta vacía, lista vacía; sin resultados, lista vacía también — de
	 * personas no se sugieren altas, las crea la carga de fichas (D14).
	 */
	@Transactional(readOnly = true)
	public List<PersonaResponse> buscar(String texto) {
		String consulta = texto == null ? "" : texto.trim();
		if (consulta.isEmpty()) {
			return List.of();
		}
		return repositorio.buscarPorNombre(consulta, LIMITE_DE_RESULTADOS).stream()
				.map(PersonaResponse::desde)
				.toList();
	}

	@Transactional
	public PersonaResponse actualizar(Long id, PersonaRequest req) {
		Persona persona = buscar(id);
		persona.actualizar(req.nombre());
		return PersonaResponse.desde(persona);
	}

	@Transactional
	public void borrar(Long id) {
		if (!repositorio.existsById(id)) {
			throw new PersonaNoEncontradaException(id);
		}
		repositorio.deleteById(id);
	}

	/**
	 * Buscar-o-crear persona para las participaciones de una producción (D14, HU-20): con
	 * {@code id} resuelve una persona ya existente; con {@code nombre} reusa la que coincida
	 * (sin distinguir mayúsculas) o la da de alta en el momento. Los duplicados que se
	 * cuelen son deuda de datos aceptada, que el admin corrige a mano (D14).
	 * Requiere transacción abierta del llamador.
	 */
	public Persona buscarOCrearEntidad(Long id, String nombre) {
		if (id != null) {
			return buscar(id);
		}
		String limpio = nombre.trim();
		return repositorio.findFirstByNombreIgnoreCaseOrderByIdAsc(limpio)
				.orElseGet(() -> repositorio.save(new Persona(limpio)));
	}

	private Persona buscar(Long id) {
		return repositorio.findById(id).orElseThrow(() -> new PersonaNoEncontradaException(id));
	}
}
