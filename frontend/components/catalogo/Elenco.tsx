import Link from "next/link";

import { ChipDeRol } from "@/components/ui/Chip";
import type { Participacion } from "@/lib/api/tipos";
import { rutaArtista } from "@/lib/rutas";

/**
 * Elenco y equipo de la ficha, **agrupado por persona**.
 *
 * ⚠️ La API las manda **una por rol** (D17): quien dirigió y además actuó viene dos veces.
 * Agrupar es presentación y no lógica de negocio —`FRONTEND_ARCHITECTURE.md` lo pone
 * explícitamente de este lado—, y sin agrupar la misma persona aparece dos veces en la misma
 * lista, que es lo que se lee como un bug.
 */
export function Elenco({ participaciones }: { participaciones: Participacion[] }) {
  const personas = agruparPorPersona(participaciones);
  if (personas.length === 0) return null;

  return (
    <section>
      <h2 className="mb-2 font-titulo text-xl">Elenco y equipo</h2>
      <ul>
        {personas.map(({ persona, roles }) => (
          <li key={persona.id} className="flex flex-wrap items-center gap-2 border-t border-borde py-3">
            <Link href={rutaArtista(persona.id, persona.nombre)} className="text-acento-tinta">
              {persona.nombre}
            </Link>
            {roles.map((rol) => (
              <ChipDeRol key={rol} rol={rol} />
            ))}
          </li>
        ))}
      </ul>
    </section>
  );
}

/** Se conserva el orden en que vino la primera participación de cada persona. */
function agruparPorPersona(participaciones: Participacion[]) {
  const porPersona = new Map<number, { persona: Participacion["persona"]; roles: Participacion["rol"][] }>();

  for (const participacion of participaciones) {
    const yaEsta = porPersona.get(participacion.persona.id);
    if (yaEsta) {
      if (!yaEsta.roles.includes(participacion.rol)) yaEsta.roles.push(participacion.rol);
    } else {
      porPersona.set(participacion.persona.id, {
        persona: participacion.persona,
        roles: [participacion.rol],
      });
    }
  }

  return [...porPersona.values()];
}
