import type { ComponentProps, ReactNode } from "react";

/**
 * El campo de un formulario: etiqueta, control, ayuda y **el error debajo del input**, que es
 * lo que pide la tabla de errores (`400` con `errores` → "texto de error debajo de cada
 * input, en `peligro-tinta`, `text-sm`").
 *
 * ⚠️ **No es uno de los diez de `DESIGN_SYSTEM.md` y no los convierte en once**: los diez son
 * el vocabulario visual del producto —lo que se ve en una pantalla terminada— y esto es la
 * plomería de un formulario. Vive en `ui/` por la regla de pertenencia y no por la lista: no
 * sabe nada de ningún dominio y lo usan cuatro pantallas (alta, login, sugerir y el gesto),
 * así que nacer en la carpeta de un dominio sería elegir uno al azar. Es el mismo lugar y el
 * mismo criterio que `usarDialogo.ts` (D82).
 *
 * Lo que resuelve una sola vez, que es justo lo que se escribe mal en la cuarta pantalla:
 * `htmlFor`/`id` atados, `aria-invalid` cuando hay error, y `aria-describedby` apuntando a la
 * ayuda **y** al error a la vez — un `aria-describedby` que pisa al otro deja la mitad del
 * mensaje sin leer.
 */

type Props = {
  id: string;
  etiqueta: string;
  /** El mensaje del `400` con `errores`, o el de la validación previa del cliente. */
  error?: string;
  /** La línea tenue de abajo: la advertencia del username, el contador de la contraseña. */
  ayuda?: ReactNode;
} & Omit<ComponentProps<"input">, "id" | "className">;

/** Los inputs van en `text-base` (16 px) **siempre**: por debajo, iOS hace zoom al enfocar. */
const CONTROL =
  "h-11 w-full rounded-md border bg-superficie px-3 text-base text-tinta " +
  "placeholder:text-tinta-tenue";

export function Campo({ id, etiqueta, error, ayuda, ...resto }: Props) {
  const idAyuda = ayuda ? `${id}-ayuda` : null;
  const idError = error ? `${id}-error` : null;
  const descrito = [idAyuda, idError].filter(Boolean).join(" ");

  return (
    <div>
      <label htmlFor={id} className="block text-sm text-tinta-suave">
        {etiqueta}
      </label>

      <input
        id={id}
        aria-invalid={error ? true : undefined}
        aria-describedby={descrito || undefined}
        className={`${CONTROL} mt-1 ${error ? "border-peligro" : "border-borde-control"}`}
        {...resto}
      />

      {ayuda && (
        <p id={idAyuda!} className="mt-1 text-sm text-tinta-tenue">
          {ayuda}
        </p>
      )}

      {error && (
        <p id={idError!} className="mt-1 text-sm text-peligro-tinta">
          {error}
        </p>
      )}
    </div>
  );
}
