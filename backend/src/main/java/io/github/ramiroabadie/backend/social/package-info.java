/**
 * Módulo Social: el grafo social y las interacciones sobre contenido.
 * Autoridad sobre follows y likes a reseñas (referencia user_id y reseña_id como IDs opacos).
 * No depende de ningún otro módulo (MODULE_MAP.md).
 */
@ApplicationModule(
		displayName = "Social",
		allowedDependencies = {}
)
package io.github.ramiroabadie.backend.social;

import org.springframework.modulith.ApplicationModule;
