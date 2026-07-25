package io.github.ramiroabadie.backend.social.internal.seguimiento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51). El grafo es una
 * tabla de dos columnas y todo lo que hace falta preguntarle sale de métodos derivados.
 */
interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

	boolean existsBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);

	void deleteBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);

	long countBySeguidoId(Long seguidoId);

	long countBySeguidorId(Long seguidorId);

	/**
	 * Los ids que sigue alguien, los últimos seguidos primero. Escrita a mano porque lo que se
	 * quiere son los ids y no las filas: el feed no necesita nada más y traer entidades enteras
	 * para tirar todo menos una columna es trabajo de la base al pedo.
	 */
	@Query("select s.seguidoId from Seguimiento s where s.seguidorId = :seguidorId order by s.creadoEn desc")
	List<Long> seguidosPor(@Param("seguidorId") Long seguidorId);
}
