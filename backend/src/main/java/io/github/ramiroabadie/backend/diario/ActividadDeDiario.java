package io.github.ramiroabadie.backend.diario;

/**
 * Una línea de actividad: alguien registró algo. Es el insumo del feed (D29), que arma la capa de
 * aplicación — Diario aporta qué pasó y de quién, y no sabe ni cómo se llama esa persona.
 *
 * <p>Es {@link RegistroDeDiario} con el autor al lado, y no un tipo nuevo con los mismos campos:
 * lo que se muestra en el feed es exactamente una línea del diario de otro.</p>
 *
 * @param usuarioId el autor, opaco (D30): el username lo pone quien pueda preguntarle a Identidad
 */
public record ActividadDeDiario(Long usuarioId, RegistroDeDiario registro) {
}
