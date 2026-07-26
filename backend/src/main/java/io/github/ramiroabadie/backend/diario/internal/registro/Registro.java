package io.github.ramiroabadie.backend.diario.internal.registro;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import io.github.ramiroabadie.backend.diario.GranularidadFecha;

/**
 * El acto central del producto: "vi esta producción" (D18). Rating y reseña son parte del
 * registro y no entidades aparte — no existe puntuar sin registrar.
 *
 * <p>{@code usuarioId} y {@code produccionId} son ids opacos, sin clave foránea (D30): el
 * usuario es de Identidad y la producción de Catálogo, y una FK sería una tabla de otro módulo
 * metiéndose en el esquema de este. La contra es que borrar una producción deja registros
 * apuntando a la nada, y por eso el registro guarda además una copia del título (D62): el
 * historial es de la persona y tiene que seguir diciendo qué vio aunque el catálogo cambie de
 * opinión. La copia es un respaldo, no la verdad — mientras la ficha exista, el título que se
 * muestra es el suyo.</p>
 *
 * <p>La fecha viene normalizada al comienzo de su período y {@code granularidad} dice hasta
 * dónde leerla (MD-1). Guardarla así hace que el "último rating" de D20 se pueda decidir en la
 * base; el orden del diario (MD-2) necesita además la precisión y se arma en el servicio.</p>
 */
@Entity
@Table(name = "registro", indexes = {
		@Index(name = "idx_registro_usuario", columnList = "usuario_id"),
		@Index(name = "idx_registro_produccion", columnList = "produccion_id")
})
class Registro {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id", nullable = false, updatable = false)
	private Long usuarioId;

	@Column(name = "produccion_id", nullable = false)
	private Long produccionId;

	/**
	 * Copia del título al momento de registrar, para que el historial sobreviva al borrado de la
	 * ficha (D62). Sin {@code nullable = false} a propósito: mientras el esquema lo genere
	 * Hibernate con {@code ddl-auto: update} (D53), una columna obligatoria nueva no puede
	 * agregarse a una tabla que ya tiene filas. Cuando entre Flyway (Fase 5) el baseline la
	 * puede endurecer.
	 */
	@Column(name = "titulo_produccion")
	private String tituloProduccion;

	/** Nula cuando no se acuerda cuándo fue (MD-1); las demás granularidades la exigen. */
	private LocalDate fecha;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private GranularidadFecha granularidad;

	/** Entero de 1 a 10 (D9), opcional: registrar sin puntuar es válido. */
	private Integer rating;

	@Column(length = 5000)
	private String resenia;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	protected Registro() {
		// requerido por JPA
	}

	Registro(Long usuarioId, Long produccionId, String tituloProduccion, LocalDate fecha,
			GranularidadFecha granularidad, Integer rating, String resenia) {
		this.usuarioId = usuarioId;
		this.creadoEn = Instant.now();
		actualizar(produccionId, tituloProduccion, fecha, granularidad, rating, resenia);
	}

	/** Editar es reemplazar el gesto entero, producción incluida (HU-11). */
	final void actualizar(Long produccionId, String tituloProduccion, LocalDate fecha,
			GranularidadFecha granularidad, Integer rating, String resenia) {
		this.produccionId = produccionId;
		this.tituloProduccion = tituloProduccion;
		this.fecha = fecha;
		this.granularidad = granularidad;
		this.rating = rating;
		this.resenia = resenia;
	}

	/**
	 * La moderación borra el texto y deja la salida al teatro (HU-22, D70): el registro sigue
	 * contando qué vio esa persona y con qué puntaje, así que el promedio de D20 no se entera.
	 * Después de esto el registro deja de ser una reseña: sale de las reseñas de la ficha y sigue
	 * en el feed sin texto, porque el feed son los registros y no las reseñas (D66).
	 */
	void borrarResenia() {
		this.resenia = null;
	}

	boolean esDe(Long usuarioId) {
		return this.usuarioId.equals(usuarioId);
	}

	Long getId() {
		return id;
	}

	Long getUsuarioId() {
		return usuarioId;
	}

	Long getProduccionId() {
		return produccionId;
	}

	String getTituloProduccion() {
		return tituloProduccion;
	}

	LocalDate getFecha() {
		return fecha;
	}

	GranularidadFecha getGranularidad() {
		return granularidad;
	}

	Integer getRating() {
		return rating;
	}

	String getResenia() {
		return resenia;
	}

	Instant getCreadoEn() {
		return creadoEn;
	}
}
