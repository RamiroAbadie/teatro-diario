# Screen Specs

> Estado: v1.3 — el paso 2 de la Fase 4 escribió **las pantallas 1, 3, 4, 5, 6 y 13** y este
> documento se corrige en dos puntos contra lo que se vio al dibujarlas: **la celda de la grilla
> no repite el título** (D86, ver la pantalla 6) y **la placa de `og:image` ya tiene con qué
> generarse** (D85, punto 2 de "lo que este documento deja anotado", ahora cerrado). Ninguna
> pantalla cambia de contenido ni de datos.
>
> La v1.0 cerró el paso 0 de la Fase 4; es el cuarto y último de los documentos
> que van antes de la primera pantalla, después de `architecture/API.md` (v1.2),
> `architecture/FRONTEND_ARCHITECTURE.md` (v1.3) y `product/DESIGN_SYSTEM.md` (v1.2).
> Lo respalda **D80**; la v1.1 **D81** (el armazón pasa de tres piezas a cuatro: menú principal
> y barra de destinos — ninguna pantalla nueva, el alcance sigue congelado) y la v1.2 **D83**,
> que corrige contra el código del paso 1 **dónde vive el armazón**: es
> `app/(sitio)/layout.tsx` y no el layout raíz, porque los layouts del App Router se anidan y el
> panel es la excepción de esa misma frase. Con eso, **el 404 global pasa a tener trabajo propio
> en la pantalla 13**. Ninguna pantalla cambia de contenido.
>
> **Qué es:** una entrada por cada una de las 13 pantallas de `USER_FLOWS.md` (más las cinco
> caras del panel), con qué datos pide, cómo se compone, qué isla es cliente y **qué se ve en
> cada estado**. Es el documento que se abre al sentarse a escribir una pantalla.
>
> **Qué NO es:** ni código, ni maquetas, ni una herramienta nueva. Los tokens y los componentes
> ya están decididos en `DESIGN_SYSTEM.md` y acá se usan, no se rediscuten. El contrato de datos
> es `API.md` y **este documento no inventa ni un campo**: si algo hace falta y no está, es un
> hueco del backend y se anota, no se resuelve en el cliente (D34).

## Cómo se lee una entrada

Cada pantalla tiene la misma ficha: **datos** (qué llama y por qué cliente), **composición**
(celular primero, de arriba hacia abajo), **islas cliente**, **estados** y **criterios**. Los
estados son siempre los mismos cuatro y ninguno se puede dejar en blanco: **cargando · vacío ·
error · sin sesión**. Donde uno no aplica, dice por qué.

Convenciones de las tablas:
- **Cliente**: `apiPublic` (sin cookie, cacheable) o `apiSession` (reenvía cookie, `no-store`).
  **Lo decide la llamada y no el endpoint** (D78): en las pantallas marcadas ⚖️, la misma URL va
  por un cliente o por el otro según haya sesión, y con sesión **todo el árbol** va sin caché.
- ⏳ = depende de algo que el backend todavía no tiene (D76 `vecesQueLaVi`, D77 afiches).

---

## Reglas transversales (se definen acá una vez y valen en las 13)

### El armazón: `app/(sitio)/layout.tsx`

⚠️ **No es el layout raíz, y por eso está escrito así** (D83). El raíz es `<html>`/`<body>` y
los tokens, nada más: los layouts del App Router **se anidan**, así que si el armazón viviera
arriba, el panel —que es justo la excepción de la frase que sigue— no tendría forma de
sacárselo. El armazón vive en el grupo `(sitio)` y `app/admin/` cuelga por afuera con el suyo.
**Los paréntesis no cambian ninguna URL.**

**Cuatro piezas**, presentes en todas las pantallas salvo el panel (D81). La de abajo es **un
bloque con dos partes** —el botón persistente y, debajo, la barra de destinos— y por eso se cuenta
como una sola pieza: se dibujan juntas, se fijan juntas y comparten el padding y el área segura.

| Pieza | Celular | ≥`md` |
|---|---|---|
| **Cabecera** | botón **"Menú"** a la izquierda (hamburguesa + la palabra escrita), título/marca al lado (hoy sólo texto: P2 sigue abierto), lupa a la derecha | **sin botón de menú**: título/marca, el buscador desplegado, **los destinos**, el menú de cuenta y el botón de registrar |
| **Menú principal** | panel **a pantalla completa** que abre el botón de la cabecera, con los destinos agrupados por categoría | **no se dibuja** |
| **Bloque inferior** | **fijo abajo**: arriba el botón primario persistente, debajo la **barra de destinos** (4 celdas con sesión, 3 sin ella), `pb-[env(safe-area-inset-bottom)]` | **no hay bloque fijo**: el botón va en la cabecera, a la derecha, y los destinos también viven en la cabecera |
| **Pie** | links mínimos: en cartel, buscar, y el link al repo (AGPL, D46) | ídem |

⚠️ **El botón persistente cambia de identidad según la sesión, y eso resuelve un problema de
verdad:**

| Sesión | Etiqueta | Qué hace |
|---|---|---|
| Con sesión | **"Registrar"** (primario) | abre la hoja del gesto (pantalla 9) |
| Sin sesión | **"Crear tu diario"** (primario) | va a `/registro` |

Así el botón que D71 pide que esté siempre presente **nunca abre un formulario que va a rebotar
en un `401`**: para el anónimo es directamente el CTA de adquisición del Flujo 1, que es lo que
esa pantalla necesitaba igual (hueco 2 de USER_FLOWS). Un solo control, dos productos.

La sesión se resuelve **una sola vez por pedido, en `app/(sitio)/layout.tsx`**, con
`GET /api/auth/yo` por `apiSession` (`FRONTEND_ARCHITECTURE.md`: no hay librería de estado
global; la sesión baja del servidor, y `yo()` está memoizada con `cache()` para que la ficha y
el perfil puedan volver a pedirla sin una llamada más). Su
`401` **no se muestra y no navega**: es "anónimo", que es el estado de la mayoría de las visitas.
De esa única respuesta salen **las tres cosas del armazón que dependen de la sesión**: la etiqueta
del botón persistente, si la barra tiene celda **"Mi diario"**, y si el menú principal dibuja la
sección **Panel** —que va sólo si `rol === "ADMIN"`—.

### El bloque inferior (celular)

