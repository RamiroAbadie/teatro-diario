import type { Metadata } from "next";
import { notFound, permanentRedirect } from "next/navigation";
import { cache } from "react";

import { GrillaDeProducciones } from "@/components/catalogo/GrillaDeProducciones";
import { EstadoVacio } from "@/components/ui/EstadoVacio";
import { sala } from "@/lib/api/catalogo.servidor";
import { FalloDeApi } from "@/lib/api/errores";
import type { SalaPublica } from "@/lib/api/tipos";
import { metadatosCompartibles } from "@/lib/metadatos";
import { rutaDeLaPlaca } from "@/lib/og";
import { idDesdeSlug, rutaSala, slugify } from "@/lib/rutas";

/**
 * **Pantalla 5 · Página de sala**. No tiene historia propia: es el destino del "sala con
 * link" de la ficha (HU-04) y el hueco 1 de USER_FLOWS. Server Component, `apiPublic` con
 * TTL de 300 s.
 *
 * **Nombre, complejo y qué hay en cartel ahí. Y se termina**: no hay dirección, ni mapa, ni
 * horarios — el contrato no los trae y pedirlos sería una agenda (X4/P6).
 */

type Props = { params: Promise<{ slug: string }> };

const cargar = cache((id: number) => sala(id));

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const id = idDesdeSlug((await params).slug);
  if (id === null) return {};

  let laSala: SalaPublica;
  try {
    laSala = await cargar(id);
  } catch {
    return {};
  }

  return metadatosCompartibles({
    titulo: laSala.complejo ? `${laSala.nombre} · ${laSala.complejo}` : laSala.nombre,
    descripcion: `Qué se puede ver ahora en ${laSala.nombre}.`,
    ruta: rutaSala(laSala.id, laSala.nombre),
    imagen: rutaDeLaPlaca("sala", laSala.id),
  });
}

export default async function SalaPage({ params }: Props) {
  const { slug } = await params;

  const id = idDesdeSlug(slug);
  if (id === null) notFound();

  let laSala: SalaPublica;
  try {
    laSala = await cargar(id);
  } catch (error) {
    if (error instanceof FalloDeApi && error.status === 404) notFound();
    throw error;
  }

  if (slug !== `${laSala.id}-${slugify(laSala.nombre)}`) {
    permanentRedirect(rutaSala(laSala.id, laSala.nombre));
  }

  return (
    <div className="mx-auto max-w-5xl">
      <h1 className="font-titulo text-3xl leading-tight sm:text-4xl">{laSala.nombre}</h1>
      {/* La línea que viene nula desaparece entera, etiqueta incluida. */}
      {laSala.complejo && <p className="mt-2 text-tinta-suave">{laSala.complejo}</p>}

      <section className="mt-10">
        <h2 className="mb-4 font-titulo text-xl">Ahora en esta sala</h2>
        {laSala.enCartel.length === 0 ? (
          // **El caso normal la mitad del año**, y por eso se dice así y no como una falla.
          <EstadoVacio titulo="No hay funciones cargadas en esta sala ahora">
            Cuando vuelva a haber algo en cartel acá, va a aparecer en esta página.
          </EstadoVacio>
        ) : (
          // `sinSala`: estamos parados en la sala, así que repetirla en cada celda es
          // decir veinte veces lo que el `h1` ya dijo.
          <GrillaDeProducciones producciones={laSala.enCartel} sinSala />
        )}
      </section>
    </div>
  );
}
