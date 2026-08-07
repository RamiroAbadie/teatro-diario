import type { Metadata } from "next";
import Link from "next/link";
import { redirect } from "next/navigation";

import { FormularioDeLogin } from "@/components/identidad/FormularioDeLogin";
import { yo } from "@/lib/api/identidad.servidor";
import { destinoDeVuelta } from "@/lib/rutas";

/**
 * **Pantalla 11 · Login** — `/login` (HU-02).
 *
 * La página es un Server Component **con una sola isla**: el formulario. Sirve para dos
 * cosas que el cliente no puede hacer solo — mirar si ya hay sesión y **no mostrarle un
 * login a alguien que ya entró** (`SCREEN_SPECS.md`), y quedarse con el `?volver=` ya
 * saneado antes de que llegue a ningún `push`.
 */

export const metadata: Metadata = { title: "Entrar" };

type Props = { searchParams: Promise<{ volver?: string }> };

export default async function LoginPage({ searchParams }: Props) {
  const { volver } = await searchParams;

  // ⚠️ **Sólo rutas relativas del propio sitio**: un `?volver=https://…` es un redirect
  // abierto, y el momento en que se dispara —recién abierta la sesión— es el peor posible.
  const destino = destinoDeVuelta(volver);

  if (await yo()) redirect(destino);

  return (
    <div className="mx-auto max-w-sm py-6">
      <h1 className="font-titulo text-3xl">Entrar</h1>

      <FormularioDeLogin destino={destino} />

      <p className="mt-8 text-sm text-tinta-suave">
        ¿Todavía no tenés cuenta?{" "}
        {/* El `?volver=` viaja de una puerta a la otra: quien vino de una ficha a entrar y
            termina creando la cuenta tiene que volver a la misma ficha. */}
        <Link href={`/registro?volver=${encodeURIComponent(destino)}`} className="text-acento-tinta">
          Crear tu diario
        </Link>
      </p>
    </div>
  );
}
