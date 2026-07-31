# Frontend Architecture

> Estado: v1.1 — cierre del paso 0 de la Fase 4, corregido en su primera revisión (los `GET`
> personalizados contra el feed, los dos contextos del `401`, las dos formas del `409` del alta,
> las reglas de la semilla CSRF, la validación del id y cómo se sirven los afiches en desarrollo).
> **Este documento es al frontend lo que MODULE_MAP es al backend**: el que dice dónde va cada
> cosa nueva. Existe porque eso fue lo que hizo rápida la Fase 1 —cuando aparecía algo
> imprevisto, no había que decidir dónde ponerlo— y el frontend no tenía nada equivalente.
>
> Lo respalda **D78**. El contrato con el backend es `API.md`; los tokens y componentes son
> `product/DESIGN_SYSTEM.md` (v1.1, D79/D81) y las pantallas `product/SCREEN_SPECS.md`
> (v1.1, D80/D81).
> **Con esos cuatro, el paso 0 de la Fase 4 está cerrado** (ROADMAP).

## Las cinco reglas innegociables

Si una sola cosa se lee de este documento, que sean estas. El resto es detalle:

1. **Ninguna lógica de negocio en Next** (D34). Ni promedios, ni reglas de re-visto, ni decidir
   qué es una reseña. El frontend pide, muestra y manda.
2. **El `fetch` al backend vive solo en `lib/api/`.** Cero `fetch` en un componente, en una
   página o en un hook suelto. Si una pantalla necesita un dato nuevo, se agrega una función al
   cliente del módulo que corresponde.
3. **Server Component por defecto.** `"use client"` es la excepción y se justifica: hace falta
   estado, evento o efecto.
4. **Lo que decide la caché es la llamada, no el endpoint.** Sin cookie se cachea con TTL; con
   cookie, jamás. Son dos clientes distintos y confundirlos sirve la página de una persona a otra.
5. **Las mutaciones las hace el navegador contra Spring, no Next.** Sin Server Actions
   (ver abajo).

## Estructura de carpetas

```
frontend/
  app/                          rutas (App Router). Una carpeta = una URL
    layout.tsx                  el armazón de cuatro piezas (D81): cabecera, menú principal,
                                bloque inferior (botón de registrar siempre presente, D71 +
                                barra de destinos) y pie. Resuelve la sesión una sola vez
    page.tsx                    home: feed si hay sesión, landing si no
    obra/[slug]/page.tsx        ficha — SSR + Open Graph (crítico)
    artista/[slug]/page.tsx
    sala/[slug]/page.tsx
    usuario/[username]/page.tsx perfil/diario — SSR + Open Graph (crítico)
    en-cartel/page.tsx
    buscar/page.tsx
    sugerir/page.tsx
    login/page.tsx  registro/page.tsx
    admin/                      el panel: salas, personas, producciones, colas
    not-found.tsx  error.tsx
  components/
    ui/                         los 10 de DESIGN_SYSTEM.md (D79): tarjeta, fila, afiche,
                                puntaje, chip, usuario, botón, estado vacío, aviso, confirmación
    layout/                     el armazón, y sólo lo usa layout.tsx (D81):
                                Cabecera.tsx, MenuPrincipal.tsx, BloqueInferior.tsx,
                                BarraDestinos.tsx
    <dominio>/                  compuestos: FichaCabecera, FilaDeDiario, ItemDeFeed...
  lib/
    api/                        ← el ÚNICO lugar con fetch
      client.ts                 navegador: CSRF, errores, mismo origen
      server.ts                 server components: URL interna, cookies, caché
      catalogo.ts diario.ts social.ts identidad.ts admin.ts
      tipos.ts                  los tipos de API.md, a mano
    rutas.ts                    construir y parsear /obra/{id}-{slug} (D74)
    formato.ts                  fecha difusa, puntaje, enums a castellano
```

**`lib/api/` espeja los módulos del backend.** No es decoración: cuando algo no encaja en
ninguno de los cinco archivos, casi siempre es porque tampoco encajaba en el backend y hay algo
que pensar. Es el mismo mecanismo de aviso que dio MODULE_MAP en la Fase 1.

