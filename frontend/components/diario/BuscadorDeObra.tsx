"use client";

import { useCallback, useEffect, useId, useRef, useState } from "react";

import { ChipDeEstado } from "@/components/ui/Chip";
import { buscarProducciones } from "@/lib/api/catalogo.cliente";
import type { ProduccionResumen } from "@/lib/api/tipos";
import { salaConComplejo } from "@/lib/formato";

/**
 * **El primer paso del gesto y el que decide si el gesto entra en menos de un minuto** (P8):
 * el autocompletado de HU-09, que reusa la búsqueda de HU-07 (D65).
 *
 * ⚠️ **La última opción de la lista es SIEMPRE "no está en el catálogo → sugerirla", con
 * resultados o sin ellos** (D80). Esperar a que la lista vuelva vacía era lo natural y **no
 * alcanza**: con `pg_trgm` casi siempre vuelve *algo*, así que el caso real no es "no hay
 * nada" sino "ninguno de estos es el mío", y esperar el vacío deja al usuario intensivo con
 * historial —la primera impresión que R2 dice que no hay que arruinar— sin salida visible.
 */

/** Las dos medidas de HU-09: 250 ms desde la última tecla y mínimo 2 caracteres. */
const ESPERA = 250;
const MINIMO = 2;

type Props = {
  /** Cuando ya hay obra elegida, el campo desaparece: no se busca lo que ya se eligió. */
  obra: { id: number; titulo: string } | null;
  error?: string;
  onElegir: (obra: { id: number; titulo: string }) => void;
  onLimpiar: () => void;
  /** Ir a sugerir con lo tipeado, sin perderlo (HU-08). */
  onSugerir: (titulo: string) => void;
};

