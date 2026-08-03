package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.github.ramiroabadie.backend.catalogo.internal.persona.PersonaNoEncontradaException;
import io.github.ramiroabadie.backend.catalogo.internal.sala.SalaNoEncontradaException;

/**
 * CRUD de producciones (HU-20). Cara de escritura del catálogo, solo-admin. La autorización
 * real llega con Spring Security en la Fase 2 (D52); el prefijo {@code /api/admin} deja lista
 * la frontera para asegurarla entonces. Sigue el molde de {@code SalaController} (HU-19).
 *
 * <p>La subida de afiche (D72/D77) cierra lo último que a HU-20 le faltaba del backend. Es el
 * único endpoint de la API que no habla JSON de entrada, y el único cuyo orden de operaciones es
 * la decisión: vive en {@code AficheService}.</p>
 */
@RestController
@RequestMapping("/api/admin/producciones")
class ProduccionController {

	private final ProduccionService servicio;

	private final AficheService afiches;

	ProduccionController(ProduccionService servicio, AficheService afiches) {
		this.servicio = servicio;
		this.afiches = afiches;
	}

	@PostMapping
	public ResponseEntity<ProduccionResponse> crear(@Valid @RequestBody ProduccionRequest req) {
		ProduccionResponse creada = servicio.crear(req);
		return ResponseEntity.created(URI.create("/api/admin/producciones/" + creada.id())).body(creada);
	}

	@GetMapping
	public List<ProduccionResumenResponse> listar(@RequestParam(required = false) EstadoProduccion estado) {
		return servicio.listar(estado);
	}

	@GetMapping("/{id}")
	public ProduccionResponse obtener(@PathVariable Long id) {
		return servicio.obtener(id);
	}

	@PutMapping("/{id}")
	public ProduccionResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProduccionRequest req) {
		return servicio.actualizar(id, req);
	}

	@PatchMapping("/{id}/estado")
	public ProduccionResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest req) {
		return servicio.cambiarEstado(id, req.estado());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> borrar(@PathVariable Long id) {
		servicio.borrar(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Sube o reemplaza el afiche (D77). Devuelve la ficha entera y no solo la URL: después de
	 * reemplazar, la pantalla tiene que quedarse con la URL nueva —la vieja ya no existe— y la
	 * ficha es lo que esa pantalla estaba mostrando.
	 */
	@PostMapping(value = "/{id}/afiche", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ProduccionResponse subirAfiche(@PathVariable Long id,
			@RequestPart("archivo") MultipartFile archivo) throws IOException {
		return afiches.subir(id, archivo.getBytes());
	}

	/** Idempotente: 204 aunque ya no tuviera afiche. El 404 es solo para la ficha inexistente. */
	@DeleteMapping("/{id}/afiche")
	public ResponseEntity<Void> borrarAfiche(@PathVariable Long id) {
		afiches.borrar(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Lo que subieron no sirve como afiche. Es 400 y no 413: el 413 es del tamaño del archivo y
	 * lo decide el contenedor antes de llegar acá.
	 */
	@ExceptionHandler(AficheInvalidoException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProblemDetail aficheInvalido(AficheInvalidoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	/**
	 * Una sala o una persona inexistente referenciada desde la ficha también es un 404: lo que
	 * falta es la entidad que se pidió, y el mensaje dice cuál.
	 */
	@ExceptionHandler({ ProduccionNoEncontradaException.class, SalaNoEncontradaException.class,
			PersonaNoEncontradaException.class })
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrada(RuntimeException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ProblemDetail participacionRepetida(DataIntegrityViolationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"La misma persona no puede repetir el mismo rol en la producción");
	}
}
