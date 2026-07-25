package io.github.ramiroabadie.backend.social.internal.like;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.ramiroabadie.backend.social.LikesDeResenias;
import io.github.ramiroabadie.backend.social.internal.like.LikeRepository.ConteoDeLikes;

/**
 * Los casos de uso de los likes (HU-17) y la única implementación de {@link LikesDeResenias}.
 * Igual de chico que el grafo: dar like es insertar una fila y el resto es contar.
 *
 * <p>No valida que la reseña exista ni que la cuenta exista, por el mismo motivo que
 * {@code SeguimientoService} (D67): Social no depende ni de Diario ni de Identidad (MODULE_MAP) y
 * no tiene con qué preguntarlo. Quien llama —la capa de aplicación— ya lo comprobó.</p>
 */
@Service
class LikeService implements LikesDeResenias {

	private final LikeRepository repositorio;

	LikeService(LikeRepository repositorio) {
		this.repositorio = repositorio;
	}

	/**
	 * El chequeo previo evita la excepción en el caso normal de volver a tocar el botón; la carrera
	 * de dos clics a la vez la resuelve el índice único de la tabla, y quien llama la traduce a "ya
	 * le habías dado like".
	 */
	@Override
	@Transactional
	public void darLike(Long usuarioId, Long reseniaId) {
		if (repositorio.existsByReseniaIdAndUsuarioId(reseniaId, usuarioId)) {
			return;
		}
		repositorio.save(new LikeDeResenia(reseniaId, usuarioId));
	}

	/** Borrar lo que no está no es un error: el estado que se pedía ya es el que hay. */
	@Override
	@Transactional
	public void quitarLike(Long usuarioId, Long reseniaId) {
		repositorio.deleteByReseniaIdAndUsuarioId(reseniaId, usuarioId);
	}

	/**
	 * La lista vacía se ataja antes de ir a la base, como en el feed (D66): una ficha sin reseñas o
	 * una página sin textos no tiene nada que contar, y un {@code in ()} vacío es una consulta que
	 * Postgres igual tiene que resolver para devolver nada.
	 */
	@Override
	@Transactional(readOnly = true)
	public Map<Long, Long> contarPorResenia(Collection<Long> reseniaIds) {
		if (reseniaIds.isEmpty()) {
			return Map.of();
		}
		return repositorio.contarPorResenia(reseniaIds).stream()
				.collect(Collectors.toMap(ConteoDeLikes::getReseniaId, ConteoDeLikes::getCantidad));
	}

	@Override
	@Transactional(readOnly = true)
	public Set<Long> conLikeDe(Long usuarioId, Collection<Long> reseniaIds) {
		if (reseniaIds.isEmpty()) {
			return Set.of();
		}
		return Set.copyOf(repositorio.conLikeDe(usuarioId, reseniaIds));
	}
}
