import Link from "next/link";

import { EstadoVacio } from "@/components/ui/EstadoVacio";

/**
 * **Pantalla 13 · 404**, del lado de adentro del armazón: el `notFound()` de una ficha, un
 * artista, una sala o un perfil que no existen —y el de un id que no pasa la regex de D74—
 * cae acá, y como `(sitio)/layout.tsx` está arriba, **ya llega con cabecera, bloque inferior
 * y pie**.
 *
 * ⚠️ **Con la búsqueda embebida, que no es decoración**: una 404 sin salidas es una pantalla
 * muerta al final del Flujo 1, y quien llegó por un link roto no tiene otra puerta.
 *
 * El 404 de una URL que **no matchea ninguna ruta** no pasa por acá: usa el de la raíz, que
 * está fuera del armazón y tiene que componerlo a mano (ver `app/not-found.tsx`).
 */
export default function NoEncontrado() {
  return (
    <div className="mx-auto max-w-3xl">
      <EstadoVacio titulo="No encontramos esta página" conBuscador nivel={1}>
        Puede que el link esté mal, o que la ficha ya no esté en el catálogo. Probá buscando
        lo que estabas mirando.
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
  );
}
