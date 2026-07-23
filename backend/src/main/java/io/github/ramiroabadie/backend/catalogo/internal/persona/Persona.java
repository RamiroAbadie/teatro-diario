package io.github.ramiroabadie.backend.catalogo.internal.persona;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Artista reutilizable entre producciones (D14). Habilita la página de artista.
 * Atributo deliberadamente mínimo (MODO ESENCIAL): nombre y nada más en el MVP — sin
 * foto, sin bio. Duplicados y homónimos son deuda de datos que corrige el admin a mano.
 * Interna del módulo Catálogo.
 */
@Entity
@Table(name = "persona")
class Persona {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nombre;

	protected Persona() {
		// requerido por JPA
	}

	Persona(String nombre) {
		this.nombre = nombre;
	}

	void actualizar(String nombre) {
		this.nombre = nombre;
	}

	Long getId() {
		return id;
	}

	String getNombre() {
		return nombre;
	}
}
