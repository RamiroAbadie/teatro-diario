import Link from "next/link";

import { BloqueInferior } from "@/components/layout/BloqueInferior";
import { Cabecera } from "@/components/layout/Cabecera";
import { Pie } from "@/components/layout/Pie";
import { EstadoVacio } from "@/components/ui/EstadoVacio";
import { yo } from "@/lib/api/identidad.servidor";

/**
 * **Pantalla 13 · el 404 global**: una URL que **no matchea ninguna ruta** (`/cualquiera`).
 *
 * ⚠️ **Este archivo compone el armazón a mano, y no es una duplicación evitable.** Un 404 de
 * ruta inexistente no entra en ningún grupo, así que se dibuja con el layout **raíz**, que
 * por D83 es `<html>`, `<body>` y los tokens y nada más. `SCREEN_SPECS.md` es explícito en
 * que las dos pantallas de error llevan el armazón completo —"una 404 sin salidas es una
 * pantalla muerta al final del Flujo 1"—, así que las cuatro piezas se piden acá.
 *
 * El `notFound()` de una ficha o un perfil inexistente **no pasa por acá**: cae en el
 * `not-found.tsx` de `(sitio)`, que ya está adentro del armazón.
 */
export default async function NoEncontradoGlobal() {
  const cuenta = await yo();

  return (
    <div className="pb-[calc(7.5rem+env(safe-area-inset-bottom))] md:pb-0">
      <Cabecera cuenta={cuenta} />
      <main className="mx-auto max-w-7xl px-4 py-6 sm:px-6">
        <div className="mx-auto max-w-3xl">
          <EstadoVacio titulo="No encontramos esta página" conBuscador nivel={1}>
            El link no lleva a ningún lado. Probá buscando lo que estabas mirando.
          </EstadoVacio>

          <p className="mt-6 flex flex-wrap justify-center gap-x-6 gap-y-2 text-sm">
            <Link href="/en-cartel" className="text-acento-tinta">
              Ver qué hay en cartel
            </Link>
            <Link href="/" className="text-acento-tinta">
              Ir al inicio
            </Link>
          </p>
        </div>
      </main>
      <Pie />
      <BloqueInferior cuenta={cuenta} />
    </div>
  );
}
