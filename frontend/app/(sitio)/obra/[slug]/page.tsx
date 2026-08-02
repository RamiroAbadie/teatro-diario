import type { Metadata } from "next";
import Link from "next/link";
import { notFound, permanentRedirect } from "next/navigation";
import { cache } from "react";

import { Elenco } from "@/components/catalogo/Elenco";
import { Resenias } from "@/components/catalogo/Resenias";
import { ChipDeEstado } from "@/components/ui/Chip";
import { EstadoVacio } from "@/components/ui/EstadoVacio";
import { BotonReintentar } from "@/components/ui/BotonReintentar";
import { PuntajePromedio } from "@/components/ui/Puntaje";
import { fichaConSesion, fichaPublica } from "@/lib/api/catalogo.servidor";
import { opinionesConSesion, opinionesPublicas } from "@/lib/api/diario.servidor";
import { FalloDeApi } from "@/lib/api/errores";
import { yo } from "@/lib/api/identidad.servidor";
import type { Ficha, Opiniones } from "@/lib/api/tipos";
import { salaConComplejo } from "@/lib/formato";
import { metadatosCompartibles, recortar } from "@/lib/metadatos";
import { idDesdeSlug, rutaObra, rutaSala, slugify } from "@/lib/rutas";
import { rutaDeLaPlaca } from "@/lib/og";

/**
 * **Pantalla 3 · Ficha de producción** (HU-04, HU-14; HU-10 y HU-17/18 ⏳). Es **la pantalla
 * más importante del producto**: el destino del Flujo 1 y la que HU-04 mide con un test
 * literal — pegar el link en WhatsApp y ver el preview.
 *
 * **Dos llamadas a dos módulos distintos** (D60): la ficha es de Catálogo y las opiniones son
 * de Diario. Que la ficha sobreviva a que fallen las opiniones **no es un lujo**: la mitad
 * que sostiene el Flujo 1 —título, sinopsis, el preview de WhatsApp— es la primera.
 *
 * ⚖️ **Las dos van por el mismo cliente**, y cuál depende de si hay sesión: sin cookie
 * `apiPublic` con TTL de 60 s, con cookie **las dos** por `apiSession` y `no-store`. Una
 * página con sesión se renderiza entera sin caché (D78).
 */

type Props = { params: Promise<{ slug: string }> };

/**
 * `cache()` por el mismo motivo que `yo()`: **`generateMetadata` y la página son dos
 * llamadas al mismo dato en el mismo pedido**, y sin memoizar, la ficha se pide dos veces
 * —cuatro con las opiniones—. Muere con el render: no es una caché entre pedidos.
 */
const cargarFicha = cache(async (id: number): Promise<Ficha> => {
  const cuenta = await yo();
  return cuenta ? fichaConSesion(id) : fichaPublica(id);
});

const cargarOpiniones = cache(async (id: number): Promise<Opiniones> => {
  const cuenta = await yo();
  return cuenta ? opinionesConSesion(id) : opinionesPublicas(id);
});

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const id = idDesdeSlug((await params).slug);
  if (id === null) return {};

  let ficha: Ficha;
  try {
    ficha = await cargarFicha(id);
  } catch {
    // El `404` y el `5xx` los dibuja la página; acá sólo hay que no romper el `<head>`.
    return {};
  }

  const sala = salaConComplejo(ficha.sala);

  return metadatosCompartibles({
    titulo: ficha.titulo,
    // **Nunca una descripción vacía**: sin sinopsis, la ficha igual tiene qué decir de sí
    // misma, y esa línea es la mitad del preview.
    descripcion: ficha.sinopsis
      ? recortar(ficha.sinopsis)
      : [`Teatro en Buenos Aires`, sala].filter(Boolean).join(" · "),
    ruta: rutaObra(ficha.id, ficha.titulo),
    // El afiche tal cual cuando exista (D77, ⏳); si no, la placa tipográfica (D71/D79).
    imagen: ficha.aficheUrl ?? rutaDeLaPlaca("obra", ficha.id),
  });
}

