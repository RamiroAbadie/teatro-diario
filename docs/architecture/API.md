# API

> Estado: v1.4 — el paso 3 de la Fase 4 (lo que le quedaba al backend) cierra **los tres huecos**
> que este documento tenía anotados, y **este documento se queda sin ⏳**: `vecesQueLaVi` existe,
> el manejo global de errores existe —la sección de errores se reescribió: donde había tres
> familias que el frontend tenía que tolerar, ahora hay una sola (D87)— y **los afiches también,
> con un cambio en el contrato que hay que leer: la URL termina en `.jpg` y no en `.webp`**,
> porque no existe un escritor de WebP en Java puro (D88, que enmienda D77 en ese punto y en
> ningún otro). Lo anterior no cambia.
>
> La v1.2 se escribió al abrir la Fase 4 y se corrigió en dos revisiones (la segunda: el feed
> separado de los `GET` abiertos personalizados, la nulabilidad real de la cola de reportes, las
> dos formas del `409` del alta y el contrato inmutable de los afiches).
> **Es el contrato que consume el frontend**: las formas de respuesta vivían solo en los
> records de Java y eso obligaba a leer backend para escribir una pantalla.
>
> Regla de mantenimiento: si cambia un DTO, cambia este documento **en el mismo PR**. Un
> contrato desactualizado es peor que ninguno, porque se le cree.

✅ **Ya no queda nada marcado como pendiente.** Todo lo que este documento describe está
implementado y verificado contra el código; el ⏳ que llevaban los afiches y `vecesQueLaVi` se
cayó en el paso 3 de la Fase 4. Lo que sigue afuera del backend es el reparto de `/afiches` en
Caddy y el volumen, que son de la Fase 5 y no cambian este contrato.

## Convenciones transversales

**Base:** todo cuelga de `/api`. En producción el navegador y el backend comparten origen
detrás de Caddy (STACK.md), así que no hay CORS ni tokens que guardar en el cliente.

**Quién puede qué** (`aplicacion/internal/seguridad/SecurityConfig.java`, D21/D56):

| Regla | Alcance |
|---|---|
| Abierto sin cuenta | **todos los `GET` de `/api`** salvo las dos excepciones de abajo |
| Pide sesión | `GET /api/feed`, `GET /api/auth/yo`, y **todas** las escrituras salvo registro, login y logout |
| Pide rol `ADMIN` | todo `/api/admin/**`, cualquier método |

⚠️ **`POST /api/auth/logout` pide token CSRF pero no pide sesión**, aunque ninguna regla lo diga:
lo atiende el `LogoutFilter`, que en la cadena de Spring Security corre **antes** del filtro de
autorización y corta ahí. Un `POST` anónimo con token válido responde `204` igual. No es un
agujero —desloguear a quien no está logueado no hace nada— y para el frontend es una comodidad
que conviene aprovechar: **cerrar sesión con la sesión ya vencida devuelve `204`, no `401`**, así
que el botón de salir no necesita un camino de error. Lo que sí importa: **no clasificarlo como
"pide sesión"**, porque el contrato diría algo que la cadena de filtros no hace.

Que casi toda la lectura sea pública es lo que permite el SSR sin sesión de las páginas
compartibles (ADR-003): ficha, artista, sala, en cartel y perfil se renderizan en servidor sin
cookie ninguna.

⚠️ **Pero "público" no es lo mismo que "igual para todos".** Hay **dos** `GET` abiertos cuya
**respuesta cambia si la llamada lleva cookie**, y confundirlos con contenido estático es lo que
hace que se le sirva a alguien la página de otro:

| Endpoint abierto y personalizado | Qué agrega la sesión |
|---|---|
| `GET /api/producciones/{id}/opiniones` | `leDiLike` en cada reseña y `vecesQueLaVi` |
| `GET /api/usuarios/{username}` | `loSigo` |

**`GET /api/feed` no es de esta familia y no hay que contarlo entre ellos**: no es un endpoint
abierto que se personaliza, es un endpoint que **exige sesión** y responde `401` sin ella
(`SecurityConfig`, D66). Se pide siempre por el cliente con sesión y siempre `no-store`; no
existe la variante anónima que se podría cachear. La otra lectura que exige sesión es
`GET /api/auth/yo`, y vale lo mismo.

**Una respuesta pedida con cookie es una respuesta con sesión, aunque el endpoint sea público**,
y no se cachea nunca. La regla operativa está en `FRONTEND_ARCHITECTURE.md` (D78).

**Sesión:** cookie `JSESSIONID`, HTTP-only, `SameSite=Lax` (D44/D56). El frontend nunca la lee
ni la manda a mano: viaja sola. `secure` entra con HTTPS en la Fase 5.

**CSRF (D57):** toda escritura necesita el header `X-XSRF-TOKEN` con el valor de la cookie
`XSRF-TOKEN`, que es legible por JavaScript a propósito. La cookie llega con cualquier respuesta
previa. **El token se rota en las tres puertas de la sesión: `registro`, `login` y `logout`** —
las tres pasan por la misma estrategia de autenticación—, así que después de cualquiera de ellas
hay que releer la cookie: la anterior ya no sirve. Sin el header, la respuesta es `403`.

### Errores: una sola forma (D87)

✅ **Todo error de esta API es un `ProblemDetail` con `detail` en castellano.** Las tres familias
que este documento describía —y que el frontend tenía que tolerar— se unificaron en el paso 3 de
la Fase 4: entró un `@ControllerAdvice` global y los dos manejadores de la cadena de filtros.

```jsonc
// la forma, siempre
{ "type": "about:blank", "title": "Not Found", "status": 404,
  "detail": "No existe una producción con id 7" }

// y cuando el problema es de campos, el mapa además — es lo que permite pintar el error al lado
// de cada input, y ahora lo devuelven TODOS los formularios, el panel admin incluido
{ "type": "about:blank", "title": "Bad Request", "status": 400,
  "detail": "Revisá los datos ingresados",
  "errores": { "username": "Entre 3 y 20 caracteres: letras, números o guión bajo" } }
```

Lo que garantiza:

