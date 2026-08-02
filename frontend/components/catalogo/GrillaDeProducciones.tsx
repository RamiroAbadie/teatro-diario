import { Afiche } from "@/components/ui/Afiche";
import { ChipDeEstado } from "@/components/ui/Chip";
import { TarjetaEnlazada } from "@/components/ui/Tarjeta";
import type { ProduccionResumen } from "@/lib/api/tipos";
import { salaConComplejo } from "@/lib/formato";
import { rutaObra } from "@/lib/rutas";

/**
 * La grilla de "en cartel" (D79, la tercera de las cosas que D71 mandó diseñar a mano). La
 * usan tres pantallas —en cartel (6), sala (5) y la home del visitante (1)— y vive en
 * `components/catalogo/` y no en `ui/` porque **sabe qué es una producción**: `ui/` no
 * conoce el dominio.
 *
 * **2 columnas en el celular, 3 en `sm`, 4 en `lg`.** Sin filtros, sin calendario, sin
 * barrio y sin orden configurable: **el orden lo da la API** y el primer filtro es la primera
 * pieza de una agenda, que X4/P6 dejaron afuera.
 *
 * ⚠️ **La prueba de aceptación de esta grilla es la mezcla**: con la mitad de las celdas sin
 * afiche tiene que leerse como una decisión y no como "faltan imágenes". Se mide al cargar
 * las ~50 fichas de D38, y si falla, la salida ya está escrita (D71): convertirla en una
 * lista de `Fila`. **Las placas no se agrupan ni se mandan al final.**
 */
export function GrillaDeProducciones({
  producciones,
  conChip = false,
  sinSala = false,
}: {
  producciones: ProduccionResumen[];
  /** "Próximamente" lleva el chip en cada celda; "En cartel" no lo repite veinte veces. */
  conChip?: boolean;
  /** La pantalla de sala la apaga: ahí la sala es el título, y cada celda la repetiría. */
  sinSala?: boolean;
}) {
  return (
    <ul className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
      {producciones.map((produccion) => (
        <li key={produccion.id}>
          <Celda produccion={produccion} conChip={conChip} sinSala={sinSala} />
        </li>
      ))}
    </ul>
  );
}

/**
 * ⚠️ **El título aparece una sola vez por celda, y dónde depende de si hay afiche.** Con
 * afiche va en el pie tipográfico, debajo de la imagen; **sin afiche ya está adentro de la
 * placa**, en la serif y en grande, y repetirlo abajo era decir dos veces lo mismo en 164 px
 * de ancho — se probó en pantalla y **se lee como un bug, no como una decisión**, que es
 * justo lo que el criterio de aceptación de esta grilla no perdona (D79). Lo que queda
 * debajo de la placa es la sala, que la placa no dice.
 */
function Celda({
  produccion,
  conChip,
  sinSala,
}: {
  produccion: ProduccionResumen;
  conChip: boolean;
  sinSala: boolean;
}) {
  const sala = sinSala ? null : salaConComplejo(produccion.sala);
  const conAfiche = Boolean(produccion.aficheUrl);

  return (
    // `flex-col` + una caja de imagen que crece: la grilla estira todas las celdas de una
    // fila a la altura de la más alta (que es lo que pide "altura pareja"), y sin esto la
    // celda más corta rellena ese sobrante con un rectángulo vacío debajo del pie — el hueco
    // otra vez. Estirando la placa, la celda sigue siendo una placa.
    <TarjetaEnlazada href={rutaObra(produccion.id, produccion.titulo)} className="flex h-full flex-col">
      <Afiche aficheUrl={produccion.aficheUrl} titulo={produccion.titulo} className="flex-1" />
      {/* Y si no queda nada que poner —sin afiche, sin sala y sin chip— **el pie no se
          dibuja**: un bloque de padding vacío debajo de la placa es el hueco que la regla 2
          de D79 prohíbe. */}
      {(conAfiche || sala || conChip) && (
        <div className="p-3">
          {conAfiche && <p className="font-titulo text-lg leading-tight">{produccion.titulo}</p>}
          {/* La línea que viene nula desaparece entera: no hay "Sala: —" (D79). */}
          {sala && <p className={`text-sm text-tinta-suave ${conAfiche ? "mt-1" : ""}`}>{sala}</p>}
          {conChip && (
            <p className="mt-2">
              <ChipDeEstado estado={produccion.estado} />
            </p>
          )}
        </div>
      )}
    </TarjetaEnlazada>
  );
}
