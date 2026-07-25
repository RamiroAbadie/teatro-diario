package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51).
 * Los {@code @EntityGraph} traen de una lo que la respuesta va a mapear dentro de la
 * transacción, sin escribir queries a mano. La excepción es la búsqueda: pg_trgm es de
 * Postgres y no hay método derivado que lo exprese.
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

	/**
	 * Sostiene "en cartel" (HU-06) con una sola query para las dos secciones. Ordenado por
	 * título porque la lista pública se lee, no se barre: sin agenda de funciones (D8) no hay
	 * fecha por la cual ordenar.
	 */
	@EntityGraph(attributePaths = "sala")
	List<Produccion> findByEstadoInOrderByTituloAsc(Collection<EstadoProduccion> estados);

	/** Sostiene la página de sala: qué se puede ver ahí ahora (pantalla 5, parte de HU-04). */
	@EntityGraph(attributePaths = "sala")
	List<Produccion> findBySalaIdAndEstadoOrderByTituloAsc(Long salaId, EstadoProduccion estado);

	/**
	 * La búsqueda de producciones (HU-07), con la tolerancia a typos que pide el criterio de
	 * aceptación y que en este proyecto es pg_trgm (D42) y no un motor aparte.
	 *
	 * <p>Tres formas de coincidir, y cada una cubre lo que a las otras se les escapa:
	 * {@code ILIKE} garantiza que lo tipeado literal siempre aparezca, {@code %} (similitud
	 * sobre el título entero, umbral 0.3) tolera el error de tipeo, y {@code <%} —la consulta
	 * contra el mejor pedazo del título, umbral 0.6— es la que hace usable el autocompletado
	 * de HU-09, donde se escriben tres letras de un título largo y la similitud sobre el
	 * título entero da casi cero. Las tres usan el índice GIN de {@code db/busqueda.sql}.</p>
	 *
	 * <p>Ordena por el mejor de los dos puntajes y desempata por la similitud sobre el título
	 * entero, que es lo que distingue "Hamlet" de "El Hamlet" y de "Hamlet en llamas": los tres
	 * sacan 1 en el primer puntaje —la consulta entra completa en los tres— y sin el desempate
	 * el orden lo terminaba decidiendo el alfabeto, que ponía "El Hamlet" antes que la obra
	 * escrita tal cual. Después va el título y al final el id, para que el orden sea total: los
	 * duplicados que el catálogo acepta (D14, D63) tienen el mismo título.</p>
	 *
	 * <p>Límite aceptado: los comodines de {@code ILIKE} que alguien tipee ({@code %},
	 * {@code _}) amplían la búsqueda en vez de romperla, y con el tope de resultados eso no
	 * llega a molestar. Escaparlos costaría o distorsionar los trigramas —el texto escapado
	 * también alimenta a {@code similarity}— o pasar el mismo texto dos veces de dos formas,
	 * y ninguna de las dos vale para un catálogo de títulos de teatro.</p>
	 *
	 * <p>Devuelve ids y no entidades porque una query nativa no puede traer la sala con un
	 * {@code @EntityGraph}: el orden lo pone esta consulta y {@link #findByIdIn} trae las
	 * filas completas de una, sin un {@code select} por sala.</p>
	 */
	@Query(value = """
			SELECT id FROM produccion
			WHERE titulo ILIKE '%' || :texto || '%' OR titulo % :texto OR :texto <% titulo
			ORDER BY GREATEST(similarity(titulo, :texto), word_similarity(:texto, titulo)) DESC,
			         similarity(titulo, :texto) DESC, titulo ASC, id ASC
			LIMIT :limite
			""", nativeQuery = true)
	List<Long> buscarIdsPorTitulo(@Param("texto") String texto, @Param("limite") int limite);

	/** El paso dos de la búsqueda: las filas completas, con su sala, de los ids que ya se eligieron. */
	@EntityGraph(attributePaths = "sala")
	List<Produccion> findByIdIn(Collection<Long> ids);
}
