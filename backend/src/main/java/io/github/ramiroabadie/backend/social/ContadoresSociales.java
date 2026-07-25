package io.github.ramiroabadie.backend.social;

/**
 * Los contadores del perfil (HU-15): cuánta gente lo sigue y a cuánta sigue.
 *
 * @param seguidores personas que siguen a esta cuenta
 * @param seguidos personas a las que esta cuenta sigue
 */
public record ContadoresSociales(long seguidores, long seguidos) {
}
