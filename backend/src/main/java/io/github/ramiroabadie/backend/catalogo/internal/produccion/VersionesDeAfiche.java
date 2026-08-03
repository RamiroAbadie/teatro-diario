package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Las dos escrituras cortas del afiche, cada una en su transacción y **separadas a propósito**
 * (D77): entre reservar el número y publicarlo hay un archivo que escribir, y ese trabajo no
 * puede pasar con una transacción abierta.
 *
 * <p>Están en su propia clase y no como métodos privados del servicio por un motivo mecánico y
 * no estético: {@code @Transactional} lo aplica un proxy, y una llamada de un método de un bean a
 * otro método del mismo bean no pasa por el proxy — así que ahí las transacciones simplemente no
 * existirían, que es el tipo de bug que no se ve hasta que dos subidas se solapan.</p>
 */
@Service
class VersionesDeAfiche {

	private final ProduccionRepository repositorio;

	VersionesDeAfiche(ProduccionRepository repositorio) {
		this.repositorio = repositorio;
	}

	/**
	 * Paso 1: quema un número de versión y lo devuelve, **antes de tocar el disco**. Es una sola
	 * sentencia atómica y no leer-sumar-guardar, que es lo que hace la promesa cumplible: dos
	 * subidas simultáneas reservan números distintos, y cada intento quema el suyo salga bien o
	 * mal. Si el número se asignara al publicar, dos intentos que fallan a mitad de camino
	 * calcularían el mismo y escribirían los dos sobre el mismo nombre — y ahí "una URL de afiche
	 * nunca cambia de contenido" deja de ser cierto.
	 *
	 * @throws ProduccionNoEncontradaException si esa ficha no existe
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	int reservar(Long produccionId) {
		Integer version = this.repositorio.reservarVersionDeAfiche(produccionId);
		if (version == null) {
			throw new ProduccionNoEncontradaException(produccionId);
		}
		return version;
	}

	/**
	 * Paso 3: recién acá la URL nueva existe para el mundo, y solo se llega con el archivo ya
	 * escrito. Va con la fila bloqueada (el mismo {@code select ... for update} de D69/D70)
	 * porque en la sección crítica pasan dos cosas que tienen que ser una: se publica la versión
	 * nueva y se captura cuál era la anterior. Sin el bloqueo, dos publicaciones intercaladas
	 * pueden dejar a una borrando el archivo que la otra acaba de publicar.
	 *
	 * @return la versión que estaba publicada, o {@code null} si no había ninguna. **El archivo
	 * que se borra después es ese**, no el que resulte de volver a leer la base
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	Integer publicar(Long produccionId, int version) {
		return bloquear(produccionId).publicarAfiche(version);
	}

	/** El {@code DELETE}, que es el mismo orden invertido: primero la base, después el archivo. */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	Integer despublicar(Long produccionId) {
		return bloquear(produccionId).despublicarAfiche();
	}

	private Produccion bloquear(Long produccionId) {
		return this.repositorio.bloquearPorId(produccionId)
				.orElseThrow(() -> new ProduccionNoEncontradaException(produccionId));
	}
}