**Un componente nace en la carpeta de su dominio.** Sube a `ui/` recién cuando lo usa una
segunda pantalla, y ahí pierde todo lo que sabía del dominio: `ui/` no conoce producciones.

**`components/layout/` es la excepción que confirma esa regla, no un agujero en ella** (D81). El
menú principal y la barra de destinos parecen candidatos obvios a `ui/` porque se ven en las 13
pantallas, pero **los usa `app/layout.tsx` una sola vez, para todas**: no hay una segunda pantalla
que los repita, que es lo que la regla mide. Van acá, y **el listado de diez de `DESIGN_SYSTEM.md`
no cambia**. `BloqueInferior.tsx` es el contenedor fijo de abajo —el botón persistente arriba, la
barra debajo, con el `border-top` y el `env(safe-area-inset-bottom)`— y existe porque esas dos
piezas comparten posicionamiento y área segura: separarlas obliga a repetir el fijado en dos
lugares y a mantenerlos sincronizados a mano. La barra y el menú son **islas cliente** (necesitan
la ruta activa y el estado de abierto/cerrado); la cabecera y el bloque, no.

⚠️ **La sesión se sigue resolviendo una sola vez, en el layout**, con `GET /api/auth/yo` por
`apiSession` — el armazón nuevo **no agrega ni una llamada** ni pide una librería de estado
global. De esa única respuesta salen las tres cosas que dependen de ella:

| Qué | Con sesión | Sin sesión |
|---|---|---|
| Etiqueta del botón persistente | "Registrar" (abre la hoja del gesto) | "Creá tu diario" (va a `/registro`) |
| Celda **"Mi diario"** de la barra | sí, con el monograma de `Usuario` | **no existe**, y "Feed" se llama "Inicio" |
| Sección **Panel** del menú principal | sólo si `rol === "ADMIN"` | no |

Su `401` no se muestra y no navega (es "anónimo", la tabla de errores de más abajo), así que **el
armazón del visitante se dibuja con el mismo camino de código y sin ningún estado de carga**. Y el
menú principal es un `<dialog>` con las reglas de siempre —foco, `Esc`, clic afuera, y
`pushState`/`popstate` para que "atrás" lo cierre—, sin dependencia nueva (D73).

## Rutas y URLs (D74/D75)

| Pantalla | Ruta | Render |
|---|---|---|
| Home visitante / logueada | `/` | según sesión — ver abajo |
| Ficha | `/obra/{id}-{slug}` | **SSR + OG** |
| Artista | `/artista/{id}-{slug}` | SSR + OG |
| Sala | `/sala/{id}-{slug}` | SSR + OG |
| Perfil / diario | `/usuario/{username}` | **SSR + OG** |
| En cartel | `/en-cartel` | SSR |
| Búsqueda | `/buscar?q=` | cliente |
| Sugerir · Login · Registro | `/sugerir` `/login` `/registro` | cliente |
| Panel | `/admin/...` | cliente |

**El id es lo único que se parsea; el slug es decorativo** (D74). `lib/rutas.ts` tiene las dos
mitades y nadie construye una URL a mano:

```ts
export const rutaObra = (id: number, titulo: string) => `/obra/${id}-${slugify(titulo)}`;

/** null si el segmento no empieza con un entero positivo: la ruta no existe. */
export function idDesdeSlug(slug: string): number | null {
  const [cabeza] = slug.split("-");
  return /^[1-9]\d*$/.test(cabeza) ? Number(cabeza) : null;
}
```

⚠️ **El id se valida antes de llamar a nada, y con una expresión regular y no con `Number`.**
El problema no es que `Number` devuelva basura —`Number("hola")` es `NaN` y eso se ve venir—:
es que **acepta de más**, y cada forma que acepta es una URL distinta que sirve la misma ficha.
Estas son reales:

