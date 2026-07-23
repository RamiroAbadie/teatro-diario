package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.ramiroabadie.backend.catalogo.internal.persona.PersonaResponse;
import io.github.ramiroabadie.backend.catalogo.internal.persona.PersonaService;

/**
 * Casos de uso de lectura pública del catálogo (HU-04/05/06). Separado de
 * {@code ProduccionService} a propósito: son las mismas entidades pero la otra cara — sin
 * login (D21) y de solo lectura, mientras que la de escritura queda solo-admin (D7).
 *
 * <p>La ficha todavía no trae promedio ni reseñas: eso es del módulo Diario, que no existe
 * hasta la Fase 2, y se va a componer en la capa de aplicación (D20, HU-14) sin que Catálogo
 * dependa de nadie.</p>
 */
@Service
class CatalogoPublicoService {

	private final ProduccionRepository producciones;

	private final ParticipacionRepository participaciones;

	private final PersonaService personas;

	CatalogoPublicoService(ProduccionRepository producciones, ParticipacionRepository participaciones,
			PersonaService personas) {
		this.producciones = producciones;
		this.participaciones = participaciones;
		this.personas = personas;
	}

	/**
	 * Ficha pública (HU-04). Las producciones cerradas también se muestran: el diario se
	 * escribe hacia atrás (D24) y una ficha vieja tiene que seguir teniendo su link vivo.
	 */
	@Transactional(readOnly = true)
	public ProduccionResponse ficha(Long id) {
		return ProduccionResponse.desde(
				producciones.findById(id).orElseThrow(() -> new ProduccionNoEncontradaException(id)));
	}

	/** Qué se puede ir a ver ahora, más lo que está por estrenar (HU-06). */
	@Transactional(readOnly = true)
	public EnCartelResponse enCartel() {
		List<Produccion> vigentes = producciones.findByEstadoInOrderByTituloAsc(
				List.of(EstadoProduccion.EN_CARTEL, EstadoProduccion.PROXIMAMENTE));
		return new EnCartelResponse(
				resumir(vigentes, EstadoProduccion.EN_CARTEL),
				resumir(vigentes, EstadoProduccion.PROXIMAMENTE));
	}

	/**
	 * Página de artista (HU-05). Vive acá y no en el paquete {@code persona} porque lo que la
	 * página muestra son participaciones, que son de la producción.
	 */
	@Transactional(readOnly = true)
	public ArtistaResponse artista(Long personaId) {
		PersonaResponse persona = personas.obtener(personaId);
		return ArtistaResponse.desde(persona, participaciones.findByPersonaIdOrderByRolAscProduccionTituloAsc(personaId));
	}

	private List<ProduccionResumenResponse> resumir(List<Produccion> producciones, EstadoProduccion estado) {
		return producciones.stream()
				.filter(produccion -> produccion.getEstado() == estado)
				.map(ProduccionResumenResponse::desde)
				.toList();
	}
}
