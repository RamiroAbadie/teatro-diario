package io.github.ramiroabadie.backend.aplicacion.internal.diario;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.aplicacion.internal.SesionActual;
import io.github.ramiroabadie.backend.catalogo.CatalogoProducciones;
import io.github.ramiroabadie.backend.diario.Diario;
import io.github.ramiroabadie.backend.diario.OpinionesDeProduccion;
import io.github.ramiroabadie.backend.diario.ProduccionInexistenteException;
import io.github.ramiroabadie.backend.diario.ReseniaDeProduccion;
import io.github.ramiroabadie.backend.identidad.Usuarios;
import io.github.ramiroabadie.backend.social.LikesDeResenias;

/**
 * El promedio y las reseñas de una producción (HU-14), que es la mitad de la ficha que no es del
 * catálogo. Va como recurso aparte de {@code /api/producciones/{id}} y no adentro: son dos
 * módulos con dos ritmos de cambio distintos —la ficha casi no cambia, las opiniones cambian
 * cada vez que alguien registra— y la pantalla arma las dos cosas con dos pedidos.
 *
 * <p>Acá viven juntas porque cada una sola no alcanza: Catálogo dice si la producción existe,
 * Diario qué se opinó de ella, Identidad quién lo dijo y Social cuántos likes tiene cada reseña
 * (HU-17). Ninguno de los cuatro conoce a los otros.</p>
 */
@RestController
@RequestMapping("/api/producciones")
class OpinionesController {

	private final CatalogoProducciones catalogo;

	private final Diario diario;

	private final Usuarios usuarios;

	private final LikesDeResenias likes;

	private final SesionActual sesion;

	OpinionesController(CatalogoProducciones catalogo, Diario diario, Usuarios usuarios,
			LikesDeResenias likes, SesionActual sesion) {
		this.catalogo = catalogo;
		this.diario = diario;
		this.usuarios = usuarios;
		this.likes = likes;
		this.sesion = sesion;
	}

	/**
	 * Una producción que existe pero que nadie registró todavía responde con promedio nulo y la
	 * lista vacía: la ficha recién estrenada también tiene que poder mostrarse.
	 *
	 * <p>La ficha se lee sin cuenta (D21) y lo único que cambia si la hay son las dos cosas que
	 * son de a uno: el estado del botón de like, igual que el de seguir en el perfil (D67), y
	 * cuántas veces la vio quien mira (HU-10, D76). Los contadores son los mismos para todos, y
	 * sin sesión no hay botón que dibujar ni a quién contarle nada.</p>
	 */
	@GetMapping("/{id}/opiniones")
	public OpinionesResponse opiniones(@PathVariable Long id, Authentication autenticado) {
		if (catalogo.porId(id).isEmpty()) {
			throw new ProduccionInexistenteException(id);
		}
		OpinionesDeProduccion opiniones = diario.opinionesDe(id);
		List<Long> autores = opiniones.resenias().stream()
				.map(ReseniaDeProduccion::usuarioId)
				.distinct()
				.toList();
		List<Long> resenias = opiniones.resenias().stream()
				.map(ReseniaDeProduccion::registroId)
				.toList();
		Optional<Long> yo = sesion.idSiEstaLogueado(autenticado);
		Set<Long> mios = yo.map(quien -> likes.conLikeDe(quien, resenias)).orElse(null);
		Long vecesQueLaVi = yo.map(quien -> diario.vecesRegistrada(quien, id)).orElse(null);
		return OpinionesResponse.desde(opiniones, usuarios.porIds(autores),
				likes.contarPorResenia(resenias), mios, vecesQueLaVi);
	}

	@ExceptionHandler(ProduccionInexistenteException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrada(ProduccionInexistenteException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}
}
