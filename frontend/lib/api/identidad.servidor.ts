import { cache } from "react";

import { FalloDeApi } from "./errores";
import { apiSession } from "./server";
import type { Cuenta } from "./tipos";

/**
 * `lib/api/` espeja los módulos del backend (D78). Este es Identidad, **lado servidor**.
 *
 * El par `<modulo>.servidor.ts` / `<modulo>.cliente.ts` no es capricho: un módulo con las
 * dos mitades en un archivo no compila, porque el que importa `next/headers` termina en el
 * bundle del navegador. La regla sigue siendo la de D78 —un módulo del backend, un cliente
 * del frontend—; lo que se parte es el archivo, no el módulo.
 */

/**
 * Quién sos. Lo llama el layout del sitio para el armazón, y **lo vuelve a llamar cada
 * página que necesite elegir entre `apiPublic` y `apiSession`** —la ficha y el perfil, que
 * cambian de respuesta con cookie—, porque **Next no pasa datos de un layout a sus hijos**.
 *
 * Por eso va envuelta en `cache()` de React: dentro de un mismo pedido, la segunda llamada
 * y las que sigan devuelven lo ya resuelto **sin tocar la red**. Es lo que hace verdadera
 * la frase de D78 —"la sesión se resuelve una sola vez"— cuando quien la necesita son dos
 * componentes que no se conocen. No es una caché entre pedidos: `apiSession` sigue siendo
 * `no-store` y la memoización muere con el render.
 *
 * ⚠️ Su `401` **no es un error**: es "anónimo", que es el estado normal de la mayoría de
 * las visitas. No se muestra, no navega a ningún lado y se dibuja la versión sin sesión de
 * la pantalla — por eso devuelve `null` y no tira.
 */
export const yo = cache(async (): Promise<Cuenta | null> => {
  try {
    return await apiSession<Cuenta>("/api/auth/yo");
  } catch (error) {
    // ⚠️ Primero lo que NO es un error: Next señala con excepciones varias cosas de control
    // —bajar a render dinámico porque se leyó la cookie, `redirect()`, `notFound()`— y las
    // marca con un `digest`. Tragárselas rompe el mecanismo desde adentro, y encima en
    // silencio. Se dejan pasar sin mirarlas.
    if (typeof (error as { digest?: unknown })?.digest === "string") throw error;

    if (error instanceof FalloDeApi && error.status === 401) return null;

    // ⚠️ **De acá para abajo cae TODO lo demás, y el alcance es más ancho de lo que suena**
    // (D83): no sólo el `5xx` y la red caída, sino **cualquier excepción que no sea control
    // de Next ni el `401`** — un `403` inesperado, un cuerpo que no es el JSON que dice el
    // contrato, un `TypeError` de esta misma función. Está escrito ancho **a propósito**: la
    // regla no es "qué error fue", es **"el armazón nunca tira la pantalla abajo"**.
    //
    // Por qué: tirar desde acá **no lo atrapa `error.tsx`** —un error de layout lo agarra el
    // límite del segmento de ARRIBA, y arriba está la raíz—, así que cualquier fallo al
    // resolver la sesión serviría la pantalla global de error en TODAS las URLs, incluidas
    // las públicas que Next todavía puede servir de su caché de datos. Eso es exactamente lo
    // que `SCREEN_SPECS.md` prohíbe para la ficha: "la degradación es por bloque", y la
    // mitad que sostiene el Flujo 1 es la que tiene que sobrevivir. Medido, no supuesto:
    // propagando, la home responde 500 y sin armazón.
    //
    // Los dos costos, escritos:
    //  1. a alguien con sesión el armazón le va a decir "Crear tu diario" mientras el
    //     backend no responda. Se corrige solo: cualquier acción protegida se come su `401`
    //     y va al login.
    //  2. **un bug de esta función se ve como "no hay sesión" y no como un error.** Por eso
    //     el `console.error` de abajo no es decorativo: es el único lugar donde ese caso
    //     aparece, y hay que mirarlo.
    //
    // ⏳ **`Aviso` ya existe (paso 4), así que el pendiente de D83 dejó de ser "no hay con
    // qué dibujarla" y pasó a ser una decisión: por ahora, NO se dibuja** (D91). Tres razones,
    // y la primera es la que manda: para saber que hubo un fallo —y no un visitante anónimo,
    // que es el caso normal— esta función tendría que devolver algo más que `Cuenta | null`,
    // y eso lo pagan sus cuatro llamadores para un estado que **se corrige solo** (cualquier
    // acción protegida se come su `401` y va al login). Segunda: la banda caería sobre las
    // páginas públicas que Next todavía puede servir de su caché, que es exactamente lo que
    // esta degradación existe para proteger. Tercera: "no pudimos verificar tu sesión" no es
    // accionable — no hay nada que el usuario pueda hacer con eso.
    // **Se revisa en el paso 5**, que es cuando el fallo pasa a ser visible de verdad: con el
    // feed escrito, un `yo()` caído le muestra la landing del visitante a alguien logueado.
    console.error("No se pudo resolver la sesión; se dibuja el armazón del visitante", error);
    return null;
  }
});
