"use client";

import { useRouter } from "next/navigation";
import { useState, useTransition } from "react";

import { Boton } from "./Boton";

/**
 * El "Reintentar" de `EstadoVacio` variante `error` cuando **lo que falló es una pantalla o un
 * bloque que el servidor ya atrapó** —y no una excepción que subió hasta `error.tsx`—. Hoy son
 * dos: el bloque de opiniones de la ficha, que puede caerse sin llevarse la ficha con él (D80:
 * "la degradación es por bloque"), y la lista de "en cartel", donde la pantalla **es** la
 * lista y atraparla acá deja en su lugar el `h1` y las salidas del armazón.
 *
 * Es la isla más chica posible y existe por una razón: **reintentar es volver a pedirle la
 * pantalla al servidor**, y eso es `router.refresh()`. Un `Link` a la misma URL no sirve —
 * puede resolverse contra la caché del router y devolver exactamente el mismo error, que
 * desde afuera se ve como un botón que no hace nada.
 *
 * `error.tsx` **no usa esto**: ahí el que reintenta es el `reset()` que Next le pasa al
 * límite de error, que además vuelve a montar el subárbol.
 */
export function BotonReintentar() {
  const router = useRouter();
  const [pendiente, iniciar] = useTransition();
  const [refrescando, setRefrescando] = useState(false);

  return (
    <Boton
      variante="secundario"
      cargando={pendiente || refrescando}
      etiquetaCargando="Reintentando…"
      onClick={() => {
        setRefrescando(true);
        iniciar(() => router.refresh());
        // El `refresh` no avisa cuándo terminó de pintar; el `transition` cubre el pedido y
        // esto evita que el botón quede en gerundio si algo queda colgado.
        setTimeout(() => setRefrescando(false), 3000);
      }}
    >
      Reintentar
    </Boton>
  );
}
