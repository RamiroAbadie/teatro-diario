/**
 * Los tipos de `docs/architecture/API.md`, escritos a mano (D78).
 *
 * Crecen pantalla por pantalla: acá está sólo lo que consume el armazón. Si una pantalla
 * necesita un tipo nuevo, se agrega el del contrato — **no se inventa ni un campo**: si
 * algo hace falta y no está en API.md, es un hueco del backend y se anota (D34).
 */

export type Rol = "USUARIO" | "ADMIN";

/** `CuentaResponse`: lo que devuelven registro, login y `GET /api/auth/yo`. */
export type Cuenta = {
  id: number;
  username: string;
  email: string;
  rol: Rol;
};

/**
 * Las tres familias de error de API.md, aplanadas en un solo tipo (D78).
 * Ninguna pantalla ve la diferencia entre un `ProblemDetail` con `detail`, uno con el
 * mapa `errores` y lo que arme el framework.
 */
export type ErrorDeApi = {
  status: number;
  mensaje: string;
  errores?: Record<string, string>;
};
