"use client";

import { abrirGesto } from "@/components/diario/gesto";
import { Boton } from "@/components/ui/Boton";

/**
 * El CTA del gesto en la ficha (pantalla 3), que **cierra el hueco que el paso 2 dejó
 * anotado**: el backend ya mandaba `vecesQueLaVi` (D76) y faltaba la hoja.
 *
 * Es la convención de tres estados de D67/D68/D76 bajada a pantalla, y los tres dibujan cosas
 * distintas:
 *
 * | `vecesQueLaVi` | Qué se ve |
 * |---|---|
 * | `null` | **nada**: no hay sesión, así que no hay zona (el CTA de adquisición ya es el botón persistente) |
 * | `0` | `Boton` primario **"Registrar que la vi"** |
 * | `N ≥ 1` | "La viste N veces" + secundario **"Registrar de nuevo"** |
 *
 * ⚠️ **El re-visto se ofrece, no se esconde** (D19): registrar la misma obra otra vez es una
 * función del producto y no un duplicado a evitar.
 *
 * Abre la hoja **con la obra ya elegida**: desde la ficha, el gesto no vuelve a buscar lo que
 * se está mirando — que es la mitad de los seis toques del camino feliz.
 */
export function CtaDelGesto({
  vecesQueLaVi,
  produccionId,
  titulo,
}: {
  vecesQueLaVi: number | null;
  produccionId: number;
  titulo: string;
}) {
  if (vecesQueLaVi === null) return null;

  const abrir = () => abrirGesto({ produccionId, titulo });

  if (vecesQueLaVi === 0) return <Boton onClick={abrir}>Registrar que la vi</Boton>;

  return (
    <div className="flex flex-wrap items-center gap-3">
      <span className="text-tinta-suave">
        {vecesQueLaVi === 1 ? "La viste una vez" : `La viste ${vecesQueLaVi} veces`}
      </span>
      <Boton variante="secundario" onClick={abrir}>
        Registrar de nuevo
      </Boton>
    </div>
  );
}
