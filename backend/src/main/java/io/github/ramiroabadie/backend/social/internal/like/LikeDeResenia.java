package io.github.ramiroabadie.backend.social.internal.like;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Alguien destacó una reseña (D11). La tabla se llama {@code like_resenia} y no {@code like}
 * porque {@code LIKE} es palabra reservada de SQL y una tabla que hay que citar en cada consulta
 * es una trampa para el día que alguien escriba una a mano.
 *
 * <p>Las dos puntas son ids opacos, sin clave foránea, por el mismo motivo que en el grafo (D30):
 * la cuenta es de Identidad y la reseña es de Diario, y una FK sería el esquema de este módulo
 * atado a tablas ajenas. La contra es que borrar una reseña (HU-11) deja likes apuntando a la
 * nada: no se muestran en ningún lado —el conteo se pide siempre para reseñas que existen— y no
 * pueden reaparecer pegados a otra, porque los ids de registro los da una secuencia que no
 * reutiliza números. Se limpian con un {@code DELETE} el día que molesten.</p>
 *
 * <p>El índice único sostiene la idempotencia de {@code darLike}: dos clics simultáneos terminan
 * igual que uno, porque el segundo no entra. Con {@code resenia_id} adelante sirve además a las
 * dos lecturas —contar los likes de una lista de reseñas y ver cuáles tienen el mío—, así que no
 * hace falta un segundo índice.</p>
 */
@Entity
@Table(name = "like_resenia",
		uniqueConstraints = @UniqueConstraint(name = "uk_like_resenia_par",
				columnNames = { "resenia_id", "usuario_id" }))
class LikeDeResenia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "resenia_id", nullable = false, updatable = false)
	private Long reseniaId;

	@Column(name = "usuario_id", nullable = false, updatable = false)
	private Long usuarioId;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	protected LikeDeResenia() {
		// requerido por JPA
	}

	LikeDeResenia(Long reseniaId, Long usuarioId) {
		this.reseniaId = reseniaId;
		this.usuarioId = usuarioId;
		this.creadoEn = Instant.now();
	}
}
