package io.github.ramiroabadie.backend.diario;

/**
 * Cuántas veces fue al teatro alguien en un año (D26). Cuenta registros, no producciones
 * distintas: ver dos veces la misma obra en 2024 son dos salidas, y el diario cuenta salidas.
 */
public record ObrasPorAnio(int anio, long cantidad) {
}
