package io.github.ramiroabadie.backend.identidad.internal.usuario;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.ramiroabadie.backend.identidad.UsuarioPublico;
import io.github.ramiroabadie.backend.identidad.Usuarios;

/**
 * Casos de uso de la cuenta: alta (HU-01) y lectura de la cuenta propia. La autenticación en sí
 * la maneja Spring Security a través de {@link UsuarioDetailsService}.
 *
 * <p>Es también el adaptador de la interfaz pública del módulo ({@link Usuarios}): la misma
 * cuenta vista desde afuera, sin email y sin hash.</p>
 */
@Service
public class UsuarioService implements Usuarios {

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

	/**
	 * Perfil público por username (HU-03). Normaliza igual que el alta: la URL del perfil puede
	 * venir con cualquier combinación de mayúsculas y tiene que llegar al mismo lado (MD-4).
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<UsuarioPublico> porUsername(String username) {
		return repositorio.findByUsername(normalizar(username)).map(UsuarioService::publico);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Long, UsuarioPublico> porIds(Collection<Long> ids) {
		Map<Long, UsuarioPublico> porId = new LinkedHashMap<>();
		for (Usuario usuario : repositorio.findAllById(ids)) {
			porId.put(usuario.getId(), publico(usuario));
		}
		return porId;
	}

	private static UsuarioPublico publico(Usuario usuario) {
		return new UsuarioPublico(usuario.getId(), usuario.getUsername(), usuario.getCreadoEn());
	}

	static String normalizar(String valor) {
		return valor.trim().toLowerCase(Locale.ROOT);
	}
}
