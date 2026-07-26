package io.github.ramiroabadie.backend.social.internal.reporte;

import java.util.List;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51). Dos métodos
 * derivados y uno escrito a mano, que es el de resolver — necesita traerse las filas bloqueadas.
 */
interface ReporteRepository extends JpaRepository<ReporteDeResenia, Long> {

	/** Si esta persona ya tiene un aviso pendiente sobre esta reseña, el segundo no agrega nada. */
	boolean existsByReseniaIdAndReportanteIdAndEstado(Long reseniaId, Long reportanteId, EstadoReporte estado);

	/**
	 * La cola, de lo más viejo a lo más nuevo. Sin paginar a propósito, igual que la de sugerencias
	 * (D69): el día que no entre en una pantalla, ese día se pagina.
	 */
	List<ReporteDeResenia> findByEstadoOrderByCreadoEnAscIdAsc(EstadoReporte estado);

	/**
	 * Todos los reportes pendientes de la reseña a la que apunta este reporte, con las filas
	 * tomadas hasta el fin de la transacción ({@code select ... for update}).
	 *
	 * <p>Trae el conjunto y no la fila porque el veredicto es sobre el contenido: si tres personas
	 * reportaron la misma reseña, resolverla saca los tres de la cola (D70). Que el bloqueo lo tome
	 * la misma consulta que arma el conjunto es lo que evita el interbloqueo de tomarlos de a uno:
	 * dos resoluciones sobre la misma reseña piden las mismas filas en el mismo orden.</p>
	 *
	 * <p>Sin el bloqueo, resolver es leer-comprobar-escribir: dos clics que se solapan leen los dos
	 * "pendiente", pasan los dos y escriben los dos, y una reseña puede terminar borrada y con sus
	 * reportes marcados como desestimados. Con él, el segundo espera, vuelve a leer —y Postgres
	 * descarta las filas que ya no cumplen la condición— y se encuentra con la cola vacía, que es
	 * el 409 que promete D70. Es pesimista y no optimista por lo mismo que en D69: quien llega
	 * segundo no tiene nada que reintentar.</p>
	 *
	 * <p>El reporte que se pide puede no estar en el resultado: si ya se resolvió, lo único que
	 * puede volver son otros pendientes de la misma reseña, llegados después. Por eso quien llama
	 * comprueba que esté, en vez de confiar en que la lista no sea vacía.</p>
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select r from ReporteDeResenia r
			where r.estado = :pendiente
			  and r.reseniaId = (select otro.reseniaId from ReporteDeResenia otro where otro.id = :id)
			order by r.id
			""")
	List<ReporteDeResenia> paraResolver(@Param("id") Long reporteId, @Param("pendiente") EstadoReporte pendiente);
}
