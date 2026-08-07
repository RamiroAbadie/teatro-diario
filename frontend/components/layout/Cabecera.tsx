import Link from "next/link";

import { IconoBuscar } from "@/components/ui/Iconos";
import type { Cuenta } from "@/lib/api/tipos";

import { BotonPersistente } from "./BotonPersistente";
import { DestinosDeCabecera } from "./DestinosDeCabecera";
import { MenuDeCuenta } from "./MenuDeCuenta";
import { MenuPrincipal } from "./MenuPrincipal";

/**
 * La primera pieza del armazón (D81).
 *
 * | | Celular | ≥`md` |
 * |---|---|---|
 * | | botón "Menú", título/marca al lado, lupa a la derecha | sin botón de menú: marca, buscador desplegado, los destinos, el menú de cuenta y el botón de registrar |
 *
 * ⚠️ **El título es sólo texto y no hay logotipo: P2 sigue abierto.** El sistema funciona
 * sin nombre y sin logo (regla 3 de D79): hay un hueco reservado y anotado, y el día que
 * P2 cierre se llena sin tocar nada más.
 */
export function Cabecera({ cuenta }: { cuenta: Cuenta | null }) {
  return (
    <header className="border-b border-borde bg-papel">
      <div className="mx-auto flex h-14 max-w-7xl items-center gap-3 px-4 sm:px-6">
        <MenuPrincipal cuenta={cuenta} />

        {/* ⬛ Hueco de P2: el día que haya nombre y logotipo, van acá y nada más cambia. */}
        <Link href="/" className="font-titulo text-lg whitespace-nowrap">
          Diario de teatro
        </Link>

        {/* ≥md: el buscador desplegado. Es un `form` de verdad, así que funciona sin
            JavaScript y no necesita ser una isla cliente. */}
        {/* `method="get"` explícito: el envío nativo es el que se quiere, y D90 pide que
            todo formulario declare el suyo en vez de confiar en el default. */}
        <form action="/buscar" method="get" className="ml-4 hidden flex-1 md:block">
          <label htmlFor="q-cabecera" className="sr-only">
            Buscar obras, artistas y usuarios
          </label>
          <input
            id="q-cabecera"
            name="q"
            type="search"
            placeholder="Buscar"
            className="h-9 w-full max-w-sm rounded-md border border-borde-control bg-superficie px-3 text-base"
          />
        </form>

        <div className="ml-auto flex items-center gap-2">
          <DestinosDeCabecera cuenta={cuenta} />

          {/* Celular: la lupa a la derecha. En ≥md el buscador ya está desplegado. */}
          <Link
            href="/buscar"
            aria-label="Buscar"
            className="inline-flex size-11 items-center justify-center rounded-md text-tinta md:hidden"
          >
            <IconoBuscar />
          </Link>

          <div className="hidden items-center gap-2 md:flex">
            {cuenta ? (
              <MenuDeCuenta cuenta={cuenta} />
            ) : (
              <Link href="/login" className="px-2 text-sm text-acento-tinta">
                Entrar
              </Link>
            )}
            <BotonPersistente cuenta={cuenta} />
          </div>
        </div>
      </div>
    </header>
  );
}
