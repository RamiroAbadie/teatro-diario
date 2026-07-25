package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.catalogo.internal.persona.PersonaNoEncontradaException;
import io.github.ramiroabadie.backend.catalogo.internal.persona.PersonaResponse;
import io.github.ramiroabadie.backend.catalogo.internal.sala.SalaNoEncontradaException;

/**
 * Lectura pública del catálogo: ficha, en cartel, página de artista y página de sala
 * (HU-04/05/06). Sin prefijo {@code /api/admin}: acá no hay login (D21), y esa frontera es
 * la que se va a asegurar en la Fase 2 (D52) dejando este lado abierto — de ahí que la sala
 * necesite su lectura propia acá y no le alcance con la del panel.
 *
 * <p>Un solo controller para los cuatro endpoints porque son una sola capacidad —la cara de
 * lectura del catálogo— y las cuatro respuestas se arman con datos de producción.</p>
 *
 * <p>Versión JSON. El SSR y los metadatos Open Graph que piden HU-04 y HU-05 (ADR-003) son
 * del frontend, que entra en la Fase 4: estos endpoints son lo que va a consumir.</p>
 */
@RestController
@RequestMapping("/api")
class CatalogoPublicoController {

	private final CatalogoPublicoService servicio;

	CatalogoPublicoController(CatalogoPublicoService servicio) {
		this.servicio = servicio;
	}

	@GetMapping("/producciones/{id}")
	public ProduccionResponse ficha(@PathVariable Long id) {
		return servicio.ficha(id);
	}

	/** Ruta propia y no {@code /producciones/en-cartel} para no chocar con la ficha por id. */
	@GetMapping("/en-cartel")
	public EnCartelResponse enCartel() {
		return servicio.enCartel();
	}

	@GetMapping("/personas/{id}")
	public ArtistaResponse artista(@PathVariable Long id) {
		return servicio.artista(id);
	}

	/**
	 * Por id y no por slug como anota USER_FLOWS.md: el slug es de la URL del frontend
	 * (Fase 4) y la Sala no tiene ese campo. La API va por id, igual que ficha y artista.
	 */
	@GetMapping("/salas/{id}")
	public SalaPublicaResponse sala(@PathVariable Long id) {
		return servicio.sala(id);
	}

	/**
	 * Las dos búsquedas del catálogo (HU-07): producciones y personas, cada una por su lado,
	 * porque la pantalla de resultados las muestra en secciones separadas y el autocompletado
	 * del gesto de registro (HU-09) solo pide la primera. Las salas no se buscan (D23).
	 *
	 * <p>Bajo {@code /api/buscar/...} y no {@code /api/producciones/buscar} por lo mismo que
	 * "en cartel" tiene ruta propia: no chocar con la ficha por id. Que la búsqueda de
	 * usuarios cuelgue del mismo prefijo desde el módulo Identidad no las acopla — la URL no
	 * es un límite de módulo; cada módulo busca sobre lo suyo y nadie compone nada (D23).</p>
	 */
	@GetMapping("/buscar/producciones")
	public List<ProduccionResumenResponse> buscarProducciones(@RequestParam String q) {
		return servicio.buscar(q);
	}

	@GetMapping("/buscar/personas")
	public List<PersonaResponse> buscarPersonas(@RequestParam String q) {
		return servicio.buscarPersonas(q);
	}

	@ExceptionHandler({ ProduccionNoEncontradaException.class, PersonaNoEncontradaException.class,
			SalaNoEncontradaException.class })
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrada(RuntimeException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}
}