export function BuscadorDeObra({ obra, error, onElegir, onLimpiar, onSugerir }: Props) {
  const idBase = useId();
  const [texto, setTexto] = useState("");
  const [consulta, setConsulta] = useState("");
  /**
   * ⚠️ **La lista viaja con la consulta de la que salió, y eso es la corrección entera.**
   * Antes eran dos estados sueltos —`resultados` por un lado, un `buscando` por otro— y la
   * vigencia se calculaba comparando el texto contra `consulta`, que es **la pregunta**, no
   * **la respuesta que está en pantalla**. Con eso, el modelo **permitía** un render con
   * `texto === consulta`, `buscando` todavía en `false` e `items` de la consulta anterior: el
   * `setBuscando(true)` vivía adentro del efecto, y **React no promete** que un efecto que no
   * nace de una interacción corra antes del pintado o antes del evento siguiente. Ese render
   * **no se llegó a observar en Chromium** —ver D91: ni el muestreo cuadro por cuadro ni el
   * barrido de `Enter` lo expusieron—, pero que hoy no aparezca depende de un orden que nadie
   * garantiza, y una regla que se cumple por casualidad no es una regla.
   *
   * Atando los dos datos en un solo estado, el invariante deja de depender de ese orden y
   * pasa a ser estructural: **no existe un render donde los ítems sean de A y `lista.consulta`
   * diga B**, porque se escriben juntos. `buscando` desaparece: era una forma indirecta y con
   * retraso de preguntar lo mismo.
   */
  const [lista, setLista] = useState<{ consulta: string; items: ProduccionResumen[] }>({
    consulta: "",
    items: [],
  });
  const [activo, setActivo] = useState(0);
  const ultima = useRef(0);
  const campo = useRef<HTMLInputElement>(null);

  /**
   * ⚠️ **La lista y el resaltado se escriben juntos, por el mismo motivo que los ítems y su
   * consulta.** El índice vive aparte del arreglo que indexa, así que si se lo deja atrás
   * termina apuntando a una opción que ya no existe: con diez resultados en pantalla, una
   * flecha durante el pedido lo lleva a 10, y si la respuesta nueva trae uno solo, ese 10 no
   * es nada — `aria-activedescendant` anuncia un id inexistente y `Enter` no hace nada, que
   * es justo lo que "flechas y Enter navegan" (`SCREEN_SPECS.md`) promete que no pasa.
   * Una lista nueva empieza siempre por arriba.
   */
  const mostrar = useCallback((nueva: { consulta: string; items: ProduccionResumen[] }) => {
    setLista(nueva);
    setActivo(0);
  }, []);

  useEffect(() => {
    const t = setTimeout(() => setConsulta(texto.trim()), ESPERA);
    return () => clearTimeout(t);
  }, [texto]);

  useEffect(() => {
    if (consulta.length < MINIMO) {
      mostrar({ consulta, items: [] });
      return;
    }

    const pedido = ++ultima.current;
    buscarProducciones(consulta)
      .then((items) => {
        if (pedido !== ultima.current) return; // una respuesta vieja no pisa a la nueva
        mostrar({ consulta, items });
      })
      .catch(() => {
        if (pedido !== ultima.current) return;
        // ⚠️ Un fallo de la búsqueda **no rompe el gesto y no muestra una pantalla de
        // error**: la opción de sugerir sigue estando, que es la salida que importa.
        mostrar({ consulta, items: [] });
      });
  }, [consulta, mostrar]);

  const resultados = lista.items;

  if (obra) {
    return (
      <div>
        <p className="text-sm text-tinta-suave">Obra</p>
        <div className="mt-1 flex items-center gap-3 rounded-md border border-borde bg-superficie px-3 py-2">
          <span className="min-w-0 flex-1 truncate text-base">{obra.titulo}</span>
          <button
            type="button"
            onClick={() => {
              onLimpiar();
              setTexto("");
              setConsulta("");
            }}
            className="shrink-0 text-sm text-acento-tinta"
          >
            Cambiar
          </button>
        </div>
        {error && <p className="mt-1 text-sm text-peligro-tinta">{error}</p>}
      </div>
    );
  }

  // La opción de sugerir es una más de la lista y ocupa el último lugar: se navega con las
  // flechas hasta ella igual que a cualquier resultado.
  const cantidadDeOpciones = resultados.length + 1;
  const indiceDeSugerir = resultados.length;

  /**
   * ⚠️ **La red de contención, y va además del `mostrar` de arriba.** Aquél mantiene el
   * resaltado en su lugar cuando la lista cambia; esto garantiza que **ningún render pueda
   * nombrar una opción que no existe**, pase lo que pase con el orden de las actualizaciones.
   * Es la misma elección que en el resto de este componente: preferir un invariante que se
   * cumple por construcción antes que uno que depende de cuándo corre cada cosa.
   */
  const activoSeguro = Math.min(activo, indiceDeSugerir);

  /**
   * ⚠️ **"Lo que está en la lista no es lo que dice el campo".** Una sola comparación, contra
   * **la consulta de la que salieron los ítems**, y por eso ya no hay ventanas que enumerar:
   * la espera de los 250 ms, el pedido en vuelo y el render que quedaba entre los dos —el que
   * el modelo anterior **permitía** después de que el temporizador movió `consulta` y antes de
   * que el efecto arrancara el pedido, y que **no se llegó a observar** (D91)— son el mismo
   * caso mirado tres veces. Comparar contra `consulta` dejaba abierto el tercero.
   */
  const desactualizada = texto.trim() !== lista.consulta;

  function elegirActivo() {
    // ⚠️ **`Enter` con la lista desactualizada no elige: adelanta la búsqueda.** Es el único
    // camino en el que la persona **no apuntó a una fila** —está confiando en "el primero"—,
    // así que es el único donde una lista vieja puede registrar la obra equivocada: escribir
    // "mac" sobre "ham" y apretar `Enter` antes de los 250 ms elegía Hamlet. Un clic o un
    // toque sí eligen igual, porque ahí se apuntó a algo que está a la vista.
    if (desactualizada) return setConsulta(texto.trim());

    if (activoSeguro === indiceDeSugerir) return onSugerir(texto.trim());
    const elegida = resultados[activoSeguro];
    if (elegida) onElegir({ id: elegida.id, titulo: elegida.titulo });
  }

  return (
    <div>
      <label htmlFor={`${idBase}-obra`} className="block text-sm text-tinta-suave">
        ¿Qué viste?
      </label>

      <div className="relative mt-1">
        <input
          id={`${idBase}-obra`}
          ref={campo}
          type="text"
          role="combobox"
          aria-expanded={consulta.length >= MINIMO}
          aria-controls={`${idBase}-opciones`}
          aria-activedescendant={
            consulta.length >= MINIMO ? `${idBase}-opcion-${activoSeguro}` : undefined
          }
          aria-autocomplete="list"
          autoComplete="off"
          autoFocus
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          onKeyDown={(evento) => {
            if (consulta.length < MINIMO) return;
            if (evento.key === "ArrowDown") {
              evento.preventDefault();
              setActivo((activoSeguro + 1) % cantidadDeOpciones);
            } else if (evento.key === "ArrowUp") {
              evento.preventDefault();
              setActivo((activoSeguro - 1 + cantidadDeOpciones) % cantidadDeOpciones);
            } else if (evento.key === "Enter") {
              // El `Enter` de la lista elige una opción y **no envía el formulario**.
              evento.preventDefault();
              elegirActivo();
            } else if (evento.key === "Escape") {
              // ⚠️ Sin esto, `Esc` cierra la hoja entera: el `<dialog>` nativo lo atiende
              // por su cuenta. Acá `Esc` cierra la lista, que es lo que espera cualquiera
              // con un desplegable abierto.
              evento.preventDefault();
              evento.stopPropagation();
              setTexto("");
              setConsulta("");
            }
          }}
          placeholder="Buscá el título"
          className={
            "h-11 w-full rounded-md border bg-superficie px-3 pr-10 text-base " +
            (error ? "border-peligro" : "border-borde-control")
          }
        />

        {/* El estado *cargando* del autocompletado: **no bloquea lo tipeado**. Es un punto
            que late, sin animación de giro, para no chocar con `prefers-reduced-motion`.
            Aparece con la lista desactualizada y no sólo con el pedido en vuelo: desde
            afuera, esperar la tecla siguiente y esperar la respuesta son lo mismo. */}
        {desactualizada && (
          <span
            aria-hidden
            className="absolute top-1/2 right-3 size-2 -translate-y-1/2 rounded-full bg-acento"
          />
        )}
      </div>

      {error && <p className="mt-1 text-sm text-peligro-tinta">{error}</p>}

      {consulta.length >= MINIMO && (
        <>
          {/* Cuántos hay se anuncia, no se deduce del silencio. */}
          <p aria-live="polite" className="sr-only">
            {desactualizada
              ? "Buscando"
              : `${resultados.length} ${resultados.length === 1 ? "resultado" : "resultados"}`}
          </p>

          {/* ⚠️ **La lista anterior se queda mientras se escribe la nueva, y se atenúa desde
              la primera tecla** (ver `desactualizada`, que es una sola comparación y por eso
              no hay ventanas que enumerar). Vaciarla en cada consulta la haría parpadear a
              vacío en cada pausa de tipeo, que es lo contrario de los seis toques de P8; lo
              que no puede pasar es que una lista vieja **se lea como si fuera la nueva**, y
              eso lo dicen la opacidad, el punto del campo y el `aria-live`. Elegir con el dedo
              sigue siendo elegir lo que está a la vista; el `Enter` a ciegas es el que espera
              a la lista nueva. */}
          <ul
            id={`${idBase}-opciones`}
            role="listbox"
            aria-label="Producciones"
            className={
              "mt-2 max-h-64 overflow-y-auto rounded-md border border-borde transition-opacity duration-150 " +
              (desactualizada ? "opacity-60" : "")
            }
          >
            {resultados.map((produccion, i) => (
              <li
                key={produccion.id}
                id={`${idBase}-opcion-${i}`}
                role="option"
                aria-selected={activoSeguro === i}
              >
                <button
                  type="button"
                  onMouseEnter={() => setActivo(i)}
                  onClick={() => onElegir({ id: produccion.id, titulo: produccion.titulo })}
                  className={
                    "flex w-full items-center gap-2 border-b border-borde px-3 py-2 text-left " +
                    (activoSeguro === i ? "bg-acento-suave" : "")
                  }
                >
                  <span className="min-w-0 flex-1">
                    <span className="block truncate text-base">{produccion.titulo}</span>
                    {salaConComplejo(produccion.sala) && (
                      <span className="block truncate text-sm text-tinta-suave">
                        {salaConComplejo(produccion.sala)}
                      </span>
                    )}
                  </span>
                  <ChipDeEstado estado={produccion.estado} />
                </button>
              </li>
            ))}

            <li
              id={`${idBase}-opcion-${indiceDeSugerir}`}
              role="option"
              aria-selected={activoSeguro === indiceDeSugerir}
            >
              <button
                type="button"
                onMouseEnter={() => setActivo(indiceDeSugerir)}
                onClick={() => onSugerir(texto.trim())}
                className={
                  "w-full px-3 py-3 text-left text-sm text-acento-tinta " +
                  (activoSeguro === indiceDeSugerir ? "bg-acento-suave" : "")
                }
              >
                No está en el catálogo → sugerirla
              </button>
            </li>
          </ul>
        </>
      )}
    </div>
  );
}