| Segmento | `Number(...)` | Qué pasaría sin la regex |
|---|---|---|
| `/obra/12.0-hamlet` | `12` | la ficha 12 en una URL que no es la canónica |
| `/obra/1e2-hamlet` | `100` | la ficha 100, escrita de una forma que nadie generó |
| `/obra/+12-hamlet` | `12` | ídem |
| `/obra/012-hamlet` | `12` | ídem, y con infinitos ceros adelante hay infinitas URLs |
| `/obra/0x0c-hamlet` | `12` | ídem, en hexadecimal |
| `/obra/%2012-hamlet` (espacio) | `12` | ídem |

Todas responderían `200` con contenido duplicado para Google y links que envejecen distinto del
canónico. El `/^[1-9]\d*$/` las descarta todas de una: **un segmento que no empieza con un entero
positivo escrito de la única forma que existe es `notFound()`, y ahí termina.** (`/obra/12abc`
no es de esta familia: `Number("12abc")` es `NaN`, así que ese caso ya lo atajaba cualquier
chequeo. El agujero eran los de arriba.)

Si el slug falta o quedó viejo —el admin corrigió el título—, la página **redirige a la forma
canónica con `permanentRedirect()`, que es `308`**, antes de renderizar. `308` y no `301` porque
es lo que emite Next y porque preserva el método; y se hace con el título que ya vino en la
respuesta, así que no cuesta ninguna consulta extra.

⚠️ **`/api` y `/afiches` no son rutas de la app.** En producción Caddy los manda a Spring y al
volumen de imágenes (D77); en desarrollo `/api` lo reenvía el rewrite y `/afiches` es un
directorio estático de `public/` (ver más abajo). En los dos entornos, **cualquier ruta de Next
con esos nombres queda inalcanzable**. La raíz tampoco es libre: el perfil vive bajo `/usuario/`
justamente para no competir con ella (D75).

## Servidor y cliente

**Server Component por defecto** — es lo que hace que el SSR y los metadatos Open Graph de
ADR-003 salgan gratis. Las cinco pantallas públicas son server components enteros.

**`"use client"` solo donde hay interacción real**, y lo más abajo posible en el árbol: el
autocompletado del gesto, el botón de like, el de seguir, los formularios, el scroll infinito
del feed. Una ficha es servidor con dos islas cliente adentro, no una pantalla cliente.

**Open Graph** (P13, el test literal de HU-04: pegar el link en WhatsApp) se resuelve con
`generateMetadata` en las cuatro páginas compartibles. Título, descripción y `og:image` con el
afiche cuando exista (D77) o la imagen tipográfica de respaldo cuando no (D71) — que no es un
detalle estético: es la mitad de la mecánica de crecimiento.

### Cómo viaja la sesión

Son dos caminos y hay que tenerlos separados en la cabeza:

**1. El navegador contra Spring** (todas las mutaciones y las lecturas del cliente). Mismo
origen detrás de Caddy: la cookie `JSESSIONID` viaja sola. Lo único que hay que hacer a mano es
CSRF — leer `XSRF-TOKEN` y mandarla en `X-XSRF-TOKEN` (D57)—, y eso lo hace `client.ts` para
todo el mundo. **Después de `registro`, `login` y `logout` hay que releer la cookie**: el token
se rota en las tres.

**2. Un Server Component contra Spring** (el SSR). Acá no hay navegador: la cookie **hay que
reenviarla a mano**, tomándola de `cookies()` y poniéndola en el header de la llamada. Es el
error clásico de Next con sesiones y por eso vive en un solo lugar, `server.ts`.

La URL del backend es distinta en cada caso: el navegador usa rutas relativas (`/api/...`) y el
servidor de Next usa la URL interna del Compose, por variable de entorno. Nunca al revés.

### Caché: la regla que más caro sale romper

⚠️ **Lo que decide no es el endpoint: es la llamada.** **Dos** `GET` abiertos cambian de
respuesta cuando llevan cookie: `opiniones` agrega `leDiLike` y `vecesQueLaVi`, y el perfil
agrega `loSigo` (ver `API.md`). La misma URL va por un cliente o por el otro según haya sesión, y
clasificar "el perfil" como público de una vez y para siempre es exactamente el error que hay que
evitar.

