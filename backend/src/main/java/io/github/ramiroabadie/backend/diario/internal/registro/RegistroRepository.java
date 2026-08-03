package io.github.ramiroabadie.backend.diario.internal.registro;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51). La única query
 * escrita a mano de todo el proyecto es la del promedio, y está escrita a mano porque D20 no se
 * puede expresar con un método derivado.
 */
interface RegistroRepository extends JpaRepository<Registro, Long> {

	/**
	 * El diario (HU-12). Sin {@code order by}: el orden que pide MD-2 no lo puede expresar esta
	 * consulta. Como la fecha se guarda al comienzo de su período (D59), el 1 de enero de 2023 y
	 * "2023" a secas son los dos el 2023-01-01, y ahí desempata la precisión de la granularidad
	 * —primero el día exacto, después el mes, después el año—, que guardada como texto ordena
	 * alfabéticamente ('ANIO' antes que 'DIA') y no sirve. El orden vive en el servicio, que es
	 * donde se puede escribir la regla completa y leerla de un vistazo.
	 */
	List<Registro> findByUsuarioIdAndFechaIsNotNull(Long usuarioId);

	/** La sección de los sin fecha, que va aparte al final (MD-2). */
	List<Registro> findByUsuarioIdAndFechaIsNull(Long usuarioId);

	/** Las reseñas de la ficha (HU-14): las más nuevas primero. */
	List<Registro> findByProduccionIdAndReseniaIsNotNullOrderByCreadoEnDesc(Long produccionId);

	/**
	 * Cuántas veces alguien registró esa producción (HU-10, D76). Cuenta registros y no
	 * producciones distintas: el re-visto (D19) es justamente lo que este número muestra.
	 */
	long countByUsuarioIdAndProduccionId(Long usuarioId, Long produccionId);

	/**
	 * Quién escribió esa reseña, si ese id es una reseña y no un registro sin texto o un número
	 * inventado (HU-17, HU-18). Devuelve la columna sola y no la fila: lo único que se pregunta es
	 * de quién es.
	 */
	@Query("select r.usuarioId from Registro r where r.id = :id and r.resenia is not null")
	Optional<Long> autorDeResenia(@Param("id") Long id);

	/** Los registros de una lista de ids: el contexto de la cola de reportes (HU-22). */
	List<Registro> findByIdIn(Collection<Long> ids);

	/** La primera página del feed de seguidos (HU-16). */
	List<Registro> findByUsuarioIdInOrderByCreadoEnDescIdDesc(Collection<Long> usuarioIds, Limit limite);

	/** La primera página del feed global, el fallback de quien no sigue a nadie (D22). */
	List<Registro> findAllByOrderByCreadoEnDescIdDesc(Limit limite);

	/**
	 * Las páginas siguientes del feed de seguidos: lo cargado antes del último registro que se
	 * entregó (D66). Escrita a mano porque el corte del cursor es sobre dos columnas —instante e
	 * id, para que dos cargas en el mismo instante no se pisen— y un método derivado no puede
	 * expresar el paréntesis que eso necesita.
	 */
	@Query("""
			select r from Registro r
			where r.usuarioId in :usuarioIds
			  and (r.creadoEn < :creadoEn or (r.creadoEn = :creadoEn and r.id < :registroId))
			order by r.creadoEn desc, r.id desc
			""")
	List<Registro> actividadDesde(@Param("usuarioIds") Collection<Long> usuarioIds,
			@Param("creadoEn") Instant creadoEn, @Param("registroId") Long registroId, Limit limite);

	/** Lo mismo, sin filtrar por autor: las páginas siguientes del feed global. */
	@Query("""
			select r from Registro r
			where r.creadoEn < :creadoEn or (r.creadoEn = :creadoEn and r.id < :registroId)
			order by r.creadoEn desc, r.id desc
			""")
	List<Registro> actividadGlobalDesde(@Param("creadoEn") Instant creadoEn,
			@Param("registroId") Long registroId, Limit limite);

	/**
	 * La mudanza de la fusión (D63), en una sola sentencia: los registros de la ficha duplicada
	 * pasan a la canónica y se llevan el título nuevo, porque la copia que guardaban era la de la
	 * ficha que está por desaparecer.
	 */
	@Modifying(clearAutomatically = true)
	@Query("""
			update Registro r set r.produccionId = :destinoId, r.tituloProduccion = :tituloDestino
			where r.produccionId = :origenId
			""")
	int reasignar(@Param("origenId") Long origenId, @Param("destinoId") Long destinoId,
			@Param("tituloDestino") String tituloDestino);

	/**
	 * ⚠️ El promedio de D20: NO es {@code avg(rating)} sobre todos los registros. Con re-visto
	 * (D19) una persona puede haber puntuado la misma obra varias veces, y solo cuenta la última
	 * — la de la función más reciente, no la de la carga más reciente: quien sube hoy una salida
	 * de 2019 no pisa con eso lo que opinó el mes pasado.
	 *
	 * <p>{@code DISTINCT ON} es de Postgres y elige, para cada {@code usuario_id}, la primera
	 * fila del orden que sigue: fecha más nueva, los sin fecha al fondo (solo ganan si esa
	 * persona no fechó ninguna), y a igual fecha la carga más reciente. La base es Postgres y no
	 * va a dejar de serlo (D42); la alternativa portable son tres queries o un {@code group by}
	 * con subconsulta correlacionada, más lentos y menos legibles que esto.</p>
	 *
	 * <p>{@code cantidad} son personas que puntuaron, no registros: es el número que acompaña al
	 * promedio en la ficha (HU-14).</p>
	 */
	@Query(value = """
			SELECT avg(ultimos.rating)::float8 AS promedio, count(*) AS cantidad
			FROM (SELECT DISTINCT ON (usuario_id) rating
			      FROM registro
			      WHERE produccion_id = :produccionId AND rating IS NOT NULL
			      ORDER BY usuario_id, fecha DESC NULLS LAST, creado_en DESC, id DESC) AS ultimos
			""", nativeQuery = true)
	Valoracion valoracionDe(@Param("produccionId") Long produccionId);

	/** Proyección de la query del promedio. Sin ratings devuelve {@code null} y cero. */
	interface Valoracion {

		Double getPromedio();

		long getCantidad();
	}
}
