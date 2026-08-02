package io.github.ramiroabadie.backend.identidad.internal.usuario;

import java.net.URI;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Alta de cuenta, login y "quién soy" (HU-01/02). El logout no está acá: lo resuelve el filtro
 * de Spring Security configurado en {@code SecurityConfig}, que invalida la sesión, borra las
 * dos cookies y emite un token CSRF nuevo para que se pueda volver a entrar sin dar un rodeo.
 *
 * <p>La sesión se inicia a mano (no hay formLogin: esta API habla JSON) siguiendo el mismo orden
 * que el filtro estándar — autenticar, rotar el id de sesión, guardar el contexto — para no
 * perder la protección contra fijación de sesión.</p>
 */
@RestController
@RequestMapping("/api/auth")
class AuthController {

	private final UsuarioService servicio;

	private final AuthenticationManager autenticador;

	private final SecurityContextRepository contextos;

	private final SessionAuthenticationStrategy estrategiaDeSesion;

	AuthController(UsuarioService servicio, AuthenticationManager autenticador,
			SecurityContextRepository contextos, SessionAuthenticationStrategy estrategiaDeSesion) {
		this.servicio = servicio;
		this.autenticador = autenticador;
		this.contextos = contextos;
		this.estrategiaDeSesion = estrategiaDeSesion;
	}

	/** HU-01: al completar el alta la persona ya queda logueada, sin pasar por el login. */
	@PostMapping("/registro")
	public ResponseEntity<CuentaResponse> registrar(@Valid @RequestBody RegistroRequest req,
			HttpServletRequest request, HttpServletResponse response) {
		CuentaResponse cuenta = servicio.registrar(req);
		iniciarSesion(cuenta.username(), req.password(), request, response);
		return ResponseEntity.created(URI.create("/api/auth/yo")).body(cuenta);
	}

	@PostMapping("/login")
	public CuentaResponse login(@Valid @RequestBody LoginRequest req,
			HttpServletRequest request, HttpServletResponse response) {
		Authentication autenticado = iniciarSesion(req.identificador(), req.password(), request, response);
		return servicio.obtenerPorUsername(autenticado.getName());
	}

	@GetMapping("/yo")
	public CuentaResponse yo(Authentication autenticado) {
		return servicio.obtenerPorUsername(autenticado.getName());
	}

	private Authentication iniciarSesion(String identificador, String password,
			HttpServletRequest request, HttpServletResponse response) {
		Authentication autenticado = autenticador
				.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(identificador, password));
		estrategiaDeSesion.onAuthentication(autenticado, request, response);
		SecurityContext contexto = SecurityContextHolder.createEmptyContext();
		contexto.setAuthentication(autenticado);
		SecurityContextHolder.setContext(contexto);
		contextos.saveContext(contexto, request, response);
		return autenticado;
	}

	@ExceptionHandler(CampoEnUsoException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ProblemDetail campoEnUso(CampoEnUsoException ex) {
		return porCampo(HttpStatus.CONFLICT, ex);
	}

	/** Lo que la validación por anotaciones no alcanza a ver, pero sigue siendo un campo mal. */
	@ExceptionHandler(CampoInvalidoException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProblemDetail campoInvalido(CampoInvalidoException ex) {
		return porCampo(HttpStatus.BAD_REQUEST, ex);
	}

	private ProblemDetail porCampo(HttpStatus estado, CampoInvalidoException ex) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, ex.getMessage());
		problema.setProperty("errores", Map.of(ex.getCampo(), ex.getMessage()));
		return problema;
	}

	/** Dos altas simultáneas con el mismo dato: el índice único de la base es el que decide. */
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ProblemDetail altaDuplicada(DataIntegrityViolationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"Ese nombre de usuario o email ya está tomado");
	}

	/** Mensaje genérico a propósito: no se revela cuál de los dos campos falló (HU-02). */
	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ProblemDetail credencialesInvalidas(AuthenticationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
				"Email/usuario o contraseña incorrectos");
	}

	@ExceptionHandler(SesionSinCuentaException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ProblemDetail sesionSinCuenta(SesionSinCuentaException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}
}
