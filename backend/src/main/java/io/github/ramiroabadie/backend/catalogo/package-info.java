/**
 * Módulo Catálogo: entidades teatrales y su curaduría (panel admin + sugerencias).
 * Autoridad sobre producciones, personas, participaciones, salas y estados.
 * No depende de ningún otro módulo (MODULE_MAP.md).
 */
@ApplicationModule(
		displayName = "Catálogo",
		allowedDependencies = {}
)
package io.github.ramiroabadie.backend.catalogo;

import org.springframework.modulith.ApplicationModule;