| Antes | Ahora |
|---|---|
| Los formularios del admin devolvían `400` sin decir qué campo | **`errores` por campo en todos**: salas, personas, producciones, sugerencias, reportes, registros y alta de cuenta |
| El `401`/`403` de Spring Security y el `403` de CSRF salían con lo que armara el framework, o con el cuerpo vacío | `ProblemDetail` con `detail`, igual que el resto |
| Un `404` de ruta inexistente, un JSON roto o un `405` salían con el cuerpo genérico de Boot, en inglés cuando traían mensaje | `ProblemDetail` con `detail` **en castellano** |
| Un fallo inesperado escapaba sin forma | `500` con `detail` genérico —"Algo falló de nuestro lado…"— y el error completo en el log del servidor, nunca en la respuesta |

Los mensajes por estado, cuando el error lo detectó el framework y no el dominio: `400` "Revisá
los datos ingresados" · `401` "No hay una sesión iniciada" · `403` "No tenés permiso para hacer
eso" (o "El token de seguridad venció. Probá de nuevo" si es de CSRF, que son dos cosas
distintas: una no se arregla reintentando y la otra sí) · `404` "Eso no existe o ya no está" ·
`405` "Esa acción no se puede hacer así" · `415` "Ese tipo de contenido no se acepta" · `5xx`
"Algo falló de nuestro lado. Probá de nuevo en un rato".

**Lo que NO cambió, y es a propósito:** los errores de dominio siguen declarados en su
controlador. La forma es la misma para todos, pero *qué significa* cada uno lo decide el
endpoint, no el tipo — `DataIntegrityViolationException` es un `409` al borrar una sala y un
`204` al dar dos veces el mismo like—, y la mitad de esas excepciones son internas de su módulo,
que la capa de aplicación no puede ni nombrar (ADR-001). Un `@ExceptionHandler` de un controlador
le gana al global, que es lo que mantiene en pie los mensajes que son una decisión: el `401` del
login sigue diciendo "Email/usuario o contraseña incorrectos" sin revelar cuál de los dos falló
(HU-02).

**Lo que el frontend sigue teniendo que hacer**, y no es deuda sino buen cliente HTTP:
- **Degradar con gracia igual**: si un día falta `detail`, un mensaje propio por código de
  estado. Nunca mostrar `undefined`. Hay respuestas sin cuerpo por diseño (todos los `204`).
- **`errores` está solo cuando el problema es de campos.** Un `409` de alta de cuenta puede venir
  con el mapa o sin él, según sea el chequeo o la carrera del índice único: ver abajo.
- Un `401` **no es un error a mostrar**: es "no hay sesión", y qué hacer con él depende de para
  qué se llamó, no del código (`FRONTEND_ARCHITECTURE.md`).

### Fechas

Dos tipos distintos y no hay que confundirlos:
- `fecha` + `granularidad` = **cuándo fue al teatro** (MD-1/D59). `fecha` es `YYYY-MM-DD`
  normalizada al comienzo del período y `granularidad` dice hasta dónde leerla: `DIA`, `MES`,
  `ANIO` o `SIN_FECHA` (con `fecha: null`). **El frontend formatea con las dos**: `MES` se
  muestra "marzo de 2023", nunca "1 de marzo de 2023".
- `creadoEn` = **cuándo lo cargó**, instante ISO-8601 UTC. Es lo que ordena el feed (D66).

### Nulos

**La convención de tres estados** (D67/D68), en `loSigo`, `leDiLike` y `vecesQueLaVi`:

| Valor | Significa |
|---|---|
| valor concreto | hay algo que dibujar, y ese es su estado |
| `null` | **no hay nada que dibujar**: sin sesión, en el perfil propio, o un ítem sin reseña |

**Y aparte, los nulos ordinarios.** Estos se olvidan y rompen pantallas, así que van juntos:

| Campo | `null` cuando |
|---|---|
| `autor`, `reportante`, `sugerente` | **la cuenta ya no existe.** Pasa en reseñas de la ficha, ítems del feed y las dos colas del admin — hay que dibujar un autor desconocido, no romper. En la cola de reportes `autor` viaja nulo además cuando desapareció el registro entero: ver la cola |
| `sala`, `complejo`, `sinopsis`, `obraOriginal`, `autorOriginal` | la ficha no los tiene cargados |
| `rating`, `resenia` | el registro no los tiene: un registro pelado es válido y es el caso frecuente |
| `fecha` | `granularidad: "SIN_FECHA"` |
| `promedio` | nadie puntuó todavía · `promedioPropio`: nunca puntuó |
| `siguienteCursor` | no hay más páginas — **pero ojo con lo contrario**, ver el feed |
| `texto` de la cola de reportes | el texto se borró **o** el registro entero desapareció — se distinguen mirando `produccion`, ver la cola |
| `aficheUrl` | la ficha no tiene afiche, que es el caso normal y no un error (D71) |

**Enums:** `EstadoProduccion` = `EN_CARTEL` · `CERRADA` · `PROXIMAMENTE` (D8).
`RolParticipacion` = `ACTUACION` · `DIRECCION` · `DRAMATURGIA` (D17).
`RolUsuario` = `USUARIO` · `ADMIN`. Llegan crudos: traducirlos a castellano es del frontend.

## Índice

| Método | Ruta | Quién | Historia |
|---|---|---|---|
| POST | `/api/auth/registro` | abierto | HU-01 |
| POST | `/api/auth/login` | abierto | HU-02 |
| POST | `/api/auth/logout` | abierto con CSRF (ver arriba) | HU-02 |
| GET | `/api/auth/yo` | sesión | HU-02 |
| GET | `/api/producciones/{id}` | abierto | HU-04 |
| GET | `/api/producciones/{id}/opiniones` | abierto | HU-14, HU-10 |
| GET | `/api/personas/{id}` | abierto | HU-05 |
| GET | `/api/salas/{id}` | abierto | HU-04 |
| GET | `/api/en-cartel` | abierto | HU-06 |
| GET | `/api/buscar/producciones\|personas\|usuarios?q=` | abierto | HU-07 |
| GET | `/api/usuarios/{username}` | abierto | HU-03/12/13/15 |
| POST · PUT · DELETE | `/api/registros` · `/{id}` | sesión | HU-09/10/11 |
| GET | `/api/feed?cursor=&tamanio=` | **sesión** | HU-16 |
| POST · DELETE | `/api/usuarios/{username}/seguir` | sesión | HU-15 |
| POST · DELETE | `/api/resenias/{id}/like` | sesión | HU-17 |
| POST | `/api/resenias/{id}/reporte` | sesión | HU-18 |
| POST | `/api/sugerencias` | sesión | HU-08 |
| CRUD | `/api/admin/salas` · `/api/admin/personas` | admin | HU-19/20 |
| CRUD + `?estado=` + PATCH | `/api/admin/producciones` | admin | HU-20 |
| POST · DELETE | `/api/admin/producciones/{id}/afiche` | admin | HU-20 (D77/D88) |
| POST | `/api/admin/producciones/{id}/fusionar` | admin | HU-20 (D63) |
| GET + aprobar/rechazar | `/api/admin/sugerencias` | admin | HU-21 |
| GET + borrar/desestimar | `/api/admin/reportes` | admin | HU-22 |

