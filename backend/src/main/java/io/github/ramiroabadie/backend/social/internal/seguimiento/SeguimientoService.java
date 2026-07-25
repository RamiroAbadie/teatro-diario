package io.github.ramiroabadie.backend.social.internal.seguimiento;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.ramiroabadie.backend.social.ContadoresSociales;
import io.github.ramiroabadie.backend.social.GrafoSocial;
import io.github.ramiroabadie.backend.social.SeguimientoInvalidoException;

/**
 * Los casos de uso del grafo (HU-15) y la única implementación de la interfaz pública del módulo.
 * Es tan chico como parece: seguir es insertar una fila y el resto es contar.
 *
 * <p>No valida que las cuentas existan, y no es un olvido: Social no depende de Identidad
 * (MODULE_MAP) y no tiene con qué preguntarlo. Quien llama —la capa de aplicación— llega hasta
 * acá con dos ids que resolvió desde la sesión y desde la URL del perfil, así que ya sabe que
 * existen.</p>
 */
@Service
class SeguimientoService implements GrafoSocial {

	private final SeguimientoRepository repositorio;

	SeguimientoService(SeguimientoRepository repositorio) {
		this.repositorio = repositorio;
	}

	/**
	 * El chequeo previo evita la excepción en el caso normal de volver a tocar el botón; la
	 * carrera de dos clics a la vez la resuelve el índice único de la tabla, y quien llama la
	 * traduce a "ya lo seguías".
	 */
	@Override
	@Transactional
	public void seguir(Long seguidorId, Long seguidoId) {
		if (seguidorId.equals(seguidoId)) {
			throw new SeguimientoInvalidoException("Nadie se sigue a sí mismo");
		}
		if (repositorio.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId)) {
			return;
		}
		repositorio.save(new Seguimiento(seguidorId, seguidoId));
	}

	/** Borrar lo que no está no es un error: el resultado que se pedía ya es el que hay. */
	@Override
	@Transactional
	public void dejarDeSeguir(Long seguidorId, Long seguidoId) {
		repositorio.deleteBySeguidorIdAndSeguidoId(seguidorId, seguidoId);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean sigue(Long seguidorId, Long seguidoId) {
		return repositorio.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> seguidosPor(Long usuarioId) {
		return repositorio.seguidosPor(usuarioId);
	}

	@Override
	@Transactional(readOnly = true)
	public ContadoresSociales contadoresDe(Long usuarioId) {
		return new ContadoresSociales(repositorio.countBySeguidoId(usuarioId),
				repositorio.countBySeguidorId(usuarioId));
	}
}
