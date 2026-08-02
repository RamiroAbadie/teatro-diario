import { Boton } from "@/components/ui/Boton";
import type { Cuenta } from "@/lib/api/tipos";

import { BotonRegistrar } from "./BotonRegistrar";

/**
 * El botón que D71 pide que esté **siempre presente**, y que cambia de identidad según la
 * sesión (D81):
 *
 * | Sesión | Etiqueta | Qué hace |
 * |---|---|---|
 * | Con sesión | "Registrar" | abre la hoja del gesto (pantalla 9) |
 * | Sin sesión | "Crear tu diario" | va a `/registro` |
 *
 * Así **nunca abre un formulario que va a rebotar en un `401`**: para el anónimo es
 * directamente el CTA de adquisición del Flujo 1, que es lo que esa pantalla necesitaba
 * igual. Un solo control, dos productos — y por eso **no hay banners de registro
 * adicionales, ni interstitials, ni un segundo CTA al final del scroll** (D80).
 */
export function BotonPersistente({
  cuenta,
  className,
}: {
  cuenta: Cuenta | null;
  className?: string;
}) {
  if (!cuenta) {
    return (
      <Boton href="/registro" className={className}>
        Crear tu diario
      </Boton>
    );
  }

  return <BotonRegistrar className={className} />;
}
