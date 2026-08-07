"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import { Aviso } from "@/components/ui/Aviso";
import { Boton } from "@/components/ui/Boton";
import { Campo, CampoLargo } from "@/components/ui/Campo";
import { usarBorrador } from "@/components/ui/usarBorrador";
import { sugerir } from "@/lib/api/catalogo.cliente";
import { comoFormulario } from "@/lib/api/errores";
import type { SugerenciaRecibida } from "@/lib/api/tipos";

/**
 * **Pantalla 10 · Sugerir producción** (HU-08). La válvula del catálogo cerrado (D7/D24) y
 * **la segunda de las tres cosas que D71 mandó diseñar a mano**: ninguna referencia la tiene.
 *
 * La regla que gobierna las tres: **lo tipeado nunca se pierde y nunca se promete lo que MD-3
 * dijo que no hay.**
 *
 * - **Un solo campo obligatorio, el título**, y llega ya cargado: por `?titulo=` desde la
 *   búsqueda o el gesto, o por el borrador si se volvió del login. El foco entra **en el
 *   campo siguiente**, no en el primero: lo que ya está escrito no se vuelve a escribir.
 * - **El borrador se guarda en `sessionStorage` mientras se escribe**, porque este formulario
 *   pide sesión: sin cuenta, el `POST` se come un `401`, el manejador global manda al login y
 *   al volver esto tiene que estar entero.
 * - **La confirmación es una pantalla y no un cartel que se va** (ver `Acuse`).
 */

const CLAVE = "borrador:sugerencia";

type Borrador = {
  /** De qué `?titulo=` salió este borrador: ver el efecto de abajo. */
  semilla: string;
  titulo: string;
  sala: string;
  anio: string;
  elenco: string;
  comentario: string;
};

export function FormularioDeSugerencia({ tituloInicial }: { tituloInicial: string }) {
  const router = useRouter();
  const { valor, setValor, olvidar } = usarBorrador<Borrador>(CLAVE, {
    semilla: tituloInicial,
    titulo: tituloInicial,
    sala: "",
    anio: "",
    elenco: "",
    comentario: "",
  });

  const [enviando, setEnviando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const [errores, setErrores] = useState<Record<string, string>>({});
  const [recibida, setRecibida] = useState<SugerenciaRecibida | null>(null);

  // ⚠️ **Un borrador viejo no puede secuestrar una sugerencia nueva.** Si se llega desde la
  // búsqueda con un título distinto del que dejó el borrador, gana el de la URL: es la
  // intención de ahora. Si es el mismo —el caso de volver del login—, manda el borrador, que
  // es lo último que la persona escribió.
  useEffect(() => {
    if (!tituloInicial) return;
    setValor((actual) =>
      actual.semilla === tituloInicial
        ? actual
        : { ...actual, semilla: tituloInicial, titulo: tituloInicial },
    );
  }, [tituloInicial, setValor]);

  const cambiar = (campo: keyof Borrador) => (texto: string) =>
    setValor((actual) => ({ ...actual, [campo]: texto }));

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (enviando) return;

    const propios = validar(valor);
    if (Object.keys(propios).length > 0) {
      setErrores(propios);
      setAviso(null);
      return;
    }

    setEnviando(true);
    setErrores({});
    setAviso(null);

    try {
      const respuesta = await sugerir({
        titulo: valor.titulo.trim(),
        sala: aTextoONulo(valor.sala),
        anio: valor.anio.trim() === "" ? null : Number(valor.anio),
        elenco: aTextoONulo(valor.elenco),
        comentario: aTextoONulo(valor.comentario),
      });
      // Lo que ya se envió no es un borrador.
      olvidar();
      setRecibida(respuesta);
    } catch (error) {
      // **El formulario no se vacía**, ni con un `400` ni con un `5xx`: lo que hay que
      // cambiar es un dato, y si el que falló fue el servidor no hay nada que cambiar.
      const { general, campos } = comoFormulario(error);
      setErrores(campos);
      setAviso(general);
      setEnviando(false);
    }
  }

  if (recibida) {
    return (
      <Acuse
        sugerencia={recibida}
        alVolver={() => router.back()}
        alSugerirOtra={() => {
          setValor({ semilla: "", titulo: "", sala: "", anio: "", elenco: "", comentario: "" });
          setRecibida(null);
          setEnviando(false);
        }}
      />
    );
  }

  return (
    <form method="post" onSubmit={enviar} noValidate className="mt-6 space-y-5">
      {aviso && <Aviso variante="error">{aviso}</Aviso>}

      <Campo
        id="titulo"
        etiqueta="Título de la obra"
        value={valor.titulo}
        onChange={(e) => cambiar("titulo")(e.target.value)}
        autoFocus={tituloInicial === ""}
        required
        maxLength={250}
        error={errores.titulo}
      />

      <div>
        <p className="text-sm text-tinta-tenue">Si te acordás, ayuda — todo esto es opcional.</p>

        <div className="mt-3 space-y-5">
          <Campo
            id="sala"
            etiqueta="Sala"
            value={valor.sala}
            onChange={(e) => cambiar("sala")(e.target.value)}
            // El foco entra acá cuando el título ya vino cargado.
            autoFocus={tituloInicial !== ""}
            maxLength={250}
            error={errores.sala}
          />

          <Campo
            id="anio"
            etiqueta="Año"
            type="number"
            inputMode="numeric"
            min={1800}
            max={2100}
            value={valor.anio}
            onChange={(e) => cambiar("anio")(e.target.value)}
            error={errores.anio}
          />

          <CampoLargo
            id="elenco"
            etiqueta="Elenco o dirección"
            rows={3}
            value={valor.elenco}
            onChange={(e) => cambiar("elenco")(e.target.value)}
            maxLength={1000}
            error={errores.elenco}
          />

          <CampoLargo
            id="comentario"
            etiqueta="Algo más que ayude a encontrarla"
            rows={3}
            value={valor.comentario}
            onChange={(e) => cambiar("comentario")(e.target.value)}
            maxLength={1000}
            error={errores.comentario}
          />
        </div>
      </div>

      <Boton type="submit" cargando={enviando} etiquetaCargando="Enviando…" className="w-full">
        Enviar
      </Boton>
    </form>
  );
}

