package io.github.ramiroabadie.backend.aplicacion.internal.social;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.aplicacion.internal.SesionActual;

/**
 * La cara HTTP del feed (HU-16). El caso de uso —la composición de los tres módulos y la regla
 * del fallback global— vive en {@link ArmadoDelFeed}; acá solo pasa lo que es del protocolo:
 * quién está logueado, hasta dónde llegó la página anterior y cuántos ítems entran en la que
 * sigue (D34).
 *
 * <p>Es el único {@code GET} de la API que pide sesión, además de {@code /api/auth/yo}: el resto
 * del contenido se lee sin cuenta (D21), pero "lo que registraron los que sigo" no existe sin un
 * yo. La regla está en {@code SecurityConfig}.</p>
 */
@RestController
class FeedController {

	/** Lo que trae una página si el cliente no pide otra cosa. */
	private static final int TAMANIO_POR_DEFECTO = 20;

	/** Techo del tamaño de página: el feed es para scrollear, no para bajarse la plataforma. */
	private static final int TAMANIO_MAXIMO = 50;

	private final ArmadoDelFeed feed;

	private final SesionActual sesion;

	FeedController(ArmadoDelFeed feed, SesionActual sesion) {
		this.feed = feed;
		this.sesion = sesion;
	}

	@GetMapping("/api/feed")
	public FeedResponse feed(@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "" + TAMANIO_POR_DEFECTO) int tamanio,
			Authentication autenticado) {
		return feed.pagina(sesion.id(autenticado), CursorDelFeed.decodificar(cursor),
				Math.clamp(tamanio, 1, TAMANIO_MAXIMO));
	}

	@ExceptionHandler(CursorInvalidoException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProblemDetail cursorInvalido(CursorInvalidoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
	}
}
