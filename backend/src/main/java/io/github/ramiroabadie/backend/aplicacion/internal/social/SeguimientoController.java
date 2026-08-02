package io.github.ramiroabadie.backend.aplicacion.internal.social;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.aplicacion.internal.CuentaInexistenteException;
import io.github.ramiroabadie.backend.aplicacion.internal.SesionActual;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;
import io.github.ramiroabadie.backend.identidad.Usuarios;
import io.github.ramiroabadie.backend.social.GrafoSocial;
import io.github.ramiroabadie.backend.social.SeguimientoInvalidoException;

/**
 * Seguir y dejar de seguir (HU-15). Vive en la capa de aplicación por el mismo motivo que los
 * endpoints del diario (D60): hacen falta dos ids que ningún módulo puede conseguir solo —el de
 * quien está logueado y el de la cuenta que está en la URL—, y los dos los sabe Identidad, de la
 * que Social no depende (MODULE_MAP).
 *
 * <p>Cuelga del perfil ({@code /api/usuarios/{username}/seguir}) y no de un recurso propio porque
 * seguir es algo que se le hace a un perfil, y el username es su URL (MD-4). Escribir pide sesión:
 * lo resuelve {@code SecurityConfig} con su {@code anyRequest().authenticated()}.</p>
 */
@RestController
@RequestMapping("/api/usuarios/{username}/seguir")
class SeguimientoController {

	private final GrafoSocial grafo;

	private final Usuarios usuarios;

	private final SesionActual sesion;

	SeguimientoController(GrafoSocial grafo, Usuarios usuarios, SesionActual sesion) {
		this.grafo = grafo;
		this.usuarios = usuarios;
		this.sesion = sesion;
	}

	/** 204 y nada más: el estado del botón lo devuelve el perfil, que es de donde se lo toca. */
	@PostMapping
	public ResponseEntity<Void> seguir(@PathVariable String username, Authentication autenticado) {
		grafo.seguir(sesion.id(autenticado), idDe(username));
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping
	public ResponseEntity<Void> dejarDeSeguir(@PathVariable String username, Authentication autenticado) {
		grafo.dejarDeSeguir(sesion.id(autenticado), idDe(username));
		return ResponseEntity.noContent().build();
	}

	private Long idDe(String username) {
		return usuarios.porUsername(username)
				.map(UsuarioPublico::id)
				.orElseThrow(() -> new CuentaInexistenteException(username));
	}

	@ExceptionHandler(CuentaInexistenteException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrada(CuentaInexistenteException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(SeguimientoInvalidoException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProblemDetail invalido(SeguimientoInvalidoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	/**
	 * Dos clics simultáneos en el botón: el segundo choca contra el índice único de la tabla. El
	 * resultado que pedía ya está —lo sigue—, así que responde lo mismo que el primero. En este
	 * controlador la única escritura es esa fila, así que no hay otra violación posible que
	 * quedara tapada.
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void yaLoSeguia(DataIntegrityViolationException ex) {
		// sin cuerpo: 204
	}
}
