import type { Metadata } from "next";

import { FormularioDeSugerencia } from "@/components/catalogo/FormularioDeSugerencia";

/**
 * **Pantalla 10 · Sugerir producción** — `/sugerir` (HU-08).
 *
 * Se llega desde dos lugares y **en los dos el título ya viene escrito**: el estado vacío de
 * la búsqueda (pantalla 7) y la última opción del autocompletado del gesto (pantalla 9), que
 * está siempre, con resultados o sin ellos (D7/D24).
 *
 * ⚠️ **La página no comprueba la sesión, y es a propósito.** El endpoint la pide, así que su
 * `401` manda a `/login?volver=/sugerir?titulo=…` y al volver el borrador restaura todo
 * (`SCREEN_SPECS.md`). Cortar antes obligaría a alguien que ya empezó a escribir a perder lo
 * tipeado para enterarse de lo mismo.
 */

export const metadata: Metadata = { title: "Sugerir una obra" };

type Props = { searchParams: Promise<{ titulo?: string }> };

export default async function SugerirPage({ searchParams }: Props) {
  const { titulo } = await searchParams;

  return (
    <div className="mx-auto max-w-3xl py-6">
      <h1 className="font-titulo text-3xl">Sugerir una obra</h1>
      <p className="mt-2 max-w-[55ch] text-tinta-suave">
        El catálogo lo carga una persona a mano, así que puede faltar algo. Contanos cuál es y
        la buscamos.
      </p>

      <FormularioDeSugerencia tituloInicial={titulo ?? ""} />
    </div>
  );
}
