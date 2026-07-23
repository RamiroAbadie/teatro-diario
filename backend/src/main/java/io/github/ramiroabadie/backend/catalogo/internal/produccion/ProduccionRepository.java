package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51).
 * Los {@code @EntityGraph} traen de una lo que la respuesta va a mapear dentro de la
 * transacción, sin escribir queries a mano.
 */
interface ProduccionRepository extends JpaRepository<Produccion, Long> {

	@Override
	@EntityGraph(attributePaths = { "sala", "participaciones", "participaciones.persona" })
	Optional<Produccion> findById(Long id);

	@Override
	@EntityGraph(attributePaths = "sala")
	List<Produccion> findAll();

	/** Sostiene el barrido semanal de estados del admin (HU-20). */
	@EntityGraph(attributePaths = "sala")
	List<Produccion> findByEstado(EstadoProduccion estado);
}
