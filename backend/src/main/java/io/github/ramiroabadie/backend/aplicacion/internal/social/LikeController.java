package io.github.ramiroabadie.backend.aplicacion.internal.social;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.aplicacion.internal.ReseniaInexistenteException;
import io.github.ramiroabadie.backend.aplicacion.internal.SesionActual;
import io.github.ramiroabadie.backend.diario.Diario;
import io.github.ramiroabadie.backend.social.LikesDeResenias;

/**
 * El toggle del like (HU-17). Vive en la capa de aplicación por el mismo motivo que seguir (D60,
 * D67): hace falta traducir la sesión a un {@code usuarioId}, que lo sabe Identidad, y comprobar
 * que la reseña existe, que lo sabe Diario — y Social no depende de ninguno de los dos
 * (MODULE_MAP). Ninguna regla vive acá: componer tres módulos que no se conocen es exactamente el
 * trabajo de esta capa.
 *
 * <p>La ruta dice {@code /api/resenias/{id}} y no {@code /api/registros/{id}} aunque el id sea el
 * mismo: una reseña es un registro con texto, y un registro sin texto no tiene reseña que
 * destacar. Con este nombre, el 404 de "eso no es una reseña" es el 404 normal de un recurso que
 * no está, en vez de una regla escondida adentro de otro recurso.</p>
 *
 * <p>Responde 204 sin cuerpo, como el botón de seguir: el contador actualizado viaja en la ficha y
 * en el feed, que es de donde se toca el botón.</p>
 */
@RestController
@RequestMapping("/api/resenias/{reseniaId}/like")
class LikeController {

	private final LikesDeResenias likes;

	private final Diario diario;

	private final SesionActual sesion;

	LikeController(LikesDeResenias likes, Diario diario, SesionActual sesion) {
		this.likes = likes;
		this.diario = diario;
		this.sesion = sesion;
	}

	@PostMapping
	public ResponseEntity<Void> darLike(@PathVariable Long reseniaId, Authentication autenticado) {
		Long yo = sesion.id(autenticado);
		exigirQueExista(reseniaId);
		likes.darLike(yo, reseniaId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Quitar el like también comprueba que la reseña exista, igual que dejar de seguir comprueba
	 * que la cuenta exista: un id inventado es un error del cliente en los dos sentidos del botón.
	 */
	@DeleteMapping
	public ResponseEntity<Void> quitarLike(@PathVariable Long reseniaId, Authentication autenticado) {
		Long yo = sesion.id(autenticado);
		exigirQueExista(reseniaId);
		likes.quitarLike(yo, reseniaId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Pregunta por el autor y usa solo si lo hay: al like no le importa quién escribió —darse like
	 * a uno mismo está permitido (D68)—, pero al reporte sí (HU-18), y una sola pregunta a Diario
	 * sirve a las dos (D70).
	 */
	private void exigirQueExista(Long reseniaId) {
		if (diario.autorDeResenia(reseniaId).isEmpty()) {
			throw new ReseniaInexistenteException(reseniaId);
		}
	}

	@ExceptionHandler(ReseniaInexistenteException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrada(ReseniaInexistenteException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	/**
	 * Dos clics simultáneos en el botón: el segundo choca contra el índice único de la tabla. El
	 * estado que pedía ya está —tiene su like—, así que responde lo mismo que el primero. En este
	 * controlador la única escritura es esa fila, así que no hay otra violación posible que
	 * quedara tapada.
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void yaTeniaLike(DataIntegrityViolationException ex) {
		// sin cuerpo: 204
	}

	/** La sesión sigue viva pero la cuenta se borró: para quien escribe es no estar logueado. */
	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ProblemDetail sesionSinCuenta(AuthenticationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}
}
