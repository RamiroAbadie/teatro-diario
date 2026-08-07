"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import { Aviso } from "@/components/ui/Aviso";
import { Boton } from "@/components/ui/Boton";
import { CampoLargo } from "@/components/ui/Campo";
import { Confirmacion } from "@/components/ui/Confirmacion";
import { IconoCruz } from "@/components/ui/Iconos";
import { usarBorrador } from "@/components/ui/usarBorrador";
import { usarDialogo } from "@/components/ui/usarDialogo";
import { borrarRegistro, crearRegistro, editarRegistro } from "@/lib/api/diario.cliente";
import { comoFormulario, FalloDeApi } from "@/lib/api/errores";
import type { Granularidad, RegistroDeDiario } from "@/lib/api/tipos";

import { BuscadorDeObra } from "./BuscadorDeObra";
import { EscalaDePuntaje } from "./EscalaDePuntaje";
import { EVENTO_ABRIR_GESTO, type DetalleDelGesto } from "./gesto";
import { hoyLocal, SelectorDeFecha } from "./SelectorDeFecha";

/**
 * **Pantalla 9 · El gesto de registro** (HU-09/10/11). **Esta pantalla ES el producto**
 * (Flujo 3), y el criterio no es que esté completa: es que **el camino feliz entre en menos
 * de un minuto en un celular a las 23:30** (P8) — abrir, buscar, tocar la obra, aceptar la
 * fecha de hoy, tocar un número, publicar: **seis toques y ningún tipeo más que el título**.
 *
 * **Es una hoja y no una ruta** (D80): se abre desde tres lugares —el botón persistente, el
 * CTA de la ficha y "Registrar de nuevo"— y en dos de ellos el contexto de atrás importa.
 * Vive montada una sola vez en `app/(sitio)/layout.tsx`, con sesión, y los tres disparadores
 * le hablan por un evento del DOM (`gesto.ts`).
 *
 * Lo que no es obvio y está acá:
 *
 * - **El `404` del `POST` no es un error**: la obra dejó de existir entre que se eligió y se
 *   publicó, y eso abre el camino a sugerir con el título ya cargado (HU-08).
 * - **El `5xx` no cierra la hoja**, con todo lo tipeado adentro.
 * - **El borrador vive en `sessionStorage`**, así que si la sesión venció en el medio, el
 *   `401` manda al login y **al volver la hoja se reabre con lo que se estaba escribiendo**.
 * - **Registrar la misma obra otra vez no avisa de duplicado ni pide confirmación** (D19): el
 *   re-visto es una función, no un accidente.
 */

const CLAVE = "borrador:gesto";

/** La marca que deja el `401` para que, al volver del login, la hoja se reabra sola. */
const INTERRUMPIDA = "gesto:interrumpida";

type Formulario = {
  /** `null` es crear; con id, el mismo formulario es editar (HU-11). */
  registroId: number | null;
  obra: { id: number; titulo: string } | null;
  granularidad: Granularidad;
  fecha: string | null;
  rating: number | null;
  resenia: string;
};

const vacio = (): Formulario => ({
  registroId: null,
  obra: null,
  // **Hoy por defecto, un toque.**
  granularidad: "DIA",
  fecha: hoyLocal(),
  rating: null,
  resenia: "",
});

/**
 * "No hay nada escrito acá": ni obra, ni puntaje, ni reseña, ni una precisión de fecha
 * distinta de la que viene por defecto — y no es una edición. Es lo que separa **un borrador**
 * de **el estado inicial**, y lo único que se pierde al recalcular es una fecha elegida a mano
 * en un formulario por lo demás vacío, que además se ve en el campo apenas se abre.
 */
const estaEnBlanco = (f: Formulario): boolean =>
  f.registroId === null &&
  f.obra === null &&
  f.rating === null &&
  f.resenia.trim() === "" &&
  f.granularidad === "DIA";

