package io.github.ramiroabadie.backend.diario;

import java.util.List;

/**
 * El diario completo de una persona (HU-12), en dos listas y no en una: los registros con fecha
 * van cronológicos descendentes y los que no tienen fecha van en una sección propia al final
 * (MD-2). Separarlos acá y no en el cliente evita la tentación de inventarles una fecha para
 * que ordenen.
 *
 * <p>Las estadísticas (HU-13) viajan en la misma respuesta porque salen de los mismos registros:
 * son un bloque del perfil, no una pantalla aparte.</p>
 */
public record DiarioDeUsuario(
		List<RegistroDeDiario> registros,
		List<RegistroDeDiario> sinFecha,
		EstadisticasDeDiario estadisticas
) {
}
