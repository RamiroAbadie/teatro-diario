/**
 * Módulo Diario: todo lo que un usuario dice de una producción (el corazón del producto).
 * Autoridad sobre registros, ratings, reseñas, promedio por producción (D20) y stats (D26).
 * Única dependencia módulo-a-módulo del sistema: Diario → Catálogo (MODULE_MAP.md).
 */
@ApplicationModule(
		displayName = "Diario",
		allowedDependencies = { "catalogo" }
)
package io.github.ramiroabadie.backend.diario;

import org.springframework.modulith.ApplicationModule;
