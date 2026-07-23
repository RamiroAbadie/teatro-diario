package io.github.ramiroabadie.backend.catalogo.internal.persona;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD de personas. Cara de escritura del catálogo, solo-admin. La autorización real
 * llega con Spring Security en la Fase 2 (D52); el prefijo {@code /api/admin} deja lista la
 * frontera para asegurarla entonces. Sigue el molde de {@code SalaController} (HU-19).
 */
@RestController
@RequestMapping("/api/admin/personas")
class PersonaController {

	private final PersonaService servicio;

	PersonaController(PersonaService servicio) {
		this.servicio = servicio;
	}

	@PostMapping
	public ResponseEntity<PersonaResponse> crear(@Valid @RequestBody PersonaRequest req) {
		PersonaResponse creada = servicio.crear(req);
		return ResponseEntity.created(URI.create("/api/admin/personas/" + creada.id())).body(creada);
	}

	@GetMapping
	public List<PersonaResponse> listar() {
		return servicio.listar();
	}

	@GetMapping("/{id}")
	public PersonaResponse obtener(@PathVariable Long id) {
		return servicio.obtener(id);
	}

	@PutMapping("/{id}")
	public PersonaResponse actualizar(@PathVariable Long id, @Valid @RequestBody PersonaRequest req) {
		return servicio.actualizar(id, req);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> borrar(@PathVariable Long id) {
		servicio.borrar(id);
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(PersonaNoEncontradaException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrada(PersonaNoEncontradaException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ProblemDetail enUso(DataIntegrityViolationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"La persona tiene participaciones cargadas: sacalas antes de borrarla");
	}
}