**La API es toda por id.** Las URLs del frontend llevan slug (`/obra/{id}-{slug}`, D74) pero el
slug es decorativo y no viaja nunca al backend: lo único que se parsea es el id.

---

## Identidad

### `POST /api/auth/registro` → `201`
Alta de cuenta, que **deja la sesión abierta** (HU-01: al completar, quedás logueado).

```jsonc
// pide
{ "username": "ramiro", "email": "vos@example.com", "password": "unaClaveLarga" }
// devuelve: CuentaResponse
{ "id": 1, "username": "ramiro", "email": "vos@example.com", "rol": "USUARIO" }
```

| Campo | Regla |
|---|---|
| `username` | 3-20 caracteres, `[A-Za-z0-9_]`. Es parte de la URL del perfil (MD-4/D75) e inmutable |
| `email` | formato válido, máximo 254 |
| `password` | ver abajo |

⚠️ **La contraseña se mide dos veces y en dos unidades.** El `@Size(min = 8, max = 72)` cuenta
**caracteres**, y después `UsuarioService` vuelve a medir en **bytes UTF-8** contra el mismo 72,
que es el límite real de BCrypt: 40 letras con tilde ya son 80 bytes. Las dos validaciones son
del backend y las dos responden `400` con `errores.password` —la segunda con "los acentos y
emojis ocupan más de un lugar"—, así que el formulario puede mostrarlas igual. **Para que el
error no llegue recién al enviar, el contador del campo cuenta bytes UTF-8, no caracteres.**

**`400`** con `errores` por campo · **`409`** si el username o el email ya están tomados.

⚠️ **El `409` tiene dos formas y la pantalla tiene que aguantar las dos** (`AuthController`):

| Caso | Cuerpo | Qué pinta el formulario |
|---|---|---|
| **El normal**: el dato ya existía cuando se comprobó | `ProblemDetail` + `errores` con `username` **o** `email` | el error al lado de ese campo |
| **La carrera**: dos altas simultáneas con el mismo dato, resuelta por el índice único de la base | `ProblemDetail` **sin `errores`**, con `detail` "Ese nombre de usuario o email ya está tomado" | un mensaje general arriba del formulario |

La segunda es rara pero es la que rompe una pantalla escrita contra la primera: leer
`errores.username` sin comprobar que `errores` existe muestra `undefined`. **En los dos casos el
formulario se queda como está** —no se refresca ni se vacía—, porque lo que hay que cambiar es el
dato tipeado.

### `POST /api/auth/login` → `200` · `CuentaResponse`
```jsonc
{ "identificador": "ramiro | vos@example.com", "password": "unaClaveLarga" }
```
**`401`** con detalle genérico —"Email/usuario o contraseña incorrectos"— sin decir cuál de los
dos falló (HU-02).

### `POST /api/auth/logout` → `204` · `GET /api/auth/yo` → `200` · `CuentaResponse`
El logout responde `204` **siempre**, con sesión o sin ella (ver arriba): no hay camino de error
que dibujar detrás del botón de salir.

`yo` es quién sos, con email y rol: es lo que decide si se dibuja el acceso al panel admin.
Sin sesión responde **`401`**, y eso no es un error a mostrar sino "no hay nadie": es la
respuesta esperada del visitante anónimo, que además es la mayoría. **Ese `401` no redirige a
ningún lado** — el que redirige es el `401` de una acción protegida, que es otro contexto
(`FRONTEND_ARCHITECTURE.md`). Ese mismo `401` es el que el frontend usa como semilla del token
CSRF: por cómo está armada la cadena de filtros trae `Set-Cookie: XSRF-TOKEN` igual que
cualquier otra respuesta. ✅ **Comprobado** contra el backend corriendo (D82): el `401` de
`/api/auth/yo` trae la cookie, directo y a través del rewrite de desarrollo. Detalle en
`FRONTEND_ARCHITECTURE.md`.

---

## Catálogo público

### `GET /api/producciones/{id}` → ficha (HU-04)
```jsonc
{
  "id": 12, "titulo": "...", "sinopsis": "...",
  "obraOriginal": "...", "autorOriginal": "...",   // D13, texto libre
  "estado": "EN_CARTEL",
  "aficheUrl": "/afiches/12-3.jpg",                // D77/D88 — null si no tiene
  "sala": { "id": 3, "nombre": "Sala Casacuberta", "complejo": "Teatro San Martín" },
  "participaciones": [
    { "id": 88, "persona": { "id": 40, "nombre": "..." }, "rol": "DIRECCION" }
  ]
}
```
⚠️ **La ficha no trae promedio ni reseñas**: eso es Diario y se pide aparte (D60). La pantalla
hace dos llamadas, que no es lógica de negocio en el front (D34).
Una persona puede aparecer en varias participaciones de la misma ficha, una por rol (D17):
**agrupar por persona es trabajo de la pantalla**.

### `GET /api/producciones/{id}/opiniones` → promedio, reseñas y tu historial (HU-14, HU-10)
```jsonc
{
  "promedio": 8.4,          // ⚠️ último rating de cada usuario (D20), NO un AVG. null si nadie puntuó
  "cantidadRatings": 17,    // personas, no registros
  "vecesQueLaVi": 2,        // D76 — null sin sesión; 0..N con sesión
  "resenias": [
    { "registroId": 91, "autor": "ramiro", "texto": "...", "rating": 9,
      "fecha": "2026-03-01", "granularidad": "MES",
      "likes": 4, "leDiLike": false, "creadoEn": "2026-07-12T02:31:00Z" }
  ]
}
```
`registroId` es con lo que se dan likes y se reporta. `leDiLike` es `null` sin sesión, y `autor`
es `null` si esa cuenta ya no existe.

