import { personasQuePuntuaron, promedioLocal } from "@/lib/formato";

/**
 * `ui/4 · Puntaje` (D79). El entero 1-10 de D9 y el promedio con un decimal de D20.
 *
 * **Dos decisiones que viven acá adentro:**
 * - **El "/10" aparece una sola vez por pantalla, con el promedio.** Repetirlo en veinte
 *   filas del feed es ruido, y el que llega de WhatsApp lo aprende en la ficha, que es donde
 *   cae.
 * - **El puntaje no se colorea según el valor.** Nada de rojo para el 3 y verde para el 9:
 *   es una opinión, no un indicador, y teñirla es que el producto opine sobre la opinión.
 *
 * ⚠️ **Sin valor no se renderiza nada** —ni guión, ni "s/p", ni un espacio reservado—: el
 * que sabe qué decir en ese hueco es la pantalla, no el componente.
 */

/** El puntaje de un registro: el entero solo. `tabular-nums` para que la columna no baile. */
export function Puntaje({ rating }: { rating: number | null }) {
  if (rating === null) return null;

  return (
    <span className="inline-flex h-8 min-w-8 items-center justify-center rounded-md border border-borde bg-superficie px-2 text-base tabular-nums">
      {rating}
    </span>
  );
}

/**
 * El promedio de la ficha: **el último rating de cada usuario** (D20), nunca un `AVG`, con
 * un decimal y **coma decimal**.
 *
 * ⚠️ `promedio: null` **no es "0"**: 0 no existe como puntaje y mentiría. Se dice que
 * todavía nadie puntuó, que es otra cosa.
 */
export function PuntajePromedio({
  promedio,
  cantidadRatings,
}: {
  promedio: number | null;
  cantidadRatings: number;
}) {
  if (promedio === null) {
    return <p className="text-tinta-tenue">Todavía nadie puntuó</p>;
  }

  return (
    <p className="flex flex-wrap items-baseline gap-x-2">
      <span className="font-titulo text-3xl tabular-nums">{promedioLocal(promedio)}</span>
      <span className="text-tinta-suave">/10</span>
      <span className="text-sm text-tinta-suave">· {personasQuePuntuaron(cantidadRatings)}</span>
    </p>
  );
}
