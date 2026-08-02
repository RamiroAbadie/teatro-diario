import { readFile } from "node:fs/promises";
import { join } from "node:path";

import { ImageResponse } from "next/og";

import { artista, fichaPublica, sala } from "@/lib/api/catalogo.servidor";
import { estadoEnCastellano, salaConComplejo } from "@/lib/formato";
import type { TipoDePlaca } from "@/lib/og";

/**
 * La **placa tipográfica de 1200×630** que va como `og:image` cuando no hay afiche —que hoy
 * es siempre, porque la subida espera a P16—. Es **la mitad de la mecánica de crecimiento**:
 * el preview que se ve al pegar el link en WhatsApp, o sea el test literal de HU-04.
 *
 * **Una sola ruta para los tres tipos** (obra, artista, sala) y no tres archivos casi
 * iguales: la placa es un diseño, y tres copias se separan en la segunda corrección. Lo
 * único que cambia por tipo es de dónde salen el título y el pie.
 *
 * **Siempre en la versión oscura, en los dos temas** (D79): WhatsApp no tiene tema y la
 * preview tiene que ser una sola cosa.
 *
 * ⚠️ **La fuente va embebida, y esto es lo que D79 dejó anotado sin cerrar.** `next/og`
 * compone con satori, que **no puede usar fuentes del sistema**: sólo dibuja con los bytes
 * que se le pasan, y lo único que trae por defecto es una sans. Sin un archivo, la placa
 * saldría sin la serif, que es la mitad de la identidad propia de D71. Por eso hay un Noto
 * Serif **subseteado a latín** (30 KB) en `frontend/assets/`, con su licencia OFL al lado.
 * **No es una webfont y no contradice a D79**: el navegador no la descarga nunca —se usa en
 * el servidor y sólo para generar esta imagen— y el sitio sigue con las dos familias del
 * sistema.
 *
 * Todo por `apiPublic`: esta imagen la pide un servidor de WhatsApp, no una persona.
 */

const ANCHO = 1200;
const ALTO = 630;

/** Los hex de la versión oscura de D79, a mano: satori no ve el CSS de tokens. */
const FONDO = "#26231F";
const TITULO = "#F5F2EC"; // 14,00:1 contra el fondo
const META = "#C0BAB0";
const ACENTO = "#E8A317"; // 7,21:1

const TIPOS: TipoDePlaca[] = ["obra", "artista", "sala"];

export async function GET(
  _pedido: Request,
  { params }: { params: Promise<{ tipo: string; id: string }> },
) {
  const { tipo, id } = await params;
  const numero = Number(id);

  if (!TIPOS.includes(tipo as TipoDePlaca) || !/^[1-9]\d*$/.test(id)) {
    return new Response(null, { status: 404 });
  }

  let placa: { titulo: string; pie: string | null };
  try {
    placa = await contenido(tipo as TipoDePlaca, numero);
  } catch {
    // Una placa no puede tirar una pantalla: si el recurso no está, se responde 404 y el
    // preview se queda con título y descripción, que es degradado y no roto (D80).
    return new Response(null, { status: 404 });
  }

  const serif = await readFile(join(process.cwd(), "assets", "NotoSerif-Regular-subset.ttf"));

  return new ImageResponse(
    (
      <div
        style={{
          width: "100%",
          height: "100%",
          display: "flex",
          flexDirection: "column",
          justifyContent: "flex-end",
          backgroundColor: FONDO,
          // **Margen de seguridad de 60 px** por lado: WhatsApp y Twitter recortan distinto.
          padding: 60,
          fontFamily: "Noto Serif",
        }}
      >
        <div style={{ display: "flex", width: 120, height: 6, backgroundColor: ACENTO }} />
        <div
          style={{
            display: "flex",
            marginTop: 32,
            fontSize: cuerpoSegunLargo(placa.titulo),
            lineHeight: 1.1,
            color: TITULO,
          }}
        >
          {recortar(placa.titulo)}
        </div>
        {placa.pie && (
          <div style={{ display: "flex", marginTop: 24, fontSize: 32, color: META }}>
            {placa.pie}
          </div>
        )}
        {/* ⬛ Abajo a la izquierda queda un espacio reservado y vacío para el logotipo del
            día que P2 cierre. Hoy no se dibuja nada y la placa se sostiene igual, que es
            exactamente lo que pide la regla 3 de D79. */}
      </div>
    ),
    { width: ANCHO, height: ALTO, fonts: [{ name: "Noto Serif", data: serif, style: "normal" }] },
  );
}

async function contenido(tipo: TipoDePlaca, id: number): Promise<{ titulo: string; pie: string | null }> {
  if (tipo === "obra") {
    const ficha = await fichaPublica(id);
    return {
      titulo: ficha.titulo,
      pie: [salaConComplejo(ficha.sala), estadoEnCastellano(ficha.estado)].filter(Boolean).join(" · "),
    };
  }

  if (tipo === "artista") {
    const persona = await artista(id);
    return { titulo: persona.nombre, pie: null };
  }

  const laSala = await sala(id);
  return { titulo: laSala.nombre, pie: laSala.complejo };
}

/** Los mismos tres escalones de cuerpo que la placa de pantalla, en la escala de 1200×630. */
function cuerpoSegunLargo(titulo: string): number {
  if (titulo.length <= 24) return 92;
  if (titulo.length <= 60) return 68;
  return 52;
}

/**
 * Satori no tiene `line-clamp`: si el título no entra en tres líneas, se corta acá. El tope
 * sale de cuántos caracteres entran al cuerpo más chico.
 */
function recortar(titulo: string): string {
  const tope = 120;
  return titulo.length <= tope ? titulo : `${titulo.slice(0, tope - 1).trimEnd()}…`;
}
