package io.github.ramiroabadie.backend.identidad.internal.usuario;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Puente entre la entidad Usuario y Spring Security. Acepta email o username indistintamente
 * (HU-02): el identificador se normaliza igual que en el alta y se prueba contra los dos campos.
 *
 * <p>El {@code UserDetails} que devuelve siempre lleva el username canónico, no lo que tipeó
 * la persona, para que el resto de la app tenga una sola identidad en la sesión.</p>
 */
@Service
class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository repositorio;

	UsuarioDetailsService(UsuarioRepository repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String identificador) throws UsernameNotFoundException {
		String normalizado = UsuarioService.normalizar(identificador);
		Usuario usuario = repositorio.findByUsernameOrEmail(normalizado, normalizado)
				.orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
		return new User(usuario.getUsername(), usuario.getPasswordHash(),
				List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())));
	}
}
