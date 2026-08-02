import Link from "next/link";
import type { ComponentProps, ReactNode } from "react";

/**
 * `ui/7 · Boton` (D79). Todas las acciones, incluido el de registrar, que está siempre
 * presente (D71). `Boton` absorbe el "botón seguir" de la lista vieja: seguir no es un
 * componente, es una variante alternante — la misma que el like.
 *
 * ⚠️ Falta la variante **`alternante`** (dos etiquetas y dos aspectos, con actualización
 * optimista y vuelta atrás si falla). Entra con la primera isla que la use —seguir (HU-15)
 * o el like (HU-17), paso 5 de la Fase 4—, porque su comportamiento es la mitad de la
 * variante y escribirla sin consumidor sería adivinarlo.
 */

type Variante = "primario" | "secundario" | "fantasma" | "peligro";
type Tamanio = "md" | "sm";

type Props = {
  children: ReactNode;
  variante?: Variante;
  /** `md` = 44 px, el del celular. `sm` = 32 px, **sólo panel admin** (D79). */
  tamanio?: Tamanio;
  /** Se deshabilita y la etiqueta pasa al gerundio. Sin spinner: dice más y cuesta menos. */
  cargando?: boolean;
  etiquetaCargando?: string;
  href?: string;
  className?: string;
} & Omit<ComponentProps<"button">, "className" | "children">;

const BASE =
  "inline-flex items-center justify-center gap-2 rounded-md px-4 text-base font-medium " +
  "transition-colors duration-150 disabled:cursor-not-allowed disabled:opacity-50";

const VARIANTES: Record<Variante, string> = {
  primario: "bg-acento text-sobre-acento hover:opacity-90",
  secundario: "border border-borde-control text-tinta hover:bg-acento-suave",
  fantasma: "text-acento-tinta hover:bg-acento-suave",
  // El borde de 1 px lo pide el tema oscuro —el relleno contra el fondo da 2,79:1 y no
  // alcanza para que se distinga el contorno del control— y se pone en los dos por simetría.
  peligro: "border border-peligro-tinta bg-peligro text-sobre-peligro hover:opacity-90",
};

const TAMANIOS: Record<Tamanio, string> = {
  md: "h-11", // 44 px: el objetivo táctil de D79
  sm: "h-8 px-3 text-sm",
};

export function Boton({
  children,
  variante = "primario",
  tamanio = "md",
  cargando = false,
  etiquetaCargando,
  href,
  className = "",
  disabled,
  ...resto
}: Props) {
  // La opacidad va sobre el botón entero y no sólo sobre el texto: así el texto conserva
  // su contraste contra el relleno, que es lo que se rompe al deshabilitar sólo la letra.
  const clases = `${BASE} ${VARIANTES[variante]} ${TAMANIOS[tamanio]} ${className}`.trim();
  const bloqueado = disabled || cargando;
  const contenido = cargando && etiquetaCargando ? etiquetaCargando : children;

  if (href && !bloqueado) {
    return (
      <Link href={href} className={clases}>
        {contenido}
      </Link>
    );
  }

  return (
    <button className={clases} disabled={bloqueado} {...resto}>
      {contenido}
    </button>
  );
}
