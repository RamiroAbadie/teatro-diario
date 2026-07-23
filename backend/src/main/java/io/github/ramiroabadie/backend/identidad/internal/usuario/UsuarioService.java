package io.github.ramiroabadie.backend.identidad.internal.usuario;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de la cuenta: alta (HU-01) y lectura de la cuenta propia. La autenticación en sí
 * la maneja Spring Security a través de {@link UsuarioDetailsService}.
 */
@Service
public class UsuarioService {

	private final UsuarioRepository repositorio;

	private final PasswordEncoder encoder;

	UsuarioService(UsuarioRepository repositorio, PasswordEncoder encoder) {
		this.repositorio = repositorio;
		this.encoder = encoder;
	}

	@Transactional
	public CuentaResponse registrar(RegistroRequest req) {
		String username = normalizar(req.username());
		String email = normalizar(req.email());
		if (repositorio.existsByUsername(username)) {
			throw new CampoEnUsoException("username", "Ese nombre de usuario ya está tomado");
		}
		if (repositorio.existsByEmail(email)) {
			throw new CampoEnUsoException("email", "Ya existe una cuenta con ese email");
		}
		Usuario usuario = repositorio.save(new Usuario(username, email, encoder.encode(req.password())));
		return CuentaResponse.desde(usuario);
	}

	/**
	 * La cuenta de quien está logueado. El username viene de la sesión, así que si no está en
	 * la base es porque la cuenta se borró con la sesión abierta.
	 */
	@Transactional(readOnly = true)
	public CuentaResponse obtenerPorUsername(String username) {
		return repositorio.findByUsername(username)
				.map(CuentaResponse::desde)
				.orElseThrow(() -> new SesionSinCuentaException(username));
	}

	static String normalizar(String valor) {
		return valor.trim().toLowerCase(Locale.ROOT);
	}
}
