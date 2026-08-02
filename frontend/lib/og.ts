/**
 * La placa tipográfica de `og:image` (D79), del lado de quien la pide.
 *
 * **Por qué una ruta propia y no `opengraph-image.tsx`**: el archivo convencional de Next
 * gana sobre lo que devuelva `generateMetadata`, y la regla de D79 es que **cuando la ficha
 * tenga afiche, el `og:image` es el afiche tal cual** (ya es una URL absoluta, estática e
 * inmutable: no hay nada que generar). Con el archivo convencional esa elección no se puede
 * escribir; con una ruta que se nombra desde `generateMetadata`, sí — y queda en una sola
 * línea legible al lado del `??`.
 *
 * `/og` es un prefijo de la app y no de Caddy: los que quedan quemados son `/api` y
 * `/afiches` (D78), no éste.
 */

/** Los tres que tienen pantalla hoy. El perfil (`usuario`) entra con la pantalla 8. */
export type TipoDePlaca = "obra" | "artista" | "sala";

export const rutaDeLaPlaca = (tipo: TipoDePlaca, id: number | string) => `/og/${tipo}/${id}`;
