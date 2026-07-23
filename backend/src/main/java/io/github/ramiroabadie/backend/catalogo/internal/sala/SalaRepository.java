package io.github.ramiroabadie.backend.catalogo.internal.sala;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51).
 */
interface SalaRepository extends JpaRepository<Sala, Long> {
}