**`vecesQueLaVi` es lo que cierra HU-10** —"la ficha evidencia que la viste N veces"— y por eso
está acá y no en el cliente: contar registros propios cruzando el diario entero sería lógica de
negocio en el frontend (D34) y además pediría una respuesta que no pagina para dibujar un
número. `0` es "no la viste" y habilita el CTA de registrar; `null` es "no hay sesión".

### `GET /api/personas/{id}` → artista (HU-05)
```jsonc
{ "id": 40, "nombre": "...",
  "participaciones": [ { "id": 88, "rol": "ACTUACION", "produccion": { /* resumen */ } } ] }
```

### `GET /api/salas/{id}` → sala (pantalla 5 de USER_FLOWS)
```jsonc
{ "id": 3, "nombre": "...", "complejo": "...", "enCartel": [ /* resúmenes */ ] }
```

### `GET /api/en-cartel` → dos listas (HU-06)
```jsonc
{ "enCartel": [ /* resúmenes */ ], "proximamente": [ /* resúmenes */ ] }
```

**Resumen de producción** — la forma corta que devuelven en-cartel, artista, sala, búsqueda y el
listado del admin:
```jsonc
{ "id": 12, "titulo": "...", "estado": "EN_CARTEL",
  "aficheUrl": "/afiches/12-3.jpg",      // D77/D88
  "sala": { "id": 3, "nombre": "...", "complejo": "..." } }
```

---

## Búsqueda (HU-07, D65)

Tres endpoints independientes, uno por tipo de cosa. Las salas no se buscan (D23).

| Ruta | Devuelve |
|---|---|
| `/api/buscar/producciones?q=` | `[ resumen de producción ]` — es el del autocompletado de HU-09 |
| `/api/buscar/personas?q=` | `[ { "id", "nombre" } ]` |
| `/api/buscar/usuarios?q=` | `[ { "id", "username", "creadoEn" } ]` |

Aguantan typos y títulos a medio escribir (`pg_trgm`). **Tope de 10 y sin paginado.** `q` vacío
devuelve lista vacía, no el catálogo entero. Sin resultados es **`200` con `[]`, no `404`** —
que es lo que necesita el estado vacío que deriva a sugerir (HU-08).

---

## Diario

### `GET /api/usuarios/{username}` → el perfil entero (HU-03/12/13/15)
Una sola llamada: cuenta, diario, stats y contadores sociales. Es la pantalla más compuesta de
la API.

```jsonc
{
  "usuario": { "id": 1, "username": "ramiro", "creadoEn": "..." },
  "estadisticas": {
    "totalRegistros": 42, "totalProducciones": 39,   // distintos por el re-visto (D19)
    "promedioPropio": 7.8,                            // plano y propio, nada que ver con D20
    "registrosSinFecha": 3,
    "porAnio": [ { "anio": 2026, "cantidad": 12 } ]
  },
  "seguidores": 8, "seguidos": 12,
  "loSigo": false,                 // null en el perfil propio o sin sesión → no se dibuja el botón
  "registros": [ /* con fecha, descendente */ ],
  "sinFecha": [ /* sección propia al final (MD-2) */ ]
}
```
⚠️ **Dos listas y no una**, a propósito: no hay que mezclarlas ni inventarle fecha a las de
`sinFecha` para que ordenen. **`404`** si no existe ese username.

**Registro de diario** — la unidad que aparece en `registros`, `sinFecha` y (con otro nombre) en
el feed:
```jsonc
{ "id": 91,
  "produccion": { "id": 12, "titulo": "...", "enCatalogo": true },  // D62
  "fecha": "2023-01-01", "granularidad": "ANIO",
  "rating": 9, "resenia": "...", "creadoEn": "..." }
```
`enCatalogo: false` significa que el admin borró la ficha: **el título se muestra igual pero no
se linkea** (D62). Es un caso a dibujar, no un error. **No trae `aficheUrl`** ni lo va a traer
(D77): la fila del diario y del feed funciona tipográficamente.

### `POST /api/registros` → `201` · `PUT /api/registros/{id}` → `200` (HU-09/10/11)
El gesto. El mismo cuerpo sirve para crear y para editar —editar reemplaza el gesto entero,
incluida la producción, porque equivocarse de obra al elegirla del buscador es el error más
probable—:
```jsonc
{ "produccionId": 12, "fecha": "2026-07-12", "granularidad": "DIA", "rating": 9, "resenia": "..." }
```
Solo `produccionId` y `granularidad` son obligatorios. `rating` entero 1-10 o `null` (D9).
`resenia` hasta 5000. Con `granularidad: "SIN_FECHA"`, `fecha` va en `null`. Registrar la misma
obra otra vez es válido y esperado: es el re-visto (D19), no un duplicado a evitar.

**`400`** con `errores` si la fecha no cierra con su granularidad o si la función es futura ·
**`404`** si esa producción no está en el catálogo — **ese 404 es el camino a HU-08**, no una
pantalla de error · **`403`** si el registro es de otra persona (existe, es público, no es tuyo).

### `DELETE /api/registros/{id}` → `204`
El promedio de la producción se recalcula solo. La confirmación previa es de la pantalla (HU-11).

---

## Social

### `GET /api/feed` → `200` (HU-16, D66) — **exige sesión: `401` sin ella**
La única lectura de contenido que pide cuenta (la otra que pide sesión es `GET /api/auth/yo`,
que no devuelve contenido sino quién sos). **No es un `GET` abierto que se personaliza**: sin
cookie no hay respuesta que cachear, así que va siempre por el cliente con sesión y `no-store`.

`?cursor=<instante>_<idRegistro>` y `?tamanio=` (default 20, tope 50; fuera de rango se recorta
en silencio).

