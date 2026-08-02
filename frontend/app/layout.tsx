import type { Metadata } from "next";
import type { ReactNode } from "react";

import { SITIO } from "@/lib/metadatos";

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
  // ⚠️ **`metadataBase` es lo que hace absolutos los `og:image` y los `canonical`.** Sin él,
  // Next emite rutas relativas y **el preview de WhatsApp se queda sin imagen**: el servidor
  // que lo arma no está en nuestro origen y no tiene contra qué resolverlas. Es la pieza que
  // sostiene el test literal de HU-04 y no se ve en ninguna pantalla.
  metadataBase: SITIO,
  // Sin nombre de producto: P2 sigue abierto. La frase funciona sola y el día que P2
  // cierre se le pone el nombre encima sin tocar el resto (D79, regla 3).
  title: { default: "Diario de teatro", template: "%s · Diario de teatro" },
  description: "El diario de tu teatro de acá en adelante.",

  // ⚠️ **El Open Graph genérico del sitio va acá y no en la home**, y sin esto la home no
  // tiene ninguno: Next **no deduce `og:*` de `title` y `description`** — sólo emite lo que
  // encuentra en `openGraph`. Puesto en la raíz cubre de una vez la pantalla 1 (que
  // `SCREEN_SPECS.md` pide con OG genérico) y cualquier pantalla futura que no traiga el
  // suyo; las cuatro compartibles lo pisan entero con `metadatosCompartibles`.
  //
  // **Sin `og:image` a propósito**: la placa de D85 se dibuja con el título de una ficha, un
  // artista o una sala, y acá no hay ninguno — una placa genérica sería una imagen de marca,
  // que es justo lo que P2 no cerró todavía. El preview de la home queda con título y
  // descripción, que es degradado y no roto (`SCREEN_SPECS.md`).
  openGraph: {
    title: "Diario de teatro",
    description: "El diario de tu teatro de acá en adelante.",
    url: "/",
    type: "website",
    locale: "es_AR",
  },
  twitter: {
    card: "summary",
    title: "Diario de teatro",
    description: "El diario de tu teatro de acá en adelante.",
  },
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="es">
      <body>{children}</body>
    </html>
  );
}
