package io.github.ramiroabadie.backend.catalogo.internal.sugerencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51). La cola sale de un
 * método derivado y no hace falta nada más: una sugerencia se escribe una vez y se resuelve una
 * vez.
 */
interface SugerenciaRepository extends JpaRepository<Sugerencia, Long> {

	/**
	 * La cola, de lo más viejo a lo más nuevo. Sin paginar a propósito: son las propuestas sin
	 * resolver de una plataforma que arranca, y el barrido semanal (D37) las vacía. El día que la
	 * cola no entre en una pantalla, ese día se pagina.
	 */
	List<Sugerencia> findByEstadoOrderByCreadoEnAscIdAsc(EstadoSugerencia estado);
}
