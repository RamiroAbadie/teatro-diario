package io.github.ramiroabadie.backend.diario;

import java.util.Collection;
import java.util.List;

/**
 * La interfaz pública del módulo Diario: el corazón del producto (MODULE_MAP). Casos de uso, no
 * endpoints — los invoca hoy la capa de aplicación por HTTP y mañana lo que haga falta, sin
 * que el núcleo se entere (D34).
 *
 * <p>El {@code usuarioId} llega siempre de afuera: Diario no sabe quién está logueado ni tiene
 * cómo averiguarlo (no depende de Identidad, D30). Sí decide de quién es cada registro, que es
 * distinto: la autorización de dueño la resuelve acá porque los datos son suyos, y quien llama
 * solo tiene que decir con la verdad a nombre de quién viene.</p>
 */
public interface Diario {

	/**
	 * El gesto de registro (HU-09). Registrar la misma producción varias veces es válido y
	 * esperado: es el re-visto (D19), no un duplicado a evitar.
	 *
	 * @throws ProduccionInexistenteException si la producción no está en el catálogo
	 * @throws RegistroInvalidoException si la fecha, la granularidad o el rating no cierran
	 */
	RegistroDeDiario registrar(Long usuarioId, NuevoRegistro nuevo);

	/**
	 * Corrige un registro propio (HU-11). Reemplaza el gesto entero, incluida la producción:
	 * equivocarse de obra al elegirla del buscador es el error más probable de todos.
	 *
	 * @throws RegistroAjenoException si el registro es de otra persona
	 */
	RegistroDeDiario editar(Long usuarioId, Long registroId, NuevoRegistro cambios);

	/**
	 * Borra un registro propio (HU-11). El promedio de la producción se recalcula solo: no hay
	 * nada desnormalizado que mantener, {@link #opinionesDe} lo calcula al leer.
	 */
	void borrar(Long usuarioId, Long registroId);

	/**
	 * Muda todos los registros de una producción a otra y devuelve cuántos movió (D63). La usa la
	 * fusión de fichas duplicadas: los registros conservan su id —así los likes y reportes que
	 * lleguen en la Fase 3 no se pierden—, su fecha, su puntaje y su reseña, y solo cambian de
	 * obra. Si alguien había registrado las dos fichas, sus registros quedan como re-vistos de la
	 * misma producción, que es lo que siempre fueron (D19).
	 *
	 * <p>No borra la ficha vieja: eso es de Catálogo, y quien ordena las dos cosas en una sola
	 * transacción es la capa de aplicación.</p>
	 *
	 * @throws ProduccionInexistenteException si el destino no está en el catálogo
	 */
	int reasignarRegistros(Long produccionOrigenId, Long produccionDestinoId);

	/** El diario y las estadísticas de una persona (HU-12/13). Público como todo (D21). */
	DiarioDeUsuario deUsuario(Long usuarioId);

	/**
	 * La actividad de un conjunto de personas, de la más nueva a la más vieja por fecha de carga:
	 * el insumo del feed de seguidos (HU-16), que compone la capa de aplicación (D29). Diario no
	 * sabe qué es seguir a alguien — recibe una lista de ids y devuelve lo que escribieron.
	 *
	 * <p>Ordena por cuándo se cargó y no por cuándo se vio la obra, que es al revés que el diario
	 * (MD-2): un feed muestra lo que la gente está contando ahora, y quien sube hoy una salida de
	 * 2019 la está contando hoy.</p>
	 *
	 * @param desde el último registro de la página anterior, o {@code null} para la primera
	 * @param limite cuántos como máximo; quien llama decide, el módulo no inventa un default
	 */
	List<ActividadDeDiario> actividadDe(Collection<Long> usuarioIds, CursorDeActividad desde, int limite);

	/**
	 * Lo mismo pero de todo el mundo: el fallback del feed para quien todavía no sigue a nadie
	 * (D22). Que todo el contenido sea público (D21) es lo que lo hace posible.
	 */
	List<ActividadDeDiario> actividadGlobal(CursorDeActividad desde, int limite);

	/** Promedio (D20) y reseñas de una producción, para la ficha (HU-14). */
	OpinionesDeProduccion opinionesDe(Long produccionId);

	/**
	 * Si ese registro es una reseña: existe y tiene texto. Lo pregunta la capa de aplicación antes
	 * de dejar que alguien le dé like (HU-17), y no es la misma situación que seguir a alguien
	 * (D67): ahí los dos ids se resolvieron para llegar hasta el caso de uso, y acá el id llega
	 * crudo de la URL, así que sin este chequeo se guardarían likes a números inventados.
	 *
	 * <p>Devuelve un booleano y no la reseña porque es lo único que se pregunta. Un registro sin
	 * texto —solo puntaje— no es una reseña y no se puede destacar: lo que HU-17 destaca es lo que
	 * alguien escribió.</p>
	 */
	boolean existeResenia(Long registroId);
}
