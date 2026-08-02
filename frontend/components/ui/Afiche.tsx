/**
 * `ui/3 · Afiche` (D79). La imagen cuando está y **la placa tipográfica cuando no**, que es
 * el mismo componente y no un caso de error: hoy **ninguna ficha tiene afiche** —el endpoint
 * de subida espera a P16— así que la placa es lo que se ve siempre, y tiene que verse como
 * una decisión.
 *
 * ⚠️ **La placa existe donde hay que rellenar una celda de tamaño fijo** —la grilla, la
 * miniatura, el `og:image`—, **no en la ficha**: esa pantalla cambia de forma y por eso no
 * usa este componente cuando no hay imagen (ver la pantalla 3). Es lo que separa "una imagen
 * que no cargó" de "esta ficha se ve así".
 *
 * ⏳ **Lo que falta y por qué no está**: el estado *error de carga* (`onError` → cae a la
 * placa) necesita un `onError`, o sea una isla cliente en cada celda de la grilla. Entra con
 * los afiches (D77/P16), que es cuando puede haber una imagen que falle: escribirlo hoy
 * sería poner JavaScript en todas las pantallas públicas para un caso que no puede ocurrir.
 */

type Variante = "grilla" | "miniatura";

type Props = {
  aficheUrl?: string | null;
  titulo: string;
  /** La línea de abajo de la placa: sólo lo que exista (sala, u obra original, o estado). */
  pie?: string | null;
  variante?: Variante;
  /** Para que la caja pueda crecer cuando la celda de la grilla estira (ver la grilla). */
  className?: string;
};

/** Las cajas de la tabla de dimensiones de D79. La grilla y la miniatura son **2:3 fija**. */
const CAJAS: Record<Variante, string> = {
  grilla: "aspect-[2/3] w-full rounded-lg",
  miniatura: "h-18 w-12 shrink-0 rounded-md",
};

export function Afiche({ aficheUrl, titulo, pie, variante = "grilla", className = "" }: Props) {
  const caja = `${CAJAS[variante]} ${className}`.trim();

  if (aficheUrl) {
    return (
      // `<img>` y no `next/image`: los afiches son estáticos que sirve Caddy sobre el mismo
      // origen (D77), y pasarlos por el optimizador de Next los haría volver a la aplicación
      // — que es justo lo que esa decisión evita. `cover` anclado arriba, que es donde los
      // afiches ponen el título.
      <img
        src={aficheUrl}
        alt={`Afiche de ${titulo}`}
        loading="lazy"
        className={`${caja} border border-borde object-cover object-top`}
      />
    );
  }

  return <Placa titulo={titulo} pie={pie} variante={variante} caja={caja} />;
}

/**
 * La placa tipográfica: se construye con lo único que la ficha trae siempre —el título—.
 *
 * **No dice nunca "sin afiche" ni muestra un ícono de imagen.** Fondo `acento-suave`, una
 * regla de 3 px en `acento` arriba y el título en la serif, alineado abajo a la izquierda.
 * (`acento-suave` es el token que en tema oscuro ya vale `#2A2011`: por eso no hace falta
 * ningún `dark:`, que es la regla de D79.)
 */
function Placa({
  titulo,
  pie,
  variante,
  caja,
}: {
  titulo: string;
  pie?: string | null;
  variante: Variante;
  caja: string;
}) {
  const miniatura = variante === "miniatura";

  return (
    // ⚠️ **El `aria-hidden` es sólo de la miniatura, y la diferencia no es cosmética.**
    // `SCREEN_SPECS.md` dice que la placa es decorativa "porque el título ya está al lado
    // como texto" — y en la miniatura eso es verdad: la fila lo tiene enseguida. **En la
    // grilla dejó de serlo con D86**: ahí el título de la celda sin afiche vive *adentro* de
    // la placa, así que ocultarla deja el link de la celda **sin nombre accesible** —en la
    // pantalla de sala, donde tampoco hay sala ni chip, literalmente sin nada que anunciar—.
    // Con afiche no hay placa y el nombre lo pone el `alt`. La regla real es la razón, no la
    // frase: se oculta lo que se repite al lado, nunca lo único que nombra al control.
    <div
      aria-hidden={miniatura || undefined}
      className={`${caja} flex flex-col justify-end overflow-hidden border border-borde border-t-[3px] border-t-acento bg-acento-suave`}
    >
      {/* En la miniatura no entra nada más que dos o tres letras: es una marca de lugar,
          no una placa. Se queda con la inicial del título en la serif. */}
      {miniatura ? (
        <span className="p-1 font-titulo text-lg leading-none text-tinta-suave">
          {titulo.charAt(0).toUpperCase()}
        </span>
      ) : (
        <div className="p-3">
          <p className={`font-titulo leading-tight text-tinta ${escalonDelTitulo(titulo)} line-clamp-3`}>
            {titulo}
          </p>
          {pie && <p className="mt-1 line-clamp-1 text-sm text-tinta-suave">{pie}</p>}
        </div>
      )}
    </div>
  );
}

/**
 * Los **tres escalones de cuerpo según el largo** de D79. Un título de cuatro palabras y uno
 * de veinte no pueden entrar en la misma celda con el mismo tamaño sin que uno de los dos se
 * vea mal: el corto se ve chico y perdido, el largo se corta a la mitad.
 */
function escalonDelTitulo(titulo: string): string {
  if (titulo.length <= 24) return "text-2xl";
  if (titulo.length <= 60) return "text-xl";
  return "text-base";
}
