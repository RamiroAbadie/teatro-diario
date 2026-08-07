"use client";

import { useEffect, useRef, useState } from "react";
import type { ReactNode } from "react";

import { Aviso } from "@/components/ui/Aviso";
import { Boton } from "@/components/ui/Boton";
import { ChipDeEstado } from "@/components/ui/Chip";
import { EstadoVacio } from "@/components/ui/EstadoVacio";
import { FilaResultado, FilasEnEsqueleto } from "@/components/ui/Fila";
import { IconoBuscar } from "@/components/ui/Iconos";
import { buscarPersonas, buscarProducciones } from "@/lib/api/catalogo.cliente";
import { buscarUsuarios } from "@/lib/api/identidad.cliente";
import type { Persona, ProduccionResumen, UsuarioResultado } from "@/lib/api/tipos";
import { mesYAnio, salaConComplejo } from "@/lib/formato";
import { rutaArtista, rutaObra, rutaUsuario } from "@/lib/rutas";

/**
 * **Pantalla 7 · Búsqueda** (HU-07 → HU-08). **Tres llamadas en paralelo y tres secciones
 * independientes** (D65: son tres endpoints y no uno, porque cada módulo busca sobre lo
 * suyo). Que sean independientes no es un detalle de implementación: **cada sección aparece
 * cuando llega la suya, y la que falla muestra su error mientras las otras dos se dibujan.**
 *
 * ⚠️ **`[]` no es un error, nunca.** Sin resultados la API responde `200` con lista vacía
 * (D65), y en producciones eso es **el camino a sugerir** (HU-08), con lo tipeado adentro del
 * botón para que sea evidente que no hay que volver a escribirlo. En personas y usuarios es
 * una línea tenue y nada más: **no hay adónde derivar**, y ofrecer "sugerir una persona"
 * sería inventar un endpoint.
 */

/** La misma espera que el autocompletado del gesto: 250 ms desde la última tecla. */
const ESPERA = 250;

type Seccion<T> = { estado: "cargando" | "listo" | "error"; items: T[] };

/** Una función y no una constante: cada sección se queda con su propia lista vacía. */
const vacia = <T,>(): Seccion<T> => ({ estado: "listo", items: [] });

