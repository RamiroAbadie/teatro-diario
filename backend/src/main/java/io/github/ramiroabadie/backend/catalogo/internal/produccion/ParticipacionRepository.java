package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51).
 * Existe para leer las participaciones desde el lado de la persona (HU-05); las escrituras
 * siguen entrando por la producción, que es su dueña.
 */
interface ParticipacionRepository extends JpaRepository<Participacion, Long> {

	/**
	 * Sostiene la página de artista: las participaciones quedan etiquetadas por rol y
	 * agrupadas de hecho, porque el orden las junta (HU-05). El {@code @EntityGraph} trae la
	 * producción y su sala de una, para armar los links a las fichas sin una query por fila.
	 */
	@EntityGraph(attributePaths = { "produccion", "produccion.sala" })
	List<Participacion> findByPersonaIdOrderByRolAscProduccionTituloAsc(Long personaId);
}
