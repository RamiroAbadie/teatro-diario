import Link from "next/link";

import { REPO } from "@/lib/rutas";

/**
 * La cuarta pieza del armazón (D81): links mínimos, iguales en celular y en escritorio.
 * El link al repo no es decorativo — la AGPL obliga a ofrecer el código a quien usa el
 * servicio (D46).
 */
export function Pie() {
  return (
    <footer className="mt-16 border-t border-borde">
      <div className="mx-auto flex max-w-7xl flex-wrap gap-x-6 gap-y-2 px-4 py-6 text-sm text-tinta-suave sm:px-6">
        <Link href="/en-cartel">En cartel</Link>
        <Link href="/buscar">Buscar</Link>
        <a href={REPO} target="_blank" rel="noreferrer">
          Código (AGPL-3.0)
        </a>
      </div>
    </footer>
  );
}
