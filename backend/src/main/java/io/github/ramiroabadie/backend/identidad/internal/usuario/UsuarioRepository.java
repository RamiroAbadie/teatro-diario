package io.github.ramiroabadie.backend.identidad.internal.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51).
 * Todas las búsquedas son exactas porque username y email se guardan normalizados.
 */
interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	Optional<Usuario> findByUsername(String username);

	/** Login por email o username indistintamente (HU-02). */
	Optional<Usuario> findByUsernameOrEmail(String username, String email);
}
