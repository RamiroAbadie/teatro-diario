package io.github.ramiroabadie.backend.catalogo.internal.sala;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Espacio teatral físico de CABA (D15). Catálogo chico y estable, curado por el admin.
 * Atributos deliberadamente mínimos (MODO ESENCIAL): nombre obligatorio y un campo
 * {@code complejo} opcional para agrupar salas de un mismo edificio (San Martín,
 * Paseo La Plaza — P9 del modelo). Interna del módulo Catálogo.
 */
@Entity
@Table(name = "sala")
public class Sala {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nombre;

	@Column
	private String complejo;

	protected Sala() {
		// requerido por JPA
	}

	Sala(String nombre, String complejo) {
		this.nombre = nombre;
		this.complejo = complejo;
	}

	void actualizar(String nombre, String complejo) {
		this.nombre = nombre;
		this.complejo = complejo;
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getComplejo() {
		return complejo;
	}
}
