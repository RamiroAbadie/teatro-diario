import type { EstadoProduccion, Granularidad, RolParticipacion } from "./api/tipos";

/**
 * Presentación pura: **la traducción vive una sola vez** (D78/D79). Nada de esto es lógica
 * de negocio —D34 la pone explícitamente del lado del frontend— pero sí es lo que se
 * duplica en la tercera pantalla si no tiene un solo lugar.
 */

/**
 * La fecha difusa de MD-1/D59, con las dos mitades del contrato: `fecha` normalizada al
 * comienzo del período y `granularidad`, que dice **hasta dónde leerla**.
 *
 * ⚠️ `MES` se muestra "marzo de 2023" y **nunca "1 de marzo de 2023"**: el día que trae la
 * respuesta es relleno del backend, no un dato que el usuario dio.
 */
export function fechaDifusa(fecha: string | null, granularidad: Granularidad): string | null {
  if (granularidad === "SIN_FECHA" || !fecha) return null;

  // `YYYY-MM-DD` a mano y no `new Date(fecha)`: el constructor la interpreta como UTC y en
  // Buenos Aires (UTC-3) devuelve el día anterior. Una fecha sin hora no es un instante.
  const [anio, mes, dia] = fecha.split("-").map(Number);
  if (!anio) return null;

  if (granularidad === "ANIO") return String(anio);
  if (granularidad === "MES") return `${MESES[mes - 1]} de ${anio}`;
  return `${dia} de ${MESES[mes - 1]} de ${anio}`;
}

const MESES = [
  "enero", "febrero", "marzo", "abril", "mayo", "junio",
  "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
];

/** "en la plataforma desde {mes}" del perfil, y cualquier otro instante ISO-8601. */
export function mesYAnio(instanteIso: string): string {
  const fecha = new Date(instanteIso);
  if (Number.isNaN(fecha.getTime())) return "";
  return `${MESES[fecha.getMonth()]} de ${fecha.getFullYear()}`;
}

/**
 * Los enums llegan crudos (`API.md`) y se traducen acá. **En minúscula**, salvo los chips
 * de estado y de rol, que los pone en mayúscula el propio `Chip` con `uppercase` (D79).
 */
const ESTADOS: Record<EstadoProduccion, string> = {
  EN_CARTEL: "en cartel",
  PROXIMAMENTE: "próximamente",
  CERRADA: "cerrada",
};

const ROLES: Record<RolParticipacion, string> = {
  ACTUACION: "actuación",
  DIRECCION: "dirección",
  DRAMATURGIA: "dramaturgia",
};

export const estadoEnCastellano = (estado: EstadoProduccion): string => ESTADOS[estado] ?? estado;
export const rolEnCastellano = (rol: RolParticipacion): string => ROLES[rol] ?? rol;

/** El encabezado de un grupo de la página de artista: el rol con la primera en mayúscula. */
export function conMayuscula(texto: string): string {
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

/**
 * El promedio de D20 con **un decimal y coma decimal** ("8,4"). Un solo lugar, porque es el
 * número que HU-14 mide y el que más fácil se escribe con punto en la segunda pantalla.
 */
export function promedioLocal(promedio: number): string {
  return promedio.toFixed(1).replace(".", ",");
}

/** "17 personas puntuaron" / "1 persona puntuó": el singular se olvida y se ve. */
export function personasQuePuntuaron(cantidad: number): string {
  return cantidad === 1 ? "1 persona puntuó" : `${cantidad} personas puntuaron`;
}

/** La línea de sala de una ficha o de una celda: "Sala Casacuberta · Teatro San Martín". */
export function salaConComplejo(sala: { nombre: string; complejo: string | null } | null): string | null {
  if (!sala) return null;
  return sala.complejo ? `${sala.nombre} · ${sala.complejo}` : sala.nombre;
}
