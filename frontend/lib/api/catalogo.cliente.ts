"use client";

import { apiGet, apiMutar } from "./client";
import type { NuevaSugerencia, Persona, ProduccionResumen, SugerenciaRecibida } from "./tipos";

/**
 * Catálogo, **lado navegador**: lo que se pide mientras alguien escribe y lo que manda un
 * formulario. Ver `catalogo.servidor.ts` para las lecturas del SSR y `identidad.servidor.ts`
 * para por qué el módulo está partido en dos archivos (D82).
 *
 * ⚠️ **Las salas no se buscan** (D23): se navegan desde las fichas. Que falte esa función no
 * es un hueco.
 */

/**
 * El autocompletado del gesto (HU-09) y la primera sección de la pantalla de búsqueda
 * (HU-07). Aguanta typos y títulos a medio escribir (`pg_trgm`, D65), devuelve **hasta 10 y
 * no pagina**, y **sin resultados responde `200` con `[]`, que no es un error**: es
 * justamente lo que necesita el camino a sugerir.
 */
export const buscarProducciones = (q: string) =>
  apiGet<ProduccionResumen[]>(`/api/buscar/producciones?q=${encodeURIComponent(q)}`);

export const buscarPersonas = (q: string) =>
  apiGet<Persona[]>(`/api/buscar/personas?q=${encodeURIComponent(q)}`);

/**
 * La válvula del catálogo cerrado (HU-08/D7/D24). **Pide sesión**, así que su `401` es el
 * del manejador global: manda a `/login?volver=/sugerir…` y al volver el formulario se
 * restaura solo desde el borrador.
 *
 * La respuesta **es** la confirmación de recibido (D69): no hay estado que consultar después.
 */
export const sugerir = (sugerencia: NuevaSugerencia) =>
  apiMutar<SugerenciaRecibida>("POST", "/api/sugerencias", sugerencia);
