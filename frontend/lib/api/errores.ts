import type { ErrorDeApi } from "./tipos";

/**
 * `client.ts` y `server.ts` aplanan acá las tres familias de error de API.md en un solo
 * tipo (D78). **Nunca se muestra `undefined`** ni se asume que el cuerpo tiene forma: hay
 * endpoints que responden con el cuerpo vacío y otros que devuelven lo que arma el
 * framework, sin `detail` ni `errores`.
 */

/** Los errores dicen qué hacer (D79). El `detail` del backend gana cuando vino. */
const MENSAJES: Record<number, string> = {
  400: "Revisá los datos: hay algo que el servidor no aceptó.",
  401: "Necesitás entrar con tu cuenta para hacer esto.",
  403: "No tenés permiso para hacer esto.",
  404: "No encontramos lo que buscabas.",
  409: "Eso ya cambió. Refrescá y probá de nuevo.",
  413: "El archivo pesa más de lo permitido.",
  422: "Revisá los datos: hay algo que el servidor no aceptó.",
};

const GENERICO = "Algo falló de nuestro lado. Probá de nuevo.";

export function mensajePorEstado(status: number): string {
  return MENSAJES[status] ?? GENERICO;
}

/**
 * Lee el cuerpo de una respuesta fallida sin asumir nada de su forma.
 * Un cuerpo vacío, un HTML de error o un JSON sin `detail` terminan igual: con el
 * mensaje de la tabla por código de estado.
 */
export async function aErrorDeApi(respuesta: Response): Promise<ErrorDeApi> {
  const base: ErrorDeApi = {
    status: respuesta.status,
    mensaje: mensajePorEstado(respuesta.status),
  };

  let cuerpo: unknown;
  try {
    const texto = await respuesta.text();
    cuerpo = texto ? JSON.parse(texto) : undefined;
  } catch {
    return base;
  }

  if (!cuerpo || typeof cuerpo !== "object") return base;

  const problema = cuerpo as { detail?: unknown; errores?: unknown };
  const detail = typeof problema.detail === "string" && problema.detail.trim() !== ""
    ? problema.detail
    : undefined;
  const errores = esMapaDeErrores(problema.errores) ? problema.errores : undefined;

  return { ...base, mensaje: detail ?? base.mensaje, ...(errores ? { errores } : {}) };
}

function esMapaDeErrores(valor: unknown): valor is Record<string, string> {
  return (
    !!valor &&
    typeof valor === "object" &&
    !Array.isArray(valor) &&
    Object.values(valor).every((v) => typeof v === "string")
  );
}

/**
 * Lo que tiran los dos clientes cuando la respuesta no es 2xx. Las pantallas la atrapan y
 * deciden por CÓDIGO Y CONTEXTO: el mismo `404` es una página de error en una ruta
 * pública y el camino feliz a sugerir en el gesto de registro (la tabla de D78).
 */
export class FalloDeApi extends Error implements ErrorDeApi {
  readonly status: number;
  readonly errores?: Record<string, string>;

  constructor(error: ErrorDeApi) {
    super(error.mensaje);
    this.name = "FalloDeApi";
    this.status = error.status;
    this.errores = error.errores;
  }

  get mensaje(): string {
    return this.message;
  }
}
