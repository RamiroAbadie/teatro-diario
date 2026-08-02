import Link from "next/link";
import type { ReactNode } from "react";

/**
 * `ui/1 · Tarjeta` (D79). El bloque de superficie con borde. **Es el contenedor y nada más:
 * no sabe qué hay adentro.**
 *
 * Se separa con `border` de 1 px y no con elevación: sobre `#121110` una sombra no existe,
 * así que una interfaz construida con sombras se aplana entera al cambiar de tema. La única
 * sombra del sistema es la de lo que flota (la hoja del gesto y `Confirmacion`).
 */

const BASE = "rounded-lg border border-borde bg-superficie";

/** `plana`: un bloque de contenido. Stats del perfil, columna de datos de la ficha. */
export function Tarjeta({ children, className = "" }: { children: ReactNode; className?: string }) {
  return <div className={`${BASE} p-4 ${className}`.trim()}>{children}</div>;
}

/**
 * `enlazada` + `grilla`: toda la celda es un link —afiche arriba, pie tipográfico abajo—.
 *
 * ⚠️ **El hover cambia `border-color` y nada más: nunca escala ni levanta.** Es la regla de
 * movimiento de D79 (150 ms, sólo color/fondo/borde/opacidad), y en una grilla de veinte
 * celdas es la diferencia entre una página quieta y una que respira sola.
 */
export function TarjetaEnlazada({
  href,
  children,
  className = "",
}: {
  href: string;
  children: ReactNode;
  className?: string;
}) {
  return (
    <Link
      href={href}
      className={`${BASE} block overflow-hidden transition-colors duration-150 hover:border-borde-control ${className}`.trim()}
    >
      {children}
    </Link>
  );
}
