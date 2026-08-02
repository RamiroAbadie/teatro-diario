import { estadoEnCastellano, rolEnCastellano } from "@/lib/formato";
import type { EstadoProduccion, RolParticipacion } from "@/lib/api/tipos";

/**
 * `ui/5 · Chip` (D79). Una etiqueta corta de metadato. **Nunca es un control**: no se toca,
 * no filtra, no navega.
 *
 * ⚠️ **Lo que llega por color lleva además texto**: el chip dice "EN CARTEL", no es sólo
 * ámbar. Es la regla de accesibilidad de las 13 pantallas y acá se cumple sola porque el
 * contenido del chip **es** la etiqueta.
 */

const BASE = "inline-flex items-center rounded-full px-2 py-0.5 text-xs tracking-wide uppercase";

/**
 * **EN CARTEL** es el único chip con color, porque es el único que informa disponibilidad.
 * PRÓXIMAMENTE va con borde y fondo transparente; CERRADA, tenue.
 */
const ESTADOS: Record<EstadoProduccion, string> = {
  EN_CARTEL: "bg-acento-suave text-acento-tinta",
  PROXIMAMENTE: "border border-acento-tinta text-acento-tinta",
  CERRADA: "border border-borde text-tinta-tenue",
};

export function ChipDeEstado({ estado }: { estado: EstadoProduccion }) {
  return <span className={`${BASE} ${ESTADOS[estado]}`}>{estadoEnCastellano(estado)}</span>;
}

/** El rol de una participación (D17): neutro, nunca con el color del estado. */
export function ChipDeRol({ rol }: { rol: RolParticipacion }) {
  return <span className={`${BASE} border border-borde text-tinta-suave`}>{rolEnCastellano(rol)}</span>;
}

/**
 * La variante `nota`: los casos degradados —"ficha eliminada" (D62), "sin fecha" (MD-2)—.
 * **En minúscula y sin `uppercase`**: no es un estado del catálogo, es una aclaración.
 */
export function ChipDeNota({ children }: { children: string }) {
  return (
    <span className="inline-flex items-center rounded-full border border-borde px-2 py-0.5 text-xs text-tinta-tenue">
      {children}
    </span>
  );
}
