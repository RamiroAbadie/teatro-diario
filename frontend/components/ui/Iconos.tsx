/**
 * SVG en línea escritos a mano, y **no más de diez** (D79/D81): buscar, corazón, aviso,
 * cruz, cheurón, más, hamburguesa, casa, cartel y tres puntos. No entra ninguna librería
 * de íconos: sería la dependencia que D73 dejó afuera.
 *
 * Acá están los que ya usa el armazón; los otros entran con la pantalla que los estrena.
 *
 * ⚠️ **Ningún ícono viaja solo sin etiqueta accesible.** Todos son `aria-hidden` porque
 * en las diez apariciones del sistema el texto está al lado; si alguna vez uno queda solo,
 * la etiqueta la pone quien lo usa (`aria-label` en el control), no el ícono.
 */

type Props = { className?: string };

const comunes = {
  viewBox: "0 0 24 24",
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 1.75,
  strokeLinecap: "round" as const,
  strokeLinejoin: "round" as const,
  "aria-hidden": true,
  focusable: false,
};

export function IconoBuscar({ className = "size-5" }: Props) {
  return (
    <svg {...comunes} className={className}>
      <circle cx="11" cy="11" r="7" />
      <path d="m16.5 16.5 4 4" />
    </svg>
  );
}

export function IconoHamburguesa({ className = "size-5" }: Props) {
  return (
    <svg {...comunes} className={className}>
      <path d="M4 7h16M4 12h16M4 17h16" />
    </svg>
  );
}

export function IconoCruz({ className = "size-5" }: Props) {
  return (
    <svg {...comunes} className={className}>
      <path d="m6 6 12 12M18 6 6 18" />
    </svg>
  );
}

export function IconoCasa({ className = "size-5" }: Props) {
  return (
    <svg {...comunes} className={className}>
      <path d="M4 10.5 12 4l8 6.5" />
      <path d="M6 9.5V20h12V9.5" />
    </svg>
  );
}

/** Cartel: un rectángulo con cabecera — la marquesina de una sala. */
export function IconoCartel({ className = "size-5" }: Props) {
  return (
    <svg {...comunes} className={className}>
      <rect x="3.5" y="5" width="17" height="14" rx="1.5" />
      <path d="M3.5 9.5h17" />
    </svg>
  );
}

export function IconoCheuron({ className = "size-4" }: Props) {
  return (
    <svg {...comunes} className={className}>
      <path d="m7 10 5 5 5-5" />
    </svg>
  );
}