/**
 * **El tercer momento del camino del catálogo cerrado** (D79): la confirmación **es una
 * pantalla y no un cartel que se va**. Muestra lo que se envió —que es literalmente lo que
 * devuelve la API (D69: la respuesta *es* la confirmación)— y dice la expectativa sin
 * adornarla.
 *
 * ⚠️ **Lo que no va** (MD-3): un estado "pendiente" consultable —no existe el endpoint y no
 * habría qué mostrar—, una barra de progreso, un tilde verde, y cualquier forma de "te
 * avisamos". Y **no es el `Confirmacion` de `DESIGN_SYSTEM.md`**, que es un diálogo para lo
 * irreversible: confundirlas pone un modal donde va una pantalla.
 */
function Acuse({
  sugerencia,
  alVolver,
  alSugerirOtra,
}: {
  sugerencia: SugerenciaRecibida;
  alVolver: () => void;
  alSugerirOtra: () => void;
}) {
  const datos: [string, string | null][] = [
    ["Título", sugerencia.titulo],
    ["Sala", sugerencia.sala],
    ["Año", sugerencia.anio === null ? null : String(sugerencia.anio)],
    ["Elenco o dirección", sugerencia.elenco],
    ["Algo más", sugerencia.comentario],
  ];

  return (
    <div className="mt-6">
      <Aviso variante="info">
        <strong className="font-medium">Lo recibimos.</strong> El catálogo lo revisa una persona,
        así que puede tardar. <strong className="font-medium">No te vamos a avisar cuando
        entre</strong>: cuando esté, vas a poder buscarla y registrarla.
      </Aviso>

      <dl className="mt-6 divide-y divide-borde border-y border-borde">
        {datos
          // Un campo que no se completó **no deja un hueco**: no se dibuja (D79).
          .filter(([, valor]) => valor)
          .map(([etiqueta, valor]) => (
            <div key={etiqueta} className="py-3">
              <dt className="text-sm text-tinta-suave">{etiqueta}</dt>
              <dd className="whitespace-pre-line">{valor}</dd>
            </div>
          ))}
      </dl>

      <div className="mt-6 flex flex-wrap gap-3">
        <Boton variante="secundario" onClick={alVolver}>
          Volver a lo que estaba haciendo
        </Boton>
        <Boton variante="fantasma" onClick={alSugerirOtra}>
          Sugerir otra
        </Boton>
      </div>
    </div>
  );
}

/** Un campo opcional vacío viaja como `null` y no como `""`: es "no lo sé", no "es vacío". */
function aTextoONulo(texto: string): string | null {
  const limpio = texto.trim();
  return limpio === "" ? null : limpio;
}

/**
 * Los mismos límites que el backend, con sus mismos mensajes (`SugerenciaRequest`). El `400`
 * es la red de contención, no la fuente del mensaje (D78) — aunque sugerir sea de los
 * endpoints que devuelven `errores` por campo.
 */
function validar(valor: Borrador): Record<string, string> {
  const errores: Record<string, string> = {};

  if (valor.titulo.trim() === "") errores.titulo = "El título de la obra es obligatorio";
  else if (valor.titulo.trim().length > 250)
    errores.titulo = "El título no puede superar los 250 caracteres";

  if (valor.sala.trim().length > 250) errores.sala = "La sala no puede superar los 250 caracteres";
  if (valor.elenco.trim().length > 1000)
    errores.elenco = "El elenco no puede superar los 1000 caracteres";
  if (valor.comentario.trim().length > 1000)
    errores.comentario = "El comentario no puede superar los 1000 caracteres";

  if (valor.anio.trim() !== "") {
    const anio = Number(valor.anio);
    // El rango es ancho a propósito: ataja el dedazo sin achicar el historial viejo que D24
    // quiere rescatar.
    if (!Number.isInteger(anio) || anio < 1800 || anio > 2100) {
      errores.anio = "El año tiene que estar entre 1800 y 2100";
    }
  }

  return errores;
}
