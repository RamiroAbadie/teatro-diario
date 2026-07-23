/**
 * Capa de aplicación: casos de uso, autorización transversal y composición de lecturas.
 * Aquí vive el feed (composición, D29), no como módulo ni tabla materializada.
 * Compone a todos los módulos; ninguna lógica de negocio vive en controladores ni en el front.
 */
@ApplicationModule(
		displayName = "Aplicación",
		allowedDependencies = { "identidad", "catalogo", "diario", "social" }
)
package io.github.ramiroabadie.backend.aplicacion;

import org.springframework.modulith.ApplicationModule;
