package io.github.ramiroabadie.backend.diario.internal.registro;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
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
