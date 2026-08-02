import type { Metadata } from "next";
import type { ReactNode } from "react";

import "./globals.css";

/**
 * El layout raíz es **deliberadamente mínimo**: `<html>`, `<body>` y los tokens. Nada más.
 *
 * ⚠️ **Y no es minimalismo: es la única forma de que el panel pueda no llevar armazón.**
 * `SCREEN_SPECS.md` dice que las cuatro piezas están en todas las pantallas **salvo el
 * panel** (D81), y en el App Router **los layouts se anidan**: un `app/admin/layout.tsx` no
 * reemplaza al de arriba, se mete adentro. Si el armazón viviera acá, el panel lo llevaría
 * puesto para siempre y sacárselo obligaría a mover el árbol de rutas entero.
 *
 * Por eso el armazón vive un escalón más abajo, en el grupo `(sitio)`, y el panel va a
 * colgar de `app/admin/` con el suyo. **Los paréntesis no cambian la URL**: `(sitio)/page`
 * sigue siendo `/`.
 *
 * ⏳ Consecuencia anotada para la **pantalla 13**: una URL que no matchea ninguna ruta usa
 * el `app/not-found.tsx` de la raíz, que se dibuja con **este** layout y no con el del
 * sitio. `SCREEN_SPECS.md` pide que las dos pantallas de error lleven el armazón completo,
 * así que ese archivo **tiene que componerlo a mano** —pedir `yo()` y envolver en
 * `Cabecera`, `Pie` y `BloqueInferior`—. Dejarlo pelado no es una alternativa: es la única
 * salida que le queda a alguien que llegó por un link roto.
 */

export const metadata: Metadata = {
  // Sin nombre de producto: P2 sigue abierto. La frase funciona sola y el día que P2
  // cierre se le pone el nombre encima sin tocar el resto (D79, regla 3).
  title: "Diario de teatro",
  description: "El diario de tu teatro de acá en adelante.",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="es">
      <body>{children}</body>
    </html>
  );
}
