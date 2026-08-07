import type { Metadata } from "next";

import { Buscador } from "@/components/busqueda/Buscador";

/**
 * **Pantalla 7 · Búsqueda** — `/buscar?q=` (HU-07 → HU-08).
 *
 * La pantalla es cliente entera (D78): se pide mientras se escribe. Lo único que hace el
 * servidor es **entregarle el `?q=` de la URL ya resuelto**, que es lo que hace que un
 * resultado compartido llegue con la búsqueda hecha —el `form` de la cabecera y el de la 404
 * navegan acá con `?q=`— y que la pantalla no dependa de JavaScript para saber qué buscar.
 *
 * **Sin `generateMetadata` con la consulta adentro**: no es una pantalla compartible (no está
 * entre las cuatro con OG) y ponerle el término tipeado en el `<title>` sólo agregaría URLs
 * indexables con contenido que cambia solo.
 */

export const metadata: Metadata = { title: "Buscar" };

type Props = { searchParams: Promise<{ q?: string }> };

export default async function BuscarPage({ searchParams }: Props) {
  const { q } = await searchParams;

  return <Buscador consultaInicial={q ?? ""} />;
}
