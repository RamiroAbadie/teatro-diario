package io.github.ramiroabadie.backend.aplicacion.internal;

/**
 * No hay ninguna cuenta con ese username. La levantan las dos cosas que se piden por la URL del
 * perfil (MD-4): verlo y seguirlo. El 404 de perfil tiene su propia pantalla con búsqueda
 * embebida (USER_FLOWS.md), así que el mensaje va corto y sin rodeos.
 */
public class CuentaInexistenteException extends RuntimeException {

	public CuentaInexistenteException(String username) {
		super("No existe la cuenta " + username);
	}
}
