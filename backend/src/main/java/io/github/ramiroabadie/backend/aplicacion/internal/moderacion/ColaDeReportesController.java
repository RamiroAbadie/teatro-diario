package io.github.ramiroabadie.backend.aplicacion.internal.moderacion;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.ramiroabadie.backend.diario.ActividadDeDiario;
import io.github.ramiroabadie.backend.diario.Diario;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;
import io.github.ramiroabadie.backend.identidad.Usuarios;
import io.github.ramiroabadie.backend.social.ReporteNoEncontradoException;
import io.github.ramiroabadie.backend.social.ReporteRecibido;
import io.github.ramiroabadie.backend.social.ReporteResueltoException;
import io.github.ramiroabadie.backend.social.ReportesDeResenias;

/**
 * La cola de reportes (HU-22, D40), la otra parada de la rutina semanal del admin. Cuelga de
 * {@code /api/admin} como el resto del panel, así que el candado de rol lo pone
 * {@code SecurityConfig} sin ninguna regla nueva.
 *
 * <p>Está en la capa de aplicación porque es una composición de tres módulos que no se conocen
 * (D60, mismo caso que la cola de sugerencias): Social tiene los reportes con ids opacos, Diario
 * el texto reportado y su contexto, e Identidad los nombres. Son tres consultas para la cola
 * entera —los reportes, los registros, las cuentas— más la que Diario le hace a Catálogo por los
 * títulos: ninguna por fila, que es la regla que viene desde D66.</p>
 *
 * <p>Las dos acciones responden 204 y el panel vuelve a pedir la cola, que es donde se ve el
 * efecto. Y las dos sacan de la cola todos los reportes de esa reseña, no solo el que se tocó
 * (D70): el veredicto es sobre el texto.</p>
 */
@RestController
@RequestMapping("/api/admin/reportes")
class ColaDeReportesController {

	private final ReportesDeResenias reportes;

	private final Diario diario;

	private final Usuarios usuarios;

	private final ResolucionDeReportes resolucion;

	ColaDeReportesController(ReportesDeResenias reportes, Diario diario, Usuarios usuarios,
			ResolucionDeReportes resolucion) {
		this.reportes = reportes;
		this.diario = diario;
		this.usuarios = usuarios;
		this.resolucion = resolucion;
	}

	@GetMapping
	public List<ReporteEnColaResponse> pendientes() {
		List<ReporteRecibido> pendientes = reportes.pendientes();
		Map<Long, ActividadDeDiario> resenias = diario
				.registrosPorIds(pendientes.stream().map(ReporteRecibido::reseniaId).distinct().toList())
				.stream()
				.collect(Collectors.toMap(actividad -> actividad.registro().id(), Function.identity()));
		Map<Long, UsuarioPublico> cuentas = usuarios.porIds(Stream.concat(
				pendientes.stream().map(ReporteRecibido::reportanteId),
				resenias.values().stream().map(ActividadDeDiario::usuarioId)).distinct().toList());
		return pendientes.stream()
				.map(reporte -> ReporteEnColaResponse.desde(reporte, resenias.get(reporte.reseniaId()), cuentas))
				.toList();
	}

	@PostMapping("/{id}/borrar-resenia")
	public ResponseEntity<Void> borrarResenia(@PathVariable Long id) {
		resolucion.borrarResenia(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/desestimar")
	public ResponseEntity<Void> desestimar(@PathVariable Long id) {
		resolucion.desestimar(id);
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(ReporteNoEncontradoException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail noEncontrado(ReporteNoEncontradoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	/**
	 * La cola abierta en dos pestañas, o dos reportes de la misma reseña resueltos de a uno: el
	 * segundo intento llega tarde, no está roto.
	 */
	@ExceptionHandler(ReporteResueltoException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ProblemDetail yaResuelto(ReporteResueltoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}
}
