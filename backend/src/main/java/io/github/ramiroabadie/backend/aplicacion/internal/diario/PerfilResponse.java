package io.github.ramiroabadie.backend.aplicacion.internal.diario;

import java.util.List;

import io.github.ramiroabadie.backend.diario.DiarioDeUsuario;
import io.github.ramiroabadie.backend.diario.EstadisticasDeDiario;
import io.github.ramiroabadie.backend.diario.RegistroDeDiario;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;

/**
 * El perfil público: la cuenta (Identidad) con su diario y sus números (Diario) en una sola
 * respuesta, que es como se ve la pantalla (HU-03/12/13). La composición es el trabajo de la
 * capa de aplicación; ninguno de los dos módulos conoce al otro.
 *
 * <p>Los seguidores y seguidos (HU-15) van a sumarse acá en la Fase 3, del mismo modo: un
 * módulo más que aporta lo suyo a la misma respuesta.</p>
 */
record PerfilResponse(
		UsuarioPublico usuario,
		EstadisticasDeDiario estadisticas,
		List<RegistroDeDiario> registros,
		List<RegistroDeDiario> sinFecha
) {

	static PerfilResponse desde(UsuarioPublico usuario, DiarioDeUsuario diario) {
		return new PerfilResponse(usuario, diario.estadisticas(), diario.registros(), diario.sinFecha());
	}
}
