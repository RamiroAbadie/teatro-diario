import type { RegistroDeDiario } from "@/lib/api/tipos";

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
 */

export const EVENTO_ABRIR_GESTO = "teatro:abrir-gesto";

/**
 * Con qué se abre la hoja, que son tres casos y ninguno inventa un campo:
 *
 * - **vacía** (el botón persistente): hay que buscar la obra.
 * - **con la obra ya elegida** (el CTA de la ficha, "Registrar de nuevo"): el gesto no
 *   vuelve a buscar lo que ya se está mirando.
 * - **con un registro entero** (editar, HU-11): el formulario llega precargado con lo que la
 *   pantalla ya tiene del diario o del feed, porque **no hay `GET` de un registro suelto**
 *   (hueco 6 de `API.md`).
 */
export type DetalleDelGesto = {
  produccionId?: number;
  titulo?: string;
  registro?: RegistroDeDiario;
};

export function abrirGesto(detalle: DetalleDelGesto = {}): void {
  document.dispatchEvent(new CustomEvent(EVENTO_ABRIR_GESTO, { detail: detalle }));
}
