import { cookies } from "next/headers";

import { aErrorDeApi, FalloDeApi } from "./errores";

/**
 * El `fetch` de los Server Components. Acá no hay navegador: la cookie de sesión **hay
 * que reenviarla a mano**, que es el error clásico de Next con sesiones y por eso vive en
 * un solo lugar (D78).
 *
 * ⚠️ **Lo que decide la caché es la llamada, no el endpoint.** Son dos clientes distintos
 * y confundirlos sirve la página de una persona a otra:
 *
 *   apiPublic   → sin cookie, cacheable con TTL
 *   apiSession  → reenvía cookie, no-store SIEMPRE
 *
 * `GET /api/producciones/{id}/opiniones` y `GET /api/usuarios/{username}` son abiertos
 * PERO cambian de respuesta con cookie: los pide uno u otro cliente según haya sesión, y
 * si hay sesión **todo el árbol de la página** va por `apiSession`.
 */

/** El servidor de Next usa la URL interna del Compose. Nunca rutas relativas. */
const BASE = process.env.BACKEND_INTERNAL_URL ?? "http://localhost:8080";

/** Los dos TTL de la tabla de D78, con nombre para que no se escriban a ojo. */
export const TTL_CONTENIDO_VIVO = 60; // ficha, opiniones, perfil: entran reseñas, cambian promedios
export const TTL_CATALOGO = 300; // en cartel, artista, sala: sólo cambian cuando el admin toca

/** Sin cookie y cacheable. Para el visitante anónimo y para lo que no se personaliza. */
export async function apiPublic<T>(ruta: string, revalidate: number): Promise<T> {
  const respuesta = await fetch(BASE + ruta, {
    headers: { Accept: "application/json" },
    next: { revalidate },
  });
  return leer<T>(respuesta);
}

/**
 * Reenvía la cookie y **jamás cachea**. Para lo que exige sesión (`/api/feed`,
 * `/api/auth/yo`, todo el panel) y para las lecturas abiertas pedidas con sesión.
 */
export async function apiSession<T>(ruta: string): Promise<T> {
  const cookie = (await cookies()).toString();
  const respuesta = await fetch(BASE + ruta, {
    headers: {
      Accept: "application/json",
      ...(cookie ? { Cookie: cookie } : {}),
    },
    cache: "no-store",
  });
  return leer<T>(respuesta);
}

async function leer<T>(respuesta: Response): Promise<T> {
  if (!respuesta.ok) throw new FalloDeApi(await aErrorDeApi(respuesta));
  if (respuesta.status === 204) return null as T;

  const texto = await respuesta.text();
  return (texto ? JSON.parse(texto) : null) as T;
}
