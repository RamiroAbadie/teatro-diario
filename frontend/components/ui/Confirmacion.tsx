"use client";

import type { ReactNode } from "react";

import { Aviso } from "./Aviso";
import { Boton } from "./Boton";
import type { usarDialogo } from "./usarDialogo";

/**
 * `ui/10 · Confirmacion` (D79). **Lo irreversible, y sólo eso**: borrar un registro (HU-11),
 * borrar una reseña reportada (HU-22) y fusionar duplicados (D63), que es la más irreversible
 * de todas. **El cambio de estado de una producción no pasa por acá**: es reversible en otro
 * clic y el barrido semanal de D37 vive de que eso sea un solo toque.
 *
 * Reglas, todas de D79 y ninguna decorativa:
 *
 * - **El botón que confirma nombra la acción** —"Borrar registro"—, nunca "Aceptar".
 * - **El foco entra en Cancelar**, no en el botón que destruye.
 * - **`Esc` y el clic afuera cancelan, nunca confirman.**
 * - **El error aparece adentro del diálogo y el diálogo no se cierra**: cerrarlo dejaría al
 *   usuario sin saber si pasó o no.
 *
 * ⚠️ **No es el acuse de una sugerencia.** "Confirmación" en HU-08 quiere decir otra cosa
 * —una pantalla de recibido— y confundirlas pone un modal donde va una pantalla.
 *
 * El diálogo lo abre y lo cierra **quien lo contiene**, con `usarDialogo`: así el que
 * confirma con éxito puede cerrarlo por el mismo camino que el que cancela, y la entrada de
 * historial que abrir empujó se consume igual en los dos casos.
 */

type Props = {
  dialogo: ReturnType<typeof usarDialogo>;
  titulo: string;
  children?: ReactNode;
  /** Nombra la acción. Nunca "Aceptar". */
  etiqueta: string;
  etiquetaCargando: string;
  cargando?: boolean;
  error?: string | null;
  onConfirmar: () => void;
};

export function Confirmacion({
  dialogo,
  titulo,
  children,
  etiqueta,
  etiquetaCargando,
  cargando = false,
  error = null,
  onConfirmar,
}: Props) {
  return (
    <dialog
      ref={dialogo.ref}
      onClick={dialogo.alClicEnElFondo}
      aria-labelledby="confirmacion-titulo"
      className="m-auto w-[min(28rem,calc(100vw-2rem))] rounded-lg border border-borde bg-papel p-6 text-tinta"
    >
      <h2 id="confirmacion-titulo" className="font-titulo text-xl">
        {titulo}
      </h2>

      {children && <p className="mt-2 text-tinta-suave">{children}</p>}

      {error && (
        <Aviso variante="error" className="mt-4">
          {error}
        </Aviso>
      )}

      <div className="mt-6 flex justify-end gap-3">
        {/* El foco entra acá: `autoFocus` sobre Cancelar, que es lo que el `<dialog>` nativo
            enfoca primero y lo que D79 pide explícitamente. */}
        <Boton variante="secundario" autoFocus disabled={cargando} onClick={dialogo.cerrar}>
          Cancelar
        </Boton>
        <Boton
          variante="peligro"
          cargando={cargando}
          etiquetaCargando={etiquetaCargando}
          onClick={onConfirmar}
        >
          {etiqueta}
        </Boton>
      </div>
    </dialog>
  );
}
