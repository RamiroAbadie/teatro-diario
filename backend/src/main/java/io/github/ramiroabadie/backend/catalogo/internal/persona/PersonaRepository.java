package io.github.ramiroabadie.backend.catalogo.internal.persona;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
