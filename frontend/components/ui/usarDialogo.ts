"use client";

import { useRouter } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";
import type { MouseEvent } from "react";

/**
 * El comportamiento común de todo lo que se superpone —la hoja del gesto, `Confirmacion` y
 * el menú principal del armazón (D80/D81)—, escrito una sola vez.
 *
 * Se apoya en el elemento **`<dialog>` nativo**, que trae la captura de foco, el `Esc`, el
 * fondo inerte, la capa superior y la vuelta del foco al elemento que lo abrió **sin una
 * línea de JavaScript ni una dependencia** (D73). Lo que el navegador no trae y hay que
 * escribir es esto:
 *
 * 1. **Abrir empuja una entrada al historial y "atrás" cierra.** En un celular "atrás"
 *    quiere decir "cerrá esto", y sin esto el usuario se va de la pantalla creyendo que
 *    cierra la hoja.
 * 2. **Navegar desde adentro no deja la entrada colgada.** Un link del menú cierra primero
 *    —lo que consume la entrada que empujamos— y recién después navega, así el "atrás" de
 *    la pantalla nueva vuelve a la anterior y no a una entrada fantasma.
 * 3. **El clic afuera y `Esc` cancelan, nunca confirman.**
 *
 * ⚠️ **`conHistorial: false` es para lo que se superpone a otra cosa que ya empujó su
 * entrada**, y hoy es un solo caso: el `Confirmacion` de borrar, que se abre **adentro** de
 * la hoja del gesto. Si los dos empujaran, cancelar la confirmación haría `history.back()`,
 * y ese `popstate` cerraría también la hoja que está debajo — o sea que cancelar borraría lo
 * tipeado. Sin entrada propia, cancelar no toca el historial y "atrás" cierra las dos, que es
 * lo que se espera de "atrás".
 */
export function usarDialogo({ conHistorial = true }: { conHistorial?: boolean } = {}) {
  const ref = useRef<HTMLDialogElement>(null);
  const [abierto, setAbierto] = useState(false);
  const empujado = useRef(false);
  const destino = useRef<string | null>(null);
  const router = useRouter();

  const abrir = useCallback(() => {
    const dialogo = ref.current;
    if (!dialogo || dialogo.open) return;
    dialogo.showModal();
    if (conHistorial) {
      window.history.pushState({ dialogo: true }, "");
      empujado.current = true;
    }
    setAbierto(true);
  }, [conHistorial]);

  const cerrar = useCallback(() => ref.current?.close(), []);

  /** Cerrar y después navegar, en ese orden (regla 2 de arriba). */
  const navegar = useCallback((href: string) => {
    destino.current = href;
    ref.current?.close();
  }, []);

  const alClicEnElFondo = useCallback((evento: MouseEvent<HTMLDialogElement>) => {
    if (evento.target === ref.current) ref.current?.close();
  }, []);

  useEffect(() => {
    const dialogo = ref.current;
    if (!dialogo) return;

    const alCerrar = () => {
      setAbierto(false);
      // Cerramos nosotros (Esc, la cruz, un link): hay que consumir la entrada que
      // empujamos. La navegación pendiente, si la hay, sale del `popstate` que esto
      // dispara — mandarla acá compite con el `back()` y se pierde una de las dos.
      if (empujado.current) {
        empujado.current = false;
        window.history.back();
        return;
      }
      const href = destino.current;
      destino.current = null;
      if (href) router.push(href);
    };

    dialogo.addEventListener("close", alCerrar);
    return () => dialogo.removeEventListener("close", alCerrar);
  }, [router]);

  useEffect(() => {
    const alVolver = () => {
      empujado.current = false;
      ref.current?.close(); // si ya está cerrado, no hace nada
      const href = destino.current;
      destino.current = null;
      if (href) router.push(href);
    };

    window.addEventListener("popstate", alVolver);
    return () => window.removeEventListener("popstate", alVolver);
  }, [router]);

  return { ref, abierto, abrir, cerrar, navegar, alClicEnElFondo };
}
