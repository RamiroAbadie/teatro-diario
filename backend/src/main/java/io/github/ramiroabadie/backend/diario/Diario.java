package io.github.ramiroabadie.backend.diario;

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

	/** Promedio (D20) y reseñas de una producción, para la ficha (HU-14). */
	OpinionesDeProduccion opinionesDe(Long produccionId);
}
