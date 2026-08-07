import type { ReactNode } from "react";

/**
 * `ui/9 · Aviso` (D79). **Decir algo sobre contenido que sí está.** Banda en línea, no
 * flotante y no temporal — es lo que lo separa de un estado vacío, donde no hay contenido, y
 * de un `toast`, que no existe en este sistema.
 *
 * - **`info`**: el `global: true` del feed (D22/D66) y la expectativa honesta de la
 *   sugerencia (MD-3).
 * - **`error`**: el **mensaje general de un formulario** — el `409` sin `errores` del alta
 *   (la carrera del índice único) y el `400` sin `errores` que quede en el panel.
 *
 * ⚠️ **No se puede cerrar, a propósito**: para recordar que alguien lo cerró hace falta
 * guardar esa preferencia en algún lado, y ese lado no existe (la misma razón que mató el
 * interruptor de tema). Un aviso que vuelve en cada carga es peor que uno que nunca se fue.
 *
 * ⚠️ **Nunca es un `toast`.** Un mensaje que se va solo a los tres segundos es inaccesible
 * para quien lee despacio, no deja rastro del error y pide un sistema de notificaciones
 * flotantes que nadie pidió.
 */

type Props = {
  children: ReactNode;
  variante?: "info" | "error";
  className?: string;
};

const VARIANTES = {
  info: "border-l-4 border-l-acento bg-acento-suave text-tinta",
  // El `error` se anuncia solo: casi siempre aparece **después** de que alguien envió un
  // formulario, así que quien no ve la pantalla se enteraría del fallo sólo si vuelve a
  // recorrerla. `role="alert"` es lo que lo dice sin agregar nada visible.
  error: "border border-peligro bg-superficie text-peligro-tinta",
} as const;

export function Aviso({ children, variante = "info", className = "" }: Props) {
  return (
    <p
      role={variante === "error" ? "alert" : undefined}
      className={`rounded-md px-4 py-3 text-base ${VARIANTES[variante]} ${className}`.trim()}
    >
      {children}
    </p>
  );
}
