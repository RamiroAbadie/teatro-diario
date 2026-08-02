import type { Metadata } from "next";

import { GrillaDeProducciones } from "@/components/catalogo/GrillaDeProducciones";
import { BotonReintentar } from "@/components/ui/BotonReintentar";
import { EstadoVacio } from "@/components/ui/EstadoVacio";
import { enCartel } from "@/lib/api/catalogo.servidor";
import type { EnCartel } from "@/lib/api/tipos";
import { metadatosCompartibles } from "@/lib/metadatos";

/**
 * **Pantalla 6 · En cartel** (HU-06). Server Component entero, `apiPublic` con TTL de 300 s:
 * es la parte más barata de servir del producto y la que primero ve el que llega sin cuenta.
 *
 * Dos secciones **en el orden en que las manda la API** (D8), y "Próximamente" es
 * visiblemente secundaria. **Sin filtros, sin calendario, sin barrio, sin horarios**: no hay
 * agenda de funciones (X4/P6) y un filtro por sala sería su primera pieza.
 */

export const metadata: Metadata = metadatosCompartibles({
  titulo: "En cartel",
  descripcion: "Qué se puede ver ahora en el teatro de Buenos Aires, y qué está por estrenar.",
  ruta: "/en-cartel",
});

export default async function EnCartelPage() {
  let cartelera: EnCartel;
  try {
    cartelera = await enCartel();
  } catch (error) {
    // `5xx`: acá la pantalla ES la lista, así que no hay degradación por bloque que valga.
    // **Con "Reintentar", que la tabla de errores pide para todo `5xx`**: sin él, la única
    // salida de esta pantalla es que el usuario adivine que hay que recargar. Se atrapa acá
    // en vez de dejarlo subir a `error.tsx` porque así el `h1` "En cartel" y las salidas del
    // armazón se quedan en su lugar: la pantalla no desaparece, se vacía.
    console.error("No se pudo cargar la cartelera", error);
    return (
      <Encabezado>
        <EstadoVacio titulo="No pudimos cargar la cartelera" accion={<BotonReintentar />}>
          Algo falló de nuestro lado. Probá de nuevo.
        </EstadoVacio>
      </Encabezado>
    );
  }

  const vacia = cartelera.enCartel.length === 0 && cartelera.proximamente.length === 0;

  return (
    <Encabezado>
      {vacia ? (
        <EstadoVacio titulo="Todavía no hay nada en cartel">
          El catálogo lo carga una persona a mano. Cuando haya funciones cargadas, van a
          aparecer acá.
        </EstadoVacio>
      ) : (
        <div className="space-y-12">
          {cartelera.enCartel.length > 0 && <GrillaDeProducciones producciones={cartelera.enCartel} />}

          {/* Sólo `proximamente` vacía: **la sección desaparece entera, sin cartel**. Un
              encabezado con un estado vacío debajo es peor que no estar. */}
          {cartelera.proximamente.length > 0 && (
            <section>
              <h2 className="mb-4 font-titulo text-xl">Próximamente</h2>
              <GrillaDeProducciones producciones={cartelera.proximamente} conChip />
            </section>
          )}
        </div>
      )}
    </Encabezado>
  );
}

function Encabezado({ children }: { children: React.ReactNode }) {
  return (
    <div className="mx-auto max-w-5xl">
      <h1 className="mb-6 font-titulo text-2xl">En cartel</h1>
      {children}
    </div>
  );
}
