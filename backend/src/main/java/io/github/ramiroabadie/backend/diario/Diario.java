package io.github.ramiroabadie.backend.diario;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
	 * Cuántas veces esa persona registró esa producción (HU-10, D76): lo que hace que la ficha
	 * pueda decir "la viste 2 veces" en vez de ofrecer registrarla como si fuera la primera.
	 *
	 * <p>Va aparte de {@link #opinionesDe} y no adentro porque son dos preguntas distintas: el
	 * promedio y las reseñas son los mismos para todo el mundo (D21) y esto es de a uno. Quien
	 * llama lo pregunta solo si hay sesión, así que la consulta no se paga en la visita anónima,
	 * que es la mayoría. Cero es una respuesta válida —"no la viste todavía"—; que eso se dibuje
	 * distinto de "no hay nadie mirando" es de la capa de aplicación, que es la única que sabe si
	 * hay sesión.</p>
	 */
	long vecesRegistrada(Long usuarioId, Long produccionId);

	/**
	 * Quién escribió esa reseña, o vacío si ese id no es una reseña —no existe, o existe y nadie
	 * escribió nada en él—. Lo pregunta la capa de aplicación antes de dejar que alguien le dé
	 * like (HU-17) o la reporte (HU-18), y no es la misma situación que seguir a alguien (D67):
	 * ahí los dos ids se resolvieron para llegar hasta el caso de uso, y acá el id llega crudo de
	 * la URL, así que sin este chequeo se guardarían likes y reportes a números inventados.
	 *
	 * <p>Devuelve el autor y no un booleano desde D70: el botón de reportar es solo para reseñas
	 * ajenas (HU-18) y el único que puede comprobarlo es quien también conoce la sesión. Un
	 * registro sin texto —solo puntaje— no es una reseña: no se destaca ni se reporta, porque lo
	 * que las dos cosas señalan es lo que alguien escribió.</p>
	 */
	Optional<Long> autorDeResenia(Long registroId);

	/**
	 * Borra el texto de una reseña y deja el registro donde está (HU-22, D40). Lo ordena el admin
	 * desde la cola de reportes, y por eso no lleva {@code usuarioId}: no es el dueño quien borra
	 * —para eso está {@link #borrar}— sino la moderación, y el candado de admin lo pone la capa de
	 * aplicación (D61).
	 *
	 * <p>Lo ofensivo es el texto, no haber ido al teatro: la salida, su fecha y su puntaje quedan,
	 * y el promedio de la producción (D20) no se mueve. Después de esto el registro deja de ser una
	 * reseña: desaparece de las reseñas de la ficha, en el feed sigue estando —el feed son los
	 * registros, con reseña o sin ella (D66)— pero sin texto, y no se le puede dar like.</p>
	 *
	 * <p>Silencioso si ese registro ya no está o si nunca tuvo texto: el estado que se pedía ya es
	 * el que hay, y quien resuelve la cola no tiene por qué saber que el autor se le adelantó.</p>
	 */
	void borrarResenia(Long registroId);

	/**
	 * Los registros de esa lista de ids, con su autor al lado. La usa la cola de reportes (HU-22)
	 * para mostrar qué texto se reportó y en qué obra fue —el contexto que pide la historia—, en
	 * una sola consulta para la cola entera y no una por fila.
	 *
	 * <p>Devuelve {@link ActividadDeDiario} y no {@link RegistroDeDiario} porque quien pregunta
	 * necesita también de quién es cada uno. Los ids que ya no existen no vuelven: un reporte
	 * puede sobrevivir a la reseña que lo motivó, y la cola tiene que poder decirlo.</p>
	 */
	List<ActividadDeDiario> registrosPorIds(Collection<Long> registroIds);
}
