"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import { IconoBuscar, IconoCartel, IconoCasa } from "@/components/ui/Iconos";
import { Monograma } from "@/components/ui/Usuario";
import type { Cuenta } from "@/lib/api/tipos";

import { destinos, esElDestinoActivo, type Icono } from "./destinos";

/**
 * La barra de destinos del bloque inferior (D81). **Isla cliente**: necesita la ruta
 * activa, y es lo único que necesita.
 *
 * 4 celdas con sesión, 3 sin ella, repartidas en partes iguales: 82 px de celda en un
 * celular de 360 con sesión, 109 px sin ella. Objetivo táctil de 48 px, por encima del
 * piso de 44 de `DESIGN_SYSTEM.md`, y etiqueta en `text-xs`, que es el piso de la escala
 * y **no baja de ahí**.
 *
 * ⚠️ **El destino activo no se marca sólo con color**: lleva regla de 2 px en `acento`
 * sobre el borde superior de la celda + `font-medium` + texto en `acento-tinta`. Una barra
 * de navegación es donde más fácil se rompe esa regla de accesibilidad.
 */
export function BarraDestinos({ cuenta }: { cuenta: Cuenta | null }) {
  const ruta = usePathname();
  const lista = destinos(cuenta);

  return (
    <nav aria-label="Destinos">
      <ul className="flex">
        {lista.map((destino) => {
          const activo = esElDestinoActivo(destino, ruta);
          return (
            <li key={destino.href} className="flex-1">
              <Link
                href={destino.href}
                aria-current={activo ? "page" : undefined}
                className={`flex h-12 flex-col items-center justify-center gap-0.5 border-t-2 text-xs transition-colors duration-150 ${
                  activo
                    ? "border-acento font-medium text-acento-tinta"
                    : "border-transparent text-tinta-suave"
                }`}
              >
                <IconoDeDestino icono={destino.icono} cuenta={cuenta} />
                <span>{destino.etiqueta}</span>
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}

/** "Mi diario" **no estrena ícono**: usa el monograma de `Usuario`, que ya existe (D81). */
function IconoDeDestino({ icono, cuenta }: { icono: Icono; cuenta: Cuenta | null }) {
  switch (icono) {
    case "casa":
      return <IconoCasa />;
    case "cartel":
      return <IconoCartel />;
    case "lupa":
      return <IconoBuscar />;
    case "monograma":
      return <Monograma username={cuenta?.username ?? null} tamanio="barra" />;
  }
}
