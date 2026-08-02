/**
 * Construir y parsear las URLs de D74/D75. **Nadie construye una URL a mano.**
 *
 * El id es lo único que se parsea; el slug es decorativo y no viaja nunca al backend.
 */

export const rutaObra = (id: number, titulo: string) => `/obra/${id}-${slugify(titulo)}`;
export const rutaArtista = (id: number, nombre: string) => `/artista/${id}-${slugify(nombre)}`;
export const rutaSala = (id: number, nombre: string) => `/sala/${id}-${slugify(nombre)}`;

/** El perfil vive bajo `/usuario/` justamente para no competir con la raíz (D75). */
export const rutaUsuario = (username: string) => `/usuario/${username}`;

/** El único link que sale del sitio: el código, que la AGPL obliga a ofrecer (D46). */
export const REPO = "https://github.com/RamiroAbadie/teatro-diario";

/**
 * `null` si el segmento no empieza con un entero positivo: la ruta no existe.
 *
 * ⚠️ **Con una expresión regular y no con `Number`.** El problema de `Number` no es que
 * devuelva basura —`Number("hola")` es `NaN` y eso se ve venir—: es que **acepta de más**,
 * y cada forma que acepta es una URL distinta que sirve la misma ficha. `12.0`, `1e2`,
 * `+12`, `012`, `0x0c` y `%2012` responderían `200` con contenido duplicado para Google y
 * links que envejecen distinto del canónico. El `/^[1-9]\d*$/` las descarta todas de una.
 */
export function idDesdeSlug(slug: string): number | null {
  const [cabeza] = slug.split("-");
  return /^[1-9]\d*$/.test(cabeza) ? Number(cabeza) : null;
}

/** Sin dependencias: el slug es decorativo, no tiene que ser reversible ni perfecto. */
export function slugify(texto: string): string {
  return texto
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "") // los diacríticos que NFD acaba de separar
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}
