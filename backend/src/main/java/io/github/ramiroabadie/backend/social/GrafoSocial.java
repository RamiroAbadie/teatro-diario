package io.github.ramiroabadie.backend.social;

import java.util.List;

/**
 * La interfaz pública del módulo Social: el grafo de quién sigue a quién (MODULE_MAP). Casos de
 * uso, no endpoints — quien los invoca hoy es la capa de aplicación, que es la única que puede
 * traducir un username a un id (D60).
 *
 * <p>Los dos ids son opacos (D30): Social no sabe qué es un usuario ni tiene cómo preguntarlo, no
 * depende de Identidad. Que las dos cuentas existan lo garantiza quien llama, que para llegar
 * hasta acá tuvo que resolverlas. Lo único que Social decide es la forma del grafo: nadie se
 * sigue a sí mismo y seguir dos veces es seguir una.</p>
 *
 * <p>Sin aprobación ni solicitudes: seguir es unilateral e inmediato, porque todo el contenido es
 * público (D21) y no hay nada que autorizar.</p>
 */
public interface GrafoSocial {

	/**
	 * Seguir a alguien (HU-15). Idempotente: seguir a quien ya seguís no cambia nada ni falla —
	 * el botón es un toggle y el doble clic es lo normal, no un error.
	 *
	 * @throws SeguimientoInvalidoException si las dos puntas son la misma persona
	 */
	void seguir(Long seguidorId, Long seguidoId);

	/** Dejar de seguir (HU-15). Idempotente por el mismo motivo que {@link #seguir}. */
	void dejarDeSeguir(Long seguidorId, Long seguidoId);

	/** El estado del botón en el perfil ajeno (HU-15). */
	boolean sigue(Long seguidorId, Long seguidoId);

	/**
	 * Los ids que sigue una persona: el insumo del feed (D29), que la capa de aplicación le pasa
	 * después a Diario. Sin paginar — a la escala del MVP nadie sigue a tanta gente como para que
	 * la lista no entre en memoria, y el día que eso pase el feed va a necesitar otra cosa
	 * (fan-out) antes que este método.
	 */
	List<Long> seguidosPor(Long usuarioId);

	/** Los dos números que muestra el perfil (HU-15). */
	ContadoresSociales contadoresDe(Long usuarioId);
}
