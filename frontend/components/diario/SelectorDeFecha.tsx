"use client";

import type { Granularidad } from "@/lib/api/tipos";

/**
 * La fecha difusa de MD-1/D59: **cuatro niveles y ninguno miente**. Día exacto, mes y año,
 * sólo año, o "no me acuerdo" —que es `SIN_FECHA` con la fecha en `null`, no una fecha
 * inventada—.
 *
 * **Hoy por defecto, un toque**: el caso frecuente es alguien que acaba de salir del teatro
 * (Flujo 3), así que el gesto no le pide que elija nada. Las otras tres son para el historial
 * viejo que D24 quiere rescatar, y elegir una **cambia qué selectores se ven**.
 *
 * ⚠️ **Lo que viaja es el comienzo del período** (D59): "marzo de 2023" es `2023-03-01` con
 * granularidad `MES`. El backend normaliza igual, pero mandar el día 15 con granularidad
 * `MES` sería mandar un dato que nadie dio.
 */

type Props = {
  granularidad: Granularidad;
  /** `YYYY-MM-DD` para `DIA`; para `MES` y `ANIO`, el comienzo del período. */
  fecha: string | null;
  error?: string;
  onCambiar: (granularidad: Granularidad, fecha: string | null) => void;
};

const MESES = [
  "enero", "febrero", "marzo", "abril", "mayo", "junio",
  "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
];

const NIVELES: { valor: Granularidad; etiqueta: string }[] = [
  { valor: "DIA", etiqueta: "día exacto" },
  { valor: "MES", etiqueta: "mes" },
  { valor: "ANIO", etiqueta: "año" },
  { valor: "SIN_FECHA", etiqueta: "no me acuerdo" },
];

export function SelectorDeFecha({ granularidad, fecha, error, onCambiar }: Props) {
  const hoy = hoyLocal();
  const [anio, mes] = (fecha ?? hoy).split("-");

  /** Al cambiar de nivel se conserva lo que ya se sabía, recortado a la nueva precisión. */
  function cambiarNivel(nuevo: Granularidad) {
    const base = fecha ?? hoy;
    const [a, m] = base.split("-");
    if (nuevo === "SIN_FECHA") return onCambiar(nuevo, null);
    if (nuevo === "ANIO") return onCambiar(nuevo, `${a}-01-01`);
    if (nuevo === "MES") return onCambiar(nuevo, `${a}-${m}-01`);
    return onCambiar(nuevo, base);
  }

  return (
    <fieldset>
      <legend className="text-sm text-tinta-suave">¿Cuándo la viste?</legend>

      <div className="mt-1">
        {granularidad === "DIA" && (
          <input
            type="date"
            aria-label="Día"
            value={fecha ?? hoy}
            // La función ya pasó: el backend rechaza una futura con un `400`, y atajarlo acá
            // es la red de contención al revés — el error no llega nunca.
            max={hoy}
            onChange={(e) => onCambiar("DIA", e.target.value || hoy)}
            className="h-11 w-full rounded-md border border-borde-control bg-superficie px-3 text-base"
          />
        )}

        {granularidad === "MES" && (
          <div className="flex gap-2">
            <select
              aria-label="Mes"
              value={Number(mes)}
              onChange={(e) => onCambiar("MES", `${anio}-${dosDigitos(e.target.value)}-01`)}
              className="h-11 flex-1 rounded-md border border-borde-control bg-superficie px-3 text-base"
            >
              {MESES.map((nombre, i) => (
                <option key={nombre} value={i + 1}>
                  {nombre}
                </option>
              ))}
            </select>
            <SelectorDeAnio
              anio={anio}
              onCambiar={(nuevo) => onCambiar("MES", `${nuevo}-${mes}-01`)}
            />
          </div>
        )}

        {granularidad === "ANIO" && (
          <SelectorDeAnio anio={anio} onCambiar={(nuevo) => onCambiar("ANIO", `${nuevo}-01-01`)} />
        )}

        {granularidad === "SIN_FECHA" && (
          <p className="text-tinta-tenue">
            Va a quedar en una sección aparte de tu diario, sin fecha inventada.
          </p>
        )}
      </div>

      {error && <p className="mt-1 text-sm text-peligro-tinta">{error}</p>}

      {/* Las alternativas, en línea y en minúscula: son una precisión, no cuatro botones de
          acción. La activa se marca con borde y peso, nunca sólo con color. */}
      <div className="mt-2 flex flex-wrap gap-2">
        {NIVELES.map(({ valor, etiqueta }) => (
          <button
            key={valor}
            type="button"
            aria-pressed={granularidad === valor}
            onClick={() => cambiarNivel(valor)}
            className={
              "h-9 rounded-full border px-3 text-sm transition-colors duration-150 " +
              (granularidad === valor
                ? "border-acento-tinta font-medium text-acento-tinta"
                : "border-borde text-tinta-suave hover:bg-acento-suave")
            }
          >
            {etiqueta}
          </button>
        ))}
      </div>
    </fieldset>
  );
}

function SelectorDeAnio({ anio, onCambiar }: { anio: string; onCambiar: (anio: string) => void }) {
  const actual = Number(hoyLocal().slice(0, 4));
  // Hasta 1950: más atrás que cualquier historial que alguien vaya a cargar a mano, y sin
  // convertir el selector en una lista infinita.
  const anios = Array.from({ length: actual - 1949 }, (_, i) => actual - i);

  return (
    <select
      aria-label="Año"
      value={anio}
      onChange={(e) => onCambiar(e.target.value)}
      className="h-11 flex-1 rounded-md border border-borde-control bg-superficie px-3 text-base"
    >
      {anios.map((a) => (
        <option key={a} value={a}>
          {a}
        </option>
      ))}
    </select>
  );
}

/**
 * Hoy **en la zona del navegador**, no en UTC. `new Date().toISOString()` en Buenos Aires
 * (UTC-3) devuelve el día siguiente después de las 21:00 — que es justo la hora del Flujo 3.
 */
export function hoyLocal(): string {
  const ahora = new Date();
  return `${ahora.getFullYear()}-${dosDigitos(ahora.getMonth() + 1)}-${dosDigitos(ahora.getDate())}`;
}

function dosDigitos(valor: number | string): string {
  return String(valor).padStart(2, "0");
}