**El feed no es uno de esos dos.** `GET /api/feed` **exige sesión** y responde `401` sin ella, así
que no tiene versión anónima que cachear: va siempre por `apiSession` y siempre `no-store`, sin
que haya nada que decidir por llamada. Lo mismo `GET /api/auth/yo`. Contarlos entre los "abiertos
personalizados" confunde dos problemas distintos: uno es *elegir cliente según la llamada*, el
otro es *un endpoint que directamente no responde sin cuenta*.

```
apiPublic   → sin cookie, cacheable con TTL
apiSession  → reenvía cookie, no-store SIEMPRE
```

| Contenido | Cliente | Caché |
|---|---|---|
| Ficha, opiniones, perfil — **visitante anónimo** | `apiPublic` | **`revalidate: 60`** |
| En cartel, artista, sala | `apiPublic` | **`revalidate: 300`** |
| Ficha, opiniones, perfil — **con sesión** | `apiSession` | **`no-store`** |
| Feed y `/auth/yo` (**exigen sesión**), todo lo del panel admin | `apiSession` | **`no-store`** |

El TTL no es cero porque el contenido **muta**: entran reseñas, cambian promedios, el admin
cierra una obra. Sesenta segundos es el compromiso para lo que tiene actividad de usuarios
encima; cinco minutos para lo que solo cambia cuando el admin toca el catálogo. Lo que no admite
compromiso es la columna de la derecha: **una respuesta pedida con cookie no se cachea nunca**.

Si se mezclan, Next sirve la página de una persona a otra — y como todo el contenido es público
(D21), el bug es silencioso: se ve una página válida, con el `loSigo` y los likes de otro. Por
eso son dos clientes distintos y no un parámetro: la elección queda escrita en cada llamada.

**Una página con sesión se renderiza entera sin caché.** No hay páginas mitad cacheadas: si el
visitante tiene cookie, todo el árbol va por `apiSession`. La home es el caso claro —con sesión,
feed; sin sesión, landing con "en cartel" cacheable— y la ficha es el caso sutil: la misma
pantalla, cacheada para el que llega de WhatsApp y fresca para el que está logueado.

### El arranque del token CSRF

Hay un agujero que solo aparece con SSR y hay que taparlo a propósito: **en una página
renderizada en el servidor, el navegador puede no haber hablado nunca con Spring**, así que no
tiene la cookie `XSRF-TOKEN` y la primera mutación se come un `403`. Le pasa al visitante que
entra por un link compartido y lo primero que hace es crear una cuenta — o sea, el Flujo 1
entero (USER_FLOWS).

`client.ts` lo resuelve **antes de cualquier mutación**, no al arrancar la app:

```
mutación → ¿hay cookie XSRF-TOKEN?
             sí → mandar con el header
             no → GET de semilla → releer la cookie → recién ahí mandar la mutación
```

Semilla: **`GET /api/auth/yo`**, que es la respuesta más chica de la API y que el cliente quiere
igual. Cinco reglas que la función de bootstrap tiene que cumplir, porque son justo las que se
improvisan mal:

1. **El `401` es un resultado aceptable, no un fallo.** Sin sesión, `yo` responde `401` y eso es
   lo esperado: lo que se fue a buscar no es el cuerpo sino el `Set-Cookie`. La función no
   propaga ese `401` ni lo trata como error.
2. **Después del GET se vuelve a leer `XSRF-TOKEN`** de `document.cookie`. No se asume que
   está: se lee.
3. **La mutación se manda recién cuando el token existe.** Si después del GET sigue sin haber
   cookie, no se dispara la mutación a ciegas: eso es un `403` garantizado y, peor, un envío que
   el usuario cree hecho.
4. **La semilla no pasa por el manejador global de `401`.** El `401` de una acción protegida
   redirige al login (ver la tabla de errores); el de la semilla no muestra nada y no navega a
   ningún lado. Si los dos caminos comparten el interceptor, el visitante anónimo que va a crear
   una cuenta termina rebotado al login **antes** de poder crearla — que es el Flujo 1 roto por
   la misma pieza que venía a arreglarlo. La semilla usa un `fetch` desnudo, sin ese manejador.
