package io.github.ramiroabadie.backend.catalogo.internal.sugerencia;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51). La cola sale de un
 * método derivado; el que va a mano es el de resolver, porque necesita traerse la fila bloqueada.
 */
interface SugerenciaRepository extends JpaRepository<Sugerencia, Long> {

	/**
	 * La sugerencia que se está por resolver, con la fila tomada hasta el fin de la transacción
	 * ({@code select ... for update}).
	 *
	 * <p>Sin el bloqueo, aprobar y rechazar son leer-comprobar-escribir, y dos resoluciones que se
	 * solapan leen las dos "pendiente", pasan las dos el chequeo y escriben las dos: la sugerencia
	 * sale de la cola dos veces y la segunda pisa a la primera, en vez del 409 que promete D69. Con
	 * él, la segunda espera, vuelve a leer y se encuentra con que ya está resuelta.</p>
	 *
	 * <p>Es un bloqueo pesimista y no una versión optimista porque acá el conflicto se resuelve
	 * cortando, no reintentando: quien llega segundo no tiene nada que volver a intentar, y la
	 * espera es la de un {@code update} sobre una fila, con un solo admin del otro lado.</p>
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from Sugerencia s where s.id = :id")
	Optional<Sugerencia> paraResolver(@Param("id") Long id);

	/**
	 * La cola, de lo más viejo a lo más nuevo. Sin paginar a propósito: son las propuestas sin
	 * resolver de una plataforma que arranca, y el barrido semanal (D37) las vacía. El día que la
	 * cola no entre en una pantalla, ese día se pagina.
	 */
	List<Sugerencia> findByEstadoOrderByCreadoEnAscIdAsc(EstadoSugerencia estado);
}
