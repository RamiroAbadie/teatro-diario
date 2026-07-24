package io.github.ramiroabadie.backend.aplicacion.internal.diario;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.catalogo.CatalogoProducciones;
import io.github.ramiroabadie.backend.diario.Diario;
import io.github.ramiroabadie.backend.diario.OpinionesDeProduccion;
import io.github.ramiroabadie.backend.diario.ProduccionInexistenteException;
import io.github.ramiroabadie.backend.diario.ReseniaDeProduccion;
import io.github.ramiroabadie.backend.identidad.Usuarios;

/**
 * El promedio y las reseñas de una producción (HU-14), que es la mitad de la ficha que no es del
 * catálogo. Va como recurso aparte de {@code /api/producciones/{id}} y no adentro: son dos
 * módulos con dos ritmos de cambio distintos —la ficha casi no cambia, las opiniones cambian
 * cada vez que alguien registra— y la pantalla arma las dos cosas con dos pedidos.
 *
 * <p>Acá viven juntas porque cada una sola no alcanza: Catálogo dice si la producción existe,
 * Diario qué se opinó de ella e Identidad quién lo dijo. Ninguno de los tres conoce a los
 * otros.</p>
 */
@RestController
@RequestMapping("/api/producciones")
class OpinionesController {

	private final CatalogoProducciones catalogo;

	private final Diario diario;

	private final Usuarios usuarios;

	OpinionesController(CatalogoProducciones catalogo, Diario diario, Usuarios usuarios) {
		this.catalogo = catalogo;
		this.diario = diario;
		this.usuarios = usuarios;
	}

	/**
	 * Una producción que existe pero que nadie registró todavía responde con promedio nulo y la
	 * lista vacía: la ficha recién estrenada también tiene que poder mostrarse.
	 */
	@GetMapping("/{id}/opiniones")
	public OpinionesResponse opiniones(@PathVariable Long id) {
		if (catalogo.porId(id).isEmpty()) {
			throw new ProduccionInexistenteException(id);
		}
		OpinionesDeProduccion opiniones = diario.opinionesDe(id);
		List<Long> autores = opiniones.resenias().stream()
				.map(ReseniaDeProduccion::usuarioId)
				.distinct()
				.toList();
		return OpinionesResponse.desde(opiniones, usuarios.porIds(autores));
	}

	@ExceptionHandler(ProduccionInexistenteException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrada(ProduccionInexistenteException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}
}