export default async function FichaPage({ params }: Props) {
  const { slug } = await params;

  // **Antes de llamar a nada**: el id con la regex de D74. Un segmento que no empieza con un
  // entero positivo escrito de la única forma que existe no es una ficha con otra URL: es
  // una ruta que no existe, y ahí termina.
  const id = idDesdeSlug(slug);
  if (id === null) notFound();

  let ficha: Ficha;
  try {
    ficha = await cargarFicha(id);
  } catch (error) {
    // `404` → la página de no encontrado, con su búsqueda embebida. Cualquier otra cosa
    // sube a `error.tsx`: es la pantalla entera la que no se pudo cargar.
    if (error instanceof FalloDeApi && error.status === 404) notFound();
    throw error;
  }

  // El slug quedó viejo —el admin corrigió el título— o directamente no vino: se redirige a
  // la forma canónica con `permanentRedirect()` (308, que es lo que emite Next y preserva el
  // método), **con el título que ya vino en la respuesta**, así que no cuesta una consulta.
  const canonica = rutaObra(ficha.id, ficha.titulo);
  if (slug !== `${ficha.id}-${slugify(ficha.titulo)}`) permanentRedirect(canonica);

  // ⚠️ **La degradación es por bloque.** Las opiniones se piden por su cuenta y su error se
  // queda adentro de su sección: la ficha se muestra igual.
  let opiniones: Opiniones | null = null;
  try {
    opiniones = await cargarOpiniones(ficha.id);
  } catch (error) {
    console.error(`No se pudieron cargar las opiniones de la producción ${ficha.id}`, error);
  }

  const sala = salaConComplejo(ficha.sala);
  const conAfiche = Boolean(ficha.aficheUrl);

  return (
    <article className="mx-auto max-w-5xl">
      {/* **Sin afiche la ficha no tiene un hueco donde iría el afiche: tiene otra portada**
          (D79). Una columna, el título más grande arriba, y **no hay placa** — la placa es
          para rellenar celdas de tamaño fijo, y ésta es la única pantalla que puede
          permitirse reacomodarse. */}
      <div className={conAfiche ? "md:flex md:items-start md:gap-8" : undefined}>
        {conAfiche && (
          <img
            src={ficha.aficheUrl!}
            alt={`Afiche de ${ficha.titulo}`}
            className="mb-6 max-h-[60vh] w-full rounded-lg border border-borde object-contain md:mb-0 md:w-80 md:shrink-0"
          />
        )}

        <div className="min-w-0 flex-1">
          <h1 className="font-titulo text-3xl leading-tight sm:text-4xl">{ficha.titulo}</h1>

          <p className="mt-3 flex flex-wrap items-center gap-x-3 gap-y-2 text-sm text-tinta-suave">
            <ChipDeEstado estado={ficha.estado} />
            {/* La línea que viene nula desaparece con su etiqueta: no hay "Sala: —". */}
            {ficha.sala && sala && (
              <Link href={rutaSala(ficha.sala.id, ficha.sala.nombre)} className="text-acento-tinta">
                {sala}
              </Link>
            )}
          </p>

          <div className="mt-6">
            {opiniones ? (
              <PuntajePromedio
                promedio={opiniones.promedio}
                cantidadRatings={opiniones.cantidadRatings}
              />
            ) : null}
            {/* ⏳ **El CTA del gesto no se dibuja todavía**, y no es un olvido: necesita
                `vecesQueLaVi` (D76, que el backend no manda) y la hoja del gesto (paso 4).
                Con `null` la regla ya está escrita: no hay sesión, no hay zona. */}
          </div>

          {ficha.sinopsis && (
            <p className="mt-6 max-w-[65ch] whitespace-pre-line">{ficha.sinopsis}</p>
          )}

          {(ficha.obraOriginal || ficha.autorOriginal) && (
            <dl className="mt-6 space-y-1 text-sm">
              {ficha.obraOriginal && (
                <div className="flex gap-2">
                  <dt className="text-tinta-suave">Obra original</dt>
                  <dd>{ficha.obraOriginal}</dd>
                </div>
              )}
              {ficha.autorOriginal && (
                <div className="flex gap-2">
                  <dt className="text-tinta-suave">Autor</dt>
                  <dd>{ficha.autorOriginal}</dd>
                </div>
              )}
            </dl>
          )}

          <div className="mt-12">
            <Elenco participaciones={ficha.participaciones} />
          </div>
        </div>
      </div>

      <section className="mt-16">
        <h2 className="mb-2 font-titulo text-xl">Reseñas</h2>
        {opiniones ? (
          <Resenias resenias={opiniones.resenias} />
        ) : (
          <EstadoVacio titulo="No pudimos cargar las reseñas" accion={<BotonReintentar />}>
            La ficha está completa; lo que falló fue el bloque de opiniones.
          </EstadoVacio>
        )}
      </section>
    </article>
  );
}
