"use client";

import { Boton } from "@/components/ui/Boton";
import { abrirGesto } from "@/components/diario/gesto";

/**
 * La mitad con sesión del botón persistente. Es lo **único** del armazón que necesita
 * JavaScript sin sesión de por medio, y por eso está separado: el visitante anónimo
 * —que son casi todas las visitas— se lleva un link y nada más.
 */
export function BotonRegistrar({ className }: { className?: string }) {
  return (
    <Boton className={className} onClick={() => abrirGesto()}>
      Registrar
    </Boton>
  );
}
