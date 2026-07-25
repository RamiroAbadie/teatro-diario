package io.github.ramiroabadie.backend.catalogo.internal.sugerencia;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import io.github.ramiroabadie.backend.catalogo.NuevaSugerencia;
import io.github.ramiroabadie.backend.catalogo.SugerenciaPropuesta;
import io.github.ramiroabadie.backend.catalogo.SugerenciaResueltaException;

/**
 * Una obra que alguien pidió que entre al catálogo (D7/D24). No es una producción a medio hacer:
 * es lo que un usuario tipeó, tal cual, y por eso la sala y el elenco son texto libre y ninguno de
 * los dos apunta a las tablas del catálogo. La ficha la escribe después el admin con el formulario
 * de HU-20, y recién ahí hay entidades de verdad.
 *
 * <p>{@code sugerenteId} es un id opaco sin clave foránea, como en Diario y en Social (D30): la
 * cuenta es de Identidad. Si esa cuenta se borra, la sugerencia sobrevive sin dueño — la cola la
 * muestra igual, porque lo que el admin necesita resolver es la propuesta, no quién la hizo.</p>
 *
 * <p>{@code produccionId} tampoco es una clave foránea, y acá sí es del mismo módulo: es un
 * apunte histórico de en qué terminó la sugerencia, y si esa ficha se borra o se fusiona después
 * (D63) el apunte queda viejo sin romper nada. Ninguna consulta lo sigue.</p>
 */
@Entity
@Table(name = "sugerencia")
class Sugerencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 250)
	private String titulo;

	@Column(length = 250)
	private String sala;

	private Integer anio;

	@Column(length = 1000)
	private String elenco;

	@Column(length = 1000)
	private String comentario;

	@Column(name = "sugerente_id", nullable = false, updatable = false)
	private Long sugerenteId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoSugerencia estado;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	@Column(name = "resuelto_en")
	private Instant resueltoEn;

	@Column(name = "motivo_rechazo", length = 500)
	private String motivoRechazo;

	@Column(name = "produccion_id")
	private Long produccionId;

	protected Sugerencia() {
		// requerido por JPA
	}

	Sugerencia(Long sugerenteId, NuevaSugerencia nueva) {
		this.sugerenteId = sugerenteId;
		this.titulo = nueva.titulo();
		this.sala = nueva.sala();
		this.anio = nueva.anio();
		this.elenco = nueva.elenco();
		this.comentario = nueva.comentario();
		this.estado = EstadoSugerencia.PENDIENTE;
		this.creadoEn = Instant.now();
	}

	void aprobar(Long produccionId) {
		exigirPendiente();
		this.estado = EstadoSugerencia.APROBADA;
		this.produccionId = produccionId;
		this.resueltoEn = Instant.now();
	}

	void rechazar(String motivo) {
		exigirPendiente();
		this.estado = EstadoSugerencia.RECHAZADA;
		this.motivoRechazo = motivo;
		this.resueltoEn = Instant.now();
	}

	/**
	 * Salir de la cola pasa una sola vez. Que la regla viva en la entidad y no en el servicio es lo
	 * que hace que las dos salidas —aprobar y rechazar— no puedan olvidarse de comprobarlo.
	 */
	private void exigirPendiente() {
		if (estado != EstadoSugerencia.PENDIENTE) {
			throw new SugerenciaResueltaException(id);
		}
	}

	SugerenciaPropuesta aPropuesta() {
		return new SugerenciaPropuesta(id, titulo, sala, anio, elenco, comentario, sugerenteId, creadoEn);
	}
}
