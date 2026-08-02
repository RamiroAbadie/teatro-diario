import type { ReactNode } from "react";

import { IconoBuscar } from "./Iconos";

/**
 * `ui/8 · EstadoVacio` (D79). Los huecos que USER_FLOWS marcó como el lugar donde los MVP de
 * una persona siempre quedan flojos. **Título corto en serif, una línea de explicación, como
 * máximo un botón.**
 *
 * **Los estados vacíos hablan del usuario, no del sistema**: "Todavía no registraste nada",
 * no "No hay datos".
 *
 * ⚠️ De las cinco variantes están escritas las tres que tienen pantalla en este paso:
 * `informativo`, `error` y `no-encontrado`. `invitacion` (diario vacío) y `sin-resultados`
 * (búsqueda, con el texto tipeado adentro del botón) entran con las suyas, porque su copy
 * **es** su comportamiento.
 */

type Props = {
  titulo: string;
  children?: ReactNode;
  /**
   * Un botón, como máximo, y lo pone quien usa el componente: el "Reintentar" del `error`
   * necesita `reset()` —o sea una isla cliente— y `EstadoVacio` no tiene por qué serlo.
   */
  accion?: ReactNode;
  /** La búsqueda embebida del `404`: es la única salida que le queda a un link roto. */
  conBuscador?: boolean;
  /**
   * ⚠️ **`1` cuando el estado vacío ES la pantalla** —las dos de error de la pantalla 13—,
   * porque la accesibilidad transversal pide **un `<h1>` por pantalla** y ahí no hay ningún
   * otro título que pueda serlo. Por defecto es `2`: el caso normal es un bloque adentro de
   * una pantalla que ya tiene su `h1` (la ficha sin reseñas, la sala sin funciones).
   */
  nivel?: 1 | 2;
};

export function EstadoVacio({ titulo, children, accion, conBuscador = false, nivel = 2 }: Props) {
  const Titulo = nivel === 1 ? "h1" : "h2";

  return (
    <div className="rounded-lg border border-borde px-4 py-10 text-center">
      <Titulo className="font-titulo text-xl">{titulo}</Titulo>
      {children && <p className="mx-auto mt-2 max-w-[45ch] text-tinta-suave">{children}</p>}
      {accion && <div className="mt-6 flex justify-center">{accion}</div>}
      {conBuscador && <BuscadorEmbebido />}
    </div>
  );
}

/**
 * Un `form` de verdad contra `/buscar`: **funciona sin JavaScript** y por eso el componente
 * sigue siendo de servidor. Es la misma forma que el buscador de la cabecera ancha.
 */
function BuscadorEmbebido() {
  return (
    <form action="/buscar" className="mx-auto mt-6 flex max-w-sm items-center gap-2">
      <label htmlFor="q-vacio" className="sr-only">
        Buscar obras, artistas y usuarios
      </label>
      <input
        id="q-vacio"
        name="q"
        type="search"
        placeholder="Buscar"
        className="h-11 min-w-0 flex-1 rounded-md border border-borde-control bg-superficie px-3 text-base"
      />
      <button
        type="submit"
        aria-label="Buscar"
        className="inline-flex size-11 shrink-0 items-center justify-center rounded-md border border-borde-control"
      >
        <IconoBuscar />
      </button>
    </form>
  );
}
