package io.github.ramiroabadie.backend.aplicacion.internal;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.github.ramiroabadie.backend.identidad.UsuarioPublico;
import io.github.ramiroabadie.backend.identidad.Usuarios;

/**
 * Quién está del otro lado, traducido a un {@code usuarioId}. Es el concern transversal que
 * describe MODULE_MAP y el motivo por el que varios controladores viven en la capa de aplicación
 * (D60): Spring Security sabe el username, los módulos guardan ids opacos (D30), y el único que
 * puede cruzar esas dos cosas es quien tiene permitido preguntarle a Identidad.
 *
 * <p>Está acá y no repetido en cada controlador porque ya son tres los que lo necesitan
 * —registrar, seguir y el feed— y la definición de "el usuario de esta sesión" tiene que ser
 * una sola.</p>
 */
@Component
public class SesionActual {

	/** El que sabe distinguir una sesión de verdad del usuario anónimo que Spring inventa. */
	private final AuthenticationTrustResolver resolutor = new AuthenticationTrustResolverImpl();

	private final Usuarios usuarios;

	SesionActual(Usuarios usuarios) {
		this.usuarios = usuarios;
	}

	/**
	 * Para lo que exige estar logueado. Que haya sesión lo garantiza {@code SecurityConfig}; lo
	 * que puede fallar igual es que la cuenta ya no exista, y para quien escribe eso es no estar
	 * logueado.
	 */
	public Long id(Authentication autenticado) {
		return idSiEstaLogueado(autenticado).orElseThrow(() -> new AuthenticationCredentialsNotFoundException(
				"La cuenta de esta sesión ya no existe"));
	}

	/**
	 * Para lo que se puede mirar sin cuenta (D21) pero cambia si la hay: el perfil ajeno muestra
	 * lo mismo para todos, con o sin el botón de seguir (HU-15).
	 */
	public Optional<Long> idSiEstaLogueado(Authentication autenticado) {
		if (autenticado == null || !autenticado.isAuthenticated() || resolutor.isAnonymous(autenticado)) {
			return Optional.empty();
		}
		return usuarios.porUsername(autenticado.getName()).map(UsuarioPublico::id);
	}
}
