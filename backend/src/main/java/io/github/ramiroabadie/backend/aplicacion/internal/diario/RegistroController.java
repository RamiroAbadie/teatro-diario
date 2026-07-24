package io.github.ramiroabadie.backend.aplicacion.internal.diario;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.diario.Diario;
import io.github.ramiroabadie.backend.diario.ProduccionInexistenteException;
import io.github.ramiroabadie.backend.diario.RegistroAjenoException;
import io.github.ramiroabadie.backend.diario.RegistroDeDiario;
import io.github.ramiroabadie.backend.diario.RegistroInvalidoException;
import io.github.ramiroabadie.backend.diario.RegistroNoEncontradoException;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;
import io.github.ramiroabadie.backend.identidad.Usuarios;

/**
 * El gesto de registro y sus correcciones (HU-09/10/11).
 *
 * <p>Vive en la capa de aplicación y no en {@code diario/internal/} —que es donde el resto de
 * los controladores del proyecto viven, adentro de su módulo— porque este necesita dos cosas de
 * dos lados: quién está logueado (Identidad) y qué se registra (Diario), y Diario no puede
 * depender de Identidad (MODULE_MAP: la única dependencia módulo-a-módulo es Diario → Catálogo).
 * Traducir una sesión a un {@code usuarioId} es exactamente lo que MODULE_MAP llama concern
 * transversal de la capa de aplicación. El caso de uso sigue estando en el módulo: acá no hay
 * ninguna regla, solo HTTP (D34).</p>
 *
 * <p>Escribir pide sesión: lo resuelve {@code SecurityConfig} con su {@code anyRequest()
 * .authenticated()}, sin regla nueva.</p>
 */
@RestController
@RequestMapping("/api/registros")
class RegistroController {

	private final Diario diario;

	private final Usuarios usuarios;

	RegistroController(Diario diario, Usuarios usuarios) {
		this.diario = diario;
		this.usuarios = usuarios;
	}

	/**
	 * Sin cabecera {@code Location} a propósito: un registro no tiene URL propia. Donde queda a
	 * la vista es en el diario de quien lo escribió, y la respuesta ya lo devuelve entero.
	 */
	@PostMapping
	public ResponseEntity<RegistroDeDiario> registrar(@Valid @RequestBody RegistroRequest req,
			Authentication autenticado) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(diario.registrar(usuarioActual(autenticado), req.aNuevoRegistro()));
	}

	@PutMapping("/{id}")
	public RegistroDeDiario editar(@PathVariable Long id, @Valid @RequestBody RegistroRequest req,
			Authentication autenticado) {
		return diario.editar(usuarioActual(autenticado), id, req.aNuevoRegistro());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> borrar(@PathVariable Long id, Authentication autenticado) {
		diario.borrar(usuarioActual(autenticado), id);
		return ResponseEntity.noContent().build();
	}

	private Long usuarioActual(Authentication autenticado) {
		return usuarios.porUsername(autenticado.getName())
				.map(UsuarioPublico::id)
				.orElseThrow(() -> new AuthenticationCredentialsNotFoundException(
						"La cuenta de esta sesión ya no existe"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProblemDetail datosInvalidos(MethodArgumentNotValidException ex) {
		Map<String, String> porCampo = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			porCampo.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Revisá los datos del registro");
		problema.setProperty("errores", porCampo);
		return problema;
	}

	/** Las reglas del dominio salen con el mismo formato que la validación: campo y mensaje. */
	@ExceptionHandler(RegistroInvalidoException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProblemDetail registroInvalido(RegistroInvalidoException ex) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problema.setProperty("errores", Map.of(ex.getCampo(), ex.getMessage()));
		return problema;
	}

	/**
	 * La obra no está en el catálogo, que es cerrado (D7). El mensaje es el pie del camino que
	 * sigue el frontend: ofrecer sugerirla sin perder lo tipeado (HU-08).
	 */
	@ExceptionHandler(ProduccionInexistenteException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail produccionInexistente(ProduccionInexistenteException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(RegistroNoEncontradoException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrado(RegistroNoEncontradoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(RegistroAjenoException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ProblemDetail ajeno(RegistroAjenoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	/** La sesión sigue viva pero la cuenta se borró: para quien escribe es no estar logueado. */
	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ProblemDetail sesionSinCuenta(AuthenticationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}
}
