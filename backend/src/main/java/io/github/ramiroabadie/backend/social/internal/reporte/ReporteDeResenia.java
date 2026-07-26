package io.github.ramiroabadie.backend.social.internal.reporte;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import io.github.ramiroabadie.backend.social.ReporteRecibido;
import io.github.ramiroabadie.backend.social.ReporteResueltoException;

/**
 * Alguien avisó que una reseña es ofensiva (D40). Como el like, une una cuenta con un texto que
 * este módulo no lee: las dos puntas son ids opacos sin clave foránea (D30).
 *
 * <p>A diferencia del like, tiene ciclo de vida —entra a una cola y sale con un veredicto— y por
 * eso no lleva índice único sobre el par: prohibir el segundo reporte de la misma persona sobre la
 * misma reseña para siempre dejaría afuera el caso legítimo de una reseña editada después de que
 * el primero se desestimara (HU-11). Que no se acumulen pendientes repetidos lo resuelve el
 * servicio, que mira si ya hay uno antes de guardar.</p>
 *
 * <p>Si la reseña se borra o el registro deja de tener texto, el reporte sigue en la cola: lo que
 * el admin tiene que hacer es sacarlo de ahí, y la cola lo muestra sin texto para que se entienda
 * que ya no hay nada que borrar.</p>
 */
@Entity
@Table(name = "reporte_resenia", indexes = {
		@Index(name = "idx_reporte_resenia", columnList = "resenia_id"),
		@Index(name = "idx_reporte_estado", columnList = "estado")
})
class ReporteDeResenia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "resenia_id", nullable = false, updatable = false)
	private Long reseniaId;

	@Column(name = "reportante_id", nullable = false, updatable = false)
	private Long reportanteId;

	/** Opcional (HU-18): el aviso vale igual sin explicación. */
	@Column(length = 500)
	private String motivo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoReporte estado;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	@Column(name = "resuelto_en")
	private Instant resueltoEn;

	protected ReporteDeResenia() {
		// requerido por JPA
	}

	ReporteDeResenia(Long reseniaId, Long reportanteId, String motivo) {
		this.reseniaId = reseniaId;
		this.reportanteId = reportanteId;
		this.motivo = motivo;
		this.estado = EstadoReporte.PENDIENTE;
		this.creadoEn = Instant.now();
	}

	/**
	 * Salir de la cola pasa una sola vez. La regla vive acá y no en el servicio por lo mismo que
	 * en la sugerencia (D69): las dos salidas comparten el camino y ninguna puede olvidarse.
	 */
	void resolver(EstadoReporte veredicto) {
		if (estado != EstadoReporte.PENDIENTE) {
			throw new ReporteResueltoException(id);
		}
		this.estado = veredicto;
		this.resueltoEn = Instant.now();
	}

	Long getId() {
		return id;
	}

	Long getReseniaId() {
		return reseniaId;
	}

	ReporteRecibido aRecibido() {
		return new ReporteRecibido(id, reseniaId, reportanteId, motivo, creadoEn);
	}
}
