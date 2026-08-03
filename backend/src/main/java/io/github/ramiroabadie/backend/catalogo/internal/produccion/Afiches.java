package io.github.ramiroabadie.backend.catalogo.internal.produccion;

/**
 * Cómo se llama un afiche. Existe para que el nombre del archivo y la URL pública **no puedan
 * separarse**: son la misma convención escrita una sola vez, y el día que se muevan tienen que
 * moverse juntas o el catálogo entero queda apuntando a imágenes que no están.
 *
 * <p>La URL vive **fuera de {@code /api}** (D77): un afiche es un archivo estático y lo sirve
 * Caddy en producción y Next en desarrollo, nunca Spring — pasarlo por la aplicación cuesta
 * memoria en un VPS chico (P9) y no compra nada.</p>
 */
final class Afiches {

	/**
	 * JPEG y no WebP (D88): no existe un escritor de WebP en Java puro y el JDK solo escribe
	 * JPEG y PNG. Lo que el contrato de D77 necesita es que la URL no se reutilice nunca, y eso
	 * lo da la versión, no el formato.
	 */
	static final String EXTENSION = ".jpg";

	private Afiches() {
	}

	/** {@code null} si la ficha no tiene afiche hoy: es un estado normal y dibujable (D71). */
	static String url(Long produccionId, Integer version) {
		return version == null ? null : "/afiches/" + nombre(produccionId, version);
	}

	static String nombre(Long produccionId, int version) {
		return produccionId + "-" + version + EXTENSION;
	}
}
