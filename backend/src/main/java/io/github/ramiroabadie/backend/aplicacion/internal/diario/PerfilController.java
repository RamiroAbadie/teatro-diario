package io.github.ramiroabadie.backend.aplicacion.internal.diario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.diario.Diario;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;
import io.github.ramiroabadie.backend.identidad.Usuarios;

/**
 * El perfil con el diario y las estadísticas (HU-03/12/13): la carta de presentación que se
 * comparte. Sin login, como todo el contenido (D21).
 *
 * <p>Por username y no por id porque la URL del perfil es {@code /{username}} (MD-4). El SSR y
 * los metadatos Open Graph que pide HU-03 son del frontend (Fase 4): esto es lo que va a
 * consumir.</p>
 */
@RestController
@RequestMapping("/api/usuarios")
class PerfilController {

	private final Usuarios usuarios;

	private final Diario diario;

	PerfilController(Usuarios usuarios, Diario diario) {
		this.usuarios = usuarios;
		this.diario = diario;
	}

	@GetMapping("/{username}")
	public PerfilResponse perfil(@PathVariable String username) {
		UsuarioPublico usuario = usuarios.porUsername(username)
				.orElseThrow(() -> new PerfilNoEncontradoException(username));
		return PerfilResponse.desde(usuario, diario.deUsuario(usuario.id()));
	}

	@ExceptionHandler(PerfilNoEncontradoException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrado(PerfilNoEncontradoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}
}
