"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import type { Cuenta } from "@/lib/api/tipos";

import { destinos, esElDestinoActivo } from "./destinos";

/**
 * Los mismos destinos de la barra, pero en la cabecera: **en ≥`md` no hay ni menú
 * principal ni bloque inferior**, así que los destinos viven acá (D81).
 *
 * Sin íconos —en escritorio el texto alcanza y sobra— pero con la misma regla de
 * accesibilidad: **el activo no se marca sólo con color**, lleva además `font-medium` y
 * su `aria-current`.
 */
export function DestinosDeCabecera({ cuenta }: { cuenta: Cuenta | null }) {
  const ruta = usePathname();

  return (
    <nav aria-label="Destinos" className="hidden md:block">
      <ul className="flex items-center gap-1">
        {destinos(cuenta).map((destino) => {
          const activo = esElDestinoActivo(destino, ruta);
          return (
            <li key={destino.href}>
              <Link
                href={destino.href}
                aria-current={activo ? "page" : undefined}
                className={`inline-flex h-11 items-center rounded-md px-3 text-sm transition-colors duration-150 ${
                  activo ? "font-medium text-acento-tinta" : "text-tinta-suave"
                }`}
              >
                {destino.etiqueta}
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
