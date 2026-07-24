package io.github.ramiroabadie.backend.aplicacion.internal.diario;

/**
 * No hay ninguna cuenta con ese username. El 404 de perfil tiene su propia pantalla con búsqueda
 * embebida (USER_FLOWS.md), así que el mensaje va corto y sin rodeos.
 */
class PerfilNoEncontradoException extends RuntimeException {

	PerfilNoEncontradoException(String username) {
		super("No existe la cuenta " + username);
	}
}
