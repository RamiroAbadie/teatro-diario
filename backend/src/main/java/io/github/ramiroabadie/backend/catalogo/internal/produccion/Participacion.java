package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.github.ramiroabadie.backend.catalogo.internal.persona.Persona;

/**
 * Vínculo de una persona con una producción en un rol (D14). Una misma persona puede tener
 * varios roles en la misma producción (D17: actúa y dirige) — de ahí que la unicidad sea
 * por la terna, no por el par. Vive con la producción, que es su dueña.
 */
@Entity
@Table(name = "participacion",
		uniqueConstraints = @UniqueConstraint(columnNames = { "produccion_id", "persona_id", "rol" }))
class Participacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "produccion_id", nullable = false)
	private Produccion produccion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "persona_id", nullable = false)
	private Persona persona;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RolParticipacion rol;

	protected Participacion() {
		// requerido por JPA
	}

	Participacion(Produccion produccion, Persona persona, RolParticipacion rol) {
		this.produccion = produccion;
		this.persona = persona;
		this.rol = rol;
	}

	Long getId() {
		return id;
	}

	Persona getPersona() {
		return persona;
	}

	RolParticipacion getRol() {
		return rol;
	}
}