5. **El reintento por `403` ocurre una sola vez.** Si la mutación, ya con token, igual vuelve
   `403`, se relee la cookie, se reintenta **una** vez, y si vuelve a fallar se muestra el error.
   Nada de bucle: un token que no sirve dos veces seguidas no se arregla insistiendo.

⚠️ **Estado de la verificación de la semilla.** Que `GET /api/auth/yo` sirva como semilla se
apoya hoy en el código, no en una prueba HTTP:

- **Respaldado por el código** (`SecurityConfig` + Spring Security): el `CsrfFilter` corre
  **antes** del filtro de autorización, así que la petición pasa por él aunque termine en `401`.
  El `CsrfTokenRequestAttributeHandler` está configurado con `setCsrfRequestAttributeName(null)`
  justamente para que el token se resuelva **sin carga diferida** en cada petición, y
  `CookieCsrfTokenRepository` escribe la cookie cuando no venía ninguna. El `401` lo emite un
  `HttpStatusEntryPoint`, que solo fija el estado sobre la misma respuesta: no descarta los
  headers ya escritos. Y el mecanismo general ya está ejercitado sobre Tomcat real:
  `AutenticacionHttpTest` hace un `GET` y después un `POST` con el token que ese `GET` dejó.
  Lo que ninguna prueba cubre todavía es el caso puntual del `GET` que termina en `401`.
- **Todavía pendiente**: un `curl` no mutante contra el backend corriendo que muestre
  `Set-Cookie: XSRF-TOKEN` **en la respuesta 401 de `/api/auth/yo`** en concreto:
  `curl -si localhost:8080/api/auth/yo | grep -i 'HTTP/\|set-cookie'`. Mientras eso no se corra,
  **esta pieza de D78 queda especificada y no comprobada**. Si el `401` no la trajera, el
  reemplazo ya identificado es `GET /api/en-cartel`, que el README usa como semilla en todos sus
  ejemplos con `curl`; el resto de las cinco reglas no cambia.

Hacerlo perezoso y no al montar la app evita una llamada extra en cada visita anónima, que son
casi todas, y cubre gratis los otros dos casos donde el token falta o quedó viejo: la cookie
expiró, o se rotó en `registro`, `login` o `logout`.

### El proxy local de `/api`, y de dónde salen los afiches en desarrollo

En producción el reparto lo hace Caddy y todo comparte origen. **En desarrollo no hay Caddy**:
Spring escucha en `:8080` y Next en `:3000`, que son dos orígenes distintos — y con dos orígenes
la cookie de sesión y el CSRF no funcionan como en producción, así que se estaría desarrollando
contra un comportamiento que no es el real.

Se tapa con un rewrite en `next.config`, que es el Caddy de los pobres y hace que el navegador
vea un solo origen igual que en producción:

```
/api/:path*  → http://localhost:8080/api/:path*
```

**`/afiches` no lleva rewrite**, y esto es una decisión y no un olvido. Un rewrite de Next solo
puede apuntar a un destino HTTP, y **hoy Spring no sirve `/afiches`** ni va a servirlo en
producción (D77: es un estático que sirve Caddy, justamente para no gastarle memoria a la
aplicación). Un rewrite hacia `http://localhost:8080/afiches/...` apuntaría a un `404`.

**Lo decidido: en desarrollo los afiches los escribe Spring dentro de `frontend/public/afiches/`
y los sirve Next como cualquier archivo estático de `public/`.** El directorio de subida ya tiene
que ser configurable —en producción es el volumen—, así que en desarrollo se lo apunta ahí y no
hace falta nada más: cero código nuevo, cero herramienta nueva (D51), y el navegador ve
`/afiches/12-3.webp` sobre el mismo origen, exactamente la misma URL que en producción.

