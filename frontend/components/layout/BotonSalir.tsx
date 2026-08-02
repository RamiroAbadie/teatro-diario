"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

import { logout } from "@/lib/api/identidad.cliente";

/**
 * Salir. **No tiene camino de error**: el logout responde `204` siempre, con sesión o sin
 * ella (`API.md`), así que no hay nada que dibujar detrás del botón.
 *
 * ⚠️ **La navegación no la hace este botón: la hace quien lo contiene, y es a propósito.**
 * En el menú principal, salir ocurre **adentro de un `<dialog>` que empujó una entrada al
 * historial** (D80/D81), así que navegar por afuera de ese mecanismo rompe de dos maneras
 * distintas y las dos son reales:
 *
 * - **desde una ruta interior**, el `router.push("/")` corre primero, el menú se cierra al
 *   cambiar la ruta y el `history.back()` de cierre **deshace la navegación**: el usuario
 *   termina deslogueado pero de vuelta en la pantalla de la que quería irse;
 * - **desde `/`**, el `push` no cambia el pathname, así que el efecto que cierra el menú
 *   **no se dispara** y el panel queda abierto encima de una sesión que ya no existe.
 *
 * Por eso `navegar` es obligatorio: el menú pasa el `navegar` de `usarDialogo` —cerrar
 * primero, navegar después— y la cabecera ancha pasa el suyo, que sólo cierra el desplegable.
 *
 * Lo del token CSRF no hace falta hacerlo acá: se rota en las tres puertas de la sesión
 * (D57), y `client.ts` relee la cookie antes de cada mutación.
 */
export function BotonSalir({
  navegar,
  className = "",
}: {
  navegar: (href: string) => void;
  className?: string;
}) {
  const [saliendo, setSaliendo] = useState(false);
  const router = useRouter();

  async function salir() {
    setSaliendo(true);
    await logout();

    // La sesión la resuelve el servidor en el layout del sitio: sin invalidar la caché del
    // router, el armazón seguiría dibujando al usuario que acaba de irse. Va antes de
    // navegar porque `navegar` puede diferir el `push` hasta el `popstate` del diálogo.
    router.refresh();
    navegar("/");
  }

  return (
    <button type="button" onClick={salir} disabled={saliendo} className={className}>
      {saliendo ? "Saliendo…" : "Salir"}
    </button>
  );
}
