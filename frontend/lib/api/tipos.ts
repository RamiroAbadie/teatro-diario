/**
 * Los tipos de `docs/architecture/API.md`, escritos a mano (D78).
 *
 * Crecen pantalla por pantalla: acá está sólo lo que consume el armazón. Si una pantalla
 * necesita un tipo nuevo, se agrega el del contrato — **no se inventa ni un campo**: si
 * algo hace falta y no está en API.md, es un hueco del backend y se anota (D34).
 */

export type Rol = "USUARIO" | "ADMIN";

/** Los enums de `API.md`. Llegan crudos: traducirlos es del frontend (`lib/formato.ts`). */
export type EstadoProduccion = "EN_CARTEL" | "CERRADA" | "PROXIMAMENTE";
export type RolParticipacion = "ACTUACION" | "DIRECCION" | "DRAMATURGIA";
export type Granularidad = "DIA" | "MES" | "ANIO" | "SIN_FECHA";

/** `CuentaResponse`: lo que devuelven registro, login y `GET /api/auth/yo`. */
export type Cuenta = {
  id: number;
  username: string;
  email: string;
  rol: Rol;
};

/* ============================================================
   Catálogo — las cuatro lecturas públicas de `API.md`
   ============================================================ */

export type Sala = { id: number; nombre: string; complejo: string | null };

export type Persona = { id: number; nombre: string };

/**
 * **Resumen de producción**: la forma corta que devuelven en-cartel, artista, sala,
 * búsqueda y el listado del admin.
 *
 * `aficheUrl` **llega siempre**: el backend lo manda desde que existe la subida de afiches
 * (D77/D88). Es `null` cuando la ficha no tiene afiche, que es el caso normal y no un error
 * (D71), y termina en `.jpg` — no en `.webp`, porque no hay escritor de WebP en Java puro.
 */
export type ProduccionResumen = {
  id: number;
  titulo: string;
  estado: EstadoProduccion;
  aficheUrl: string | null;
  sala: Sala | null;
};

export type Participacion = { id: number; persona: Persona; rol: RolParticipacion };

/** `GET /api/producciones/{id}`. **No trae promedio ni reseñas**: eso es Diario (D60). */
export type Ficha = {
  id: number;
  titulo: string;
  sinopsis: string | null;
  obraOriginal: string | null;
  autorOriginal: string | null;
  estado: EstadoProduccion;
  aficheUrl: string | null;
  sala: Sala | null;
  participaciones: Participacion[];
};

/** `GET /api/personas/{id}`. Sin foto y sin bio, a propósito (D14). */
export type Artista = {
  id: number;
  nombre: string;
  participaciones: { id: number; rol: RolParticipacion; produccion: ProduccionResumen }[];
};

/** `GET /api/salas/{id}`. Sin dirección, sin mapa y sin horarios: no hay agenda (X4). */
export type SalaPublica = Sala & { enCartel: ProduccionResumen[] };

/** `GET /api/en-cartel`. Las dos listas en una respuesta, en el orden que las manda (D8). */
export type EnCartel = { enCartel: ProduccionResumen[]; proximamente: ProduccionResumen[] };

/* ============================================================
   Diario — la otra mitad de la ficha (D60)
   ============================================================ */

/** Una reseña de la ficha. `autor` es `null` si esa cuenta ya no existe. */
export type ReseniaDeFicha = {
  registroId: number;
  autor: string | null;
  texto: string | null;
  rating: number | null;
  fecha: string | null;
  granularidad: Granularidad;
  likes: number;
  /** Convención de tres estados: `null` es "no hay botón que dibujar" (D68). */
  leDiLike: boolean | null;
  creadoEn: string;
};

/**
 * `GET /api/producciones/{id}/opiniones`. **Abierto pero personalizado**: con cookie agrega
 * `leDiLike` en cada reseña y `vecesQueLaVi` — por eso lo pide `apiPublic` o `apiSession`
 * según la llamada, y nunca se cachea una respuesta pedida con cookie (D78).
 *
 * ⚠️ `promedio` es el **último rating de cada usuario** (D20), no un `AVG`, y `null` cuando
 * nadie puntuó. `vecesQueLaVi` (D76) sigue la convención de tres estados: `null` sin sesión,
 * `0` con sesión y sin haberla visto —lo que habilita el CTA de registrar— y `N` con el
 * re-visto de D19.
 */
export type Opiniones = {
  promedio: number | null;
  cantidadRatings: number;
  vecesQueLaVi: number | null;
  resenias: ReseniaDeFicha[];
};

/**
 * El error de API.md, aplanado en un solo tipo (D78). Desde D87 el backend ya manda una sola
 * forma; este tipo sigue siendo el que ninguna pantalla mira de cerca: `mensaje` sale del
 * `detail` cuando vino y de la tabla por código cuando no, y `errores` está sólo cuando el
 * problema es de campos.
 */
export type ErrorDeApi = {
  status: number;
  mensaje: string;
  errores?: Record<string, string>;
};
