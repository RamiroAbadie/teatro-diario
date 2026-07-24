package io.github.ramiroabadie.backend.identidad;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * La interfaz pública del módulo Identidad: consulta de perfiles para componer lecturas
 * (MODULE_MAP). La usa la capa de aplicación para dos cosas: traducir la sesión a un
 * {@code usuarioId} antes de tocar el diario, y ponerle nombre a los ids opacos que guardan
 * los otros módulos (el autor de una reseña, HU-14).
 *
 * <p>Ningún módulo la usa: Diario y Social referencian usuarios por id y nada más (D30).</p>
 */
public interface Usuarios {

	Optional<UsuarioPublico> porUsername(String username);

	/** Los autores de una lista de reseñas en una sola consulta. Los ids que no existen no vienen. */
	Map<Long, UsuarioPublico> porIds(Collection<Long> ids);
}
