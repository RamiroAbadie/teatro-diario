import Link from "next/link";

import { rutaUsuario } from "@/lib/rutas";

/**
 * `ui/6 · Usuario` (D79). La firma de una persona, siempre igual, en las cuatro pantallas
 * donde aparece gente.
 *
 * **El avatar es un monograma, no una foto**: no hay subida de imágenes de perfil en el
 * alcance congelado (D27) y ningún endpoint devuelve una. Se descartó el color
 * determinístico por username: el avatar **nunca aparece solo**, el nombre está siempre al
 * lado, así que el color sólo duplicaría información a cambio de doce valores más que
 * verificar en dos temas.
 */

type Props = {
  /** `null` es "la cuenta ya no existe" — el caso de API.md para reseñas, feed y colas. */
  username: string | null;
  variante?: "linea" | "cabecera";
  meta?: string;
  /** El monograma de la barra de destinos no navega: la celda entera es el link (D81). */
  sinLink?: boolean;
};

export function Usuario({ username, variante = "linea", meta, sinLink = false }: Props) {
  const cabecera = variante === "cabecera";

  if (username === null) {
    return (
      <span className="inline-flex items-center gap-2">
        <Monograma username={null} tamanio={cabecera ? "cabecera" : "linea"} />
        <span className="text-sm text-tinta-tenue italic">cuenta eliminada</span>
      </span>
    );
  }

  const cuerpo = (
    <>
      <Monograma username={username} tamanio={cabecera ? "cabecera" : "linea"} />
      {/* En `cabecera` el username ES el título de la pantalla (el perfil, SCREEN_SPECS 8),
          así que va en el escalón que la escala reserva para eso. */}
      <span className={cabecera ? "font-titulo text-3xl sm:text-4xl" : "text-sm"}>{username}</span>
    </>
  );

  const contenedor = cabecera ? "inline-flex items-center gap-3" : "inline-flex items-center gap-2";

  return (
    <span className={cabecera ? "flex flex-col gap-1" : undefined}>
      {sinLink ? (
        <span className={contenedor}>{cuerpo}</span>
      ) : (
        <Link href={rutaUsuario(username)} className={contenedor}>
          {cuerpo}
        </Link>
      )}
      {meta && <span className="text-sm text-tinta-suave">{meta}</span>}
    </span>
  );
}

/**
 * El monograma solo. Lo usa `Usuario` y lo usa la celda **"Mi diario"** de la barra de
 * destinos, que por eso **no estrena ícono** (D81).
 */
export function Monograma({
  username,
  tamanio = "linea",
}: {
  username: string | null;
  tamanio?: "linea" | "cabecera" | "barra";
}) {
  const medidas = {
    linea: "size-6 text-xs",
    // `text-xs` es el piso absoluto de la escala y el monograma no lo baja (D79).
    barra: "size-6 text-xs",
    cabecera: "size-14 text-xl",
  }[tamanio];

  return (
    <span
      aria-hidden
      className={`inline-flex shrink-0 items-center justify-center rounded-full border border-borde bg-superficie font-medium text-tinta-suave ${medidas}`}
    >
      {username ? username.charAt(0).toUpperCase() : ""}
    </span>
  );
}
