import { apiPublic, apiSession, TTL_CONTENIDO_VIVO } from "./server";
import type { Opiniones } from "./tipos";

/**
 * Diario, **lado servidor**. Hoy es la mitad de la ficha que no es del catálogo (D60): el
 * promedio de D20, las reseñas y —cuando el backend lo tenga (⏳ D76)— `vecesQueLaVi`. El
 * perfil entero (`GET /api/usuarios/{username}`) entra con la pantalla 8, paso 5.
 *
 * ⚠️ **Es uno de los dos `GET` abiertos que cambian de respuesta con cookie** (`API.md`):
 * con sesión agrega `leDiLike` en cada reseña y `vecesQueLaVi`. Por eso son dos funciones y
 * no una con parámetro — la misma razón que en la ficha.
 */

export const opinionesPublicas = (id: number) =>
  apiPublic<Opiniones>(`/api/producciones/${id}/opiniones`, TTL_CONTENIDO_VIVO);

export const opinionesConSesion = (id: number) =>
  apiSession<Opiniones>(`/api/producciones/${id}/opiniones`);
