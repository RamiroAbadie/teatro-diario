package io.github.ramiroabadie.backend.catalogo.internal.persona;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51).
 */
interface PersonaRepository extends JpaRepository<Persona, Long> {

	/**
	 * Sostiene el buscar-o-crear de personas al cargar participaciones (D14). Los homónimos
	 * y los duplicados existen a propósito (D14 los acepta como deuda de datos), así que la
	 * consulta se queda con la coincidencia más antigua en vez de fallar cuando hay más de una.
	 */
	Optional<Persona> findFirstByNombreIgnoreCaseOrderByIdAsc(String nombre);

	/**
	 * La búsqueda de personas (HU-07). Misma forma que la de producciones —{@code ILIKE} para
	 * lo literal, {@code %} para el typo, {@code <%} para el nombre a medio escribir— sobre el
	 * único campo que la Persona tiene (D14). Acá el orden total importa más que en el
	 * catálogo: los homónimos y los duplicados de artista existen a propósito.
	 */
	@Query(value = """
			SELECT * FROM persona
			WHERE nombre ILIKE '%' || :texto || '%' OR nombre % :texto OR :texto <% nombre
			ORDER BY GREATEST(similarity(nombre, :texto), word_similarity(:texto, nombre)) DESC,
			         nombre ASC, id ASC
			LIMIT :limite
			""", nativeQuery = true)
	List<Persona> buscarPorNombre(@Param("texto") String texto, @Param("limite") int limite);
}