const desdeRegistro = (registro: RegistroDeDiario): Formulario => ({
  registroId: registro.id,
  obra: { id: registro.produccion.id, titulo: registro.produccion.titulo },
  granularidad: registro.granularidad,
  fecha: registro.fecha,
  rating: registro.rating,
  resenia: registro.resenia ?? "",
});

export function HojaDelGesto() {
  const router = useRouter();
  const hoja = usarDialogo();
  // La confirmación de borrar **no empuja su propia entrada de historial**: ver `usarDialogo`.
  const confirmacion = usarDialogo({ conHistorial: false });

  const { valor, setValor, olvidar } = usarBorrador<Formulario>(CLAVE, vacio());
  const [enviando, setEnviando] = useState(false);
  const [borrando, setBorrando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const [errorAlBorrar, setErrorAlBorrar] = useState<string | null>(null);
  const [errores, setErrores] = useState<Record<string, string>>({});
  /** El camino a sugerir que abre el `404`: el título que se intentó registrar. */
  const [aSugerir, setASugerir] = useState<string | null>(null);

  const { abrir } = hoja;

  // Los tres disparadores. **Abrir sin obra no pisa el borrador**: quien vuelve al botón
  // persistente después de cerrar la hoja encuentra lo que estaba escribiendo.
  useEffect(() => {
    const alAbrir = (evento: Event) => {
      const detalle = (evento as CustomEvent<DetalleDelGesto>).detail ?? {};
      setAviso(null);
      setErrores({});
      setASugerir(null);

      if (detalle.registro) {
        setValor(desdeRegistro(detalle.registro));
      } else if (detalle.produccionId && detalle.titulo) {
        const obra = { id: detalle.produccionId, titulo: detalle.titulo };
        // Si el borrador ya era de esta obra —volver del login desde su ficha—, se conserva
        // entero; si era de otra, esto es un gesto nuevo y empieza limpio.
        setValor((actual) =>
          actual.obra?.id === obra.id && actual.registroId === null
            ? actual
            : { ...vacio(), obra },
        );
      } else {
        // ⚠️ Abrir sin nada —el botón persistente— **es siempre un registro nuevo**. Lo
        // tipeado de un registro nuevo sí se conserva, pero un borrador de EDICIÓN
        // abandonada no puede volver por esta puerta: "Guardar" estaría pisando un registro
        // viejo cuando la persona creía estar cargando uno.
        setValor((actual) => (actual.registroId === null ? actual : vacio()));
      }

      // ⚠️ **"Hoy por defecto" se calcula al abrir y no al montar.** Un formulario en el que
      // no hay nada escrito no es "lo tipeado": es el estado inicial, y el estado inicial de
      // la fecha es hoy. Sin esto, una pestaña abierta desde ayer —o un borrador vacío
      // guardado ayer— ofrece la fecha de ayer con la misma cara con que ofrece la de hoy, y
      // la promesa de "un toque" pasa a registrar la función equivocada en silencio.
      setValor((actual) => (estaEnBlanco(actual) ? vacio() : actual));

      abrir();
    };

    document.addEventListener(EVENTO_ABRIR_GESTO, alAbrir);
    return () => document.removeEventListener(EVENTO_ABRIR_GESTO, alAbrir);
  }, [abrir, setValor]);

  // Volver del login: el `401` dejó la marca antes de que el navegador se fuera.
  useEffect(() => {
    if (sessionStorage.getItem(INTERRUMPIDA) === null) return;
    sessionStorage.removeItem(INTERRUMPIDA);
    abrir();
  }, [abrir]);

  // Si la hoja se cierra con la confirmación abierta, la confirmación se va con ella: no
  // tiene entrada de historial propia y quedaría flotando sobre una pantalla que ya no es
  // la suya.
  const cerrarConfirmacion = confirmacion.cerrar;
  useEffect(() => {
    if (!hoja.abierto) cerrarConfirmacion();
  }, [hoja.abierto, cerrarConfirmacion]);

  const editando = valor.registroId !== null;

  function cerrarLimpiando() {
    olvidar();
    setValor(vacio());
    setEnviando(false);
    hoja.cerrar();
    // La pantalla de atrás la dibujó el servidor: sin esto, la ficha seguiría diciendo que
    // no la viste nunca y el diario no tendría el registro nuevo.
    router.refresh();
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (enviando) return;

    if (!valor.obra) {
      setErrores({ produccionId: "Elegí qué producción viste" });
      return;
    }

    setEnviando(true);
    setErrores({});
    setAviso(null);
    setASugerir(null);

    const cuerpo = {
      produccionId: valor.obra.id,
      fecha: valor.granularidad === "SIN_FECHA" ? null : valor.fecha,
      granularidad: valor.granularidad,
      rating: valor.rating,
      resenia: valor.resenia.trim() === "" ? null : valor.resenia.trim(),
    };

    try {
      if (valor.registroId === null) await crearRegistro(cuerpo);
      else await editarRegistro(valor.registroId, cuerpo);
      cerrarLimpiando();
    } catch (error) {
      setEnviando(false);

      // ⚠️ **El `404` NO es un error**: la obra dejó de existir entre que se eligió y se
      // publicó. Es el camino a sugerir, con el título ya cargado (HU-08).
      if (error instanceof FalloDeApi && error.status === 404) {
        setASugerir(valor.obra.titulo);
        return;
      }

      // El `401` se lo lleva el manejador global al login; lo único que hay que hacer acá es
      // dejar dicho que la hoja estaba abierta, para reabrirla al volver.
      if (error instanceof FalloDeApi && error.status === 401) {
        sessionStorage.setItem(INTERRUMPIDA, "1");
        return;
      }

      // Todo lo demás —`400` por campo, el `403` de dueño que no debería pasar, el `5xx`—
      // se dibuja adentro y **la hoja no se cierra**.
      const { general, campos } = comoFormulario(error);
      setErrores(campos);
      setAviso(general);
    }
  }

  async function borrar() {
    if (valor.registroId === null || borrando) return;
    setBorrando(true);
    setErrorAlBorrar(null);
    try {
      await borrarRegistro(valor.registroId);
      confirmacion.cerrar();
      cerrarLimpiando();
    } catch (error) {
      // La sesión venció mientras la confirmación estaba abierta: el manejador global manda
      // al login, y esto es lo que hace que al volver la hoja se reabra con el registro que
      // se estaba por borrar. Sin la marca, la persona vuelve a una pantalla cualquiera y
      // tiene que rehacer todo el camino hasta el registro.
      if (error instanceof FalloDeApi && error.status === 401) {
        sessionStorage.setItem(INTERRUMPIDA, "1");
      }
      // El error se queda **adentro del diálogo y el diálogo no se cierra**: cerrarlo dejaría
      // al usuario sin saber si el registro se borró o no.
      setErrorAlBorrar(comoFormulario(error).general ?? "No pudimos borrar el registro.");
    } finally {
      setBorrando(false);
    }
  }

  const restantes = 5000 - valor.resenia.length;

  return (
    <>
      <dialog
        ref={hoja.ref}
        onClick={hoja.alClicEnElFondo}
        aria-label={editando ? "Editar registro" : "Registrar que la vi"}
        // Hoja desde abajo en el celular —la única animación de posición del sistema (D79)—
        // y modal centrado en ≥md.
        className="hoja-del-gesto m-0 mt-auto max-h-[90dvh] w-full max-w-none overflow-y-auto rounded-t-2xl border border-borde bg-papel text-tinta md:m-auto md:w-[34rem] md:rounded-lg"
      >
        <form
          method="post"
          onSubmit={enviar}
          className="space-y-6 p-4 pb-[calc(1rem+env(safe-area-inset-bottom))] md:p-6"
        >
          <div className="flex items-center justify-between gap-4">
            <h2 className="font-titulo text-xl">
              {editando ? "Editar registro" : "Registrar que la vi"}
            </h2>
            <button
              type="button"
              onClick={hoja.cerrar}
              aria-label="Cerrar"
              className="-mr-2 inline-flex size-11 items-center justify-center rounded-md text-tinta-suave"
            >
              <IconoCruz />
            </button>
          </div>

          {aviso && <Aviso variante="error">{aviso}</Aviso>}

          {/* El `404`: no es un error, es la puerta a HU-08 y lo tipeado viaja con ella. */}
          {aSugerir && (
            <div>
              <Aviso variante="info">
                Esa obra ya no está en el catálogo. Podés sugerirla y la revisamos.
              </Aviso>
              <Boton
                className="mt-3"
                onClick={() => hoja.navegar(`/sugerir?titulo=${encodeURIComponent(aSugerir)}`)}
              >
                Sugerir &quot;{aSugerir}&quot;
              </Boton>
            </div>
          )}

          {/* 1 · Obra. En edición se puede cambiar: **editar reemplaza el gesto entero**,
              porque equivocarse de obra al elegirla es el error más probable. */}
          <BuscadorDeObra
            obra={valor.obra}
            error={errores.produccionId}
            onElegir={(obra) => setValor((actual) => ({ ...actual, obra }))}
            onLimpiar={() => setValor((actual) => ({ ...actual, obra: null }))}
            onSugerir={(titulo) =>
              hoja.navegar(titulo ? `/sugerir?titulo=${encodeURIComponent(titulo)}` : "/sugerir")
            }
          />

          {/* 2 · Fecha */}
          <SelectorDeFecha
            granularidad={valor.granularidad}
            fecha={valor.fecha}
            error={errores.fecha ?? errores.granularidad}
            onCambiar={(granularidad, fecha) =>
              setValor((actual) => ({ ...actual, granularidad, fecha }))
            }
          />

          {/* 3 · Puntaje */}
          <div>
            <EscalaDePuntaje
              valor={valor.rating}
              onCambiar={(rating) => setValor((actual) => ({ ...actual, rating }))}
            />
            {errores.rating && <p className="mt-1 text-sm text-peligro-tinta">{errores.rating}</p>}
          </div>

          {/* 4 · Reseña */}
          <CampoLargo
            id="resenia"
            etiqueta="¿Qué te pareció? (opcional)"
            rows={4}
            maxLength={5000}
            value={valor.resenia}
            onChange={(e) => setValor((actual) => ({ ...actual, resenia: e.target.value }))}
            error={errores.resenia}
            // El contador aparece **sólo cerca del límite**: un "0/5000" desde el primer
            // caracter convierte una reseña en un trámite.
            ayuda={valor.resenia.length >= 4500 ? `Te quedan ${restantes} caracteres` : undefined}
          />

          <Boton
            type="submit"
            cargando={enviando}
            etiquetaCargando={editando ? "Guardando…" : "Publicando…"}
            className="w-full"
          >
            {editando ? "Guardar" : "Publicar"}
          </Boton>

          {editando && (
            <div className="border-t border-borde pt-4">
              <Boton
                variante="fantasma"
                className="w-full text-peligro-tinta"
                onClick={() => {
                  setErrorAlBorrar(null);
                  confirmacion.abrir();
                }}
              >
                Borrar registro
              </Boton>
            </div>
          )}
        </form>
      </dialog>

      {/* Fuera del `<form>` y fuera de la hoja: es otro diálogo, no un bloque de éste. */}
      <Confirmacion
        dialogo={confirmacion}
        titulo="¿Borrar este registro?"
        etiqueta="Borrar registro"
        etiquetaCargando="Borrando…"
        cargando={borrando}
        error={errorAlBorrar}
        onConfirmar={borrar}
      >
        Se borra tu registro de &quot;{valor.obra?.titulo}&quot;, con su fecha, su puntaje y su
        reseña. No se puede deshacer.
      </Confirmacion>
    </>
  );
}
