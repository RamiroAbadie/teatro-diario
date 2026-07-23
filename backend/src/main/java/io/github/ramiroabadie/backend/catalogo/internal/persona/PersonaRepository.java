package io.github.ramiroabadie.backend.catalogo.internal.persona;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Solo la interfaz: Spring Data genera la implementación (MODO ESENCIAL, D51).
 */
interface PersonaRepository extends JpaRepository<Persona, Long> {
}
