package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.ramiroabadie.backend.catalogo.CatalogoProducciones;
import io.github.ramiroabadie.backend.catalogo.ProduccionBasica;
import io.github.ramiroabadie.backend.catalogo.internal.persona.PersonaResponse;
import io.github.ramiroabadie.backend.catalogo.internal.persona.PersonaService;
import io.github.ramiroabadie.backend.catalogo.internal.sala.SalaResponse;
import io.github.ramiroabadie.backend.catalogo.internal.sala.SalaService;

/**
 * Casos de uso de lectura pública del catálogo (HU-04/05/06, más la página de sala que
 * USER_FLOWS.md deja adentro de HU-04 y no tiene historia propia). Separado de
 * {@code ProduccionService} a propósito: son las mismas entidades pero la otra cara — sin
 * login (D21) y de solo lectura, mientras que la de escritura queda solo-admin (D7).
 *
 * <p>La ficha no trae promedio ni reseñas: eso es del módulo Diario y se sirve aparte, en
 * {@code /api/producciones/{id}/opiniones}, compuesto en la capa de aplicación (D20, HU-14).
 * Catálogo sigue sin depender de nadie.</p>
 *
 * <p>Es también el adaptador de la interfaz pública del módulo ({@link CatalogoProducciones}):
 * la cara de lectura del catálogo es la misma para HTTP que para Diario, cambia el formato.</p>
 */
@Service
class CatalogoPublicoService implements CatalogoProducciones {

	private final ProduccionRepository producciones;

	private final ParticipacionRepository participaciones;

	private final PersonaService personas;

	private final SalaService salas;

	/** La cara de escritura, para el único caso en que la interfaz pública escribe: la fusión. */
	private final ProduccionService escrituras;

	CatalogoPublicoService(ProduccionRepository producciones, ParticipacionRepository participaciones,
			PersonaService personas, SalaService salas, ProduccionService escrituras) {
		this.producciones = producciones;
		this.participaciones = participaciones;
		this.personas = personas;
		this.salas = salas;
		this.escrituras = escrituras;
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

	/**
	 * Página de sala: el destino del "sala con link" de la ficha (HU-04). Una sala sin nada
	 * en cartel devuelve la lista vacía: la sala existe igual, y la ficha vieja que la
	 * enlaza tiene que llegar a algún lado.
	 */
	@Transactional(readOnly = true)
	public SalaPublicaResponse sala(Long salaId) {
		SalaResponse sala = salas.obtener(salaId);
		return SalaPublicaResponse.desde(sala,
				producciones.findBySalaIdAndEstadoOrderByTituloAsc(salaId, EstadoProduccion.EN_CARTEL));
	}

	/**
	 * Interfaz pública del módulo: la usa Diario para validar que la producción que se registra
	 * existe (HU-09), que es la única dependencia módulo-a-módulo del sistema.
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<ProduccionBasica> porId(Long id) {
		return producciones.findById(id).map(CatalogoPublicoService::basica);
	}

	/** El diario entero resuelve sus títulos con esta sola consulta. */
	@Override
	@Transactional(readOnly = true)
	public Map<Long, ProduccionBasica> porIds(Collection<Long> ids) {
		Map<Long, ProduccionBasica> porId = new LinkedHashMap<>();
		for (Produccion produccion : producciones.findAllById(ids)) {
			porId.put(produccion.getId(), basica(produccion));
		}
		return porId;
	}

	/**
	 * El borrado de la interfaz pública es el mismo del panel, sin nada nuevo: quien decide si
	 * corresponde borrar es el admin, acá o desde {@code ProduccionController}.
	 */
	@Override
	@Transactional
	public void borrar(Long id) {
		escrituras.borrar(id);
	}

	private static ProduccionBasica basica(Produccion produccion) {
		return new ProduccionBasica(produccion.getId(), produccion.getTitulo());
	}

	private List<ProduccionResumenResponse> resumir(List<Produccion> producciones, EstadoProduccion estado) {
		return producciones.stream()
				.filter(produccion -> produccion.getEstado() == estado)
				.map(ProduccionResumenResponse::desde)
				.toList();
	}
}