De arriba hacia abajo: **el botón persistente** —el de la tabla de acá arriba, sin ningún cambio: un
`Boton` primario de 44 px, con texto, con sus dos identidades— y **debajo la barra de destinos**.
Ese orden es la decisión, no un detalle de maquetado: el CTA de adquisición tiene que seguir siendo
texto que cambia según la sesión, así que no puede convertirse en una celda de la barra (D81).

| | Con sesión | Sin sesión |
|---|---|---|
| Celdas | 4: **Feed · En cartel · Buscar · Mi diario** | 3: **Inicio · En cartel · Buscar** |
| Ancho de celda en 360 | **82 px** (360 − 32 de padding lateral = 328, ÷ 4) | 109 px (328 ÷ 3) |
| Alto del objetivo | **48 px**, por encima del piso de 44 de `DESIGN_SYSTEM.md` | ídem |
| Etiqueta | **`text-xs` (12 px)**, que es el piso de la escala y **no baja de ahí** | ídem |
| Destino activo | **regla de 2 px en `acento` sobre el borde superior de la celda** + `font-medium` + texto en `acento-tinta`. **No se marca sólo con color** | ídem |
| Inactivo | ícono y etiqueta en `tinta-suave` | ídem |
| Separación | `border-top` de 1 px en `borde` entre el botón y la barra | ídem |
| Padding del bloque | `pt-2 px-4 pb-3` + `pb-[env(safe-area-inset-bottom)]` | ídem |

⚠️ **Dos diferencias del anónimo, y las dos tienen motivo:** **"Mi diario" no existe sin sesión**
—no hay diario que mostrar— y **"Feed" se llama "Inicio"**, porque sin cookie esa ruta es la
pantalla 1 y no la 2. Los íconos: casa, cartel y lupa; **"Mi diario" no estrena ícono**, usa el
monograma del componente `Usuario`, que ya existe.

**El costo de espacio, escrito y no estimado:** en un celular de 360×640 el cromo fijo pasa de
~128 px a **~178 px**, y quedan **~460 px de contenido**. Consecuencia concreta a verificar al
escribir la pantalla 3: con el afiche a 60 vh, el título aterriza justo en el pliegue.

### El menú principal (celular)

**Disparador:** botón en la cabecera, a la izquierda, con **ícono hamburguesa + la palabra
"Menú"** — la etiqueta va escrita, no es sólo el ícono. Con el panel abierto, el mismo disparador
pasa a decir **"Cerrar"** con la cruz.

**Forma:** panel a pantalla completa en un **`<dialog>` nativo**, con las mismas reglas que la hoja
del gesto (D73, y la sección "Diálogos y hojas" de acá abajo): captura de foco, `Esc` y clic afuera
cierran, y **abrir empuja una entrada al historial para que "atrás" cierre** en vez de sacar al
usuario de la pantalla. **No entra desplazándose**: la única animación de posición del sistema
sigue siendo la hoja del gesto (D79).

**Contenido con sesión**, agrupado por categoría:

| Categoría | Ítems |
|---|---|
| **Vos** | Mi diario · Sugerir una obra · Salir |
| **Descubrir** | En cartel · Buscar |
| **El proyecto** | Código (AGPL, D46) |
| **Panel** — sólo si `rol === "ADMIN"` | Sugerencias · Reportes · Producciones · Salas · Personas |

**Sin sesión el menú tiene dos ítems y ninguna categoría: Entrar** y **Código (AGPL)**. **"Crear
cuenta" no va**: ya es el botón persistente, y repetirlo sería un segundo CTA de adquisición, que
el criterio transversal de las pantallas 1, 3, 4, 6 y 8 prohíbe (la sección que sigue).

⚠️ **La sección del panel no lleva contadores**, ni "7 sugerencias" ni "2 reportes": el contrato de
`API.md` no los devuelve. Si más adelante se quieren, es trabajo de backend y se anota como
pendiente; **no se resuelve en el cliente pidiendo las dos colas para contar filas** (D34).

**Que "Mi diario", "En cartel" y "Buscar" estén en la barra y también en el menú es a propósito**
(D81): **la barra es para el pulgar dentro de una sesión y el menú es el mapa completo del
producto.** El que recién llega abre el menú y ve todo; el intensivo no lo abre nunca y llega a lo
suyo en un toque. El costo asumido es el otro lado: los tres ítems que **sólo** viven en el menú
—Sugerir, Salir, Código— son menos descubribles. Ninguno es parte del camino feliz, y a "Sugerir"
se llega igual por su camino natural: la última opción del autocompletado del gesto, que está
siempre (D7/D24, pantalla 9).

**En ≥`md` no hay ni menú principal ni barra de destinos**, y conviene que quede sin ambigüedad:
la cabecera ancha ya tiene el buscador desplegado, el menú de cuenta y el botón de registrar, así
que **los destinos van en la cabecera** y no hay nada que plegar. El bloque inferior tampoco
existe: el botón persistente vuelve a la cabecera, a la derecha, como ya decía la tabla.

### El CTA de crear cuenta en pantallas públicas

Criterio transversal de las pantallas **1, 3, 4, 6 y 8** (hueco 2 de USER_FLOWS). Se resuelve
**con el botón persistente y nada más**: sin sesión ya dice "Crear tu diario" y está fijo abajo en
el celular. **No hay banners de registro adicionales, ni interstitials, ni un segundo CTA al
final del scroll**: el visitante que sólo mira es la mitad del Flujo 1 y molestarlo es perderlo.

### Diálogos y hojas

Todo lo que se superpone —la hoja del gesto, el `Confirmacion` de `DESIGN_SYSTEM.md` y el menú
principal del armazón (D81)— usa el
elemento **`<dialog>` nativo**: trae la captura de foco, el `Esc`, el fondo inerte y la capa
superior sin una línea de JavaScript ni una dependencia (D73). Además:

- **Abrir empuja una entrada al historial y el botón "atrás" cierra** (`pushState` + `popstate`).
  En un celular, "atrás" quiere decir "cerrá esto", y sin esto el usuario se va de la pantalla
  creyendo que cierra la hoja.
- El clic afuera y `Esc` **cancelan, nunca confirman**.

### Errores: qué pinta cada código

La tabla completa está en `FRONTEND_ARCHITECTURE.md` y `client.ts` aplana las tres familias de
`API.md` en un solo tipo. Acá sólo la traducción a componentes:

