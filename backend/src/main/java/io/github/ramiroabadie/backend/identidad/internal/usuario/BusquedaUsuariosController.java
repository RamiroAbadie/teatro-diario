package io.github.ramiroabadie.backend.identidad.internal.usuario;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.identidad.UsuarioPublico;

/**
 * La tercera búsqueda de HU-07: la de usuarios. Vive en Identidad, que es quien tiene las
 * cuentas — cada módulo busca sobre lo suyo (D23) y nadie compone una búsqueda global en la
 * capa de aplicación: no hace falta, porque ninguna de las tres necesita datos de otro módulo.
 *
 * <p>Controlador propio y no un método más de {@code AuthController} porque no es
 * autenticación: buscar gente se hace sin cuenta, como todo lo de leer (D21). El perfil que
 * está del otro lado de cada resultado sí es composición, y por eso lo sirve la capa de
 * aplicación ({@code /api/usuarios/{username}}, D60).</p>
 */
@RestController
class BusquedaUsuariosController {

	private final UsuarioService servicio;

	BusquedaUsuariosController(UsuarioService servicio) {
		this.servicio = servicio;
	}

	@GetMapping("/api/buscar/usuarios")
	public List<UsuarioPublico> buscar(@RequestParam String q) {
		return servicio.buscar(q);
	}
}
