package io.github.ramiroabadie.backend.social;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * La otra mitad de la interfaz pública del módulo Social: los likes a reseñas (D11, HU-17). Va
 * aparte de {@link GrafoSocial} y no adentro porque son dos capacidades distintas de un mismo
 * módulo —el grafo une personas con personas; el like une una persona con un texto— y meterlas en
 * la misma interfaz obligaría a quien solo quiere contar likes a recibir el grafo entero.
 *
 * <p>Los dos ids son opacos (D30). La reseña es de Diario y Social no depende de Diario
 * (MODULE_MAP): acá un {@code reseniaId} es un número y nada más. Que la reseña exista lo
 * garantiza quien llama, igual que con las cuentas en el grafo (D67).</p>
 *
 * <p>Las dos consultas de lectura son por lista y no por reseña: la ficha muestra todas las
 * reseñas de una producción y el feed una página entera, y contar de a una sería una consulta por
 * fila, que es exactamente lo que el feed evita desde D66.</p>
 */
public interface LikesDeResenias {

	/**
	 * Destacar una reseña (HU-17). Idempotente por el mismo motivo que seguir: el botón es un
	 * toggle y el doble clic es lo normal, no un error.
	 *
	 * <p>Nada impide darle like a la propia reseña: a diferencia del grafo, donde seguirse a uno
	 * mismo convierte el feed en un espejo, acá el auto-like solo suma uno a un contador.</p>
	 */
	void darLike(Long usuarioId, Long reseniaId);

	/** Sacar el like (HU-17). Idempotente: quitar lo que no está deja las cosas como se pedían. */
	void quitarLike(Long usuarioId, Long reseniaId);

	/**
	 * Cuántos likes tiene cada una de estas reseñas: el contador visible de HU-17. Las que no
	 * tienen ninguno no vienen en el mapa — quien pregunta ya sabe que eso es cero.
	 */
	Map<Long, Long> contarPorResenia(Collection<Long> reseniaIds);

	/** Cuáles de estas reseñas ya tienen el like de esta persona: el estado del botón (HU-17). */
	Set<Long> conLikeDe(Long usuarioId, Collection<Long> reseniaIds);
}
