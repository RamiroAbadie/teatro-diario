"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";

import { IconoCheuron } from "@/components/ui/Iconos";
import { Usuario } from "@/components/ui/Usuario";
import type { Cuenta } from "@/lib/api/tipos";
import { rutaUsuario } from "@/lib/rutas";

import { BotonSalir } from "./BotonSalir";

/**
 * El menú de cuenta de la cabecera ancha (≥`md`, D81). En el celular esto vive en el menú
 * principal: los mismos ítems, otra forma.
 *
 * No es un `<dialog>` —no es modal, no tapa la pantalla y no tiene por qué robarle el
 * "atrás" al usuario—, así que el `Esc` y el clic afuera hay que escribirlos. Son ocho
 * líneas y siguen sin traer una dependencia (D73).
 */
export function MenuDeCuenta({ cuenta }: { cuenta: Cuenta }) {
  const [abierto, setAbierto] = useState(false);
  const contenedor = useRef<HTMLDivElement>(null);
  const ruta = usePathname();
  const router = useRouter();

  useEffect(() => setAbierto(false), [ruta]);

  useEffect(() => {
    if (!abierto) return;

    const alTocarAfuera = (evento: PointerEvent) => {
      if (!contenedor.current?.contains(evento.target as Node)) setAbierto(false);
    };
    const alApretarEscape = (evento: KeyboardEvent) => {
      if (evento.key === "Escape") setAbierto(false);
    };

    document.addEventListener("pointerdown", alTocarAfuera);
    document.addEventListener("keydown", alApretarEscape);
    return () => {
      document.removeEventListener("pointerdown", alTocarAfuera);
      document.removeEventListener("keydown", alApretarEscape);
    };
  }, [abierto]);

  return (
    <div ref={contenedor} className="relative">
      <button
        type="button"
        onClick={() => setAbierto((estaba) => !estaba)}
        aria-expanded={abierto}
        aria-haspopup="menu"
        className="inline-flex h-11 items-center gap-1 rounded-md px-2"
      >
        <Usuario username={cuenta.username} sinLink />
        <IconoCheuron />
      </button>

      {abierto && (
        <ul className="absolute right-0 z-50 mt-1 w-56 divide-y divide-borde rounded-md border border-borde bg-superficie">
          <li>
            <Link href={rutaUsuario(cuenta.username)} className={CLASES_DE_ITEM}>
              Mi diario
            </Link>
          </li>
          <li>
            <Link href="/sugerir" className={CLASES_DE_ITEM}>
              Sugerir una obra
            </Link>
          </li>
          {cuenta.rol === "ADMIN" && (
            <li>
              <Link href="/admin/sugerencias" className={CLASES_DE_ITEM}>
                Panel
              </Link>
            </li>
          )}
          <li>
            {/* Acá no hay `<dialog>` ni entrada de historial que consumir: cerrar el
                desplegable y navegar alcanza. */}
            <BotonSalir
              navegar={(href) => {
                setAbierto(false);
                router.push(href);
              }}
              className={CLASES_DE_ITEM}
            />
          </li>
        </ul>
      )}
    </div>
  );
}

const CLASES_DE_ITEM =
  "flex h-11 w-full items-center px-3 text-left text-sm text-tinta transition-colors duration-150 hover:bg-acento-suave";
