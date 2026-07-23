package io.github.ramiroabadie.backend.identidad.internal.usuario;

import java.nio.charset.StandardCharsets;
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

	/**
	 * BCrypt trunca lo que pase de 72 bytes y desde Spring Security 6 directamente lo rechaza.
	 * El {@code @Size} del request cuenta caracteres, que no es lo mismo: 40 letras con tilde ya
	 * son 80 bytes. Sin este control, esa contraseña pasa la validación y revienta al hashear.
	 */
	private static final int MAXIMO_BYTES_PASSWORD = 72;

	@Transactional
	public CuentaResponse registrar(RegistroRequest req) {
		if (req.password().getBytes(StandardCharsets.UTF_8).length > MAXIMO_BYTES_PASSWORD) {
			throw new CampoInvalidoException("password",
					"La contraseña es demasiado larga: los acentos y emojis ocupan más de un lugar");
		}
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