| Situación | Componente |
|---|---|
| `400` con `errores` | texto de error **debajo de cada input**, en `peligro-tinta`, `text-sm` |
| `400`/`409` sin `errores` (admin, carrera del alta) | `Aviso` variante `error` **arriba del formulario** |
| `401` de "¿hay alguien?" | nada: se dibuja la versión sin sesión |
| `401` de acción protegida | ir a `/login?volver=<url>`; al volver, **se restaura lo tipeado** |
| `403` de CSRF | invisible: se relee la cookie y se reintenta **una** vez (D78) |
| `404` de ruta pública | `EstadoVacio` variante `no-encontrado`, con búsqueda embebida |
| `404` del gesto | **no es error**: es el camino a sugerir |
| `409` de las colas | `Aviso` info + refrescar la cola |
| `5xx` | `EstadoVacio` variante `error` con "Reintentar", **sin perder lo tipeado** |

### El borrador que sobrevive

USER_FLOWS lo pide dos veces ("el contenido tipeado NO se pierde", "vuelve a donde estaba con lo
tipeado"). Se resuelve así: **los dos formularios que importan —el gesto y la sugerencia— guardan
su borrador en `sessionStorage` mientras se escribe, y lo restauran al montarse.** Se borra al
publicar con éxito.

Es una API del navegador, no una herramienta (D51), y **no contradice lo que D79 descartó para el
tema**: aquel caso necesitaba leerse *antes del primer pintado* para evitar un destello, que con
SSR produce desajustes de hidratación; éste se lee después de montar, no afecta el primer pintado
y su peor caso es un campo que se completa 50 ms tarde.

### Metadatos y Open Graph

Cuatro pantallas compartibles con `generateMetadata` (ADR-003, P13): **ficha, perfil, artista y
sala**. En todas: `title`, `description`, `og:title`, `og:description`, `og:image`,
`og:type=website`, `twitter:card=summary_large_image` y **`canonical` con la URL de D74** (que es
la mitad de para qué existe el `308`).

`og:image`: el afiche tal cual cuando existe ⏳, y si no la placa tipográfica de 1200×630, siempre
oscura (D79). El perfil y el artista **no tienen imagen propia**: usan la placa con el username o
el nombre. **Sin `og:image` no se cae nada**: el preview queda con título y descripción, que es
degradado y no roto.

### Accesibilidad, en las 13

`lang="es"` · un solo `<h1>` por pantalla · listas reales (`<ul>`/`<ol>`) para listas · el foco
visible de `DESIGN_SYSTEM.md`, que no se quita nunca · los estados que llegan por color llevan
además texto (el chip dice "EN CARTEL", no es sólo ámbar) · `aria-live="polite"` en los resultados
del autocompletado y en los contadores que cambian solos · toda imagen con `alt` (el del afiche es
`Afiche de {título}`; **la placa es decorativa sólo donde el título está al lado como texto —la
miniatura de una fila—, y NO en la celda de la grilla**, donde con D86 el título vive adentro de
la placa y ocultarla deja el link de la celda sin nombre accesible).

---

## 1 · Home visitante — `/`

| | |
|---|---|
| Historias | HU-06, y es la puerta del Flujo 1 |
| Render | Server Component |
| Datos | `catalogo.enCartel()` → `GET /api/en-cartel` · `apiPublic`, `revalidate: 300` |
| Islas cliente | ninguna |
| OG | sí (genérico del sitio) |

**Composición**
1. Una frase de qué es esto, en serif, `text-3xl`. **Sin nombre de producto** (P2): la frase
   funciona sola y el día que P2 cierre se le pone el logotipo encima sin tocar el resto.
2. Una línea de subtítulo con la promesa honesta de CORE_LOOP: "el diario de tu teatro de acá en
   adelante".
3. **"En cartel ahora"**: las primeras **6** celdas de `enCartel`, con la grilla de la pantalla 6
   (2 columnas / 3 en `sm`) + link "Ver todo lo que está en cartel".
4. Nada más. Sin sección de reseñas destacadas (habría que elegirlas y no hay con qué, X2), sin
   contadores de usuarios (a esta escala mienten).

**Estados**

| | |
|---|---|
| Cargando | es SSR: no hay estado de carga que dibujar |
| Vacío | `enCartel` vacío (catálogo recién nacido) → la grilla desaparece entera y queda el titular. **No se muestra un `EstadoVacio`**: el visitante no vino a resolver un vacío nuestro |
| Error | `5xx` → la sección de en cartel no se dibuja; el titular y el CTA sí. Una portada rota es peor que una portada corta |
| Sin sesión | **es la pantalla del sin sesión.** Con cookie, esta ruta es la pantalla 2 |

**Criterios**: carga sin cookie, con caché de 300 s · el CTA "Crear tu diario" es visible sin
scrollear en un celular de 360×640 · desde acá se llega a una ficha en un toque.

---

## 2 · Home logueada (feed) — `/`

| | |
|---|---|
| Historias | HU-16, HU-17, HU-18, HU-11 |
| Render | Server Component (primera página) + isla cliente para el resto |
| Datos | `social.feed()` → `GET /api/feed?tamanio=20` · **`apiSession`, `no-store` siempre** (exige sesión, D66/D78) |
| Islas cliente | lista paginada, botones de like, menú de cada ítem |
| OG | no |

**Composición**
1. `Aviso` variante `info` **si `global: true`**: "Estás viendo toda la plataforma. Seguí gente
   para armar tu feed" (D22). Va **arriba de todo y no se puede cerrar** (D79).
2. Lista de `Fila` variante `feed`, una por ítem: `Usuario` + título de la obra (link salvo
   `enCatalogo: false`) + fecha difusa + `Puntaje` + reseña + acciones.
3. **Acciones de cada ítem**: corazón con contador (`Boton` alternante) y, en un menú de tres
   puntos, "Reportar" si es ajena, o "Editar" y "Borrar" si es propia.
4. Al final, el **sentinela de paginado**: un `Boton` secundario "Cargar más" que un
   `IntersectionObserver` toca solo al entrar en pantalla. Es un botón de verdad y no un div
   observado, así que **si el observador falla o no corre, la paginación sigue siendo usable** —
   y no hace falta escribir dos caminos.

**Estados**

| | |
|---|---|
| Cargando | primera página: SSR. Siguientes: tres `Fila` en esqueleto debajo de las que ya están |
| Vacío | `items: []` **con `global: true`** → `EstadoVacio` informativo: no hay actividad en ningún lado todavía, con CTA a registrar. `items: []` **con `global: false`** → `EstadoVacio` **distinto**: "los que seguís no registraron nada", con CTA a buscar gente. Son dos cosas distintas y se dicen distinto (D66) |
| Error | primera página `5xx` → `EstadoVacio` error con "Reintentar". Página siguiente → `Aviso` error al pie, y las filas que ya están **no se tocan** |
| Sin sesión | no existe: con cookie esta ruta es esta pantalla, sin cookie es la 1 |

⚠️ **Dos cosas del feed que se rompen si no se leen de `API.md`:** la condición de corte es
`siguienteCursor: null`, y **una última página con `items: []` es normal** (pasa cuando la
anterior entró justa) — se trata como fin de lista, sin error y sin hueco. Y un **ítem sin
reseña es legítimo**: fila de una sola línea, sin corazón y sin reportar (`leDiLike` viene
`null`), porque esa persona igual fue al teatro (D70).

**Criterios**: HU-16 ✔ descendente por `creadoEn` · el aviso del fallback sale de la respuesta y
no lo adivina la pantalla · like sin recargar (optimista, revierte si falla) · borrar propio pide
`Confirmacion`.

---

## 3 · Ficha de producción — `/obra/{id}-{slug}` ⚖️

**La pantalla más importante del producto.** Es el destino del Flujo 1 y la que HU-04 mide con un
test literal: pegar el link en WhatsApp.

| | |
|---|---|
| Historias | HU-04, HU-14, HU-10, HU-17, HU-18 |
| Render | Server Component + islas |
| Datos | **dos llamadas** (D60): `catalogo.ficha(id)` → `GET /api/producciones/{id}` y `diario.opiniones(id)` → `GET /api/producciones/{id}/opiniones` |
| Cliente | ⚖️ sin cookie: `apiPublic`, `revalidate: 60`. Con cookie: **las dos** por `apiSession`, `no-store` |
| Islas cliente | corazón de cada reseña, menú de reportar, botón que abre el gesto |
| OG | **sí, crítico** |

**Antes de llamar a nada**: `idDesdeSlug()` con la regex de D74; si no es un entero positivo,
`notFound()`. Si el slug no coincide con el del título que volvió, `permanentRedirect()` (308) a
la forma canónica.

**Composición** (celular, de arriba hacia abajo)
1. **Cabecera.** Con afiche ⏳: imagen full-width (`contain`, alto máximo 60 vh) y debajo el
   título en `text-3xl` serif. **Sin afiche: el título arriba, más grande, y la ficha en una
   columna — no hay placa** (D79: la ficha cambia de forma, no deja un hueco). En ≥`md` con
   afiche, dos columnas.
2. **Chip de estado** (D8) + sala con link a la pantalla 5 (si `sala !== null`).
3. **Bloque de opiniones**: `Puntaje` variante `promedio` — "8,4 /10 · 17 personas puntuaron" — y
   al lado el CTA del gesto:
   - `vecesQueLaVi: null` ⏳ → nada (no hay sesión).
   - `0` → `Boton` primario "Registrar que la vi".
   - `N ≥ 1` → "La viste N veces" + `Boton` secundario "Registrar de nuevo" (D19: el re-visto se
     ofrece, no se esconde).
4. **Sinopsis** (`max-w-[65ch]`), y obra original / autor (D13). Cada línea que viene `null`
   **desaparece con su etiqueta**.
5. **Elenco y equipo**, agrupado **por persona** —la API las manda una por rol (D17) y agrupar es
   presentación, no negocio— con los roles como `Chip` y link a la pantalla 4.
6. **Reseñas**: `Fila` variante `feed` sin el título de la obra (ya estamos en ella), ordenadas
   como vienen. Cada una con `Usuario`, fecha difusa, `Puntaje`, texto, corazón y reportar.

**Estados**

| | |
|---|---|
| Cargando | SSR. Con sesión, `no-store`, así que la pantalla llega entera |
| Vacío | sin reseñas → `EstadoVacio` informativo bajo el encabezado "Reseñas". `promedio: null` → "Todavía nadie puntuó", **nunca "0"** |
| Error | **la degradación es por bloque:** si falla `opiniones` pero la ficha vino, **la ficha se muestra igual** y el bloque de opiniones lleva su `EstadoVacio` error con "Reintentar". Si falla la ficha, `404` → `no-encontrado`; `5xx` → error de pantalla |
| Sin sesión | sin corazones ni reportar (`leDiLike: null`), sin CTA de registrar (`vecesQueLaVi: null`); el promedio, las reseñas y los contadores de likes **sí** se ven (D21) |

⚠️ **Que la ficha sobreviva a que fallen las opiniones no es un lujo**: son dos llamadas a dos
módulos distintos (D60) y la mitad que sostiene el Flujo 1 —título, afiche, sinopsis, el preview
de WhatsApp— es la primera.

**Criterios**: HU-04 ✔ todos los campos, con SSR y OG · **el test literal: pegar el link en
WhatsApp muestra título + afiche + sinopsis** · HU-14 ✔ promedio con un decimal y coma decimal ·
HU-10 ⏳ la ficha evidencia que la viste N veces · una ficha sin afiche, sin sala, sin sinopsis y
sin reseñas **se sigue viendo terminada**.

---

## 4 · Página de artista — `/artista/{id}-{slug}`

| | |
|---|---|
| Historias | HU-05 |
| Render | Server Component |
| Datos | `catalogo.persona(id)` → `GET /api/personas/{id}` · `apiPublic`, `revalidate: 300` |
| Islas cliente | ninguna |
| OG | sí (placa con el nombre) |

**Composición**: nombre en `text-3xl` serif · las participaciones **agrupadas por rol** (Dirección,
Dramaturgia, Actuación), cada una como `Fila` variante `resultado` con miniatura ⏳, título, chip
de estado y link a la ficha. Nada más: D14 dice sin foto y sin bio, y esta pantalla **no inventa
un lugar donde ponerlas**.

**Estados**: *cargando* SSR · *vacío* — una persona sin participaciones es raro pero posible
(quedó suelta tras una fusión): `EstadoVacio` informativo · *error* `404` → `no-encontrado` ·
*sin sesión* — idéntica, no hay nada personalizado.

**Criterios**: HU-05 ✔ participaciones etiquetadas por rol con links · indexable · una persona
con 40 participaciones se lee sin paginar (no hay paginado y no hace falta a esta escala).

---

## 5 · Página de sala — `/sala/{id}-{slug}`

| | |
|---|---|
| Historias | HU-04 (link), hueco 1 de USER_FLOWS |
| Render | Server Component |
| Datos | `catalogo.sala(id)` → `GET /api/salas/{id}` · `apiPublic`, `revalidate: 300` |
| OG | sí |

**Composición**: nombre + complejo (si existe) · **"Ahora en esta sala"**: la grilla de `enCartel`
con `Tarjeta` variante `grilla`. Y se termina: no hay dirección, ni mapa, ni horarios — el
contrato no los trae y pedirlos sería una agenda (X4, P6).

**Estados**: *vacío* — sala sin nada en cartel, que es **el caso normal la mitad del año**:
`EstadoVacio` informativo, "no hay funciones cargadas en esta sala ahora" · *error* `404` →
`no-encontrado` · resto, igual que la 4.

---

## 6 · En cartel — `/en-cartel`

| | |
|---|---|
| Historias | HU-06 |
| Render | Server Component |
| Datos | `catalogo.enCartel()` → `GET /api/en-cartel` · `apiPublic`, `revalidate: 300` |
| OG | sí |

**Composición**: `h1` "En cartel" · grilla de `Tarjeta` variante `grilla` (**2 columnas / 3 en
`sm` / 4 en `lg`**), cada celda con afiche o placa ⏳, título y sala — **el título una sola vez:
con afiche debajo de la imagen, sin afiche adentro de la placa y nada más** (D86) · después, encabezado
`text-xl` **"Próximamente"** y la misma grilla, con su chip. **Sin filtros, sin calendario, sin
barrio, sin orden configurable** (D79): el orden lo da la API y el primer filtro es la primera
pieza de una agenda.

**Estados**: *vacío* — las dos listas vacías: `EstadoVacio` informativo; sólo `proximamente`
vacía: la sección desaparece entera, sin cartel · *error* `5xx` → `EstadoVacio` error.

**Criterio propio de esta pantalla** (D79): **una grilla con la mitad de las celdas sin afiche
tiene que leerse como una decisión.** Se mide al cargar las ~50 fichas de D38; si se lee "faltan
imágenes", la salida ya está escrita: convertir la grilla en una lista de `Fila`.

---

## 7 · Búsqueda — `/buscar?q=`

| | |
|---|---|
| Historias | HU-07 → HU-08 |
| Render | **cliente** (D78) |
| Datos | **tres llamadas en paralelo** (D65): `/api/buscar/producciones`, `/personas`, `/usuarios` · `apiPublic` |
| OG | no |

**Composición**: campo de búsqueda con el valor de `?q=` y foco al entrar · tres secciones en
este orden —**Producciones**, **Personas**, **Usuarios**—, cada una con hasta 10 `Fila` variante
`resultado` (tope de D65, sin paginado) · la URL se sincroniza con lo tipeado
(`replaceState`, con la misma espera de 250 ms que el autocompletado) para que un resultado sea
compartible.

**Estados**

| | |
|---|---|
| Cargando | tres bloques de esqueleto, independientes: cada sección aparece cuando llega la suya |
| Vacío | **`q` vacío** → no se llama a nada (la API devolvería `[]` igual) y se muestra una línea tenue de ayuda. **Sin resultados en producciones** → `EstadoVacio` variante `sin-resultados` con lo tipeado entre comillas y `Boton` primario **«Sugerir "hamllet"»**. **Sin resultados en personas o usuarios** → una línea tenue y nada más: **no hay adónde derivar** y ofrecer "sugerir una persona" sería inventar un endpoint |
| Error | por sección: la que falló muestra su `Aviso` error, las otras dos se dibujan |
| Sin sesión | el botón de sugerir se ve igual; al tocarlo, `/sugerir` manda a login y **vuelve con lo tipeado** |

**Criterios**: HU-07 ✔ los tres tipos, con typos (`pg_trgm`) · el resultado vacío de producciones
ofrece el camino a HU-08 · **`[]` no es un error**: nunca una pantalla de error acá.

---

## 8 · Perfil / diario — `/usuario/{username}` ⚖️

| | |
|---|---|
| Historias | HU-03, HU-12, HU-13, HU-15, HU-11 |
| Render | Server Component + islas |
| Datos | **una sola llamada**: `diario.perfil(username)` → `GET /api/usuarios/{username}` |
| Cliente | ⚖️ sin cookie `apiPublic` `revalidate: 60`; con cookie `apiSession` `no-store` (trae `loSigo`) |
| Islas cliente | botón de seguir, menú de cada registro propio |
| OG | **sí, crítico** (es la carta de presentación del Flujo 1) |

**Composición**
1. **Cabecera**: `Usuario` variante `cabecera` (monograma + username `text-3xl`) · "en la
   plataforma desde {mes de `creadoEn`}" · `Boton` alternante **"Seguir"/"Siguiendo"** — **sólo
   si `loSigo !== null`**: `null` es el perfil propio o el anónimo (D67).
2. **Contadores**: seguidores · seguidos · total de registros. Texto, no tarjetas.
3. **Estadísticas** (HU-13/D26): total de obras distintas, promedio propio con un decimal, y
   obras por año como **lista de barras hechas con un `div` de ancho porcentual** — no entra
   ninguna librería de gráficos (D51/D73).
   ⚠️ **Con menos de 2 registros el bloque no se dibuja** (USER_FLOWS lo pide: "se ocultan o
   versión mínima, no un dashboard vacío"), y `registrosSinFecha` sólo se menciona si es > 0.
4. **Diario**: `registros` como `Fila` variante `diario`, descendente, agrupadas por año con un
   encabezado pegajoso.
5. **Sin fecha**: `sinFecha` en **su propia sección al final**, con encabezado propio (MD-2).
   **No se mezclan ni se les inventa fecha.**

**Estados**

| | |
|---|---|
| Cargando | SSR |
| Vacío | diario vacío → `EstadoVacio` variante `invitacion`. **Si es el perfil propio**: "Todavía no registraste nada" + `Boton` primario que abre el gesto. **Si es ajeno**: "Todavía no registró nada", sin CTA — el CTA de otro no es tuyo |
| Error | `404` (username inexistente) → `no-encontrado` con búsqueda embebida · `5xx` → error |
| Sin sesión | sin botón de seguir, sin menús de editar/borrar. Todo lo demás **se ve igual** (D21) |

**Criterios**: HU-03 ✔ SSR + OG, sin login · HU-12 ✔ descendente, con la sección de sin fecha
aparte · HU-13 ✔ los tres números y nada más · HU-15 ✔ el botón sólo donde corresponde · editar
y borrar sólo en el propio, y borrar con `Confirmacion`.

⚠️ **El perfil no pagina** (hueco 4 de `API.md`): trae el diario entero. A la escala del MVP
alcanza; la pantalla no simula un paginado que el contrato no tiene.

---

## 9 · El gesto de registro — sin ruta propia

**Esta pantalla ES el producto** (Flujo 3). El criterio no es que esté completa: es que el camino
feliz entre en **menos de un minuto** en un celular a las 23:30 (P8).

| | |
|---|---|
| Historias | HU-09, HU-10, HU-11 |
| Render | **isla cliente** dentro de un `<dialog>` |
| Datos | `catalogo.buscarProducciones(q)` mientras se escribe · `diario.crear()` → `POST /api/registros` · `diario.editar(id)` → `PUT` · `diario.borrar(id)` → `DELETE` |
| OG | no |

**Por qué hoja y no ruta.** El árbol de rutas de D78 no tiene `/registrar` y no hace falta
agregarlo: el gesto se abre desde **tres lugares** —el botón persistente, el CTA de la ficha y
"Registrar de nuevo"— y en dos de ellos **el contexto de atrás importa** (estás en la ficha de la
obra que vas a registrar). Una ruta obligaría a cargar de nuevo la pantalla al volver. Lo que la
ruta daba gratis —el botón "atrás"— lo da la regla de `<dialog>` de arriba.

**Forma**: hoja desde abajo en el celular (200 ms, la única animación de posición del sistema),
modal centrado en ≥`md`.

**Los cuatro pasos, en una sola pantalla y en este orden**
1. **Obra** — campo con autocompletado. Espera de **250 ms** desde la última tecla, **mínimo 2
   caracteres**, hasta 10 resultados (D65). Cada opción: título, año o sala, chip de estado.
   Flechas y `Enter` navegan; `aria-live` anuncia cuántos hay.
   ⚠️ **La última opción de la lista es siempre "No está en el catálogo → sugerirla", con
   resultados o sin ellos.** Esperar a la lista vacía no alcanza: con `pg_trgm` casi siempre
   vuelve *algo*, y el caso real no es "no hay nada" sino "ninguno de estos es el mío" (D7/D24).
2. **Fecha** — **hoy por defecto, un toque** (`granularidad: DIA`). Debajo, tres alternativas en
   línea: "mes", "año", "no me acuerdo" (MD-1). Elegir una cambia qué selectores se ven; con
   `SIN_FECHA`, `fecha` va `null`.
3. **Puntaje** — **fila de 10 objetivos** de 44 px de alto (≥32 de ancho en 360 px: cumple WCAG
   2.5.8 AA), numerados 1 a 10. Es opcional: tocar el elegido otra vez lo saca. **Sin estrellas,
   sin medios puntos** (D9). Se descartaron dos filas de cinco: duplican el escaneo y el número
   deja de leerse como una escala.
4. **Reseña** — textarea opcional, `text-base`, contador **sólo a partir de 4500** de los 5000.

Abajo, un solo `Boton` primario: **"Publicar"** (en edición, "Guardar").

**Estados**

| | |
|---|---|
| Cargando | del autocompletado: spinner chico dentro del campo, sin bloquear lo tipeado. Del envío: el botón pasa a "Publicando…" y se deshabilita |
| Vacío | autocompletado sin resultados: sólo queda la opción de sugerir, que ya estaba |
| Error | `400` con `errores` → debajo del campo · **`404` → NO es error**: la obra dejó de existir entre que se eligió y se publicó; se abre el camino a sugerir con el título ya cargado · `403` de dueño (no debería pasar) → `Aviso` · `5xx` → `Aviso` error **y la hoja no se cierra**, con todo lo tipeado adentro |
| Sin sesión | no se llega: el botón persistente del anónimo va a `/registro`. Si la **sesión venció** en el medio, el `401` manda a `/login?volver=…`, y al volver **la hoja se reabre con el borrador de `sessionStorage`** |

**Editar y borrar (HU-11)**: el mismo formulario, precargado con los datos que la pantalla ya
tiene del diario o del feed —**no hay `GET` de un registro suelto** (hueco 6 de `API.md`)— y
editar **reemplaza el gesto entero, incluida la obra**, porque equivocarse de obra al elegirla es
el error más probable. Borrar abre `Confirmacion` variante `peligro`, con el título de la obra en
el texto y el botón que dice **"Borrar registro"**.

**Criterios**: HU-09 ✔ **el camino feliz —abrir, buscar, tocar la obra, aceptar la fecha de hoy,
tocar un número, publicar— son seis toques y ningún tipeo más que el título** · HU-10 ✔ registrar
la misma obra de nuevo no avisa de duplicado ni pide confirmación (D19) · HU-11 ✔ editar y borrar
con confirmación · el error de servidor no pierde nada.

---

## 10 · Sugerir producción — `/sugerir`

| | |
|---|---|
| Historias | HU-08 |
| Render | cliente |
| Datos | `catalogo.sugerir()` → `POST /api/sugerencias` (**pide sesión**) |
| OG | no |

Segunda de las tres cosas que D71 mandó diseñar a mano. **Ninguna referencia la tiene.**

**Composición**: `h1` "Sugerir una obra" · **título ya cargado** con lo que se venía tipeando
(por `?titulo=` o por el borrador), y el foco **en el campo siguiente**, no en el primero · debajo,
un encabezado tenue "si te acordás, ayuda — todo esto es opcional" y los cuatro campos opcionales:
sala, año, elenco, comentario · `Boton` primario "Enviar".

**La confirmación es una pantalla, no un cartel que se va.** Reemplaza el formulario, muestra lo
que se envió —que es literalmente lo que devuelve la API (D69: la respuesta *es* la confirmación)—
y dice la expectativa sin adornarla:

> **Lo recibimos.** El catálogo lo revisa una persona, así que puede tardar. **No te vamos a avisar
> cuando entre**: cuando esté, vas a poder buscarla y registrarla.

Dos salidas: "Volver a lo que estaba haciendo" y "Sugerir otra". **Lo que no va** (MD-3): un
estado "pendiente" consultable —no existe el endpoint y no habría qué mostrar—, una barra de
progreso, un tilde verde, y cualquier forma de "te avisamos".

**Estados**: *cargando* botón en "Enviando…" · *vacío* no aplica · *error* `400` con `errores`
por campo (sugerencia es uno de los cuatro endpoints que sí los manda) y `5xx` con `Aviso`
**sin vaciar el formulario** · *sin sesión* el `401` manda a `/login?volver=/sugerir` y **vuelve
con todo lo tipeado**.

**Criterios**: HU-08 ✔ sólo el título es obligatorio · se llega desde la búsqueda y desde el gesto
**sin volver a escribir el título** · la confirmación no promete un aviso.

---

## 11 · Alta de cuenta y login — `/registro`, `/login`

| | |
|---|---|
| Historias | HU-01, HU-02 |
| Render | cliente |
| Datos | `POST /api/auth/registro` · `POST /api/auth/login` · `POST /api/auth/logout` |
| OG | no |

**Alta**: username, email, contraseña. Reglas del campo, todas de `API.md`: username 3-20 y
`[A-Za-z0-9_]`, con la advertencia de que **es parte de tu URL y no se puede cambiar** (MD-4/D75)
escrita **antes** de enviar, no después · **el contador de la contraseña cuenta bytes UTF-8, no
caracteres** (72 es el límite real de BCrypt: 40 letras con tilde ya son 80 bytes), así que el
error no aparece recién al enviar.

⚠️ **El `409` tiene dos formas y las dos se dibujan** (`API.md`): con `errores` → al lado del
campo; **sin `errores`** (la carrera que resuelve el índice único) → `Aviso` error arriba, con el
`detail` que vino. **En los dos casos el formulario se queda como está**: no se refresca ni se
vacía, porque lo que hay que cambiar es un dato.

**Login**: un solo campo `identificador` (email **o** username, y la etiqueta lo dice) +
contraseña. El `401` es **un mensaje genérico** —"Email/usuario o contraseña incorrectos"— y
nunca dice cuál de los dos falló (HU-02).

**Después de las tres puertas** —registro, login y logout— **hay que releer la cookie
`XSRF-TOKEN`: el token se rota en las tres** (D57). Lo hace `client.ts`, pero la pantalla no puede
disparar una mutación encadenada asumiendo el token viejo.

**Volver a donde estaba**: las dos leen `?volver=` y navegan ahí al terminar; sin parámetro, a la
home. **Sólo se aceptan rutas relativas del propio sitio** —un `?volver=https://…` es un redirect
abierto—, y el gesto además recupera su borrador al llegar.

**Estados**: *cargando* botón en "Creando cuenta…"/"Entrando…" · *error* los de arriba · *sin
sesión* es su público; **con sesión, las dos redirigen a la home**: no se muestra un login a
alguien que ya entró · el botón de salir **no tiene camino de error**: el logout responde `204`
siempre, con sesión o sin ella (`API.md`).

**Criterios**: HU-01 ✔ errores claros por campo y al completar quedás logueado y en la home ·
HU-02 ✔ mensaje genérico · el visitante que llega por un link compartido y crea una cuenta **no se
come un `403` de CSRF** (la semilla perezosa de D78).

---

## 12 · Panel de admin — `/admin/...`

No es producto, es **herramienta** (Flujo 4). Se diseña para minimizar clics, no para verse bien:
`max-w-7xl`, `Boton` tamaño `sm`, `Fila` variante `admin`, densidad alta. **Todo cliente, todo
`apiSession`, todo `no-store`.** Se entra sólo con `rol === "ADMIN"`; con sesión sin rol, `403` →
pantalla de "no podés".

⚠️ **Ningún formulario del panel recibe `errores` por campo** (`API.md`): un `400` es "revisá el
formulario" a secas, con `Aviso` error arriba. **La validación fina la hace el cliente** y el
`400` es la red de contención.

### 12a · Salas y Personas (HU-19, HU-20)

Listado completo sin paginar + alta/edición en línea + borrar. `409` al borrar algo referenciado →
`Aviso` error con el `detail` tal cual, **que ya viene accionable** ("reasignalas antes de
borrarla"). Borrar pide `Confirmacion`.

### 12b · Producciones (HU-20)

**Listado** con `?estado=EN_CARTEL` por defecto, que es lo que hace trivial el barrido semanal:
cada fila tiene **el cambio de estado en un clic** (`PATCH /{id}/estado`), **sin salir del
listado y sin confirmación** — es reversible en otro clic y pedir confirmación acá cuesta más de
lo que protege.

**Formulario** (alta y edición): título, sinopsis, obra original, autor, estado, sala, y
**participaciones con buscar-o-crear inline** (D14): un campo que busca personas y, si no hay,
ofrece "crear «Nombre»" — ⚠️ **`personaId` y `nombrePersona` son excluyentes** (`API.md`), así
que la fila guarda uno u otro y nunca los dos. `409` si se repite persona+rol → se marca la fila
que repite.

**Afiche** (D77/D88): en el formulario, un campo de archivo con vista previa local, **límite de
5 MB avisado antes de subir** (`413` si igual pasa), y un botón de quitar que llama al `DELETE`.
**El endpoint ya existe** —P16 se cerró en D88— y acepta JPEG, PNG y WebP; lo que se guarda es
siempre un JPEG, así que la URL termina en `.jpg`.

**Fusionar** (D63): acción de la fila → elegir la ficha destino con el buscador → `Confirmacion`
variante `peligro` que dice **cuántos registros se van a mudar y que la ficha se borra**, con el
botón "Fusionar en esta ficha". Es irreversible y la confirmación lo dice con esas palabras.

### 12c · Cola de sugerencias (HU-21)

Lista de la más vieja a la más nueva, con todo lo que trajo el sugerente. Dos puertas:
**"Aprobar"** abre el formulario de producciones **precargado con lo sugerido** y recién al
guardar la ficha se llama a `aprobar` con el `produccionId` — **si el admin abandona el
formulario, la sugerencia sigue en la cola** (D69), y la pantalla no la saca antes de tiempo.
**"Rechazar"** pide un motivo obligatorio (máx. 500) en un diálogo. `sugerente: null` → "cuenta
eliminada".

### 12d · Cola de reportes (HU-22)

Lista con el contexto ya compuesto. ⚠️ **Las tres formas de la fila** (`API.md`), y el
discriminante es **`produccion`, no `autor` ni `rating`**:

| Qué pasó | Cómo se dibuja |
|---|---|
| Reseña viva | texto, autor, obra, puntaje, motivo, reportante |
| Texto borrado (`texto: null`, `produccion` presente) | "el texto ya no está" en tenue; **las dos acciones siguen** |
| Registro entero borrado (`produccion: null`) | fila mínima, "el registro completo ya no está"; **las dos acciones siguen** |

Es la única forma de vaciar la cola. Dos puertas: **"Borrar reseña"** (con `Confirmacion`, porque
borra contenido ajeno) y **"Desestimar"** (directo). Las dos alcanzan a **todos** los reportes
pendientes de esa reseña (D70), así que después de resolver **se refresca la cola entera** y no se
saca una fila a mano.

**El `409` de las dos colas no es un error roto**: otra pestaña ya lo resolvió. `Aviso` info +
refrescar y seguir.

---

## 13 · 404 y error genérico

| Archivo | Cuándo | Qué muestra |
|---|---|---|
| `app/not-found.tsx` | `notFound()`: id inválido (D74), ficha/perfil/artista/sala inexistente | `EstadoVacio` variante `no-encontrado`, **con el buscador embebido** (USER_FLOWS) y links a en cartel y a la home |
| `app/error.tsx` | cualquier excepción no atrapada | `EstadoVacio` variante `error` + "Reintentar" (`reset()`). **Sin stack trace, sin código interno** |

Las dos llevan el armazón completo: cabecera, botón persistente y pie. Una 404 sin salidas es una
pantalla muerta al final del Flujo 1. **Y las dos llevan su título en `<h1>`**: son la pantalla, no un
bloque adentro de una, y la regla de accesibilidad pide uno por pantalla.

⚠️ **Limitación medida y anotada como P18**: el `notFound()` de una ficha, un artista o una sala
—y el `error.tsx`— **sólo se pintan con JavaScript**. El estado HTTP es correcto y con JS se ve la
pantalla entera; lo que se pierde es la 404 sin JavaScript. El 404 global (`/cualquier-cosa`) **sí
llega en el HTML**.

---

## Cobertura: las 22 historias contra las pantallas

Verificación en los dos sentidos (la regla de USER_FLOWS: si un flujo pasa por una pantalla que
ninguna historia construye, hay un hueco).

| HU | Pantallas | Estado |
|---|---|---|
| HU-01 · HU-02 | 11, + el menú de cuenta del armazón en ≥`md` y **el menú principal en el celular** (D81: ahí vive "Entrar" y "Salir") | completa |
| HU-03 | 8 | completa |
| HU-04 | 3, + 5 (link a sala) | completa del lado del backend: el afiche existe (D77/D88); falta la pantalla |
| HU-05 | 4 | completa |
| HU-06 | 6, + 1 | completa |
| HU-07 | 7, + el autocompletado de 9 | completa |
| HU-08 | 10, entrada desde 7 y desde 9 | completa |
| HU-09 | 9 | completa |
| HU-10 | 9 + el CTA de 3 | completa del lado del backend: `vecesQueLaVi` existe (D76); falta la pantalla |
| HU-11 | 9 (edición) + menús de 2 y 8 | completa |
| HU-12 · HU-13 | 8 | completa |
| HU-14 | 3 | completa |
| HU-15 | 8 | completa |
| HU-16 | 2 | completa |
| HU-17 · HU-18 | 2 y 3 | completa |
| HU-19 | 12a | completa |
| HU-20 | 12b | ⏳ la subida de afiche espera P16 |
| HU-21 | 12c | completa |
| HU-22 | 12d | completa |

**Las 13 pantallas de USER_FLOWS tienen entrada y ninguna quedó sin historia.** Lo único que
bloquea el cierre de una historia es backend, no diseño: `vecesQueLaVi` (D76) y los afiches
(D77 + P16).

## Lo que este documento deja anotado y no resuelve

1. **Los dos ⏳.** Las pantallas están especificadas contra el contrato acordado; **no andan hasta
   que el backend las tenga.** Las zonas afectadas están marcadas y ninguna deja un hueco visual
   mientras tanto: sin `aficheUrl` la ficha ya se ve terminada (D79), y sin `vecesQueLaVi` el CTA
   simplemente no se dibuja.
2. ~~**Con qué se genera la placa de `og:image`.**~~ ✅ **Cerrado en D85**, al escribir la ficha:
   `next/og` con un Noto Serif subseteado de 30 KB embebido en `frontend/assets/` —satori no lee
   fuentes del sistema— y una ruta propia, `app/og/{tipo}/{id}`, para que la regla "si hay afiche,
   el `og:image` es el afiche" se pueda escribir. **No era P16 y no lo tocó.**
3. **P16** sigue abierto: herramienta, formato, calidad, EXIF y tope de píxeles decodificados. Los
   tamaños ya los fijó D79.
4. **El `@ControllerAdvice` que falta** (hueco 2 de `API.md`): mientras no exista, la validación
   fina del panel es del cliente. Está en el ROADMAP como trabajo de backend de esta fase.
5. **Lo que cuesta el armazón de cuatro piezas** (D81). En un celular de 360×640 el cromo fijo
   pasa de ~128 px a **~178 px** y quedan **~460 px de contenido**. Está aceptado, pero deja una
   **verificación pendiente y concreta: la ficha (pantalla 3) con el afiche a 60 vh contra el
   cromo nuevo** — con esa altura el título aterriza justo en el pliegue. Se mide al escribir la
   ficha, y si no entra, lo que se ajusta es el máximo del afiche: la barra no se achica por
   debajo de los 48 px ni la etiqueta por debajo de `text-xs`.
