/**
 * Módulo Identidad: cuentas, autenticación y perfil.
 * Autoridad sobre usuarios (credenciales y datos de perfil).
 * No depende de ningún otro módulo (MODULE_MAP.md).
 */
@ApplicationModule(
		displayName = "Identidad",
		allowedDependencies = {}
)
package io.github.ramiroabadie.backend.identidad;

import org.springframework.modulith.ApplicationModule;
