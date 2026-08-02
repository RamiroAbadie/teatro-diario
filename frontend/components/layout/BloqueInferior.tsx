import type { Cuenta } from "@/lib/api/tipos";

import { BarraDestinos } from "./BarraDestinos";
import { BotonPersistente } from "./BotonPersistente";

/**
 * La tercera pieza del armazón (D81): **fijo abajo en el celular**, arriba el botón
 * primario persistente y debajo la barra de destinos.
 *
 * Ese orden es la decisión, no un detalle de maquetado: el CTA de adquisición tiene que
 * seguir siendo texto que cambia según la sesión, así que no puede convertirse en una
 * celda de la barra.
 *
 * Es **una sola pieza** porque las dos partes comparten posicionamiento y área segura:
 * separarlas obliga a repetir el fijado en dos lugares y a mantenerlos sincronizados a
 * mano. La separación entre ellas es un `border-top` de 1 px, no un margen — la misma
 * regla que separa filas.
 *
 * **En ≥`md` no existe**: el botón vuelve a la cabecera, a la derecha, y los destinos
 * también viven ahí.
 */
export function BloqueInferior({ cuenta }: { cuenta: Cuenta | null }) {
  return (
    <div
      className="fixed inset-x-0 bottom-0 z-40 border-t border-borde bg-papel px-4 pt-2 pb-[calc(0.75rem+env(safe-area-inset-bottom))] md:hidden"
    >
      <BotonPersistente cuenta={cuenta} className="w-full" />
      <div className="border-t border-borde">
        <BarraDestinos cuenta={cuenta} />
      </div>
    </div>
  );
}
