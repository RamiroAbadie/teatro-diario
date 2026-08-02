import type { ReactNode } from "react";

import { BloqueInferior } from "@/components/layout/BloqueInferior";
import { Cabecera } from "@/components/layout/Cabecera";
import { Pie } from "@/components/layout/Pie";
import { yo } from "@/lib/api/identidad.servidor";

/**
 * El armazón de cuatro piezas (D81): cabecera, menú principal, bloque inferior —el botón
 * de registrar siempre presente (D71) más la barra de destinos— y pie.
 *
 * **Vive acá y no en la raíz** porque el panel admin no lo lleva (`SCREEN_SPECS.md`) y los
 * layouts del App Router se anidan: lo que está arriba no se puede sacar abajo. El grupo
 * `(sitio)` es la frontera, y `app/admin/` va a colgar por afuera con su propio layout.
 * **El paréntesis no cambia ninguna URL.**
 *
 * ⚠️ **La sesión se resuelve una sola vez por pedido**, con `GET /api/auth/yo` por
 * `apiSession` (D78). Que se resuelva acá no impide que una página vuelva a llamar a `yo()`
 * para elegir entre `apiPublic` y `apiSession` —Next no pasa datos de un layout a sus
 * hijos—: `yo()` está memoizada con `cache()` de React, así que la segunda llamada del
 * mismo render no toca la red. El armazón **no agrega ni una llamada** ni pide una librería
 * de estado global.
 *
 * De esa única respuesta salen las tres cosas que dependen de ella: la etiqueta del botón
 * persistente, si la barra tiene celda "Mi diario", y si el menú dibuja la sección Panel.
 * Su `401` no se muestra y no navega —es "anónimo"—, así que **el armazón del visitante se
 * dibuja con el mismo camino de código y sin ningún estado de carga**.
 */
export default async function LayoutDelSitio({ children }: { children: ReactNode }) {
  const cuenta = await yo();

  return (
    // El colchón de abajo es el alto del bloque fijo: sin él, el bloque tapa el final de
    // cada pantalla. En ≥md el bloque no existe y el colchón tampoco.
    <div className="pb-[calc(7.5rem+env(safe-area-inset-bottom))] md:pb-0">
      <Cabecera cuenta={cuenta} />
      <main className="mx-auto max-w-7xl px-4 py-6 sm:px-6">{children}</main>
      <Pie />
      <BloqueInferior cuenta={cuenta} />
    </div>
  );
}
