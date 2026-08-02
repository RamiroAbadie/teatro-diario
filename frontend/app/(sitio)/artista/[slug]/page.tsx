import type { Metadata } from "next";
import { notFound, permanentRedirect } from "next/navigation";
import { cache } from "react";

import { ChipDeEstado } from "@/components/ui/Chip";
import { EstadoVacio } from "@/components/ui/EstadoVacio";
import { FilaResultado } from "@/components/ui/Fila";
import { artista } from "@/lib/api/catalogo.servidor";
import { FalloDeApi } from "@/lib/api/errores";
import type { Artista, RolParticipacion } from "@/lib/api/tipos";
import { conMayuscula, rolEnCastellano, salaConComplejo } from "@/lib/formato";
import { metadatosCompartibles } from "@/lib/metadatos";
import { rutaDeLaPlaca } from "@/lib/og";
import { idDesdeSlug, rutaArtista, rutaObra, slugify } from "@/lib/rutas";

/**
 * **Pantalla 4 · Página de artista** (HU-05). Server Component entero, `apiPublic` con TTL
 * de 300 s, **ninguna isla cliente**.
 *
 * **Nombre y participaciones agrupadas por rol, y nada más**: D14 dice sin foto y sin bio, y
 * esta pantalla **no inventa un lugar donde ponerlas**. Tampoco pagina: una persona con 40
 * participaciones se lee de un scroll y a esta escala no hace falta.
 */

type Props = { params: Promise<{ slug: string }> };

const cargar = cache((id: number) => artista(id));

/** El mismo orden en las tres pantallas que muestran roles: primero quién la hizo. */
const ORDEN: RolParticipacion[] = ["DIRECCION", "DRAMATURGIA", "ACTUACION"];

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const id = idDesdeSlug((await params).slug);
  if (id === null) return {};

  let persona: Artista;
  try {
    persona = await cargar(id);
  } catch {
    return {};
  }

  return metadatosCompartibles({
    titulo: persona.nombre,
    descripcion: `Todo lo que ${persona.nombre} hizo en el teatro de Buenos Aires.`,
    ruta: rutaArtista(persona.id, persona.nombre),
    imagen: rutaDeLaPlaca("artista", persona.id),
  });
}

export default async function ArtistaPage({ params }: Props) {
  const { slug } = await params;

  const id = idDesdeSlug(slug);
  if (id === null) notFound();

  let persona: Artista;
  try {
    persona = await cargar(id);
  } catch (error) {
    if (error instanceof FalloDeApi && error.status === 404) notFound();
    throw error;
  }

  if (slug !== `${persona.id}-${slugify(persona.nombre)}`) {
    permanentRedirect(rutaArtista(persona.id, persona.nombre));
  }

  const porRol = agruparPorRol(persona);

  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="font-titulo text-3xl leading-tight sm:text-4xl">{persona.nombre}</h1>

      {porRol.length === 0 ? (
        // Una persona sin participaciones es raro pero posible: quedó suelta tras una fusión
        // (D63). No es un error y no se dibuja como tal.
        <div className="mt-8">
          <EstadoVacio titulo="Todavía no hay obras cargadas">
            Esta persona está en el catálogo, pero ninguna producción la tiene en su ficha.
          </EstadoVacio>
        </div>
      ) : (
        <div className="mt-10 space-y-10">
          {porRol.map(({ rol, participaciones }) => (
            <section key={rol}>
              <h2 className="font-titulo text-xl">{conMayuscula(rolEnCastellano(rol))}</h2>
              <ul>
                {participaciones.map(({ id: participacionId, produccion }) => (
                  <FilaResultado
                    key={participacionId}
                    href={rutaObra(produccion.id, produccion.titulo)}
                    titulo={produccion.titulo}
                    meta={salaConComplejo(produccion.sala)}
                    aficheUrl={produccion.aficheUrl}
                    chip={<ChipDeEstado estado={produccion.estado} />}
                  />
                ))}
              </ul>
            </section>
          ))}
        </div>
      )}
    </div>
  );
}

function agruparPorRol(persona: Artista) {
  return ORDEN.map((rol) => ({
    rol,
    participaciones: persona.participaciones.filter((participacion) => participacion.rol === rol),
  })).filter((grupo) => grupo.participaciones.length > 0);
}
