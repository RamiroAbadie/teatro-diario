package io.github.ramiroabadie.backend.identidad.internal.usuario;

/**
 * Vista de la cuenta propia: la que devuelven registro, login y {@code /api/auth/yo}.
 * Incluye el email porque solo la ve su dueño. El perfil público de HU-03 es otra respuesta
 * (todo el contenido es público, D21, pero el email no es contenido).
 */
public record CuentaResponse(Long id, String username, String email, RolUsuario rol) {

	public static CuentaResponse desde(Usuario usuario) {
		return new CuentaResponse(usuario.getId(), usuario.getUsername(), usuario.getEmail(), usuario.getRol());
	}
}
