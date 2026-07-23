package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.ramiroabadie.backend.catalogo.internal.persona.Persona;
import io.github.ramiroabadie.backend.catalogo.internal.persona.PersonaService;
import io.github.ramiroabadie.backend.catalogo.internal.sala.Sala;
import io.github.ramiroabadie.backend.catalogo.internal.sala.SalaService;

/**
 * Casos de uso del CRUD de producciones (HU-20). El mapeo entidad↔DTO ocurre dentro de la
 * transacción ({@code open-in-view: false}). La autorización de admin (D7) es un concern
 * transversal de la capa de aplicación y llega con Spring Security en la Fase 2 (D52).
 *
 * <p>Apoyarse en {@code SalaService} y {@code PersonaService} es tráfico interno del módulo
 * Catálogo, no una dependencia entre módulos: acá viven las tres entidades.</p>
 */
@Service
class ProduccionService {

	private final ProduccionRepository repositorio;

	private final SalaService salas;

	private final PersonaService personas;

	ProduccionService(ProduccionRepository repositorio, SalaService salas, PersonaService personas) {
		this.repositorio = repositorio;
		this.salas = salas;
		this.personas = personas;
	}

	@Transactional
	public ProduccionResponse crear(ProduccionRequest req) {
		Produccion produccion = new Produccion(req.titulo(), req.sinopsis(), req.obraOriginal(),
				req.autorOriginal(), req.estado(), resolverSala(req.salaId()));
		cargarParticipaciones(produccion, req);
		return ProduccionResponse.desde(repositorio.save(produccion));
	}

	@Transactional(readOnly = true)
	public List<ProduccionResumenResponse> listar(EstadoProduccion estado) {
		List<Produccion> producciones = estado == null
				? repositorio.findAll()
				: repositorio.findByEstado(estado);
		return producciones.stream().map(ProduccionResumenResponse::desde).toList();
	}

	@Transactional(readOnly = true)
	public ProduccionResponse obtener(Long id) {
		return ProduccionResponse.desde(buscar(id));
	}

	@Transactional
	public ProduccionResponse actualizar(Long id, ProduccionRequest req) {
		Produccion produccion = buscar(id);
		produccion.actualizar(req.titulo(), req.sinopsis(), req.obraOriginal(), req.autorOriginal(),
				req.estado(), resolverSala(req.salaId()));
		produccion.limpiarParticipaciones();
		// Sin este flush, Hibernate inserta las participaciones nuevas antes de borrar las
		// viejas y choca con la unicidad (produccion, persona, rol) al reeditar una ficha
		// dejando a la misma persona en el mismo rol.
		repositorio.flush();
		cargarParticipaciones(produccion, req);
		// Segundo flush: sin él, las participaciones recién agregadas viajarían sin id.
		repositorio.flush();
		return ProduccionResponse.desde(produccion);
	}

	@Transactional
	public ProduccionResponse cambiarEstado(Long id, EstadoProduccion estado) {
		Produccion produccion = buscar(id);
		produccion.cambiarEstado(estado);
		return ProduccionResponse.desde(produccion);
	}

	@Transactional
	public void borrar(Long id) {
		if (!repositorio.existsById(id)) {
			throw new ProduccionNoEncontradaException(id);
		}
		repositorio.deleteById(id);
	}

	private void cargarParticipaciones(Produccion produccion, ProduccionRequest req) {
		for (ParticipacionRequest participacion : req.participacionesOVacio()) {
			Persona persona = personas.buscarOCrearEntidad(participacion.personaId(), participacion.nombrePersona());
			produccion.agregarParticipacion(persona, participacion.rol());
		}
	}

	private Sala resolverSala(Long salaId) {
		return salaId == null ? null : salas.obtenerEntidad(salaId);
	}

	private Produccion buscar(Long id) {
		return repositorio.findById(id).orElseThrow(() -> new ProduccionNoEncontradaException(id));
	}
}
