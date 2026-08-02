import { apiPublic, apiSession, TTL_CATALOGO, TTL_CONTENIDO_VIVO } from "./server";
import type { Artista, EnCartel, Ficha, SalaPublica } from "./tipos";

/**
 * `lib/api/` espeja los módulos del backend (D78). Este es Catálogo, **lado servidor**: las
 * cuatro lecturas públicas que sostienen el SSR de ADR-003.
 *
 * No hay `catalogo.cliente.ts` todavía: la búsqueda y el autocompletado son del navegador y
 * entran con la pantalla 7 y el gesto (paso 4 de la Fase 4).
 *
 * ⚠️ **Lo que decide la caché es la llamada, no el endpoint** (D78). Tres de estas cuatro
 * son iguales para todo el mundo y van siempre por `apiPublic`; **la ficha es la excepción**
 * y por eso tiene dos funciones con nombre distinto en vez de un parámetro: la elección
 * queda escrita en cada llamada y no se decide "de una vez y para siempre" en un `if`.
 */

/**
 * La ficha **del visitante anónimo**. Cacheable: es la misma para todos.
 *
 * ⚠️ El TTL no es cero porque el contenido muta —el admin corrige un título, cierra una
 * obra—, pero tampoco es infinito: 60 s es el compromiso de la tabla de D78 para lo que
 * tiene actividad encima.
 */
export const fichaPublica = (id: number) =>
  apiPublic<Ficha>(`/api/producciones/${id}`, TTL_CONTENIDO_VIVO);

/**
 * La misma ficha **pedida con sesión**. Hoy `GET /api/producciones/{id}` devuelve lo mismo
 * con cookie y sin ella —el que se personaliza es `opiniones`—, pero la página entera va
 * por un solo cliente: **una página con sesión se renderiza entera sin caché** (D78). Que
 * la mitad cacheada de una pantalla no cacheada sea "inofensiva" es el razonamiento con el
 * que se termina sirviendo la página de una persona a otra.
 */
export const fichaConSesion = (id: number) => apiSession<Ficha>(`/api/producciones/${id}`);

/** En cartel: sólo cambia cuando el admin toca el catálogo, así que va con el TTL largo. */
export const enCartel = () => apiPublic<EnCartel>("/api/en-cartel", TTL_CATALOGO);

export const artista = (id: number) => apiPublic<Artista>(`/api/personas/${id}`, TTL_CATALOGO);

export const sala = (id: number) => apiPublic<SalaPublica>(`/api/salas/${id}`, TTL_CATALOGO);
