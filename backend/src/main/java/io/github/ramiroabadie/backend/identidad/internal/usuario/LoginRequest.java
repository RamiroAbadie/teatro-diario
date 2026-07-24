package io.github.ramiroabadie.backend.identidad.internal.usuario;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo del login (HU-02). Un solo campo para email o username: quien entra no tiene por qué
 * acordarse de con cuál se registró.
 */
record LoginRequest(
		@NotBlank(message = "Ingresá tu email o nombre de usuario")
		String identificador,

		@NotBlank(message = "Ingresá tu contraseña")
		String password
) {
}
