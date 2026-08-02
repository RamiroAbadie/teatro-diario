import { EstadoVacio } from "@/components/ui/EstadoVacio";
import { FilaDeOpinion } from "@/components/ui/Fila";
import { IconoCorazon } from "@/components/ui/Iconos";
import { Puntaje } from "@/components/ui/Puntaje";
import { Usuario } from "@/components/ui/Usuario";
import type { ReseniaDeFicha } from "@/lib/api/tipos";
import { fechaDifusa } from "@/lib/formato";

/**
 * Las reseñas de la ficha (HU-14), en el orden en que vienen.
 *
 * ⏳ **Sin corazón y sin menú de reportar todavía**: los dos son islas cliente de HU-17 y
 * HU-18, que son el paso 5 de la Fase 4. Lo que sí se ve desde hoy es **el contador de
 * likes**, porque leer cuántos hay no pide sesión (D21) — y `FilaDeOpinion` ya tiene el
 * hueco de `acciones` esperando a esas dos islas.
 *
 * ⚠️ **Una reseña sin texto no llega hasta acá**: el endpoint de opiniones sólo devuelve
 * registros con reseña. Aun así la fila tolera `texto: null` sin reservar alto, porque el
 * mismo componente lo va a necesitar en el feed, donde un registro pelado es legítimo (D66).
 */
export function Resenias({ resenias }: { resenias: ReseniaDeFicha[] }) {
  if (resenias.length === 0) {
    return (
      <EstadoVacio titulo="Nadie escribió una reseña todavía">
        Cuando alguien registre esta obra y cuente qué le pareció, va a aparecer acá.
      </EstadoVacio>
    );
  }

  return (
    <ul>
      {resenias.map((resenia) => (
        <FilaDeOpinion
          key={resenia.registroId}
          // `autor: null` es "la cuenta ya no existe": la reseña sigue entera, porque el
          // texto no era de la cuenta, era de la reseña (D79).
          firma={<Usuario username={resenia.autor} />}
          fecha={fechaDifusa(resenia.fecha, resenia.granularidad)}
          puntaje={<Puntaje rating={resenia.rating} />}
          cuerpo={resenia.texto}
          pie={resenia.likes > 0 ? <ContadorDeLikes likes={resenia.likes} /> : undefined}
        />
      ))}
    </ul>
  );
}

function ContadorDeLikes({ likes }: { likes: number }) {
  return (
    <span className="inline-flex items-center gap-1">
      <IconoCorazon />
      <span className="tabular-nums">{likes}</span>
      {/* El corazón no viaja solo: el número dice cuántos y esto dice de qué (D79). */}
      <span className="sr-only">me gusta</span>
    </span>
  );
}
