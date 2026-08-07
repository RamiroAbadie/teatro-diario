"use client";

import { apiMutar } from "./client";
import type { NuevoRegistro, RegistroDeDiario } from "./tipos";

/**
 * Diario, **lado navegador**: el gesto y sus correcciones (HU-09/10/11). Ver
 * `diario.servidor.ts` para las lecturas del SSR.
 *
 * ⚠️ **Los tres piden sesión**, así que su `401` es el del manejador global: manda a
 * `/login?volver=…` y al volver la hoja se reabre con el borrador (D80).
 */

/**
 * Registrar. **Registrar la misma obra otra vez es válido y esperado**: es el re-visto de
 * D19, no un duplicado a evitar — el backend no avisa nada y la pantalla tampoco pregunta.
 *
 * ⚠️ Su **`404` no es un error**: significa que la producción no está en el catálogo, que es
 * cerrado (D7), y **ése es el camino a sugerir** (HU-08), sin perder lo tipeado.
 */
export const crearRegistro = (registro: NuevoRegistro) =>
  apiMutar<RegistroDeDiario>("POST", "/api/registros", registro);

/** El mismo cuerpo que crear: editar reemplaza el gesto entero, incluida la obra. */
export const editarRegistro = (id: number, registro: NuevoRegistro) =>
  apiMutar<RegistroDeDiario>("PUT", `/api/registros/${id}`, registro);

/** El promedio de la producción se recalcula solo (D20). La confirmación es de la pantalla. */
export const borrarRegistro = (id: number) =>
  apiMutar<null>("DELETE", `/api/registros/${id}`);
