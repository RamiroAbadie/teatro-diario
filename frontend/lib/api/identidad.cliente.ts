"use client";

import { apiMutar } from "./client";
import type { Cuenta } from "./tipos";

/**
 * Identidad, **lado navegador**: las mutaciones. Ver `identidad.servidor.ts` para por qué
 * el módulo está partido en dos archivos.
 *
 * **Las tres rotan el token CSRF** (D57) y de releerlo se encarga `client.ts` antes de cada
 * mutación, así que ninguna pantalla tiene que acordarse. Lo que sí importa: una pantalla
 * **no puede encadenar una segunda mutación asumiendo el token viejo**.
 */

/** HU-01: el alta **deja la sesión abierta**, así que no hay que loguear después. */
export async function registro(datos: {
  username: string;
  email: string;
  password: string;
}): Promise<Cuenta> {
  return apiMutar<Cuenta>("POST", "/api/auth/registro", datos);
}

/**
 * HU-02. Un solo campo `identificador`: email **o** username.
 *
 * ⚠️ **Su `401` no va al manejador global, y es la razón por la que ese manejador tiene una
 * opción**: acá `401` es "esos datos no son", no "tu sesión venció". Redirigir al login
 * desde el login sería recargar la pantalla, perder lo tipeado y no mostrar nunca el mensaje
 * genérico que HU-02 pide (sin decir cuál de los dos campos falló).
 */
export async function login(datos: {
  identificador: string;
  password: string;
}): Promise<Cuenta> {
  return apiMutar<Cuenta>("POST", "/api/auth/login", datos, {
    alPerderLaSesion: "devolver-el-error",
  });
}

/**
 * Salir. Responde `204` **siempre**, con sesión o sin ella (`API.md`), así que el botón de
 * salir no tiene camino de error que dibujar.
 */
export async function logout(): Promise<void> {
  await apiMutar<null>("POST", "/api/auth/logout");
}
