package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import io.github.ramiroabadie.backend.catalogo.internal.persona.Persona;
import io.github.ramiroabadie.backend.catalogo.internal.sala.Sala;

/**
 * La unidad central del dominio (D12): un texto teatral puesto en escena por una dirección
 * y un elenco determinados. Dos puestas del mismo texto son dos producciones; el cambio de
 * sala no crea una nueva.
 *
 * <p>{@code obraOriginal} y {@code autorOriginal} son texto plano a propósito (D13): la
 * entidad Obra no existe en el MVP y el dato queda listo para normalizarse a futuro. La sala
 * es opcional (hay producciones sin sala asignada, o itinerantes). El afiche llega en la
 * fase de deploy, no acá.</p>
 *
 * <p>Las participaciones son parte de la producción: se crean, reemplazan y borran con ella
 * ({@code orphanRemoval}). La Persona, en cambio, es catálogo reutilizable y sobrevive.</p>
 */
@Entity
@Table(name = "produccion")
class Produccion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String titulo;

	@Column(length = 5000)
	private String sinopsis;

	@Column(name = "obra_original")
	private String obraOriginal;

	@Column(name = "autor_original")
	private String autorOriginal;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoProduccion estado;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sala_id")
	private Sala sala;

	@OneToMany(mappedBy = "produccion", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("rol ASC, id ASC")
	private List<Participacion> participaciones = new ArrayList<>();

	protected Produccion() {
		// requerido por JPA
	}

	Produccion(String titulo, String sinopsis, String obraOriginal, String autorOriginal,
			EstadoProduccion estado, Sala sala) {
		actualizar(titulo, sinopsis, obraOriginal, autorOriginal, estado, sala);
	}

	final void actualizar(String titulo, String sinopsis, String obraOriginal, String autorOriginal,
			EstadoProduccion estado, Sala sala) {
		this.titulo = titulo;
		this.sinopsis = sinopsis;
		this.obraOriginal = obraOriginal;
		this.autorOriginal = autorOriginal;
		this.estado = estado;
		this.sala = sala;
	}

	void cambiarEstado(EstadoProduccion estado) {
		this.estado = estado;
	}

	/** La edición de una ficha manda la lista completa de participaciones: se reemplaza entera. */
	void limpiarParticipaciones() {
		participaciones.clear();
	}

	void agregarParticipacion(Persona persona, RolParticipacion rol) {
		participaciones.add(new Participacion(this, persona, rol));
	}

	Long getId() {
		return id;
	}

	String getTitulo() {
		return titulo;
	}

	String getSinopsis() {
		return sinopsis;
	}

	String getObraOriginal() {
		return obraOriginal;
	}

	String getAutorOriginal() {
		return autorOriginal;
	}

	EstadoProduccion getEstado() {
		return estado;
	}

	Sala getSala() {
		return sala;
	}

	List<Participacion> getParticipaciones() {
		return participaciones;
	}
}