```jsonc
{
  "global": true,          // no seguís a nadie: esto es toda la plataforma (D22). Hay que avisarlo
  "items": [
    { "registroId": 91, "autor": "ramiro",
      "produccion": { "id": 12, "titulo": "...", "enCatalogo": true },
      "fecha": "...", "granularidad": "DIA", "rating": 9,
      "resenia": "...",        // null: un ítem del feed puede no tener reseña
      "likes": 4, "leDiLike": false, "creadoEn": "..." }
  ],
  "siguienteCursor": "2026-07-12T02:31:00Z_91"
}
```
Ordenado por `creadoEn` descendente, que es **al revés que el diario**: el feed muestra lo que la
gente está contando ahora. La página siguiente se pide con `siguienteCursor` tal cual llegó; no
hay número de página.

**La condición de corte del scroll infinito es `siguienteCursor: null`.** Eso es lo que dice que
no hay más y es lo que el cliente mira.

⚠️ Con una salvedad: el cursor se calcula comparando cuántos ítems volvieron contra el tamaño
pedido, así que **si la última página entra exactamente completa, el cursor viene igual** y la
llamada siguiente devuelve `items: []` con `siguienteCursor: null`. Es un caso normal —una
página de más, vacía, al final— y hay que tratarlo como fin de lista y no como error ni como
hueco en la pantalla. `siguienteCursor: null` sigue siendo la condición; `items: []` es el caso
adicional que el cliente tiene que sobrevivir.

`global: true` con `items: []` significa que la plataforma entera está vacía; `global: false`
con `items: []` significa que los tuyos no registraron nada, que es información honesta y no un
feed roto.

### `POST` / `DELETE /api/usuarios/{username}/seguir` → `204` (HU-15)
Sin cuerpo. Idempotentes: seguir a quien ya seguís responde `204` igual. **`400`** al seguirse a
uno mismo, **`404`** si no existe la cuenta. Los contadores y `loSigo` se releen del perfil.

### `POST` / `DELETE /api/resenias/{id}/like` → `204` (HU-17)
El `{id}` es el `registroId`. **`404`** si ese registro no tiene texto: un registro sin reseña no
es una reseña. No devuelve el contador nuevo — llega con la ficha o la página del feed.

### `POST /api/resenias/{id}/reporte` → `204` (HU-18)
```jsonc
{ "motivo": "..." }   // opcional (máx. 500), y el cuerpo entero también: el botón es un clic
```
**`400`** si es tu propia reseña (HU-18 dice ajena) · **`404`** si no es una reseña. No hay
feedback posterior al reportante (MD-3): la confirmación es de la pantalla.

### `POST /api/sugerencias` → `201` (HU-08, D69)
```jsonc
// pide — solo el título es obligatorio
{ "titulo": "Una que vi en 2014", "sala": "Un sótano de Almagro", "anio": 2014,
  "elenco": "...", "comentario": "..." }
// devuelve lo propuesto: ESA es la confirmación de recibido. No hay estado que consultar después
```
Máximos: `titulo` y `sala` 250, `elenco` y `comentario` 1000. `anio` entre 1800 y 2100. `sala` y
`elenco` son **texto libre**, no ids del catálogo: quien sugiere no sabe si existen. La pantalla
tiene que decir la expectativa honesta —catálogo curado, sin aviso si se aprueba (MD-3)— y llegar
acá **sin perder lo que ya se había tipeado** en el buscador del gesto.

---

## Panel de admin

Todo pide rol `ADMIN`; sin él, **`403`**. Sin sesión, **`401`**. Desde D87 estos endpoints
**sí devuelven `errores` por campo** como el resto: ver la sección de errores.

### Salas (HU-19) y Personas (HU-20)

| Método | Ruta | Devuelve |
|---|---|---|
| `GET` | `/api/admin/{salas\|personas}` | lista completa, sin paginar |
| `GET` | `/{id}` | el recurso |
| `POST` | — | `201` + el recurso + header `Location` |
| `PUT` | `/{id}` | `200` + el recurso |
| `DELETE` | `/{id}` | `204` |

| Recurso | Cuerpo | `409` cuando |
|---|---|---|
| Sala | `{ "nombre", "complejo" }` — nombre obligatorio (máx. 200), complejo opcional (máx. 200) | está referenciada por producciones |
| Persona | `{ "nombre" }` — obligatorio, máx. 200 | tiene participaciones cargadas |

Respuestas: `{ "id", "nombre", "complejo" }` y `{ "id", "nombre" }`. Los `409` traen un `detail`
accionable ("reasignalas antes de borrarla") que la pantalla puede mostrar tal cual.

### Producciones (HU-20)

| Método | Ruta | Devuelve |
|---|---|---|
| `GET` | `/api/admin/producciones?estado=EN_CARTEL` | `[ resumen ]` — **el filtro es opcional**; sin él, todas |
| `GET` | `/{id}` | la ficha completa |
| `POST` | — | `201` + ficha + `Location` |
| `PUT` | `/{id}` | `200` + ficha |
| `PATCH` | `/{id}/estado` | `200` + ficha |
| `DELETE` | `/{id}` | `204` |
| `POST` | `/{id}/fusionar` | `200` + resultado de la fusión |
| `POST` · `DELETE` | `/{id}/afiche` | ver abajo |

El `?estado=` es lo que hace trivial el barrido semanal (Flujo 4, D37): se lista lo que está
`EN_CARTEL` y se cierra en un clic con el `PATCH`, sin salir del listado.

```jsonc
// cuerpo de POST y PUT
{ "titulo": "...", "sinopsis": "...", "obraOriginal": "...", "autorOriginal": "...",
  "estado": "EN_CARTEL", "salaId": 3,
  "participaciones": [
    { "personaId": 40, "rol": "ACTUACION" },                  // persona existente
    { "nombrePersona": "Alguien Nuevo", "rol": "DIRECCION" }  // buscar-o-crear inline (D14)
  ] }
```
Máximos: `titulo` 250, `sinopsis` 5000, `obraOriginal` y `autorOriginal` 250, `nombrePersona`
200. `salaId` y `participaciones` son opcionales; `titulo` y `estado` no.

⚠️ **`personaId` y `nombrePersona` son excluyentes**: uno u otro, nunca los dos ni ninguno. Es el
buscar-o-crear inline que hace que cargar una ficha entre en 15 minutos (D37). **`404`** si la
sala o una persona referenciada no existe · **`409`** si la misma persona repite el mismo rol.

