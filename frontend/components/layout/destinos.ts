import { rutaUsuario } from "@/lib/rutas";
import type { Cuenta } from "@/lib/api/tipos";

/**
 * Los destinos del armazón (D81). Salen de la **única** respuesta de `GET /api/auth/yo`
 * que resuelve el layout: el armazón no agrega ni una llamada.
 *
 * Los usan la barra del bloque inferior (celular) y la cabecera ancha (≥`md`), que son la
 * misma lista puesta en dos lados.
 *
 * ⚠️ Dos diferencias del anónimo, y las dos tienen motivo: **"Mi diario" no existe sin
 * sesión** —no hay diario que mostrar— y **"Feed" se llama "Inicio"**, porque sin cookie
 * esa ruta es la pantalla 1 y no la 2.
 */

export type Icono = "casa" | "cartel" | "lupa" | "monograma";

export type Destino = {
  href: string;
  etiqueta: string;
  icono: Icono;
  /** `true` cuando la ruta activa se decide por prefijo y no por igualdad. */
  porPrefijo?: boolean;
};

export function destinos(cuenta: Cuenta | null): Destino[] {
  const comunes: Destino[] = [
    { href: "/en-cartel", etiqueta: "En cartel", icono: "cartel" },
    { href: "/buscar", etiqueta: "Buscar", icono: "lupa", porPrefijo: true },
  ];

  if (!cuenta) {
    return [{ href: "/", etiqueta: "Inicio", icono: "casa" }, ...comunes];
  }

  return [
    { href: "/", etiqueta: "Feed", icono: "casa" },
    ...comunes,
    { href: rutaUsuario(cuenta.username), etiqueta: "Mi diario", icono: "monograma" },
  ];
}

export function esElDestinoActivo(destino: Destino, ruta: string): boolean {
  return destino.porPrefijo ? ruta.startsWith(destino.href) : ruta === destino.href;
}
