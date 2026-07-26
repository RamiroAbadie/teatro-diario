package io.github.ramiroabadie.backend.aplicacion.internal.moderacion;

import java.time.Instant;
import java.util.Map;

import io.github.ramiroabadie.backend.diario.ActividadDeDiario;
import io.github.ramiroabadie.backend.diario.ProduccionRegistrada;
import io.github.ramiroabadie.backend.diario.RegistroDeDiario;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;
import io.github.ramiroabadie.backend.social.ReporteRecibido;

/**
 * Un ítem de la cola del admin (HU-22): el reporte, la reseña reportada y el contexto para poder
 * decidir sin abrir otra pantalla —quién la escribió, en qué obra y con qué puntaje—. Es la misma
 * composición que la cola de sugerencias (D69), con un módulo más: los ids que guarda Social son
 * opacos (D30) y el texto lo tiene Diario.
 *
 * <p>{@code texto} viaja nulo cuando la reseña ya no está: el autor pudo borrarla o editarla
 * (HU-11) mientras el reporte esperaba. El ítem sigue en la cola igual —hay que sacarlo de ahí—,
 * y verlo vacío es lo que le dice al admin que ya no hay nada que borrar.</p>
 *
 * <p>{@code autor} y {@code reportante} viajan nulos si esas cuentas ya no existen, como el
 * sugerente en la otra cola.</p>
 */
record ReporteEnColaResponse(Long id, Long reseniaId, String texto, String autor,
		ProduccionRegistrada produccion, Integer rating, String motivo, String reportante,
		Instant creadoEn) {

	static ReporteEnColaResponse desde(ReporteRecibido reporte, ActividadDeDiario resenia,
			Map<Long, UsuarioPublico> cuentas) {
		RegistroDeDiario registro = resenia == null ? null : resenia.registro();
		return new ReporteEnColaResponse(
				reporte.id(),
				reporte.reseniaId(),
				registro == null ? null : registro.resenia(),
				resenia == null ? null : username(cuentas.get(resenia.usuarioId())),
				registro == null ? null : registro.produccion(),
				registro == null ? null : registro.rating(),
				reporte.motivo(),
				username(cuentas.get(reporte.reportanteId())),
				reporte.creadoEn());
	}

	private static String username(UsuarioPublico cuenta) {
		return cuenta == null ? null : cuenta.username();
	}
}