- **`PATCH /{id}/estado`** con `{ "estado": "CERRADA" }`.
- **`POST /{id}/fusionar`** con `{ "destinoId": 12 }` (D63) →
  `{ "destinoId": 12, "titulo": "...", "registrosReasignados": 7 }`. Los registros de la
  duplicada pasan a la canónica y la duplicada se borra, todo o nada. **`400`** si origen y
  destino son el mismo. **Necesita confirmación en la pantalla: es irreversible.**

### Afiches (HU-20, D72/D77/D88)

```
POST   /api/admin/producciones/{id}/afiche    multipart/form-data, campo "archivo"  → 200 + ficha
DELETE /api/admin/producciones/{id}/afiche                                          → 204
```

| Regla | Valor |
|---|---|
| Formatos aceptados | JPEG, PNG, WebP |
| Tamaño máximo de subida | 5 MB (`413` si se pasa) |
| Qué se guarda | **un solo archivo por producción**, redimensionado al subir y convertido a **JPEG** (D45, formato por D88) |
| Versionado | dos columnas en la ficha: `afiche_version` (**contador monótono**, arranca en `0`, solo sube, **nunca se reinicia ni se reutiliza**) y `afiche_actual` (**la versión que está publicada, o `null` si hoy no tiene afiche**) |
| `aficheUrl` | se deriva de `afiche_actual`: `null` si es `null`, y si no `/afiches/{id}-{afiche_actual}.jpg` |
| Reemplazo | el mismo `POST`: reserva una versión nueva, escribe el archivo, **después** publica y **al final** borra el viejo (ver el orden) |
| Borrado | `DELETE` deja `afiche_actual: null` —y con eso `aficheUrl: null`, que es un estado normal (D71)— y después borra el archivo. **`afiche_version` no se toca** |
| URL pública | `/afiches/{produccionId}-{version}.jpg` — **fuera de `/api`**, archivo estático (Caddy en producción, ver `FRONTEND_ARCHITECTURE.md` para desarrollo) |
| Caché | `Cache-Control: public, max-age=31536000, immutable`, **puesto por Caddy en producción** — se puede porque el nombre nunca se reutiliza. En desarrollo lo sirve Next desde `public/` con su `max-age=0` por defecto, que está bien: el header largo es una decisión de producción y se prueba ahí |
| Procesamiento de la imagen | **decidido en D88**, ver abajo: se encaja en 1200×1600 sin recortar y sin agrandar, calidad 0,82, se aplica la orientación EXIF y se guarda sin metadatos |
| Errores | `400` formato no soportado o archivo vacío · `413` demasiado grande · `404` producción inexistente |

**La `{version}` es la pieza central del contrato, y lo que la sostiene es que sea monótona.**
Que cambie en cada reemplazo es lo que hace que el navegador, WhatsApp y Google vean la imagen
nueva sin invalidar cachés que no controlamos —las de WhatsApp son justo las que importan
(ADR-003)—, y es lo que permite el `immutable`: **una URL de afiche nunca cambia de contenido ni
se reutiliza**, se reemplaza por otra URL. Por eso el contador y "hoy tiene afiche" son dos datos
distintos: un solo entero que se reinicia al borrar haría que subir → borrar → subir devuelva la
`{version}` `1`, y esa URL ya está cacheada por un año en máquinas que no controlamos, con otra
imagen adentro. `afiche_version` es un número que solo crece, aunque en el medio la ficha se haya
quedado sin afiche tres veces.

**El orden de las operaciones, y qué queda si falla cada paso:**

| # | Paso | Si falla acá |
|---|---|---|
| 1 | Reservar la versión: `afiche_version = afiche_version + 1` → `n`, **confirmado en la base antes de tocar el disco** | no se escribió nada; `n` queda quemado y no se reusa nunca, que es exactamente lo que se quiere |
| 2 | Escribir `/{id}-{n}.jpg` en el volumen | la ficha sigue mostrando lo que mostraba (el afiche viejo, o ninguno). A lo sumo queda un archivo a medio escribir que nadie referencia |
| 3 | Publicar: `afiche_actual = n` — **recién acá la URL nueva existe para el mundo** | el archivo nuevo queda **huérfano e invisible**: nadie conoce su URL. La ficha sigue coherente |
| 4 | Borrar el archivo de la versión anterior | huérfano, y ya está: la ficha apunta al nuevo, que existe |

La regla que ordena todo esto: **la base nunca puede apuntar a un archivo que no está.** Al revés
sí —un archivo que nadie referencia— y eso es lo que se acepta. El `DELETE` obedece la misma
regla al revés: **primero `afiche_actual = null`, después borrar el archivo**; si el borrado del
archivo falla, queda un huérfano y la ficha ya está correcta.

Reservar la versión antes de escribir (y no después) es lo que hace la promesa cumplible:
**cada intento quema su número, salga bien o mal.** Si el número se asignara recién al publicar,
dos intentos que fallan a mitad de camino calcularían el mismo y escribirían los dos sobre el
mismo nombre — y ahí "una URL nunca cambia de contenido" deja de ser cierto.

**Y lo mismo si en vez de fallar, se solapan.** La tabla de arriba describe fallos secuenciales;
dos subidas simultáneas —el admin con dos pestañas, o un doble clic— son otro problema y lo cierra
lo mismo que ya cerró las colas de D69/D70, sin herramienta nueva:

- **La reserva del paso 1 es una sola sentencia atómica** (`UPDATE ... SET afiche_version =
  afiche_version + 1 ... RETURNING afiche_version`), en su propia transacción corta. **No es
  leer-sumar-guardar**: eso pierde actualizaciones y hace que dos subidas reserven el mismo
  número, que es exactamente lo que el contrato promete que no pasa.
- **El paso 3 va con la fila de la producción bloqueada** (`select ... for update`, el mismo
  patrón de D69/D70). En esa sección crítica se lee el `afiche_actual` viejo, se escribe el nuevo
  y **se devuelve cuál era el viejo**: el archivo que se borra después es ese, no el que resulte
  de volver a leer la base. Sin el bloqueo, dos publicaciones intercaladas pueden dejar a una
  borrando el archivo que la otra acaba de publicar — la única forma de romper "la base nunca
  apunta a un archivo que no está".