| | Quién escribe el archivo | Quién lo sirve | Qué ruta ve el navegador |
|---|---|---|---|
| **Desarrollo** | Spring, en `frontend/public/afiches/` (directorio configurable) | Next, como estático de `public/` | `http://localhost:3000/afiches/{id}-{version}.webp` |
| **Producción** | Spring, en el volumen `uploads` montado con escritura | Caddy, con `file_server` sobre el mismo volumen montado **solo lectura** | `https://.../afiches/{id}-{version}.webp` |

`frontend/public/afiches/` va al `.gitignore`: son datos de prueba, no fuente. Y como Next sirve
`public/` desde el disco en cada pedido, un afiche recién subido aparece sin reiniciar nada.

Se descartaron, en orden de cuán cerca estuvieron:
- **Que Spring sirva `/afiches` solo en desarrollo** (un `WebMvcConfigurer` detrás del perfil
  `dev` + el rewrite de Next). Es la más "simétrica" —un solo directorio, el mismo que el
  volumen— pero es código de backend que existe únicamente para desarrollo, contradice en el
  ambiente donde se prueba la decisión de que Spring no sirva archivos, y agrega un salto (Next →
  Spring → disco) para leer un `.webp`. Más piezas para el mismo resultado visible.
- **Levantar Caddy también en desarrollo.** Da paridad perfecta y rompe D54, que dejó el Docker
  local en un comando (`docker compose up -d postgres`) por una razón de ritmo que sigue vigente.
- **No tener afiches en desarrollo** (un placeholder fijo). Se descarta sola: la ficha con afiche
  y su preview OG son la pantalla más importante de la Fase 4, y no se puede diseñar a ciegas.

Dos consecuencias que hay que tener presentes:
- **El rewrite solo aplica al navegador.** Los Server Components siguen llamando a la URL interna
  por variable de entorno, sin pasar por él: en el servidor no hay nada de qué protegerse.
- **`/api` y `/afiches` quedan quemados** como prefijos de Next en los dos entornos, no solo en
  producción — `/afiches` porque en desarrollo es un directorio real de `public/` y en producción
  no llega nunca a Next. Ninguna ruta de la app puede llamarse así.

### Sin Server Actions

Las mutaciones van del navegador a Spring, directo. Una Server Action agregaría un salto que
tendría que reenviar cookie y token CSRF para terminar llamando al mismo endpoint, y sobre todo
**abriría un lugar cómodo donde la lógica de negocio se acumula sin que se note**, que es
exactamente lo que D34 prohíbe. La regla corta: si algo parece necesitar una Server Action,
casi siempre es un caso de uso que le falta al backend.

## Errores

`API.md` describe tres familias de respuesta de error, porque el backend no tiene manejo global
todavía. **`client.ts` las aplana en un solo tipo** y ninguna pantalla ve la diferencia:

```ts
type ErrorDeApi = { status: number; mensaje: string; errores?: Record<string, string> };
```

`mensaje` sale del `detail` si vino, y si no, de una tabla propia por código de estado. **Nunca
se muestra `undefined`** ni se asume que el cuerpo tiene forma: hay endpoints que responden con
el cuerpo vacío.

**El código no alcanza para decidir: hace falta el contexto.** El mismo `404` es una página de
error en una ruta pública y un camino feliz en el gesto de registro. La tabla se lee por las dos
columnas:

