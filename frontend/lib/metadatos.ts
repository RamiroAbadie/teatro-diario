import type { Metadata } from "next";

/**
 * Los metadatos de las pantallas compartibles, en un solo lugar (ADR-003, P13).
 *
 * **Por qué acá y no en cada página**: son seis campos que tienen que ser idénticos en las
 * cuatro —ficha, perfil, artista y sala— y el que se olvida no rompe nada visible. El test
 * de HU-04 es literal —pegar el link en WhatsApp y ver el preview— y un `og:title` que
 * quedó viejo se descubre recién ahí.
 */

/**
 * El origen público del sitio. En desarrollo es el de Next; en producción lo fija el
 * entorno (Fase 5), porque **Open Graph exige URLs absolutas**: un `og:image` relativo no lo
 * resuelve WhatsApp, que no está en nuestro origen.
 */
export const SITIO = new URL(process.env.SITIO_URL ?? "http://localhost:3000");

type Args = {
  titulo: string;
  descripcion: string;
  /** La ruta canónica de D74, ya con su slug: es la mitad de para qué existe el `308`. */
  ruta: string;
  /** El afiche cuando exista (D77) o la placa tipográfica cuando no (D71). */
  imagen?: string;
};

export function metadatosCompartibles({ titulo, descripcion, ruta, imagen }: Args): Metadata {
  return {
    title: titulo,
    description: descripcion,
    alternates: { canonical: ruta },
    openGraph: {
      title: titulo,
      description: descripcion,
      url: ruta,
      type: "website",
      ...(imagen ? { images: [{ url: imagen, width: 1200, height: 630 }] } : {}),
    },
    twitter: {
      card: "summary_large_image",
      title: titulo,
      description: descripcion,
      ...(imagen ? { images: [imagen] } : {}),
    },
  };
}

/**
 * La descripción cuando la ficha no tiene sinopsis, que es el caso frecuente del catálogo
 * recién cargado. **Nunca se manda una descripción vacía**: el preview se queda sin la única
 * línea de texto que lo hace parecer algo, y eso es la mitad de la mecánica de compartir.
 */
export function recortar(texto: string, tope = 160): string {
  const limpio = texto.replace(/\s+/g, " ").trim();
  return limpio.length <= tope ? limpio : `${limpio.slice(0, tope - 1).trimEnd()}…`;
}
