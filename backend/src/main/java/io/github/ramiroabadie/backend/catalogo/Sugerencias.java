package io.github.ramiroabadie.backend.catalogo;

import java.util.List;

/**
 * La otra interfaz pública de Catálogo: la recepción de sugerencias y su cola (HU-08 y HU-21).
 * Va aparte de {@link CatalogoProducciones} porque son dos capacidades distintas de la misma
 * autoridad —el catálogo curado y la cola de lo que pide entrar— y quien las usa no es el mismo:
 * la cola es del panel de admin y ningún módulo la invoca.
 *
 * <p>El {@code sugerenteId} llega siempre de afuera, como el {@code usuarioId} de Diario: Catálogo
 * guarda un id opaco (D30) y no sabe si existe esa cuenta, porque no depende de Identidad
 * (MODULE_MAP). Quien llama ya resolvió la sesión para poder llegar hasta acá.</p>
 */
public interface Sugerencias {

	/**
	 * Recibe una propuesta (HU-08). No busca duplicados ni compara contra el catálogo: si la obra
	 * ya estaba, la sugerencia llega igual y el admin la rechaza en dos clics. Adivinar acá sería
	 * decidir la identidad de una producción con el título que alguien tipeó apurado a la salida
	 * del teatro, que es justamente lo que el catálogo cerrado (D7) evita.
	 */
	SugerenciaPropuesta recibir(Long sugerenteId, NuevaSugerencia nueva);

	/** La cola del admin: lo que falta resolver, de lo más viejo a lo más nuevo. Es una fila. */
	List<SugerenciaPropuesta> pendientes();

	/**
	 * Saca la sugerencia de la cola dejando anotado en qué ficha terminó (HU-21). La producción la
	 * creó antes el admin con el formulario de HU-20, precargado con lo sugerido: acá no se crea
	 * nada, se cierra el círculo. Que esa ficha exista lo comprueba quien llama.
	 *
	 * @throws SugerenciaNoEncontradaException si no hay una sugerencia con ese id
	 * @throws SugerenciaResueltaException si esa sugerencia ya se había aprobado o rechazado
	 */
	void aprobar(Long sugerenciaId, Long produccionId);

	/**
	 * Saca la sugerencia de la cola con un motivo que solo ve el admin (HU-21): no hay notificación
	 * al que sugirió, porque no hay notificaciones en el MVP (MD-3).
	 */
	void rechazar(Long sugerenciaId, String motivo);
}
