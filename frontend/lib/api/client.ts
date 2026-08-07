"use client";

import { aErrorDeApi, FalloDeApi } from "./errores";

/**
 * El `fetch` del navegador contra Spring. Mismo origen detrás de Caddy (y detrás del
 * rewrite de `next.config.ts` en desarrollo), así que la cookie `JSESSIONID` viaja sola:
 * lo único que hay que hacer a mano es CSRF (D57/D78).
 *
 * Las mutaciones van del navegador a Spring, directo. **Sin Server Actions**: agregarían
 * un salto que tendría que reenviar cookie y token para llamar al mismo endpoint, y sobre
 * todo abrirían un lugar cómodo donde la lógica de negocio se acumula sin que se note,
 * que es lo que D34 prohíbe.
 */

type Metodo = "POST" | "PUT" | "PATCH" | "DELETE";

/**
 * Qué hacer con el `401`, que es lo único que una llamada puede necesitar decidir por su
 * cuenta. **El código no alcanza: hace falta el contexto** (D78).
 *
 * El caso que obliga a que esto exista es el login: `POST /api/auth/login` responde `401`
 * cuando las credenciales están mal, y ahí `401` **no significa "tu sesión venció"** sino
 * "esos datos no son". Mandarlo al manejador global sería recargar la pantalla de login
 * desde la pantalla de login, perdiendo lo tipeado y sin llegar a mostrar nunca el mensaje
 * genérico que HU-02 pide.
 */
type Opciones = { alPerderLaSesion?: "ir-al-login" | "devolver-el-error" };

/** Lectura desde el navegador (búsqueda, paginado del feed). No necesita token. */
export async function apiGet<T>(ruta: string): Promise<T> {
  return leer<T>(await fetch(ruta, { headers: { Accept: "application/json" }, cache: "no-store" }));
}

/**
 * Toda escritura. Resuelve el token CSRF **antes de mandar** y, si el `403` igual llega,
 * relee la cookie y reintenta **una sola vez**: un token que no sirve dos veces seguidas
 * no se arregla insistiendo.
 *
 * No hace falta releer nada a mano después de `registro`, `login` y `logout` —donde el
 * token se rota (D57)—: cada mutación vuelve a leer la cookie.
 */
export async function apiMutar<T>(
  metodo: Metodo,
  ruta: string,
  cuerpo?: unknown,
  { alPerderLaSesion = "ir-al-login" }: Opciones = {},
): Promise<T> {
  const token = await tokenCsrf();

  // Regla 3 de la semilla: la mutación NO se dispara a ciegas. Sin token es un `403`
  // garantizado y, peor, un envío que el usuario cree hecho.
  if (!token) throw new FalloDeApi({ status: 0, mensaje: SIN_TOKEN });

  const primera = await enviar(metodo, ruta, cuerpo, token);
  if (primera.status !== 403) return leer<T>(primera, alPerderLaSesion);

  // Regla 5: se relee la cookie —el `403` pudo traer una nueva— y se reintenta UNA vez.
  const relectura = await tokenCsrf();
  if (!relectura) return leer<T>(primera, alPerderLaSesion);

  return leer<T>(await enviar(metodo, ruta, cuerpo, relectura), alPerderLaSesion);
}

const SIN_TOKEN = "No pudimos preparar el envío. Revisá la conexión y probá de nuevo.";

function enviar(metodo: Metodo, ruta: string, cuerpo: unknown, token: string) {
  return fetch(ruta, {
    method: metodo,
    headers: {
      Accept: "application/json",
      "X-XSRF-TOKEN": token,
      ...(cuerpo === undefined ? {} : { "Content-Type": "application/json" }),
    },
    ...(cuerpo === undefined ? {} : { body: JSON.stringify(cuerpo) }),
  });
}

/**
 * El agujero que sólo aparece con SSR: en una página renderizada en el servidor el
 * navegador puede no haber hablado nunca con Spring, así que no tiene la cookie
 * `XSRF-TOKEN` y la primera mutación se come un `403`. Le pasa al visitante que entra por
 * un link compartido y lo primero que hace es crear una cuenta — el Flujo 1 entero.
 *
 * Se resuelve **perezosamente, antes de la mutación** y no al montar la app: así no hay
 * una llamada extra en cada visita anónima, que son casi todas, y cubre gratis los otros
 * dos casos donde el token falta o quedó viejo (expiró, o se rotó en una de las tres
 * puertas de la sesión).
 */
async function tokenCsrf(): Promise<string | null> {
  const guardado = leerCookie("XSRF-TOKEN");
  if (guardado) return guardado;

  await sembrarToken();
  return leerCookie("XSRF-TOKEN"); // regla 2: se relee, no se asume que está
}

/**
 * Semilla: `GET /api/auth/yo`, la respuesta más chica de la API. Lo que se va a buscar no
 * es el cuerpo sino el `Set-Cookie`.
 *
 * ⚠️ Va con un `fetch` desnudo **a propósito**: su `401` significa "anónimo", que es un
 * resultado aceptable y no un fallo, y **no puede pasar por el manejador global de `401`**
 * de `leer()`. Si pasara, el visitante anónimo que va a crear una cuenta terminaría
 * rebotado al login antes de poder crearla — el Flujo 1 roto por la misma pieza que venía
 * a arreglarlo.
 */
async function sembrarToken(): Promise<void> {
  try {
    await fetch("/api/auth/yo", { headers: { Accept: "application/json" }, cache: "no-store" });
  } catch {
    // Sin red no hay token. Lo resuelve quien llamó, al ver que sigue sin cookie.
  }
}

function leerCookie(nombre: string): string | null {
  const prefijo = `${nombre}=`;
  for (const trozo of document.cookie.split("; ")) {
    if (trozo.startsWith(prefijo)) return decodeURIComponent(trozo.slice(prefijo.length));
  }
  return null;
}

async function leer<T>(
  respuesta: Response,
  alPerderLaSesion: NonNullable<Opciones["alPerderLaSesion"]> = "ir-al-login",
): Promise<T> {
  if (respuesta.ok) {
    if (respuesta.status === 204) return null as T;
    const texto = await respuesta.text();
    return (texto ? JSON.parse(texto) : null) as T;
  }

  const error = await aErrorDeApi(respuesta);

  // El manejador global del `401`: sesión ausente o vencida en algo protegido. No es "un
  // error del servidor". Se vuelve a donde estaba, y lo tipeado sobrevive en
  // `sessionStorage` (D80). Los dos `401` que NO pasan por acá son los que significan otra
  // cosa: el de "¿hay alguien?" —la semilla, que va con un `fetch` desnudo— y el de las
  // credenciales del login, que lo pide con `devolver-el-error`.
  if (error.status === 401 && alPerderLaSesion === "ir-al-login") irALogin();

  throw new FalloDeApi(error);
}

function irALogin(): void {
  const volver = window.location.pathname + window.location.search;
  window.location.assign(`/login?volver=${encodeURIComponent(volver)}`);
}