- ⚠️ **El paso 4 queda FUERA de esa transacción, y esto no es un detalle.** El orden es: bajo el
  bloqueo se publica y se captura la versión anterior → **se confirma la transacción** → recién
  después, ya sin transacción abierta, se borra el archivo capturado. Borrarlo adentro y que el
  `commit` falle después deja a la base apuntando otra vez al archivo viejo, **que ya no está**:
  el único orden que rompe la regla, y encima llegando desde el lado que parecía más prolijo.
  **La misma frontera vale para el `DELETE`**: `afiche_actual = null` → commit → borrar el
  archivo. Nunca al revés, nunca en la misma transacción.
- **El archivo del paso 2 se escribe en un temporal y se mueve al nombre definitivo con un
  movimiento atómico.** Así nadie puede leer un `.jpg` a medio escribir, y el "archivo a medio
  escribir" que la tabla acepta como huérfano queda con nombre de temporal y no de afiche.

**Qué hay que testear, y qué no alcanza.** Dos subidas *seguidas* prueban que el contador avanza
—que es el caso normal— y **no prueban nada de lo de arriba**, porque el problema es el
solapamiento. Los tests que este contrato exige son **deterministas y concurrentes**: dos
publicaciones solapadas sobre la misma producción, y una subida solapada con un borrado, las dos
con la sincronización explícita (un `CountDownLatch` o una barrera para forzar el entrelazado y
que no dependa del scheduler). Es JUnit y `java.util.concurrent`: **ninguna infraestructura
nueva** (D51). Lo que sí se puede dejar afuera es simular el fallo de cada paso del filesystem:
eso pediría inyección de fallos, y el orden de arriba se razona sin ejecutarlo.

**`DELETE` es idempotente**: si la producción existe pero ya no tiene afiche, responde `204`
igual. Borrar dos veces no es un error —el estado pedido ya es el que hay, la misma semántica que
`DELETE /api/usuarios/{username}/seguir`— y el `404` queda reservado para lo que sí es un error de
verdad: que no exista esa producción.

Los huérfanos se aceptan como basura tolerada: a 50 fichas y un reemplazo ocasional no justifican
una tarea de limpieza (D51). Si algún día pesan, se barren comparando el directorio contra los
`afiche_actual` vivos de la base — todo lo demás sobra por definición.

✅ **Cómo se procesa la imagen, que este contrato había dejado abierto como P16, lo cierra D88:**

| Qué | Decidido |
|---|---|
| Con qué se lee | **TwelveMonkeys** (`imageio-jpeg` + `imageio-webp`), dos dependencias nuevas decididas como se decidió Tailwind (D73). Son **lectores**: acepta los tres formatos de entrada y no se planta con los JPEG CMYK de imprenta, que es lo que llega en un afiche |
| Con qué se escribe | el JDK, en **JPEG** — de ahí que la URL termine en `.jpg`. No existe un escritor de WebP en Java puro, y el resto de los caminos pedía un binario del sistema o código nativo |
| A qué tamaño | **encajado en 1200×1600, sin recortar y sin deformar** (D79), y **nunca se agranda**: el lado mayor que se llega a mostrar es 1200 px y el recorte 2:3 de la grilla lo hace CSS sobre el mismo archivo |
| Con qué calidad | 0,82 |
| Orientación EXIF | **se aplica al subir** y el archivo se guarda sin metadatos: si no se aplicara ahí, no la aplica nadie después |
| Tope de píxeles | **50 MP, configurable, comprobado leyendo la cabecera antes de decodificar**. Es lo único de esta lista que es de seguridad: los 5 MB son del archivo comprimido y no acotan la memoria — un PNG de 100 bytes puede declarar 400 millones de píxeles |

El costo asumido, escrito: **JPEG pesa ~25-30% más que WebP a igual calidad**, y eso se paga justo
en el preview de WhatsApp que ADR-003 vino a comprar. Es la parte reversible del contrato: gracias
al versionado, cambiar de formato el día que se pueda es el codificador y una constante, y la
migración es volver a subir los ~50 afiches (D38) — **ninguna URL vieja se reutiliza jamás**.

⚠️ **El volumen de afiches necesita su propio backup.** El `pg_dump` de D45 respalda PostgreSQL
y las imágenes no están en PostgreSQL: son dos backups distintos, con dos restauraciones
distintas que se prueban las dos antes de la beta. Sin esto, restaurar deja el catálogo entero
sin afiches. Es Fase 5 y está anotado en el roadmap (D45 ampliado por D77).

Y son dos copias de dos momentos distintos, así que **hay que pararlas juntas: mientras corre el
backup no puede haber mutaciones de afiches.** La forma esencial de conseguirlo es la más burda —
la rutina nocturna **detiene el backend**, hace el `pg_dump`, copia el volumen y lo vuelve a
levantar—, y a esta escala cuesta menos de un minuto sin usuarios despiertos. **Las dos copias se
guardan y se restauran como una pareja**, identificadas por la misma marca de tiempo: media
pareja no sirve.

⚠️ **Elegir un orden no alcanza, aunque lo parezca.** Con el backend vivo, `pg_dump` primero y
volumen después falla así: el dump captura la ficha apuntando a `v1`; antes de copiar el volumen
el admin reemplaza el afiche, se publica `v2` y se borra el archivo de `v1`; la copia del volumen
tiene `v2` y ya no `v1`; al restaurar, la base apunta a `v1`, que no existe. Un `DELETE` en esa
ventana hace lo mismo. El orden inverso rompe por el lado simétrico. **Lo que hace segura la
ventana es que no haya escrituras en ella, no en qué orden se leen las dos cosas.**

Se descartó la alternativa que también lo cerraría —**no borrar nunca un archivo**, dejando el
volumen monótono para que la ventana solo pueda agregar—: es coherente con que los huérfanos ya
se toleren, pero convierte "basura tolerada" en crecimiento permanente por diseño y necesita
después la tarea de limpieza que D51 evitó. Parar un minuto es más barato que administrar eso
para siempre.

Y si igual una restauración quedara descalzada —alguien corrió las copias a mano—, el arreglo es
una consulta: poner `afiche_actual = null` en las fichas cuyo archivo no está. Eso las deja en
"sin afiche", que es un estado normal y dibujable (D71), no una imagen rota.

