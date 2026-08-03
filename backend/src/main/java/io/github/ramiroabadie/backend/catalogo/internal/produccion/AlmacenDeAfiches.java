package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * El disco: la única parte de la subida que no es la base. El directorio es configurable porque
 * en cada entorno lo sirve otro —en desarrollo Spring escribe adentro de {@code frontend/public/}
 * y lo sirve Next como estático; en producción es el volumen que Caddy monta en solo lectura
 * (D77/D78)—. **Spring no sirve estos archivos en ninguno de los dos.**
 */
@Component
class AlmacenDeAfiches {

	private static final Logger log = LoggerFactory.getLogger(AlmacenDeAfiches.class);

	private final Path directorio;

	/**
	 * El directorio llega como texto y se convierte acá, y no dejando que Spring lo inyecte como
	 * {@code Path}: su conversor lo trata como un recurso del classpath y rechaza cualquier ruta
	 * relativa que suba un nivel — que es exactamente la de desarrollo, {@code ../frontend/public}
	 * (D78). No es un rodeo: acá es una ruta del sistema de archivos, no un recurso.
	 */
	AlmacenDeAfiches(@Value("${afiches.directorio}") String directorio) {
		this.directorio = Path.of(directorio);
	}

	/**
	 * Escribe la versión que ya tiene su número reservado. **Primero un temporal y después un
	 * movimiento atómico**: así nadie puede leer un archivo a medio escribir por la URL
	 * definitiva, y lo que deja un fallo en el medio es un temporal que nadie referencia, no un
	 * afiche roto. El huérfano es basura tolerada; la ficha apuntando a un archivo que no está,
	 * no (D77).
	 */
	void escribir(Long produccionId, int version, byte[] contenido) {
		try {
			Files.createDirectories(this.directorio);
			Path temporal = Files.createTempFile(this.directorio, "subiendo-", ".tmp");
			try {
				Files.write(temporal, contenido);
				mover(temporal, this.directorio.resolve(Afiches.nombre(produccionId, version)));
			}
			catch (IOException | RuntimeException ex) {
				Files.deleteIfExists(temporal);
				throw ex;
			}
		}
		catch (IOException ex) {
			throw new UncheckedIOException("No se pudo guardar el afiche de la producción " + produccionId, ex);
		}
	}

	/**
	 * Borra un archivo que ya nadie referencia: siempre después de que la base se haya
	 * confirmado, nunca antes. Que falle no rompe nada —queda un huérfano, que es exactamente lo
	 * que esta operación acepta— así que no propaga: la respuesta ya está decidida por la base.
	 */
	void borrar(Long produccionId, int version) {
		Path archivo = this.directorio.resolve(Afiches.nombre(produccionId, version));
		try {
			Files.deleteIfExists(archivo);
		}
		catch (IOException ex) {
			log.warn("Quedó un afiche huérfano en {}: no se pudo borrar", archivo, ex);
		}
	}

	/**
	 * El movimiento atómico está garantizado dentro del mismo sistema de archivos, y el temporal
	 * se crea en el mismo directorio justamente para eso.
	 *
	 * <p>⚠️ <b>Si el sistema de archivos no lo soporta, esto falla y no cae a un movimiento
	 * común.</b> Es a propósito: D77 promete que nadie puede leer un afiche a medio escribir, y
	 * un reemplazo no atómico es exactamente la ventana que esa promesa dice que no existe.
	 * Degradar en silencio dejaría la garantía escrita en el contrato y rota en producción, sin
	 * nada que lo delate. Lo que queda cuando falla es lo mismo que cuando falla cualquier otro
	 * paso: un temporal huérfano, la versión sin publicar y la ficha mostrando lo que mostraba.
	 * El día que un entorno real no lo soporte, se enmienda el contrato — no el silencio.</p>
	 */
	private static void mover(Path desde, Path hasta) throws IOException {
		try {
			Files.move(desde, hasta, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (AtomicMoveNotSupportedException ex) {
			throw new IOException("El sistema de archivos de " + hasta.getParent()
					+ " no soporta movimientos atómicos, que es lo que el contrato de afiches necesita", ex);
		}
	}
}
