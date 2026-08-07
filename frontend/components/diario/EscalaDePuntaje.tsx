"use client";

/**
 * El puntaje del gesto: **una fila de 10 objetivos**, enteros de 1 a 10 (D9).
 *
 * ⚠️ **No está entre los diez de `ui/`, y es por la regla de pertenencia**: hoy la usa una
 * sola pantalla —el gesto, en crear y en editar, que es el mismo formulario—, así que vive
 * en `components/diario/` hasta que otra la necesite (D79). No confundir con `ui/Puntaje`,
 * que **muestra** un puntaje: esto lo **elige**.
 *
 * Tres decisiones adentro:
 *
 * - **Es opcional, y tocar el elegido otra vez lo saca.** Puntuar no es obligatorio (D18) y
 *   sin esto no habría forma de arrepentirse sin cerrar la hoja.
 * - **Sin estrellas y sin medios puntos** (D9). Se descartaron **dos filas de cinco**:
 *   duplican el escaneo y el número deja de leerse como una escala.
 * - **44 px de alto y ~32 de ancho en 360 px.** La cuenta es la que obliga a que sea un
 *   control segmentado y no diez botones separados: en un celular de 360 quedan **328 px**
 *   adentro de la hoja, y diez objetivos con separación no llegan a 32 px cada uno. Sin
 *   separación —con los bordes compartidos— dan **31,9 px**, que es lo que se ve como una
 *   escala y cumple de sobra el mínimo de 24 px de WCAG 2.5.8 AA.
 */

const VALORES = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

export function EscalaDePuntaje({
  valor,
  onCambiar,
}: {
  valor: number | null;
  onCambiar: (valor: number | null) => void;
}) {
  return (
    <fieldset>
      <legend className="text-sm text-tinta-suave">Puntaje (opcional)</legend>

      <div
        role="group"
        className="mt-1 flex divide-x divide-borde-control overflow-hidden rounded-md border border-borde-control"
      >
        {VALORES.map((numero) => {
          const elegido = valor === numero;
          return (
            <button
              key={numero}
              type="button"
              // `aria-pressed` y no un `radio`: un grupo de radios no se puede vaciar, y acá
              // volver a tocar el elegido es justamente cómo se saca el puntaje.
              aria-pressed={elegido}
              onClick={() => onCambiar(elegido ? null : numero)}
              className={
                "h-11 min-w-0 flex-1 text-base tabular-nums transition-colors duration-150 " +
                (elegido
                  ? "bg-acento font-medium text-sobre-acento"
                  : "bg-superficie text-tinta hover:bg-acento-suave")
              }
            >
              {numero}
            </button>
          );
        })}
      </div>
    </fieldset>
  );
}