| Código | Contexto | Qué hace la pantalla |
|---|---|---|
| `400` con `errores` | cualquiera | error al lado de cada input |
| `400` sin `errores` | **todo el panel admin** | mensaje general del formulario |
| `401` | **`GET /api/auth/yo` usado para saber si hay sesión, o como semilla del token CSRF** | **no se muestra y no navega a ningún lado**: `401` acá significa "anónimo", que es el estado normal de la mayoría de las visitas. Se dibuja la versión sin sesión de la pantalla |
| `401` | **acción o pantalla protegida** (mutación, panel admin, feed, cualquier cosa que necesite un yo) | redirige a login y **vuelve a donde estaba, con lo tipeado** (USER_FLOWS). Tampoco es "un error del servidor": es sesión ausente o vencida |
| `403` | de CSRF | releer el token y **reintentar una vez**, en silencio |
| `403` | de permisos | pantalla de "no podés" — o el `403` de dueño al editar un registro ajeno, que no debería poder pasar |
| `404` | ruta pública (ficha, perfil, artista) | página propia con búsqueda embebida |
| `404` | **gesto de registro**, la obra no está | **no es error**: es el camino a sugerir (HU-08), sin perder lo tipeado |
| `404` | **panel admin** | "ese recurso ya no está" + refrescar el listado. **Nunca la página pública de búsqueda**: el admin no está navegando el catálogo, está trabajando |
| `404` | aprobar una sugerencia con un `produccionId` que no existe | error del formulario, no página: la ficha se carga primero (D69) |
| `409` | **colas del admin** (sugerencia o reporte ya resuelto) | refrescar la cola y seguir: el estado ya cambió, otra pestaña lo hizo |
| `409` | **alta de cuenta, con `errores`** (username o email tomado) | error **por campo** en el formulario. Refrescar no arregla nada: hay que cambiar el dato |
| `409` | **alta de cuenta, sin `errores`** (la carrera que resuelve el índice único: `detail` sí, mapa no) | **mensaje general** arriba del formulario, con el `detail` que vino. Mismo caso, distinta forma: el cuerpo no dice qué campo fue |
| `409` | **borrar sala o persona referenciada, o rol repetido** | mensaje accionable en el lugar donde se tocó. Tampoco se resuelve refrescando: hay que reasignar o sacar |
| `5xx` | cualquiera | mensaje genérico, y **lo tipeado no se pierde** (USER_FLOWS lo pide explícitamente) |

La regla detrás de la tabla: **"refrescar" solo es la respuesta cuando el problema es que el
estado cambió abajo del usuario.** Cuando el problema es lo que el usuario escribió, refrescar
lo hace perder el trabajo y no resuelve nada.

De ahí sale la regla del formulario de alta, que es donde las dos formas del `409` se cruzan:
**si hay `errores`, se pinta por campo; si no hay, se muestra un mensaje general — y en ninguno
de los dos casos se refresca ni se vacía el formulario.** Lo tipeado se queda donde está: cambiar
un username no puede costar volver a escribir el email y la contraseña.

**Nada de lo que el usuario escribió se pierde por un error de red.** Vale para el gesto de
registro y para la sugerencia, que son los dos formularios que importan.

## Lo que el frontend sí hace (y no es lógica de negocio)

Presentación pura, y conviene tenerlo escrito para que la regla 1 no se use de excusa para no
hacerlo:

- **Formatear la fecha difusa** con `fecha` + `granularidad`: "marzo de 2023", nunca "1 de marzo
  de 2023" (D59). En `formato.ts`, una sola vez.
- **Agrupar participaciones por persona** en la ficha: la API las manda una por rol (D17).
- **Traducir enums** a castellano.
- **Decidir si un botón se dibuja**, con la convención de `null` de tres estados (`loSigo`,
  `leDiLike`, `vecesQueLaVi`).
- **Dibujar los casos degradados**: `enCatalogo: false` (título sin link, D62), `autor: null`
  (la cuenta ya no existe), la fila de la cola de reportes con la reseña ya borrada, y la ficha
  sin afiche, que es el caso normal y no un error (D71).
- **Validar formularios antes de mandar.** El `400` del backend es la red de contención, no la
  fuente del mensaje — sobre todo en el admin, donde no hay `errores` por campo.

## Qué NO hay

Sin librería de estado global: la sesión viene del servidor en el layout y lo demás es estado
local de cada isla. Sin librería de data-fetching: `lib/api/` sobre `fetch` alcanza para la
superficie de `API.md`. Sin librería de componentes (D73). Sin i18n: el producto es de CABA y está en
castellano. **El tema claro/oscuro ya lo dijo `DESIGN_SYSTEM.md` (D79): hay tema oscuro, lo elige
el sistema operativo con `prefers-color-scheme` y no hay interruptor** — un bloque `@media`, cero
JavaScript, cero estado, y ningún componente escribe `dark:`.

Cada una de estas vuelve igual que en el backend: cuando aparezca su problema concreto, con una
decisión en el log (D51, misma filosofía que ADR-002).
