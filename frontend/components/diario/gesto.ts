/**
 * El gesto de registro (pantalla 9) **es una hoja y no una ruta** (D80): se abre desde
 * tres lugares —el botón persistente del armazón, el CTA de la ficha y "Registrar de
 * nuevo"— y en dos de ellos el contexto de atrás importa, así que una ruta obligaría a
 * cargar de nuevo la pantalla al volver.
 *
 * Tres disparadores repartidos por el árbol y una sola hoja en `app/(sitio)/layout.tsx`
 * necesitan alguna
 * forma de hablarse. Esto es la más barata que no agrega nada: un evento del DOM. **Sin
 * librería de estado global** (D78) y sin subir el estado del gesto al layout, que
 * obligaría a que el armazón entero fuera cliente.
 *
 * ⚠️ **Hoy nadie escucha**: la hoja se escribe en el paso 4 de la Fase 4. Este archivo es
 * el punto de enganche, no el gesto.
 */

export const EVENTO_ABRIR_GESTO = "teatro:abrir-gesto";

/** Opcionalmente con la obra ya elegida: desde la ficha, el gesto no vuelve a buscarla. */
export type DetalleDelGesto = { produccionId?: number; titulo?: string };

export function abrirGesto(detalle: DetalleDelGesto = {}): void {
  document.dispatchEvent(new CustomEvent(EVENTO_ABRIR_GESTO, { detail: detalle }));
}
