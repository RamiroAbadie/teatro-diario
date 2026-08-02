"use client";

import { useEffect } from "react";

import { Boton } from "@/components/ui/Boton";
import { EstadoVacio } from "@/components/ui/EstadoVacio";

/**
 * **Pantalla 13 · error genérico**: cualquier excepción que no atrapó una pantalla.
 *
 * **Va adentro de `(sitio)` y no en la raíz**, y es a propósito: un límite de error se dibuja
 * **dentro del layout de su segmento**, así que acá abajo llega con el armazón completo —que
 * es lo que pide `SCREEN_SPECS.md`— y en la raíz llegaría pelado. La contracara está escrita
 * en D83: por eso el layout **nunca** tira, ni siquiera cuando falla `yo()`.
 *
 * ⚠️ **Sin stack trace y sin código interno.** Lo que se muestra es qué hacer; lo que pasó va
 * a la consola del servidor, que es donde se mira.
 */
export default function ErrorDePantalla({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error("Error no atrapado en una pantalla del sitio", error);
  }, [error]);

  return (
    <div className="mx-auto max-w-3xl">
      <EstadoVacio
        titulo="No pudimos cargar esto"
        nivel={1}
        accion={<Boton onClick={reset}>Reintentar</Boton>}
      >
        Algo falló de nuestro lado. Probá de nuevo.
      </EstadoVacio>
    </div>
  );
}
