package io.github.ramiroabadie.backend.aplicacion.internal.curaduria;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.aplicacion.internal.SesionActual;
import io.github.ramiroabadie.backend.catalogo.Sugerencias;

/**
 * El formulario de sugerir una obra que no está (HU-08): la válvula del catálogo cerrado (D7) y
 * el final del camino triste de la búsqueda, cuando no hay resultados (HU-07) o cuando el gesto de
 * registro no encuentra la obra (HU-09).
 *
 * <p>Vive en la capa de aplicación por el mismo motivo que registrar (D60): hace falta traducir la
 * sesión a un {@code usuarioId} y eso lo sabe Identidad, de la que Catálogo no depende
 * (MODULE_MAP). El caso de uso está en el módulo; acá no hay ninguna regla.</p>
 *
 * <p>Pide sesión —lo resuelve {@code SecurityConfig} con su {@code anyRequest().authenticated()},
 * sin regla nueva— y responde 201 con lo que se propuso: es la confirmación de recibido. Sin
 * cabecera {@code Location}, porque una sugerencia no tiene URL: el que la manda no la vuelve a
 * ver, y si se aprueba, lo que aparece es la obra en el catálogo (MD-3).</p>
 */
@RestController
@RequestMapping("/api/sugerencias")
class SugerenciaController {

	private final Sugerencias sugerencias;

	private final SesionActual sesion;

	SugerenciaController(Sugerencias sugerencias, SesionActual sesion) {
		this.sugerencias = sugerencias;
		this.sesion = sesion;
	}

	@PostMapping
	public ResponseEntity<SugerenciaResponse> sugerir(@Valid @RequestBody SugerenciaRequest req,
			Authentication autenticado) {
		return ResponseEntity.status(HttpStatus.CREATED).body(SugerenciaResponse.desde(
				sugerencias.recibir(sesion.id(autenticado), req.aNuevaSugerencia())));
	}

	/**
	 * Campo y mensaje, igual que el alta de cuenta y el gesto de registro: los tres son formularios
	 * que llena una persona, y un 400 sin decir dónde está el error obliga a adivinar.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProblemDetail datosInvalidos(MethodArgumentNotValidException ex) {
		Map<String, String> porCampo = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			porCampo.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Revisá los datos de la sugerencia");
		problema.setProperty("errores", porCampo);
		return problema;
	}

	/** La sesión sigue viva pero la cuenta se borró: para quien escribe es no estar logueado. */
	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ProblemDetail sesionSinCuenta(AuthenticationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}
}
