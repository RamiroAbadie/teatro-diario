package io.github.ramiroabadie.backend.identidad.internal.usuario;

/**
 * Rol de una cuenta. El catálogo es cerrado y solo el admin escribe (D7), así que el único
 * privilegio que existe en el MVP es ese. No hay panel de gestión de roles: todas las cuentas
 * nacen {@code USUARIO} y la promoción a {@code ADMIN} se hace a mano contra la base
 * (documentado en el README) — el proyecto tiene un admin, el fundador.
 */
public enum RolUsuario {

	USUARIO,
	ADMIN
}
