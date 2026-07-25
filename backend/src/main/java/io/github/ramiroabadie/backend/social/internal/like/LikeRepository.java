package io.github.ramiroabadie.backend.social.internal.like;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51). Escribir un like es
 * insertar o borrar una fila y sale de métodos derivados; las dos lecturas van a mano porque las
 * dos son sobre una lista de reseñas —la ficha entera, la página entera del feed— y ninguna se
 * puede expresar contando de a una.
 */
interface LikeRepository extends JpaRepository<LikeDeResenia, Long> {

	boolean existsByReseniaIdAndUsuarioId(Long reseniaId, Long usuarioId);

	void deleteByReseniaIdAndUsuarioId(Long reseniaId, Long usuarioId);

	/**
	 * Los contadores de una lista de reseñas en una sola consulta. Las que no tienen ningún like no
	 * aparecen en el resultado: un {@code group by} no inventa filas para lo que no está, y el cero
	 * lo pone quien arma la respuesta.
	 */
	@Query("""
			select l.reseniaId as reseniaId, count(l) as cantidad
			from LikeDeResenia l
			where l.reseniaId in :reseniaIds
			group by l.reseniaId
			""")
	List<ConteoDeLikes> contarPorResenia(@Param("reseniaIds") Collection<Long> reseniaIds);

	/**
	 * Cuáles de estas reseñas tienen el like de esta persona. Devuelve los ids y no las filas: lo
	 * único que se pregunta es si el botón está encendido.
	 */
	@Query("select l.reseniaId from LikeDeResenia l where l.usuarioId = :usuarioId and l.reseniaId in :reseniaIds")
	List<Long> conLikeDe(@Param("usuarioId") Long usuarioId, @Param("reseniaIds") Collection<Long> reseniaIds);

	/** Proyección del conteo: qué reseña y cuántos likes tiene. */
	interface ConteoDeLikes {

		Long getReseniaId();

		long getCantidad();
	}
}
