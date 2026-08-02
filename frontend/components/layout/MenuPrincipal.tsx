"use client";

import { useEffect } from "react";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";

import { IconoCruz, IconoHamburguesa } from "@/components/ui/Iconos";
import { usarDialogo } from "@/components/ui/usarDialogo";
import type { Cuenta } from "@/lib/api/tipos";
import { REPO, rutaUsuario } from "@/lib/rutas";

import { BotonSalir } from "./BotonSalir";

/**
 * La segunda pieza del armazón (D81): el mapa completo del producto, en un panel a
 * pantalla completa que abre el botón de la cabecera. **En ≥`md` no se dibuja**: la
 * cabecera ancha ya tiene el buscador desplegado, los destinos, el menú de cuenta y el
 * botón de registrar, así que no hay nada que plegar.
 *
 * ⚠️ **Que "Mi diario", "En cartel" y "Buscar" estén también en la barra es a propósito**:
 * la barra es para el pulgar dentro de una sesión y el menú es el mapa completo. El que
 * recién llega abre el menú y ve todo; el intensivo no lo abre nunca. El costo asumido son
 * los tres ítems que **sólo** viven acá —Sugerir, Salir, Código—, ninguno de los cuales es
 * parte del camino feliz.
 *
 * **Sin sesión el menú tiene dos ítems y ninguna categoría.** "Crear cuenta" **no va**: ya
 * es el botón persistente, y repetirlo sería un segundo CTA de adquisición.
 *
 * ⚠️ **La sección del panel no lleva contadores**, ni "7 sugerencias" ni "2 reportes": el
 * contrato de `API.md` no los devuelve, y no se resuelven en el cliente pidiendo las dos
 * colas para contar filas (D34).
 *
 * **No entra desplazándose**: la única animación de posición del sistema sigue siendo la
 * hoja del gesto (D79).
 */
export function MenuPrincipal({ cuenta }: { cuenta: Cuenta | null }) {
  const { ref, abierto, abrir, cerrar, navegar, alClicEnElFondo } = usarDialogo();
  const ruta = usePathname();

  // Si la ruta cambió por fuera del menú, el panel no puede quedar flotando sobre una
  // pantalla que ya no es la suya. El layout es uno solo para las 13.
  useEffect(() => {
    if (ref.current?.open) ref.current.close();
  }, [ruta, ref]);

  return (
    <>
      {/* Disparador: hamburguesa + **la palabra escrita**, no sólo el ícono (D81). */}
      <button
        type="button"
        onClick={abrir}
        aria-expanded={abierto}
        className="-ml-2 inline-flex h-11 items-center gap-2 rounded-md px-2 text-sm font-medium text-tinta md:hidden"
      >
        <IconoHamburguesa />
        Menú
      </button>

      <dialog
        ref={ref}
        onClick={alClicEnElFondo}
        aria-label="Menú principal"
        className="m-0 h-dvh max-h-dvh w-screen max-w-none bg-papel p-0 text-tinta md:hidden"
      >
        <div className="flex h-full flex-col overflow-y-auto px-4 pt-2 pb-[calc(1rem+env(safe-area-inset-bottom))]">
          {/* En el mismo lugar donde estaba "Menú": el disparador queda tapado por el
              panel, así que "el mismo disparador pasa a decir Cerrar" se cumple acá. */}
          <div>
            <button
              type="button"
              onClick={cerrar}
              className="-ml-2 inline-flex h-11 items-center gap-2 rounded-md px-2 text-sm font-medium text-tinta"
            >
              <IconoCruz />
              Cerrar
            </button>
          </div>

          {cuenta ? (
            <>
              <Categoria titulo="Vos">
                <Item href={rutaUsuario(cuenta.username)} navegar={navegar}>
                  Mi diario
                </Item>
                <Item href="/sugerir" navegar={navegar}>
                  Sugerir una obra
                </Item>
                <li>
                  {/* El mismo `navegar` que los ítems: cerrar primero —lo que consume la
                      entrada del historial— y recién después ir a la home. */}
                  <BotonSalir navegar={navegar} className={CLASES_DE_ITEM} />
                </li>
              </Categoria>

              <Categoria titulo="Descubrir">
                <Item href="/en-cartel" navegar={navegar}>
                  En cartel
                </Item>
                <Item href="/buscar" navegar={navegar}>
                  Buscar
                </Item>
              </Categoria>

              <Categoria titulo="El proyecto">
                <ItemExterno href={REPO}>Código</ItemExterno>
              </Categoria>

              {cuenta.rol === "ADMIN" && (
                <Categoria titulo="Panel">
                  <Item href="/admin/sugerencias" navegar={navegar}>
                    Sugerencias
                  </Item>
                  <Item href="/admin/reportes" navegar={navegar}>
                    Reportes
                  </Item>
                  <Item href="/admin/producciones" navegar={navegar}>
                    Producciones
                  </Item>
                  <Item href="/admin/salas" navegar={navegar}>
                    Salas
                  </Item>
                  <Item href="/admin/personas" navegar={navegar}>
                    Personas
                  </Item>
                </Categoria>
              )}
            </>
          ) : (
            <ul className="mt-6 divide-y divide-borde border-y border-borde">
              <Item href="/login" navegar={navegar}>
                Entrar
              </Item>
              <ItemExterno href={REPO}>Código</ItemExterno>
            </ul>
          )}
        </div>
      </dialog>
    </>
  );
}

const CLASES_DE_ITEM =
  "flex h-12 w-full items-center text-left text-base text-tinta transition-colors duration-150";

function Categoria({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <section className="mt-6">
      <h2 className="text-xs font-medium tracking-wide text-tinta-tenue uppercase">{titulo}</h2>
      <ul className="mt-1 divide-y divide-borde border-y border-borde">{children}</ul>
    </section>
  );
}

/**
 * Los ítems del menú **no son `<Link>`**: cierran primero y navegan después, para que el
 * panel no deje su entrada del historial colgada (ver `usarDialogo`).
 */
function Item({
  href,
  navegar,
  children,
}: {
  href: string;
  navegar: (href: string) => void;
  children: ReactNode;
}) {
  return (
    <li>
      <button type="button" onClick={() => navegar(href)} className={CLASES_DE_ITEM}>
        {children}
      </button>
    </li>
  );
}

/** El único link que sale del sitio: el código, que la AGPL obliga a ofrecer (D46). */
function ItemExterno({ href, children }: { href: string; children: ReactNode }) {
  return (
    <li>
      <a href={href} className={CLASES_DE_ITEM} target="_blank" rel="noreferrer">
        {children}
      </a>
    </li>
  );
}
