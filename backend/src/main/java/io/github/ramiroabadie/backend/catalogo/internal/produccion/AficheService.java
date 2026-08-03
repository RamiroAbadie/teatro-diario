package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import org.springframework.stereotype.Service;

/**
 * La subida y el borrado del afiche (HU-20, D72/D77). Este servicio **es el orden de las
 * operaciones**: no hace ninguna cuenta, y lo único que tiene para aportar es en qué secuencia
 * pasan las cosas y qué queda si alguna falla.
 *
 * <p>La regla que gobierna todo: <b>la base nunca puede apuntar a un archivo que no está.</b> Al
 * revés sí —un archivo que nadie referencia— y eso es lo que se acepta: los huérfanos son basura
 * tolerada y a 50 fichas (D38) no justifican una tarea de limpieza (D51). El día que pesen, se
 * barren comparando el directorio contra los {@code afiche_actual} vivos.</p>
 *
 * <p>⚠️ <b>Nada de esto es {@code @Transactional}.</b> Y no es un olvido: entre la reserva y la
 * publicación hay un archivo que escribir, y el borrado del anterior tiene que pasar
 * <b>después</b> del commit. Borrarlo adentro de la transacción y que el commit falle deja a la
 * base apuntando otra vez al archivo viejo, que ya no está — el único orden que rompe la regla,
 * y encima llegando desde el lado que parece más prolijo. Las dos escrituras cortas están en
 * {@link VersionesDeAfiche}, cada una con su transacción.</p>
 */
@Service
class AficheService {

	private final VersionesDeAfiche versiones;

	private final AlmacenDeAfiches almacen;

	private final ProcesadorDeAfiche procesador;

	private final ProduccionService producciones;

	AficheService(VersionesDeAfiche versiones, AlmacenDeAfiches almacen, ProcesadorDeAfiche procesador,
			ProduccionService producciones) {
		this.versiones = versiones;
		this.almacen = almacen;
		this.procesador = procesador;
		this.producciones = producciones;
	}

	/**
	 * Los cuatro pasos de D77, en este orden y no en otro:
	 *
	 * <ol>
	 * <li><b>Reservar</b> la versión, confirmada en la base antes de tocar el disco. Si falla,
	 * no se escribió nada y el número queda quemado, que es exactamente lo que se quiere.</li>
	 * <li><b>Escribir</b> el archivo. Si falla, la ficha sigue mostrando lo que mostraba.</li>
	 * <li><b>Publicar</b>: recién acá la URL nueva existe para el mundo. Si falla, el archivo
	 * nuevo queda huérfano e invisible —nadie conoce su URL— y la ficha sigue coherente.</li>
	 * <li><b>Borrar</b> el archivo de la versión anterior, ya sin transacción abierta. Si falla,
	 * queda un huérfano y nada más.</li>
	 * </ol>
	 *
	 * <p>La imagen se procesa <b>antes</b> de reservar: un archivo que no es una imagen no tiene
	 * por qué gastar un número de versión, y validar es lo más barato de todo.</p>
	 */
	ProduccionResponse subir(Long produccionId, byte[] archivo) {
		byte[] jpeg = this.procesador.aJpeg(archivo);
		int version = this.versiones.reservar(produccionId);
		this.almacen.escribir(produccionId, version, jpeg);
		Integer anterior = this.versiones.publicar(produccionId, version);
		if (anterior != null) {
			this.almacen.borrar(produccionId, anterior);
		}
		return this.producciones.obtener(produccionId);
	}

	/**
	 * El mismo orden invertido: la ficha se queda sin afiche en la base y recién después se borra
	 * el archivo. <b>Idempotente</b> (D77): borrar dos veces no es un error —el estado que se
	 * pedía ya es el que hay, la misma semántica que dejar de seguir a alguien (D67)— y el
	 * {@code 404} queda para lo que sí es un error: que esa producción no exista.
	 *
	 * <p>{@code afiche_version} no se toca. Que el contador siga donde estaba es lo que hace que
	 * subir → borrar → subir devuelva la versión 3 y no otra vez la 1, que ya está cacheada por
	 * un año en máquinas que no controlamos con otra imagen adentro.</p>
	 */
	void borrar(Long produccionId) {
		Integer anterior = this.versiones.despublicar(produccionId);
		if (anterior != null) {
			this.almacen.borrar(produccionId, anterior);
		}
	}
}