La URL es directa y no un endpoint de la API porque un afiche es un archivo estático: hacerlo
pasar por Spring costaría memoria en un VPS chico (P9) y no compraría nada (todo el contenido es
público, D21). En producción el volumen se monta **solo lectura** en Caddy, que lo sirve con
`file_server`; Spring lo monta con escritura porque es el único que sube y borra. ⚠️ Eso todavía
no está escrito: hoy `caddy/Caddyfile` reparte `/api` y nada más, y el volumen `uploads` solo lo
monta el servicio `backend`. Descongelarlo es Fase 5, como el resto de la infraestructura (D54).

**`aficheUrl` viaja en la ficha y en el resumen de producción, y en ningún otro lado.** No entra
en el registro del diario ni en el ítem del feed: eso obligaría a ensanchar `ProduccionBasica`,
que es la única superficie de la única dependencia entre módulos del sistema (Diario → Catálogo),
para una imagen decorativa que además no existe cuando la ficha fue borrada (D62). El feed y el
diario quedan del lado tipográfico del eje de D71. Es reversible si al usarlo se ve pobre.

### Cola de sugerencias (HU-21)
`GET /api/admin/sugerencias` → pendientes, de la más vieja a la más nueva, sin paginar:
```jsonc
[ { "id": 5, "titulo": "...", "sala": "...", "anio": 2014, "elenco": "...",
    "comentario": "...", "sugerente": "ramiro", "creadoEn": "..." } ]
```
`sugerente` es `null` si esa cuenta ya no existe. Se vacía por dos puertas, las dos → `204`:
- `POST /{id}/aprobar` con `{ "produccionId": 12 }` — **la ficha se carga primero** con el
  formulario de HU-20 precargado, y aprobar solo anota en cuál terminó. Abandonar el formulario a
  mitad de camino deja la sugerencia esperando en vez de perderla (D69). **`404`** si esa
  producción no existe.
- `POST /{id}/rechazar` con `{ "motivo": "..." }` — obligatorio (máx. 500), y lo lee solo el admin.

**`409`** si ya se resolvió: pasa con dos pestañas abiertas o con el botón "atrás". No es un error
roto, es que el estado ya cambió — la pantalla refresca la cola y sigue.

### Cola de reportes (HU-22)
`GET /api/admin/reportes` → pendientes con el contexto ya compuesto:
```jsonc
[ { "id": 3, "reseniaId": 91,
    "texto": "...",           // null si el texto ya no está: el autor se adelantó
    "autor": "ramiro", "produccion": { "id": 12, "titulo": "...", "enCatalogo": true },
    "rating": 9, "motivo": "...", "reportante": "otro", "creadoEn": "..." } ]
```
⚠️ **Cuatro campos de la fila viajan nulos por causas independientes** (`ReporteEnColaResponse`),
y mezclarlas es lo que rompe la pantalla. Los estados posibles son estos tres:

| Qué pasó | `texto` | `produccion` | `autor` | `rating` |
|---|---|---|---|---|
| **Reseña normal** | el texto | **siempre con valor** | el username, o `null` si esa cuenta ya no existe | el puntaje, o `null` porque puntuar es opcional (D18) |
| **El texto se borró** — el autor borró la reseña o la editó dejándola sin texto (HU-11) | `null` | **sigue con valor** | igual que arriba, independiente | igual que arriba, independiente |
| **El registro entero desapareció** — el autor borró su registro (HU-11) | `null` | `null` | `null` | `null` |

**`produccion` es el discriminante confiable** entre las dos filas degradadas: es el único campo
que existe siempre que exista el registro. Mirar `autor` o `rating` para decidirlo da falsos
positivos —`autor: null` es una cuenta borrada y `rating: null` es un registro sin puntaje, las
dos cosas normales en una reseña viva—, y mirar `texto` solo dice que no hay texto, no por qué.

En los dos casos degradados **la fila sigue en la cola y las dos acciones siguen vivas**: es la
única forma de vaciarla. `motivo` es `null` cuando quien reportó no escribió nada, y no tiene
relación con lo de arriba.

**Lo que la cola *no* muestra:** filas de reportes que ya se resolvieron. Las dos salidas
alcanzan a **todos** los reportes pendientes de esa reseña (D70), así que "el texto está en
`null` porque otro reporte de la misma reseña ya se resolvió" no es un caso que se pueda ver
acá: esos reportes salieron de la cola en la misma operación.

Dos salidas, las dos → `204` y las dos alcanzan a **todos** los reportes pendientes de esa
reseña, no solo al que se tocó (D70):
- `POST /{id}/borrar-resenia` — borra el texto y **deja el registro, su fecha y su puntaje**: el
  promedio de D20 no se mueve, y en el feed la salida sigue apareciendo sin texto.
- `POST /{id}/desestimar`.

**`409`** por lo mismo que la cola de sugerencias.

---

## Huecos conocidos

Lo que falta o no cierra, para que no se descubra a mitad de una pantalla. **Los dos primeros ya
están cerrados** y quedan escritos para que se entienda qué cambió; del 3 en adelante son límites
aceptados, no deuda.

1. ~~**El ⏳ de los afiches.**~~ **Cerrado**: los dos endpoints existen, con el versionado, el
   orden de las cuatro operaciones y los tests de solapamiento que D77 exigía, y P16 se resolvió
   en D88 (TwelveMonkeys para leer, JPEG a la salida). Lo único que sigue pendiente de los
   afiches es de la **Fase 5** y no toca este contrato: que Caddy sirva `/afiches` desde el
   volumen y que ese volumen tenga su propio backup (D45 ampliada por D77).
2. ~~**No hay manejo global de errores.**~~ **Cerrado en D87**: hay `@ControllerAdvice` y los dos
   manejadores de la cadena de filtros, así que las tres familias son una sola y los formularios
   del admin devuelven `errores` por campo. Ver la sección de errores.
3. **No hay listado público del catálogo entero**: se llega por en-cartel, por búsqueda o por
   link. Es coherente con USER_FLOWS —no existe una pantalla "todas las obras"— pero conviene
   saberlo antes de buscar el endpoint. El listado completo existe pero es del admin.
4. **El perfil no pagina**: trae el diario entero en una respuesta. A la escala del MVP alcanza;
   el día que no alcance, se parte.
5. **La búsqueda no pagina y topea en 10** por tipo (D65). La pantalla de resultados de HU-07
   muestra eso y nada más.
6. **No hay `GET` de un registro suelto.** Editar (HU-11) se abre con los datos que la pantalla ya
   tiene del diario o del feed.
