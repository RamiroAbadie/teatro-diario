package io.github.ramiroabadie.backend.catalogo.internal.persona;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso del CRUD de personas. El mapeo entidad↔DTO ocurre dentro de la transacción
 * ({@code open-in-view: false}). La autorización de admin (D7) es un concern transversal
 * de la capa de aplicación y llega con Spring Security en la Fase 2 (D52).
 */
@Service
class PersonaService {

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

	private Persona buscar(Long id) {
		return repositorio.findById(id).orElseThrow(() -> new PersonaNoEncontradaException(id));
	}
}
