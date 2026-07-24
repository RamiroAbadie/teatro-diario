package io.github.ramiroabadie.backend.identidad.internal.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo del alta de cuenta (HU-01). El username se valida con formato URL-safe porque va a
 * ser la dirección del perfil público. Se acepta en cualquier capitalización y el servicio lo
 * normaliza a minúsculas.
 *
 * <p>El máximo de 72 caracteres de la contraseña no es capricho: BCrypt ignora lo que pase de
 * 72 bytes, así que aceptar más sería mentirle al usuario sobre la fuerza de su clave.</p>
 */
record RegistroRequest(
		@NotBlank(message = "El nombre de usuario es obligatorio")
		@Pattern(regexp = "^[A-Za-z0-9_]{3,20}$",
				message = "Entre 3 y 20 caracteres: letras, números o guión bajo")
		String username,

		@NotBlank(message = "El email es obligatorio")
		@Email(message = "El email no tiene un formato válido")
		@Size(max = 254, message = "El email no puede superar los 254 caracteres")
		String email,

		@NotBlank(message = "La contraseña es obligatoria")
		@Size(min = 8, max = 72, message = "La contraseña necesita entre 8 y 72 caracteres")
		String password
) {
}
