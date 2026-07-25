package io.github.ramiroabadie.backend.aplicacion.internal.curaduria;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.catalogo.CatalogoProducciones;
import io.github.ramiroabadie.backend.catalogo.SugerenciaNoEncontradaException;
import io.github.ramiroabadie.backend.catalogo.SugerenciaPropuesta;
import io.github.ramiroabadie.backend.catalogo.SugerenciaResueltaException;
import io.github.ramiroabadie.backend.catalogo.Sugerencias;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;
import io.github.ramiroabadie.backend.identidad.Usuarios;

/**
 * La cola del admin (HU-21), primera parada de la rutina semanal de curaduría (D37). Cuelga de
 * {@code /api/admin} como el resto del panel, así que el candado de rol lo pone
 * {@code SecurityConfig} sin ninguna regla nueva.
 *
 * <p>Está en la capa de aplicación y no adentro de Catálogo por una sola razón: la cola muestra
 * quién sugirió cada obra, y el {@code user_id} que Catálogo guarda es opaco (D30). Ponerle nombre
 * es exactamente el caso de composición que describe D60 —traducir un id que un módulo no puede
 * resolver solo—, y cuesta una consulta por página de la cola, nunca una por fila.</p>
 *
 * <p>Aprobar no crea la ficha: la crea el admin con el formulario de HU-20 precargado con lo
 * sugerido, y este endpoint cierra el círculo anotando en qué producción terminó. Por eso lo único
 * que se comprueba acá es que esa ficha exista, igual que se comprueba que exista la reseña antes
 * de un like (D68).</p>
 */
@RestController
@RequestMapping("/api/admin/sugerencias")
class ColaDeSugerenciasController {

	private final Sugerencias sugerencias;

	private final CatalogoProducciones catalogo;

	private final Usuarios usuarios;

	ColaDeSugerenciasController(Sugerencias sugerencias, CatalogoProducciones catalogo, Usuarios usuarios) {
		this.sugerencias = sugerencias;
		this.catalogo = catalogo;
		this.usuarios = usuarios;
	}

	@GetMapping
	public List<SugerenciaEnColaResponse> pendientes() {
		List<SugerenciaPropuesta> pendientes = sugerencias.pendientes();
		List<Long> sugerentes = pendientes.stream().map(SugerenciaPropuesta::sugerenteId).distinct().toList();
		Map<Long, UsuarioPublico> nombres = usuarios.porIds(sugerentes);
		return pendientes.stream().map(propuesta -> SugerenciaEnColaResponse.desde(propuesta, nombres)).toList();
	}

	/**
	 * Responde 204: lo que el panel hace después es volver a pedir la cola, que es donde se ve el
	 * efecto —la sugerencia ya no está—.
	 */
	@PostMapping("/{id}/aprobar")
	public ResponseEntity<Void> aprobar(@PathVariable Long id, @Valid @RequestBody AprobacionRequest req) {
		if (catalogo.porId(req.produccionId()).isEmpty()) {
			throw new FichaInexistenteException(req.produccionId());
		}
		sugerencias.aprobar(id, req.produccionId());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/rechazar")
	public ResponseEntity<Void> rechazar(@PathVariable Long id, @Valid @RequestBody RechazoRequest req) {
		sugerencias.rechazar(id, req.motivo());
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler({ SugerenciaNoEncontradaException.class, FichaInexistenteException.class })
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrada(RuntimeException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	/** La cola abierta en dos pestañas: el segundo intento llega tarde, no está roto. */
	@ExceptionHandler(SugerenciaResueltaException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ProblemDetail yaResuelta(SugerenciaResueltaException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}
}
