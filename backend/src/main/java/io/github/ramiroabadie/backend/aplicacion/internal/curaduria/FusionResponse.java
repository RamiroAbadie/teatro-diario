package io.github.ramiroabadie.backend.aplicacion.internal.curaduria;

/**
 * Qué quedó después de fusionar: la ficha que sobrevive y cuántos registros se mudaron. El
 * número es para el admin, que hizo la operación a ciegas sobre dos ids y merece saber si movió
 * tres registros o trescientos.
 */
record FusionResponse(Long destinoId, String titulo, int registrosReasignados) {
}
