package io.github.ramiroabadie.backend.aplicacion.internal.social;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.ramiroabadie.backend.diario.ActividadDeDiario;
import io.github.ramiroabadie.backend.diario.CursorDeActividad;
import io.github.ramiroabadie.backend.diario.Diario;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;
import io.github.ramiroabadie.backend.identidad.Usuarios;
import io.github.ramiroabadie.backend.social.GrafoSocial;

/**
 * El caso de uso del feed (HU-16): la composición que anuncia D29 y el motivo por el que el feed
 * no es un módulo ni una tabla materializada. Social dice a quiénes sigo, Diario qué registraron,
 * Identidad cómo se llaman; ninguno de los tres conoce a los otros y acá no se guarda nada.
 *
 * <p>Está separado del controlador y no adentro, como {@code FusionDeProducciones}, porque la
 * regla del fallback global es una decisión de producto (D22) y no HTTP: en los controladores no
 * va ninguna regla (D34, D60). Lo que queda del otro lado —traducir la sesión, decodificar el
 * cursor, acotar el tamaño de página— sí es HTTP y por eso se quedó ahí.</p>
 */
@Service
class ArmadoDelFeed {

	private final GrafoSocial grafo;

	private final Diario diario;

	private final Usuarios usuarios;

	ArmadoDelFeed(GrafoSocial grafo, Diario diario, Usuarios usuarios) {
		this.grafo = grafo;
		this.diario = diario;
		this.usuarios = usuarios;
	}

	/**
	 * El fallback global es para quien no sigue a nadie, no para quien sigue gente callada (D22):
	 * un feed vacío con seguidos es información honesta —los tuyos no registraron nada— y
	 * rellenarlo con desconocidos sería mentir sobre de quién es lo que se está leyendo.
	 */
	FeedResponse pagina(Long usuarioId, CursorDeActividad desde, int limite) {
		List<Long> seguidos = grafo.seguidosPor(usuarioId);
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
}
