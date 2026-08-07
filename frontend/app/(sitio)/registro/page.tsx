import type { Metadata } from "next";
import Link from "next/link";
import { redirect } from "next/navigation";

import { FormularioDeAlta } from "@/components/identidad/FormularioDeAlta";
import { yo } from "@/lib/api/identidad.servidor";
import { destinoDeVuelta } from "@/lib/rutas";

/**
 * **Pantalla 11 · Alta de cuenta** — `/registro` (HU-01).
 *
 * Es **el destino del botón persistente del visitante** ("Crear tu diario", D80/D84), o sea
 * el final del Flujo 1: alguien llegó por un link de WhatsApp, miró una ficha y decide
 * quedarse. Por eso el `?volver=` importa tanto acá como en el login.
 */

export const metadata: Metadata = { title: "Crear tu diario" };

type Props = { searchParams: Promise<{ volver?: string }> };

export default async function RegistroPage({ searchParams }: Props) {
  const { volver } = await searchParams;
  const destino = destinoDeVuelta(volver);

  if (await yo()) redirect(destino);

  return (
    <div className="mx-auto max-w-sm py-6">
      <h1 className="font-titulo text-3xl">Crear tu diario</h1>
      <p className="mt-2 text-tinta-suave">
        Para registrar lo que ves y tener tu historial en un lado.
      </p>

      <FormularioDeAlta destino={destino} />

      <p className="mt-8 text-sm text-tinta-suave">
        ¿Ya tenés cuenta?{" "}
        <Link href={`/login?volver=${encodeURIComponent(destino)}`} className="text-acento-tinta">
          Entrar
        </Link>
      </p>
    </div>
  );
}
