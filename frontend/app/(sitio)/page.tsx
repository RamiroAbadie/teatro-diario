import Link from "next/link";

import { GrillaDeProducciones } from "@/components/catalogo/GrillaDeProducciones";
import { enCartel } from "@/lib/api/catalogo.servidor";
import type { ProduccionResumen } from "@/lib/api/tipos";

/**
 * **Pantalla 1 · Home del visitante** (HU-06, y es la puerta del Flujo 1). Server Component,
 * `apiPublic` con TTL de 300 s, **ninguna isla cliente**.
 *
 * ⏳ **Con sesión esta misma ruta es la pantalla 2 (el feed)**, que es el paso 5 de la Fase
 * 4. Hasta entonces la landing se dibuja para todo el mundo: es la misma respuesta cacheada
 * para todos —no hay nada personalizado que se pueda filtrar—, así que no rompe la regla de
 * caché de D78, y lo que sí cambia con la sesión (el botón persistente, la barra de
 * destinos) ya lo resuelve el armazón.
 *
 * ⚠️ **El CTA de crear cuenta es el botón persistente y nada más** (criterio transversal de
 * las pantallas 1, 3, 4, 6 y 8): sin sesión ya dice "Crear tu diario" y está fijo abajo. **No
 * hay banners de registro, ni interstitials, ni un segundo CTA al final del scroll** — el
 * visitante que sólo mira es la mitad del Flujo 1 y molestarlo es perderlo.
 */
export default async function Home() {
  // `5xx`: **la sección de en cartel no se dibuja y el titular sí.** Una portada rota es
  // peor que una portada corta, y el titular es lo único que esta pantalla le debe a alguien
  // que llega por primera vez.
  let cartelera: ProduccionResumen[] = [];
  try {
    cartelera = (await enCartel()).enCartel;
  } catch (error) {
    console.error("No se pudo cargar la cartelera de la home", error);
  }

  const primeras = cartelera.slice(0, 6);

  return (
    <div className="mx-auto max-w-5xl">
      {/* Sin nombre de producto (P2): la frase funciona sola y el día que P2 cierre se le
          pone el logotipo encima sin tocar el resto. */}
      <h1 className="max-w-[20ch] font-titulo text-3xl leading-tight sm:text-4xl">
        Todo lo que viste en el teatro, en un solo lugar
      </h1>
      <p className="mt-3 max-w-[45ch] text-tinta-suave">
        El diario de tu teatro de acá en adelante: qué viste, cuándo y qué te pareció.
      </p>

      {/* Vacío: **la grilla desaparece entera y queda el titular.** No se muestra un
          `EstadoVacio`: el visitante no vino a resolver un vacío nuestro. */}
      {primeras.length > 0 && (
        <section className="mt-12">
          <h2 className="mb-4 font-titulo text-xl">En cartel ahora</h2>
          <GrillaDeProducciones producciones={primeras} />
          <Link href="/en-cartel" className="mt-4 inline-block text-acento-tinta">
            Ver todo lo que está en cartel
          </Link>
        </section>
      )}
    </div>
  );
}
