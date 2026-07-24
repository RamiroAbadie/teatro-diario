package io.github.ramiroabadie.backend.identidad.internal.usuario;

/**
 * Hay sesión válida pero la cuenta ya no existe (se borró con la sesión abierta).
 * El controlador la mapea a 401 para que el cliente rearme el login.
 */
class SesionSinCuentaException extends RuntimeException {

	SesionSinCuentaException(String username) {
		super("La cuenta " + username + " ya no existe");
	}
}
