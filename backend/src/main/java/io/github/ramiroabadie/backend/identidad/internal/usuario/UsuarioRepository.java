package io.github.ramiroabadie.backend.identidad.internal.usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51).
 * Los accesos por username y email son exactos porque los dos se guardan normalizados; el
 * único que no lo es —y no puede serlo— es el de la búsqueda de HU-07.
 */
interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	Optional<Usuario> findByUsername(String username);

	/** Login por email o username indistintamente (HU-02). */
	Optional<Usuario> findByUsernameOrEmail(String username, String email);

	/**
	 * La búsqueda de usuarios (HU-07), con la misma forma que las dos del catálogo. Solo por
	 * username: el email no se busca ni se muestra, es lo único de una cuenta que no es público
	 * (D21), y encontrar gente por su email sería otra cosa distinta de buscarla.
	 */
	@Query(value = """
			SELECT * FROM usuario
			WHERE username ILIKE '%' || :texto || '%' OR username % :texto OR :texto <% username
			ORDER BY GREATEST(similarity(username, :texto), word_similarity(:texto, username)) DESC,
			         similarity(username, :texto) DESC, username ASC, id ASC
			LIMIT :limite
			""", nativeQuery = true)
	List<Usuario> buscarPorUsername(@Param("texto") String texto, @Param("limite") int limite);
}
