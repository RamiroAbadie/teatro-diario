package io.github.ramiroabadie.backend.social;

import java.util.List;

/**
 * La tercera capacidad pública del módulo Social: los reportes a reseñas (D40, HU-18 y HU-22). Va
 * aparte de {@link GrafoSocial} y de {@link LikesDeResenias} por el mismo motivo que esas dos van
 * separadas entre sí: son interacciones distintas sobre cosas distintas, y quien resuelve la cola
 * del admin no tiene por qué recibir el grafo entero.
 *
 * <p>El límite es el mismo que el del like (MODULE_MAP): la reseña —el contenido— es de Diario;
 * el reporte —la interacción— es de Social, que guarda un {@code reseniaId} que no sabe qué es.
 * Quien llama ya comprobó que esa reseña exista (D68).</p>
 *
 * <p>Social registra el veredicto pero no lo ejecuta: borrar el texto reportado es de Diario, que
 * es el dueño del dato. Por eso {@link #confirmar} devuelve de qué reseña se trataba en vez de
 * borrar nada — quien orquesta las dos mitades es la capa de aplicación, como en la fusión de
 * fichas duplicadas (D63).</p>
 */
public interface ReportesDeResenias {

	/**
	 * Alguien avisa sobre una reseña ofensiva (HU-18). Que no sea la propia lo comprueba quien
	 * llama, que es el único que puede saber de quién es (D70).
	 *
	 * <p>Reportar dos veces la misma reseña es reportarla una: mientras haya un reporte pendiente
	 * de esa persona sobre esa reseña, el segundo aviso no agrega nada a la cola. Resuelto ese, sí
	 * se puede volver a reportar — una reseña editada después (HU-11) puede ser otra cosa.</p>
	 */
	void reportar(Long usuarioId, Long reseniaId, String motivo);

	/** La cola del admin: lo que falta resolver, de lo más viejo a lo más nuevo. Es una fila. */
	List<ReporteRecibido> pendientes();

	/**
	 * El reporte tenía razón y la reseña se va (HU-22). Saca de la cola **todos** los reportes
	 * pendientes de esa reseña, no solo este: el veredicto es sobre el texto, y hacer que el admin
	 * lea tres veces lo mismo porque tres personas avisaron es cola que no se vacía.
	 *
	 * @return el id de la reseña, para que quien llama la mande a borrar
	 * @throws ReporteNoEncontradoException si no hay un reporte con ese id
	 * @throws ReporteResueltoException si ese reporte ya se había resuelto
	 */
	Long confirmar(Long reporteId);

	/**
	 * El reporte no tenía razón y la reseña se queda (HU-22). Vacía la cola igual, y por las
	 * mismas: alcanza a todos los reportes pendientes de esa reseña. No hay aviso a quien reportó
	 * — no hay notificaciones en el MVP (MD-3).
	 */
	void desestimar(Long reporteId);
}
