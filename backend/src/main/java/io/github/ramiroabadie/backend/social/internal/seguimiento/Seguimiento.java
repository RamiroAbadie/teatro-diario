package io.github.ramiroabadie.backend.social.internal.seguimiento;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Una arista del grafo: alguien sigue a alguien (D3). Sin aprobación y sin reciprocidad — que dos
 * personas se sigan son dos filas, no una.
 *
 * <p>Las dos puntas son ids opacos, sin clave foránea (D30): las cuentas son de Identidad y una
 * FK sería la tabla de otro módulo metiéndose en el esquema de este. La contra —una cuenta
 * borrada deja seguimientos apuntando a la nada— no duele como en el diario (D62): un seguidor
 * fantasma solo infla un contador, no rompe un historial. Se limpia con un {@code DELETE} a mano
 * el día que exista borrar cuentas, que en el MVP no existe.</p>
 *
 * <p>El índice único es el que sostiene la idempotencia de {@code seguir}: dos clics simultáneos
 * en el botón terminan igual que uno, porque el segundo no entra.</p>
 */
@Entity
@Table(name = "seguimiento",
		uniqueConstraints = @UniqueConstraint(name = "uk_seguimiento_par",
				columnNames = { "seguidor_id", "seguido_id" }),
		indexes = @Index(name = "idx_seguimiento_seguido", columnList = "seguido_id"))
class Seguimiento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "seguidor_id", nullable = false, updatable = false)
	private Long seguidorId;

	@Column(name = "seguido_id", nullable = false, updatable = false)
	private Long seguidoId;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	protected Seguimiento() {
		// requerido por JPA
	}

	Seguimiento(Long seguidorId, Long seguidoId) {
		this.seguidorId = seguidorId;
		this.seguidoId = seguidoId;
		this.creadoEn = Instant.now();
	}
}
