package io.github.ramiroabadie.backend.aplicacion.internal.diario;

import java.util.List;

import io.github.ramiroabadie.backend.diario.DiarioDeUsuario;
import io.github.ramiroabadie.backend.diario.EstadisticasDeDiario;
import io.github.ramiroabadie.backend.diario.RegistroDeDiario;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;
import io.github.ramiroabadie.backend.social.ContadoresSociales;

/**
 * El perfil público: la cuenta (Identidad), su historial y sus números (Diario) y su lugar en el
 * grafo (Social), en una sola respuesta, que es como se ve la pantalla (HU-03/12/13/15). La
 * composición es el trabajo de la capa de aplicación; ninguno de los tres módulos conoce a los
 * otros.
 *
 * <p>Los contadores llegan planos y no adentro del objeto que devuelve Social: la pantalla los
 * muestra al lado del nombre, no como una sección.</p>
 *
 * @param loSigo si quien pregunta ya sigue a esta cuenta, para el estado del botón (HU-15).
 * {@code null} cuando no hay botón: nadie logueado, o el perfil es el propio
 */
record PerfilResponse(
		UsuarioPublico usuario,
		EstadisticasDeDiario estadisticas,
		long seguidores,
		long seguidos,
		Boolean loSigo,
		List<RegistroDeDiario> registros,
		List<RegistroDeDiario> sinFecha
) {

	static PerfilResponse desde(UsuarioPublico usuario, DiarioDeUsuario diario,
			ContadoresSociales contadores, Boolean loSigo) {
		return new PerfilResponse(usuario, diario.estadisticas(), contadores.seguidores(),
				contadores.seguidos(), loSigo, diario.registros(), diario.sinFecha());
	}
}
