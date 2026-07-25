package io.github.ramiroabadie.backend.aplicacion.internal.social;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.aplicacion.internal.SesionActual;
import io.github.ramiroabadie.backend.diario.ActividadDeDiario;
import io.github.ramiroabadie.backend.diario.CursorDeActividad;
import io.github.ramiroabadie.backend.diario.Diario;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;
import io.github.ramiroabadie.backend.identidad.Usuarios;
import io.github.ramiroabadie.backend.social.GrafoSocial;

/**
 * El feed de la home logueada (HU-16). Es la composición que anuncia D29 y el motivo por el que
 * el feed no es un módulo ni una tabla materializada: Social dice a quiénes sigo, Diario qué
 * registraron, Identidad cómo se llaman. Ninguno de los tres conoce a los otros y acá no se
 * guarda nada — el feed se arma al leer, con tres consultas.
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

	private final GrafoSocial grafo;

	private final Diario diario;

	private final Usuarios usuarios;

	private final SesionActual sesion;

	FeedController(GrafoSocial grafo, Diario diario, Usuarios usuarios, SesionActual sesion) {
		this.grafo = grafo;
		this.diario = diario;
		this.usuarios = usuarios;
		this.sesion = sesion;
	}

	/**
	 * El fallback global es para quien no sigue a nadie, no para quien sigue gente callada (D22):
	 * un feed vacío con seguidos es información honesta —los tuyos no registraron nada—, y
	 * rellenarlo con desconocidos sería mentir sobre de quién es lo que se está leyendo.
	 */
	@GetMapping("/api/feed")
	public FeedResponse feed(@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "" + TAMANIO_POR_DEFECTO) int tamanio,
			Authentication autenticado) {
		Long yo = sesion.id(autenticado);
		int limite = Math.clamp(tamanio, 1, TAMANIO_MAXIMO);
		CursorDeActividad desde = CursorDelFeed.decodificar(cursor);
		List<Long> seguidos = grafo.seguidosPor(yo);
		boolean global = seguidos.isEmpty();
		List<ActividadDeDiario> actividad = global
				? diario.actividadGlobal(desde, limite)
				: diario.actividadDe(seguidos, desde, limite);
		return FeedResponse.desde(global, actividad, autores(actividad), limite);
	}

	/** Los nombres de la página entera en una sola consulta, como las firmas de las reseñas. */
	private Map<Long, UsuarioPublico> autores(List<ActividadDeDiario> actividad) {
		return usuarios.porIds(actividad.stream()
				.map(ActividadDeDiario::usuarioId)
				.distinct()
				.toList());
	}

	@ExceptionHandler(CursorInvalidoException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProblemDetail cursorInvalido(CursorInvalidoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	/** La sesión sigue viva pero la cuenta se borró: no hay feed de nadie. */
	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ProblemDetail sesionSinCuenta(AuthenticationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}
}
