"use client";

import { apiMutar } from "./client";

/**
 * Identidad, **lado navegador**: las mutaciones. Ver `identidad.servidor.ts` para por qué
 * el módulo está partido en dos archivos.
 *
 * Todavía faltan `registro` y `login`, que entran con la pantalla 11 (paso 4 de la Fase 4).
 * Las tres rotan el token CSRF (D57), y de releerlo se encarga `client.ts` antes de cada
 * mutación.
 */

/**
 * Salir. Responde `204` **siempre**, con sesión o sin ella (`API.md`), así que el botón de
 * salir no tiene camino de error que dibujar.
 */
export async function logout(): Promise<void> {
  await apiMutar<null>("POST", "/api/auth/logout");
}