export function Buscador({ consultaInicial }: { consultaInicial: string }) {
  const [texto, setTexto] = useState(consultaInicial);
  const [consulta, setConsulta] = useState(consultaInicial.trim());

  const [producciones, setProducciones] = useState<Seccion<ProduccionResumen>>(vacia);
  const [personas, setPersonas] = useState<Seccion<Persona>>(vacia);
  const [usuarios, setUsuarios] = useState<Seccion<UsuarioResultado>>(vacia);

  // Cada tecla dispara tres pedidos y las respuestas pueden llegar desordenadas: sin esto,
  // una consulta vieja que tardó más pisa los resultados de la nueva. No hace falta abortar
  // nada — alcanza con ignorar lo que ya no es la última pregunta.
  const ultima = useRef(0);

  useEffect(() => {
    const t = setTimeout(() => setConsulta(texto.trim()), ESPERA);
    return () => clearTimeout(t);
  }, [texto]);

  useEffect(() => {
    // La URL se sincroniza con lo tipeado para que **un resultado sea compartible**.
    // `replaceState` nativo y no `router.replace`: cambiar la URL no tiene que volver a
    // pedirle la pantalla al servidor ni ensuciar el historial con una entrada por tecla.
    const url = consulta ? `/buscar?q=${encodeURIComponent(consulta)}` : "/buscar";
    window.history.replaceState(null, "", url);

    // `q` vacío **no se llama**: la API devolvería `[]` igual, y pedirlo tres veces para
    // saberlo es tráfico por nada.
    if (consulta === "") {
      setProducciones(vacia());
      setPersonas(vacia());
      setUsuarios(vacia());
      return;
    }

    const pedido = ++ultima.current;
    setProducciones({ estado: "cargando", items: [] });
    setPersonas({ estado: "cargando", items: [] });
    setUsuarios({ estado: "cargando", items: [] });

    const resolver = <T,>(
      promesa: Promise<T[]>,
      set: (s: Seccion<T>) => void,
    ) => {
      promesa
        .then((items) => {
          if (pedido === ultima.current) set({ estado: "listo", items });
        })
        .catch(() => {
          if (pedido === ultima.current) set({ estado: "error", items: [] });
        });
    };

    resolver(buscarProducciones(consulta), setProducciones);
    resolver(buscarPersonas(consulta), setPersonas);
    resolver(buscarUsuarios(consulta), setUsuarios);
  }, [consulta]);

  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="font-titulo text-3xl">Buscar</h1>

      {/* Un `form` de verdad: enviar no recarga nada —ya buscamos mientras se escribe— pero
          el teclado del celular muestra "Buscar" y `Enter` cierra el teclado, que es lo que
          hace la diferencia parado en la vereda. */}
      <form
        role="search"
        // ⚠️ Acá el envío nativo **es el que se quiere**: `action` y `method="get"` hacen que
        // sin JavaScript el `Enter` navegue a `/buscar?q=…` y la pantalla se sirva desde el
        // servidor con la búsqueda hecha. Con JavaScript no llega a pasar: ya buscamos
        // mientras se escribe, así que el envío se cancela.
        action="/buscar"
        method="get"
        onSubmit={(e) => e.preventDefault()}
        className="mt-4 flex items-center gap-2"
      >
        <label htmlFor="q" className="sr-only">
          Buscar obras, artistas y usuarios
        </label>
        <input
          id="q"
          name="q"
          type="search"
          // Foco al entrar: se llega acá para escribir, y en el celular ahorra un toque.
          autoFocus
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          placeholder="Una obra, una persona, alguien"
          className="h-11 min-w-0 flex-1 rounded-md border border-borde-control bg-superficie px-3 text-base"
        />
        <span
          aria-hidden
          className="inline-flex size-11 shrink-0 items-center justify-center rounded-md border border-borde text-tinta-suave"
        >
          <IconoBuscar />
        </span>
      </form>

      {consulta === "" ? (
        <p className="mt-8 text-tinta-tenue">
          Escribí para buscar obras, artistas y gente que registra lo que ve.
        </p>
      ) : (
        <>
          {/* `aria-live` en los resultados: quien no ve la pantalla se entera de que la
              lista cambió sola mientras escribía. */}
          <div aria-live="polite">
            <Seccion titulo="Producciones" seccion={producciones}>
              {producciones.items.length === 0 ? (
                <SinProducciones consulta={consulta} />
              ) : (
                <ul>
                  {producciones.items.map((p) => (
                    <FilaResultado
                      key={p.id}
                      href={rutaObra(p.id, p.titulo)}
                      titulo={p.titulo}
                      meta={salaConComplejo(p.sala)}
                      chip={<ChipDeEstado estado={p.estado} />}
                      aficheUrl={p.aficheUrl}
                    />
                  ))}
                </ul>
              )}
            </Seccion>

            <Seccion titulo="Personas" seccion={personas}>
              {personas.items.length === 0 ? (
                <SinNada>No encontramos ninguna persona con ese nombre.</SinNada>
              ) : (
                <ul>
                  {personas.items.map((persona) => (
                    <FilaResultado
                      key={persona.id}
                      href={rutaArtista(persona.id, persona.nombre)}
                      titulo={persona.nombre}
                    />
                  ))}
                </ul>
              )}
            </Seccion>

            <Seccion titulo="Usuarios" seccion={usuarios}>
              {usuarios.items.length === 0 ? (
                <SinNada>No hay nadie con ese nombre de usuario.</SinNada>
              ) : (
                <ul>
                  {usuarios.items.map((usuario) => (
                    <FilaResultado
                      key={usuario.id}
                      href={rutaUsuario(usuario.username)}
                      titulo={usuario.username}
                      meta={`en la plataforma desde ${mesYAnio(usuario.creadoEn)}`}
                    />
                  ))}
                </ul>
              )}
            </Seccion>
          </div>
        </>
      )}
    </div>
  );
}

/** El encabezado, el esqueleto y el error de una sección, que son iguales en las tres. */
function Seccion<T>({
  titulo,
  seccion,
  children,
}: {
  titulo: string;
  seccion: Seccion<T>;
  children: ReactNode;
}) {
  return (
    <section className="mt-8">
      <h2 className="font-titulo text-xl">{titulo}</h2>
      <div className="mt-2">
        {seccion.estado === "cargando" && <FilasEnEsqueleto />}
        {seccion.estado === "error" && (
          <Aviso variante="error">No pudimos buscar en {titulo.toLowerCase()}.</Aviso>
        )}
        {seccion.estado === "listo" && children}
      </div>
    </section>
  );
}

/**
 * **El primero de los tres momentos del camino del catálogo cerrado** (D79). Lo que se buscó
 * se muestra textual y entre comillas, y **el texto viaja adentro del botón**: es lo que hace
 * evidente que no hay que volver a escribirlo.
 *
 * El botón se ve igual **sin sesión**: al tocarlo, `/sugerir` se come su `401`, manda al
 * login y vuelve con lo tipeado.
 */
function SinProducciones({ consulta }: { consulta: string }) {
  return (
    <EstadoVacio
      titulo={`No encontramos nada para "${consulta}"`}
      accion={
        <Boton href={`/sugerir?titulo=${encodeURIComponent(consulta)}`}>
          Sugerir &quot;{consulta}&quot;
        </Boton>
      }
    >
      El catálogo lo carga una persona a mano, así que puede faltar algo.
    </EstadoVacio>
  );
}

/** Personas y usuarios sin resultados: una línea tenue. **No hay adónde derivar.** */
function SinNada({ children }: { children: ReactNode }) {
  return <p className="text-tinta-tenue">{children}</p>;
}
