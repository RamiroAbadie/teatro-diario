import Link from "next/link";
import type { ReactNode } from "react";

import { Afiche } from "./Afiche";

/**
 * `ui/2 · Fila` (D79). **La unidad del eje tipográfico de D71**: es lo que hace que el
 * producto funcione sin una sola imagen. Una línea de título, una línea de meta, un puntaje
 * a la derecha y un cuerpo opcional abajo.
 *
 * **Entre filas no hay margen: las separa un `border-t`** (D79), así que la lista es un
 * `<ul>` y cada fila un `<li>` — listas reales, que es lo que pide la accesibilidad de las
 * 13 pantallas.
 *
 * ⚠️ **De las cuatro variantes están escritas dos**, las que tienen pantalla: `resultado`
 * (artista, sala y después búsqueda) y `feed` (las reseñas de la ficha). `diario` y `admin`
 * entran con el perfil y el panel — escribirlas ahora sería adivinar su meta y sus acciones,
 * que es la mitad de cada variante. Es la misma regla con la que `Boton` todavía no tiene
 * `alternante`.
 */

/**
 * `resultado`: título, meta y miniatura. Toda la fila es el link, que en un celular es la
 * diferencia entre acertarle al título y acertarle a la fila.
 */
export function FilaResultado({
  href,
  titulo,
  meta,
  chip,
  aficheUrl,
}: {
  href: string;
  titulo: string;
  meta?: string | null;
  chip?: ReactNode;
  aficheUrl?: string | null;
}) {
  return (
    <li className="border-t border-borde">
      <Link href={href} className="flex items-center gap-3 py-3 transition-colors duration-150 hover:bg-acento-suave">
        <Afiche aficheUrl={aficheUrl} titulo={titulo} variante="miniatura" />
        <span className="min-w-0 flex-1">
          <span className="block truncate text-base">{titulo}</span>
          {meta && <span className="block truncate text-sm text-tinta-suave">{meta}</span>}
        </span>
        {chip}
      </Link>
    </li>
  );
}

/**
 * `feed`: la firma de quien escribe, la fecha difusa, el puntaje y la reseña. En la ficha va
 * **sin el título de la obra** —ya estamos en ella— y por eso `titulo` es opcional.
 *
 * ⚠️ **Una fila sin cuerpo es legítima y no reserva alto**: un registro sin reseña sigue
 * siendo alguien que fue al teatro (D66/D70). Y `acciones` es un hueco a propósito: el
 * corazón y el menú de reportar son islas cliente que entran con HU-17/18 (paso 5), y `ui/`
 * no tiene por qué saber qué es un like.
 */
export function FilaDeOpinion({
  firma,
  titulo,
  href,
  fecha,
  puntaje,
  cuerpo,
  pie,
  acciones,
}: {
  firma: ReactNode;
  titulo?: string;
  href?: string;
  fecha?: string | null;
  puntaje?: ReactNode;
  cuerpo?: string | null;
  pie?: ReactNode;
  acciones?: ReactNode;
}) {
  return (
    <li className="border-t border-borde py-4">
      <div className="flex items-start gap-3">
        <div className="min-w-0 flex-1">
          {firma}
          {titulo &&
            (href ? (
              <Link href={href} className="mt-1 block text-acento-tinta">
                {titulo}
              </Link>
            ) : (
              <span className="mt-1 block">{titulo}</span>
            ))}
          {fecha && <p className="mt-1 text-sm text-tinta-suave">{fecha}</p>}
        </div>
        {puntaje}
      </div>

      {/* El cuerpo se limita a 65 caracteres por línea aunque la columna sea más ancha. */}
      {cuerpo && <p className="mt-2 max-w-[65ch] text-base whitespace-pre-line">{cuerpo}</p>}

      {(pie || acciones) && (
        <div className="mt-2 flex items-center gap-4 text-sm text-tinta-suave">
          {pie}
          {acciones}
        </div>
      )}
    </li>
  );
}
