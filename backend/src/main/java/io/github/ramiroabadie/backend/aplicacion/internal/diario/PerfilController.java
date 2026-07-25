package io.github.ramiroabadie.backend.aplicacion.internal.diario;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.aplicacion.internal.CuentaInexistenteException;
import io.github.ramiroabadie.backend.aplicacion.internal.SesionActual;
import io.github.ramiroabadie.backend.diario.Diario;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;
import io.github.ramiroabadie.backend.identidad.Usuarios;
import io.github.ramiroabadie.backend.social.GrafoSocial;

/**
 * El perfil con el diario, las estadísticas y los números del grafo (HU-03/12/13/15): la carta de
 * presentación que se comparte. Sin login, como todo el contenido (D21).
 *
 * <p>Por username y no por id porque la URL del perfil es {@code /{username}} (MD-4). El SSR y
 * los metadatos Open Graph que pide HU-03 son del frontend (Fase 4): esto es lo que va a
 * consumir.</p>
 *
 * <p>Es la respuesta que más módulos compone —Identidad la cuenta, Diario el historial, Social
 * los contadores— y ninguno de los tres se entera de los otros.</p>
 */
@RestController
@RequestMapping("/api/usuarios")
class PerfilController {

	private final Usuarios usuarios;

	private final Diario diario;

	private final GrafoSocial grafo;

	private final SesionActual sesion;

	PerfilController(Usuarios usuarios, Diario diario, GrafoSocial grafo, SesionActual sesion) {
		this.usuarios = usuarios;
		this.diario = diario;
		this.grafo = grafo;
		this.sesion = sesion;
	}

	/**
	 * Quien mira sin cuenta ve exactamente lo mismo menos el estado del botón de seguir: es lo
	 * único de esta respuesta que depende de quién pregunta. En el perfil propio tampoco va,
	 * porque ahí no hay botón.
	 */
	@GetMapping("/{username}")
	public PerfilResponse perfil(@PathVariable String username, Authentication autenticado) {
		UsuarioPublico usuario = usuarios.porUsername(username)
				.orElseThrow(() -> new CuentaInexistenteException(username));
		Optional<Long> quienMira = sesion.idSiEstaLogueado(autenticado)
				.filter(yo -> !yo.equals(usuario.id()));
		return PerfilResponse.desde(usuario, diario.deUsuario(usuario.id()),
				grafo.contadoresDe(usuario.id()),
				quienMira.map(yo -> grafo.sigue(yo, usuario.id())).orElse(null));
	}

	@ExceptionHandler(CuentaInexistenteException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrado(CuentaInexistenteException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}
}
