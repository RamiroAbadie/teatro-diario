# Domain Model

> Estado: v0.1 — cierre de Etapa 2 (modelo conceptual del dominio)
> Este es un modelo **conceptual**, no un esquema de base de datos. Define qué entidades
> existen, qué significa cada una y cómo se relacionan. El esquema físico se deriva de esto
> más adelante, no al revés.

## Entidad central

### Producción
La unidad contra la que el usuario registra, puntúa y reseña (D12).
Un texto teatral puesto en escena por una dirección y un elenco determinados.

- Dos producciones distintas del mismo texto son entidades distintas (dos *Hamlet* = dos producciones).
- El cambio de sala **no** crea una producción nueva.
- Reestrenos con cambios parciales de elenco: decide el admin caso por caso, con sesgo a
  "misma producción" si se mantienen texto y dirección (P6 del Decision Log).

Atributos conceptuales: título, descripción/sinopsis, `obra_original` (texto plano),
`autor/dramaturgo original` (texto plano — ver D13), estado (`en cartel` / `cerrada` /
`próximamente`), sala actual (relación opcional), imagen/afiche.

Casos que el modelo absorbe sin tratamiento especial: unipersonales (elenco de 1),
eventos únicos y ciclos cortos (estado `cerrada` hace el trabajo), obras vistas en
festivales (son producciones normales; "festival" no es entidad del MVP).

## Entidades de soporte del catálogo

### Persona
Artista reutilizable entre producciones (D14). Habilita la página de artista:
"todo lo que hizo X".

- Atributos mínimos deliberados: **nombre y nada más** en el MVP. Sin foto, sin bio.
  Cada atributo extra encarece la curaduría de cada ficha.
- Duplicados ocasionales se aceptan como deuda de datos y se corrigen a mano
  (tolerancia operativa tipo "opción C").
- Homónimos reales: desambigua el admin caso por caso.

### Participación
Relación Persona ↔ Producción con un **rol**.

- Roles: lista cerrada inicial — `actor/actriz`, `dirección`, `dramaturgia`. Ampliable
  a futuro (iluminación, escenografía, música quedan afuera del MVP).
- Una persona puede tener varios roles en la misma producción.

### Sala
Espacio teatral de CABA (D15). Catálogo chico (~100-200), estable, cargado por el admin.

- Habilita "qué está en cartel en [sala]".
- Complejos con múltiples salas (San Martín, Paseo La Plaza): se resuelve con campo
  `complejo` opcional o auto-relación; detalle a definir en el esquema, no bloquea.

## Entidades del usuario

### Usuario
Cuenta con perfil público (D3).

### Registro (log)
El acto central del producto: "vi esta producción".
Vincula Usuario ↔ Producción con fecha elegida libremente por el usuario (D8: no existe
la entidad Función). Contenido exacto del registro: **a definir en Etapa 3**
(rating y reseña ¿parte del registro o entidades aparte? ¿re-visto?).

### Rating
Entero 1–10 sobre una producción (D9). Los promedios se muestran con decimales.

### Reseña
Texto del usuario sobre una producción (D10). Recibe likes (D11). Sin comentarios en MVP.

### Lista
Colección personal, pública, de producciones con orden manual. **Candidata a MVP,
primera en la lista de recortes** (P5 — se resuelve en Etapa 4).

### Follow
Usuario sigue a Usuario. Habilita ver la actividad de los seguidos (D3).

## Qué NO es entidad en el MVP

| Concepto | Por qué no |
|---|---|
| Obra/texto teatral | Capturada como texto plano en la producción (D13); normalizable a futuro |
| Función | No hay agenda de funciones (D8, X4) |
| Compañía | Línea borrosa en el circuito independiente; no aporta al loop central (D16) |
| Festival | Una obra vista en festival es una producción normal; tag futuro si hace falta |
| Temporada | Absorbida por el estado de la producción y el criterio del admin en reestrenos |

## Diagrama conceptual (texto)

```
Persona ──< Participación (rol) >── Producción ──── Sala
                                        │
                                        ├──< Registro ──── Usuario
                                        ├──< Rating  ──── Usuario
                                        ├──< Reseña  ──── Usuario ──< Like
                                        └──< ítem de Lista ── Usuario
                                                   Usuario ──< Follow >── Usuario
```
