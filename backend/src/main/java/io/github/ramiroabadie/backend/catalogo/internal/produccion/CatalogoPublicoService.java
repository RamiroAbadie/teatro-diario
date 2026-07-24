package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

	/**
	 * Los resultados que entran en una pantalla de celular sin scrollear (P8: el gesto de
	 * registro compite contra subir una story). Si lo que buscás no está en los diez primeros,
	 * la respuesta útil es escribir mejor la consulta, no paginar.
	 */
	private static final int LIMITE_DE_RESULTADOS = 10;

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
	 * Búsqueda de producciones (HU-07) y, con la misma consulta, el autocompletado del gesto de
	 * registro (HU-09): son la misma pregunta hecha desde dos pantallas.
	 *
	 * <p>La consulta vacía no devuelve el catálogo entero: devuelve nada. Un campo de búsqueda
	 * recién abierto no es un pedido de "mostrame todo", y "en cartel" ya es esa pantalla.</p>
	 *
	 * <p>Sin resultados devuelve la lista vacía y no un 404: que no haya nada es una respuesta
	 * válida de la búsqueda, y es el estado que el frontend convierte en el camino a sugerir
	 * la producción faltante (HU-08, Fase 3).</p>
	 */
	@Transactional(readOnly = true)
	public List<ProduccionResumenResponse> buscar(String texto) {
		String consulta = texto == null ? "" : texto.trim();
		if (consulta.isEmpty()) {
			return List.of();
		}
		List<Long> ids = producciones.buscarIdsPorTitulo(consulta, LIMITE_DE_RESULTADOS);
		if (ids.isEmpty()) {
			return List.of();
		}
		Map<Long, Produccion> porId = new LinkedHashMap<>();
		for (Produccion produccion : producciones.findByIdIn(ids)) {
			porId.put(produccion.getId(), produccion);
		}
		// El orden de relevancia es el de los ids: la segunda consulta solo trae las filas.
		return ids.stream().map(porId::get).filter(Objects::nonNull).map(ProduccionResumenResponse::desde).toList();
	}

	/**
	 * Búsqueda de personas (HU-07). Pasa por acá y no directo del controlador al servicio de
	 * personas por lo mismo que la página de artista: la cara de lectura del catálogo es una,
	 * aunque los datos vengan de dos paquetes del módulo.
	 */
	@Transactional(readOnly = true)
	public List<PersonaResponse> buscarPersonas(String texto) {
		return personas.buscar(texto);
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
