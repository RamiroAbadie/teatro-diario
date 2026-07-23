package io.github.ramiroabadie.backend.catalogo.internal.sala;

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
 * CRUD de salas (HU-19). Cara de escritura del catálogo, solo-admin. La autorización real
 * llega con Spring Security en la Fase 2 (D52); el prefijo {@code /api/admin} deja lista la
 * frontera para asegurarla entonces. Es el molde del resto del panel.
 */
@RestController
@RequestMapping("/api/admin/salas")
class SalaController {

	private final SalaService servicio;

	SalaController(SalaService servicio) {
		this.servicio = servicio;
	}

	@PostMapping
	public ResponseEntity<SalaResponse> crear(@Valid @RequestBody SalaRequest req) {
		SalaResponse creada = servicio.crear(req);
		return ResponseEntity.created(URI.create("/api/admin/salas/" + creada.id())).body(creada);
	}

	@GetMapping
	public List<SalaResponse> listar() {
		return servicio.listar();
	}

	@GetMapping("/{id}")
	public SalaResponse obtener(@PathVariable Long id) {
		return servicio.obtener(id);
	}

	@PutMapping("/{id}")
	public SalaResponse actualizar(@PathVariable Long id, @Valid @RequestBody SalaRequest req) {
		return servicio.actualizar(id, req);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> borrar(@PathVariable Long id) {
		servicio.borrar(id);
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(SalaNoEncontradaException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrada(SalaNoEncontradaException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ProblemDetail enUso(DataIntegrityViolationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"La sala está referenciada por producciones: reasignalas antes de borrarla");
	}
}
