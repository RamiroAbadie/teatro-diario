# Design System

> Estado: v1.3 — el paso 2 de la Fase 4 cierra la pregunta que este documento dejó anotada
> —**con qué se genera la placa de `og:image`**: `next/og` con una Noto Serif subseteada de
> 30 KB embebida en `frontend/assets/`, que **no es una webfont** porque el navegador no la
> descarga nunca (**D85**)— y corrige **la celda de la grilla** contra lo que se vio al
> dibujarla: **el título va una sola vez por celda** y la placa **estira** para que la celda
> más corta no deje un rectángulo vacío (**D86**). Los tokens, la paleta, la escala y los diez
> componentes **no cambian**.
>
> La v1.2 fue el tercero de los cuatro documentos del paso 0 de la Fase 4, después de
> `architecture/API.md` (v1.2) y `architecture/FRONTEND_ARCHITECTURE.md` (v1.3). El que sigue
> es `product/SCREEN_SPECS.md` (v1.2), que cierra el paso.
> Lo respalda **D79**; la v1.1 **D81** (el armazón de cuatro piezas trae los valores de la barra
> de destinos y **sube la lista de íconos de seis a diez**) y la v1.2 dos correcciones del paso 1:
> **D84 da vuelta la regla de voz —los botones van en infinitivo, sin la excepción del panel—**,
> que cierra P17, y **D83 corrige el cuerpo de `Usuario` variante `cabecera`**, que decía
> `text-lg` y va en `text-3xl`/`sm:text-4xl`. Los tokens, la paleta, la escala y los diez
> componentes de `ui/` **no cambian**.
>
> **Qué decide:** los tokens (color, tipografía, espacio, radios, sombras, breakpoints), los
> componentes de `components/ui/`, cómo se dibujan los casos degradados que la API sí devuelve,
> y a qué tamaño se ve un afiche.
> **Qué NO decide:** las pantallas una por una (eso es `SCREEN_SPECS.md`), el código de los
> componentes, y ninguna herramienta nueva — **Tailwind es la única dependencia del frontend**
> (D73) y este documento no agrega ni una.
>
> Baja a pantalla lo que decidió **D71**: identidad visual propia, **a mitad de camino en el eje
> póster↔tipografía** y **feed-first con el botón de registrar siempre presente**.

## Las cinco reglas que gobiernan todo lo de abajo

1. **La fila tipográfica se sostiene sola.** No hay ningún componente que necesite una imagen
   para verse terminado. El afiche mejora una pantalla; nunca la completa (D71, R-e).
2. **Un dato que falta no deja un hueco: cambia la forma.** Nada de cajas grises, nada de
   guiones, nada de "sin datos". Si no hay afiche, la portada es otra; si no hay puntaje, no hay
   badge; si no hay sala, no hay línea de sala.
3. **El sistema funciona sin nombre y sin logo** (P2, todavía abierto). No hay ningún token,
   componente ni imagen que dependa de una marca. Hay un hueco reservado y anotado, y el día que
   P2 cierre se llena sin tocar nada más.
4. **Mobile-first de verdad:** el ancho de referencia es **360 px** y el escenario es un celular
   a las 23:30 saliendo del teatro (Flujo 3, P8). Todo se diseña ahí primero y se ensancha
   después; nunca al revés.
5. **Se restringe antes que redefinir.** La escala tipográfica, el ritmo de espaciado y los
   breakpoints son los de Tailwind: lo que hace este documento es decir **cuáles se usan**. Menos
   valores propios que mantener es la misma economía de D73 y D51.

## Tokens

### Dónde viven

Un solo archivo, `app/globals.css`, **el único lugar del frontend donde se escribe un hex**.
Ningún componente nombra un color: todos usan la clase de Tailwind que sale del token.

```css
@import "tailwindcss";

:root {
  --papel:            #FAF8F4;   --superficie:      #FFFFFF;
  --borde:            #E5E0D6;   --borde-control:   #8F887E;
  --tinta:            #191713;   --tinta-suave:     #57534E;   --tinta-tenue: #726D66;
  --acento:           #E8A317;   --acento-tinta:    #8A5A0B;   --acento-suave: #FDF3DF;
  --sobre-acento:     #121110;
  --peligro:          #B42318;   --peligro-tinta:   #B42318;   --sobre-peligro: #FFFFFF;
  --foco:             #8A5A0B;
}

@media (prefers-color-scheme: dark) {
  :root {
    --papel:          #121110;   --superficie:      #1B1A18;
    --borde:          #322F2B;   --borde-control:   #6B665E;
    --tinta:          #F5F2EC;   --tinta-suave:     #C0BAB0;   --tinta-tenue: #938C81;
    --acento:         #E8A317;   --acento-tinta:    #F0B429;   --acento-suave: #2A2011;
    --sobre-acento:   #121110;
    --peligro:        #B42318;   --peligro-tinta:   #FF8A7A;   --sobre-peligro: #FFFFFF;
    --foco:           #E8A317;
  }
}

@theme inline {
  --color-papel: var(--papel);           --color-superficie: var(--superficie);
  --color-borde: var(--borde);           --color-borde-control: var(--borde-control);
  --color-tinta: var(--tinta);           --color-tinta-suave: var(--tinta-suave);
  --color-tinta-tenue: var(--tinta-tenue);
  --color-acento: var(--acento);         --color-acento-tinta: var(--acento-tinta);
  --color-acento-suave: var(--acento-suave);
  --color-sobre-acento: var(--sobre-acento);
  --color-peligro: var(--peligro);       --color-peligro-tinta: var(--peligro-tinta);
  --color-sobre-peligro: var(--sobre-peligro);
}
```

De ahí salen `bg-papel`, `text-tinta`, `border-borde`, `bg-acento text-sobre-acento`, etc.
**El nombre del token dice el rol, no el color**: el día que el ámbar no funcione se cambia un
hex y no 40 archivos. Si el esqueleto de la Fase 4 termina en Tailwind v3 en vez de v4, los
mismos valores van a `theme.extend.colors` apuntando a las mismas variables CSS: **la lista de
tokens no cambia y este documento tampoco**.

### La paleta, y por qué es esta

Dos neutros cálidos (papel y tinta), **un** acento y **un** color de peligro. Nada más.

El acento es un **ámbar de sala** y no el rojo telón que primero se le ocurre a cualquiera, por
una razón práctica: el rojo ya está tomado por el peligro —borrar un registro, borrar una reseña,
fusionar fichas (HU-11, HU-22, D63)— y dos rojos con significados opuestos en la misma pantalla
es exactamente el tipo de error que después no se puede deshacer. El ámbar además funciona como
**relleno con tinta oscura encima**, que es lo que necesita el botón de registrar siempre
presente (D71/D22): a las 23:30, en un celular, un botón cálido y sólido se encuentra sin buscarlo
y no encandila como uno blanco.

**No hay color de éxito y es a propósito.** La confirmación de una sugerencia (HU-08) es una
pantalla que dice una expectativa honesta, no un cartel verde; la de un registro publicado es que
el registro ya está en el diario. Un verde de "listo" es el tipo de adorno que promete algo que
MD-3 dijo que no íbamos a prometer.

### Contraste — verificado, no estimado

Calculado con la fórmula de WCAG 2.1 (luminancia relativa) sobre los hex de arriba. **Texto: AA
= 4,5:1** (3:1 para texto grande, ≥24 px o ≥18,66 px en negrita). **No textual: 3:1** (bordes de
controles, anillo de foco).

| Par | Claro | Oscuro | Requisito |
|---|---|---|---|
| `tinta` sobre `papel` / `superficie` | 16,87 / 17,90 | 16,88 / 15,56 | 4,5 ✔ |
| `tinta-suave` sobre `papel` / `superficie` | 7,19 / 7,63 | 9,78 / 9,02 | 4,5 ✔ |
| `tinta-tenue` sobre `papel` / `superficie` | 4,83 / 5,13 | 5,67 / 5,22 | 4,5 ✔ |
| `acento-tinta` (links) sobre `papel` / `superficie` | 5,58 / 5,92 | 10,12 / 9,33 | 4,5 ✔ |
| `peligro-tinta` sobre `papel` / `superficie` | 6,20 / 6,57 | 8,23 / 7,59 | 4,5 ✔ |
| `sobre-acento` sobre `acento` (botón primario) | 8,70 | 8,70 | 4,5 ✔ |
| `sobre-peligro` sobre `peligro` (botón destructivo) | 6,57 | 6,57 | 4,5 ✔ |
| `acento-tinta` sobre `acento-suave` (chip en cartel) | 5,37 | 8,58 | 4,5 ✔ |
| `borde-control` sobre `papel` / `superficie` | 3,30 / 3,51 | 3,31 / 3,05 | 3,0 ✔ |
| `foco` sobre `papel` / `superficie` | 5,58 / 5,92 | 8,70 / 8,02 | 3,0 ✔ |

⚠️ **Dos cosas que la tabla obliga y que se olvidan:**

- **`borde` no es `borde-control`.** `borde` (1,2-1,4:1) separa tarjetas y filas y es decorativo:
  no lleva información y no tiene que llegar a 3:1. **El contorno de un control —input, select,
  botón secundario, casilla— usa `borde-control`**, que sí llega. Usar `borde` en un input es la
  forma más fácil de romper 1.4.11 sin darse cuenta.
- **El botón destructivo en tema oscuro lleva un borde de 1 px en `peligro-tinta`.** El relleno
  `peligro` contra el fondo oscuro da 2,79:1 y no alcanza para que se distinga el contorno del
  control; el texto blanco adentro sí cumple, pero el borde es lo que hace que se lea como botón.
  En tema claro no hace falta (el rojo contra el papel se ve solo) y se pone igual, por simetría.

### Tipografía

**Dos familias, las dos del sistema. Cero webfonts.**

```css
--fuente-titulo: ui-serif, Georgia, "Times New Roman", serif;
--fuente-texto:  ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto,
                 "Helvetica Neue", Arial, sans-serif;
```

La serif es para títulos y para la placa tipográfica; la sans para todo lo demás. Que los títulos
sean serif es **la mitad de la identidad propia de D71 y cuesta cero bytes**: es lo que hace que
una ficha sin afiche se lea como un programa de mano y no como una tarjeta rota, y además nos
despega visualmente de la referencia estructural sin copiarle nada.

Se descartó una webfont (Google Fonts vía `next/font`, que no sería dependencia nueva porque
viene con Next): son 30-80 KB en la primera visita del que llega por WhatsApp con datos móviles,
un salto de tipografía mientras carga, y sobre todo **identidad prestada mientras P2 sigue
abierto**. Es la decisión más barata de revertir de todo el documento: el día que haya nombre y
logo, la fuente de títulos es un token y cambia una línea.

**Escala** — la de Tailwind, restringida a siete escalones y con un rol asignado a cada uno:

| Clase | px / interlínea | Rol |
|---|---|---|
| `text-xs` | 12 / 16 | chips, metadatos densos del panel admin, **etiquetas de la barra de destinos** (D81). **Piso absoluto: nada por debajo** |
| `text-sm` | 14 / 20 | meta de una fila (fecha, sala, contadores), etiquetas de formulario |
| `text-base` | 16 / 24 | cuerpo: reseñas, sinopsis, inputs. **Piso del texto que se lee** |
| `text-lg` | 18 / 28 | título de una fila destacada, nombre de usuario en la cabecera del perfil |
| `text-xl` | 20 / 28 | encabezado de sección ("Reseñas", "En cartel", "Sin fecha") |
| `text-2xl` | 24 / 32 | título de pantalla (búsqueda, sugerir, en cartel, panel) |
| `text-3xl` → `sm:text-4xl` | 30 / 36 → 36 / 40 | **título de ficha y de perfil**, y nada más |

Reglas que no se negocian:
- **Los inputs van en `text-base` (16 px) siempre.** Por debajo de 16 px, iOS hace zoom al enfocar
  un campo y el gesto de las 23:30 se convierte en una pelea con el viewport.
- **El cuerpo de una reseña se limita a `max-w-[65ch]`** aunque la columna sea más ancha.
- **Todo número que se compara verticalmente lleva `tabular-nums`**: puntajes en el diario, stats
  del perfil, contadores del admin. Sin eso, una columna de puntajes baila.
- **Pesos: sólo `font-normal`, `font-medium` y `font-semibold`.** No hay `bold` de 700 en la
  interfaz: la jerarquía la hace el tamaño y el color, no el peso.

### Espaciado, ancho y ritmo

La escala de Tailwind (4 px), restringida a **1, 2, 3, 4, 6, 8, 12, 16** (4, 8, 12, 16, 24, 32,
48, 64 px). Cómo se reparten:

| Distancia | Valor |
|---|---|
| Dentro de un componente (icono↔texto, líneas de una fila) | `1`-`2` |
| Padding de una tarjeta, de un input, de un botón | `3`-`4` |
| Entre filas de una lista | `0` — las separa un `border-t`, no un margen |
| Entre bloques de una pantalla | `6`-`8` |
| Entre secciones (ficha ↔ reseñas, diario ↔ stats) | `12`-`16` |
| Padding lateral de la página | `px-4` → `sm:px-6` |

**Anchos de columna:**

| Contenedor | Clase | Para qué |
|---|---|---|
| Columna de lectura | `max-w-3xl` (768) | feed, diario, búsqueda, sugerir, formularios |
| Página ancha | `max-w-5xl` (1024) | ficha en ≥`md` (dos columnas) y en cartel |
| Panel admin | `max-w-7xl` (1280) | tablas: acá el ancho es la herramienta (Flujo 4) |
| Cuerpo de texto | `max-w-[65ch]` | reseñas y sinopsis, dentro de cualquiera de los de arriba |

**El bloque inferior del armazón** (D81), que es lo único del sistema con medidas propias porque
es lo único que se reparte el ancho de la pantalla en celdas iguales:

| | Valor |
|---|---|
| Padding del bloque | `pt-2 px-4 pb-3` + `pb-[env(safe-area-inset-bottom)]` |
| Separación botón ↔ barra | **`border-top` de 1 px en `borde`**, no un margen (la misma regla que separa filas) |
| Ancho de celda en 360 | **82 px** con sesión (328 ÷ 4) · 109 px sin ella (328 ÷ 3) |
| Alto del objetivo táctil | **48 px**, por encima del piso de 44 de acá abajo |
| Etiqueta | **`text-xs`**, con el ícono arriba; nunca ícono solo |
| Cromo fijo total en 360×640 | **~178 px**, con ~460 px de contenido. Es el costo que D81 acepta y anota |

### Radios, sombras y bordes

| Token | Valor | Dónde |
|---|---|---|
| `rounded-md` | 6 px | botones, inputs, chips rectangulares, miniatura de afiche |
| `rounded-lg` | 8 px | tarjetas, afiche de la ficha y de la grilla, placa tipográfica |
| `rounded-xl` | 12 px | diálogo de confirmación y hoja del gesto en escritorio |
| `rounded-full` | — | avatar y chips de estado |

**Una sola sombra en todo el sistema**, y es para lo que flota: `shadow-lg` en el diálogo de
confirmación y en la hoja del gesto. Todo lo demás **se separa con `border` de 1 px**, no con
elevación. La razón es el tema oscuro: sobre `#121110` una sombra no existe, así que una interfaz
construida con sombras se aplana entera al cambiar de tema, mientras que una construida con
bordes se ve igual en los dos. Ahorra además la escala de elevaciones que ningún componente de
esta lista necesita.

### Breakpoints

Los de Tailwind, y se usan **dos y medio**:

| | Ancho | Qué cambia |
|---|---|---|
| base | 360-639 | Todo en una columna. Es el diseño real, no el caso chico |
| `sm` | ≥640 | La grilla de afiches pasa de 2 a 3 columnas; el padding lateral crece; el título de ficha sube a `text-4xl` |
| `md` | ≥768 | La ficha se parte en dos columnas (afiche/placa a la izquierda, datos a la derecha) |
| `lg` | ≥1024 | **Sólo la grilla (4 columnas) y el panel admin.** Ninguna otra pantalla lo usa |

### Movimiento, foco y objetivos táctiles

- **Transiciones: 150 ms, y sólo de `color`, `background-color`, `border-color` y `opacity`.**
  Nada se mueve de lugar, nada escala. Excepción única: la hoja del gesto entra desde abajo en
  200 ms, porque sin ese desplazamiento no se entiende de dónde salió.
- **`prefers-reduced-motion: reduce` apaga todo lo anterior**, incluida la hoja.
- **Foco visible siempre**: `outline: 2px solid var(--foco); outline-offset: 2px`. No se quita
  nunca; `:focus-visible` para que no aparezca en clics con mouse.
- **Objetivos táctiles: 44 px de alto** para todo control primario (botones, filas de la escala de
  puntaje, ítems de una lista tocable). El piso absoluto es **24×24 px** (WCAG 2.5.8, AA) y sólo
  se usa en el panel admin, que se opera con mouse (Flujo 4).
- **Íconos: SVG en línea escritos a mano, y no más de diez** (buscar, corazón, aviso, cruz,
  cheurón, más, **hamburguesa, casa, cartel y tres puntos**). No entra ninguna librería de íconos:
  sería la dependencia que D73 dejó afuera. **Ningún ícono viaja solo sin etiqueta accesible.**

  La lista era de seis y **D81 la lleva a diez**. De dónde sale cada uno de los cuatro que entran:

  | Ícono | Para qué | Por qué se agrega |
  |---|---|---|
  | **hamburguesa** | disparador del menú principal, junto a la palabra "Menú" | D81 |
  | **casa** | celda "Feed" / "Inicio" de la barra de destinos | D81 |
  | **cartel** (rectángulo con cabecera) | celda "En cartel" de la barra | D81 |
  | **tres puntos** | menú de acciones de una fila del feed y de una reseña | **ya lo pedía `SCREEN_SPECS.md`** (pantallas 2 y 3) y nunca entró en la lista de seis: **es una corrección, no un agregado** |

  Las otras dos celdas de la barra **no estrenan ícono**: "Buscar" usa la lupa que ya estaba y
  **"Mi diario" usa el monograma del componente `Usuario`**, que ya existe.

- **El destino activo de la barra no se marca sólo con color**: lleva **regla de 2 px en `acento`
  sobre el borde superior de la celda + `font-medium` + texto en `acento-tinta`**. Es la misma
  regla de accesibilidad que ya vale para las 13 pantallas —lo que llega por color lleva además
  otra señal—, y acá se anota porque una barra de navegación es donde más fácil se rompe.

## Tema oscuro: **sí, automático, sin interruptor**

**Decidido: el tema oscuro entra desde el día uno, lo elige el sistema operativo vía
`prefers-color-scheme`, y no hay control en la interfaz para cambiarlo.**

Por qué sí: el escenario que define el producto es un celular a las 23:30 saliendo del teatro
(Flujo 3, R-b). Una pantalla blanca a esa hora es hostil, y el usuario objetivo ya tiene el
celular en oscuro. Y por qué es barato: con los tokens semánticos de arriba, el tema entero es
**un bloque `@media` con quince valores** — cero JavaScript, cero estado, cero dependencia, y los
componentes no se enteran (ningún componente escribe `dark:`, porque todos usan tokens de rol).

Se descartaron las dos alternativas:

- **Sólo tema claro** (lo más barato de todo): falla el único escenario de uso que el producto se
  tomó el trabajo de escribir en tres documentos distintos. Y agregarlo después es más caro que
  ahora, porque para entonces hay 13 pantallas escritas con colores decididos a ojo.
- **Interruptor manual claro/oscuro/sistema**: necesita persistir la preferencia, y ahí empiezan
  los problemas reales. Persistirla en el servidor pide un campo de configuración de cuenta y una
  pantalla de ajustes que **USER_FLOWS descartó explícitamente** (hueco 3: no hay nada que
  configurar, D21 + MD-4). Persistirla en el cliente pide leer `localStorage` antes de pintar para
  no mostrar un destello del tema equivocado, que con SSR (ADR-003) es justo el patrón que produce
  desajustes de hidratación. Todo eso para reemplazar una preferencia que el usuario ya expresó una
  vez en su sistema operativo.

**Consecuencia que hay que respetar en todos lados: el sistema se verifica en los dos temas o no
está verificado.** Y una excepción explícita: **la imagen de Open Graph es siempre oscura**, en
los dos temas, porque WhatsApp no tiene tema y la preview tiene que ser una sola cosa.

## Los componentes de `components/ui/`

`FRONTEND_ARCHITECTURE.md` anotó **~8**: card, fila, puntaje, chip, estado vacío, avatar+username,
botón seguir, confirmación. **Son 10, y el cambio no es inflación:**

- **`Boton` absorbe "botón seguir".** Seguir no es un componente: es una **variante alternante**
  del botón, la misma que el like (D67/D68 le dieron a los dos la misma convención de tres
  estados). Tener "botón seguir" en la lista y no tener el botón común deja sin lugar al botón de
  registrar, que es el que D71 pide que esté siempre presente.
- **Entra `Afiche`.** Es el problema central de D71 —la imagen que puede no estar— y su respaldo
  tiene que ser idéntico en la ficha, en la grilla y en la miniatura. Si no es un componente, son
  tres tratamientos que se van a separar en la tercera pantalla.
- **Entra `Aviso`.** Al leer `API.md` aparece un lugar que la lista de ocho no tenía: el mensaje
  general de un formulario —el `409` sin `errores` del alta, el `400` sin `errores` de todo el
  panel— y el aviso de `global: true` del feed (D22/D66). Nada de eso es un estado vacío: hay
  contenido, y encima hay algo que decir.

**Regla de pertenencia, que ya fijó `FRONTEND_ARCHITECTURE.md` y acá no se afloja:** un componente
nace en la carpeta de su dominio y **sube a `ui/` recién cuando lo usa una segunda pantalla**, y
ahí pierde todo lo que sabía del dominio. Estos diez ya nacen arriba porque las 13 pantallas los
repiten. **La escala de 1-10 del gesto no está en esta lista** justamente por esa regla: hoy la
usa una sola pantalla (el gesto, en crear y en editar, que es el mismo formulario), así que vive
en `components/diario/` hasta que otra la necesite.

⚠️ **Por la misma regla, el menú principal y la barra de destinos de D81 no entran en esta lista y
el listado sigue siendo de diez.** Parece que los usan las 13 pantallas, pero no: **los usa el
layout, una sola vez, para todas.** Un componente sube a `ui/` cuando lo repite una segunda
pantalla, y acá no hay repetición sino un armazón que envuelve. Van en **`components/layout/`**
(`FRONTEND_ARCHITECTURE.md`), y sí usan `Boton`, `Usuario` y los íconos de este documento, que es
todo lo que necesitan de arriba.

En las tablas: **V** = variantes, **E** = estados.

### 1 · `Tarjeta`

**Qué resuelve:** el bloque de superficie con borde. Es el contenedor y nada más: no sabe qué hay
adentro.

- **V:** `plana` (bloque de contenido: stats del perfil, columna de datos de la ficha) ·
  `enlazada` (toda la tarjeta es un link — celda de la grilla de en cartel; hover cambia
  `border-color` a `borde-control`, nunca escala ni levanta) · `grilla` (afiche arriba, pie
  tipográfico abajo, altura pareja entre celdas).
- **E:** *cargando* → esqueleto: los mismos bloques en `bg-borde` sin animación de brillo (una
  animación infinita choca con `prefers-reduced-motion` y no aporta). *Vacío* y *error* no
  existen acá: los pone quien la usa. *Deshabilitada* no existe: una tarjeta no es un control.
- **Dónde:** en cartel (6), sala (5), artista (4), ficha (3), perfil (8), home visitante (1).

### 2 · `Fila`

**Qué resuelve:** **la unidad del eje tipográfico de D71.** Es lo que hace que el producto
funcione sin una sola imagen. Una línea de título, una línea de meta, un puntaje a la derecha, y
un cuerpo opcional abajo.

- **V:** `diario` (fecha · título de la obra · puntaje) · `feed` (avatar+usuario · título · fecha ·
  puntaje · reseña opcional · acciones) · `resultado` (título · meta, para búsqueda, artista y
  sala) · `admin` (título · meta · acciones a la derecha, `text-sm`, alto reducido).
- **E:** *cargando* → tres barras de esqueleto con el alto final, para que la lista no salte.
  *Error* y *vacío* son de la lista, no de la fila. *Deshabilitada* → sólo en `admin`, mientras
  una acción está en vuelo: opacidad 60 % y sin puntero.
- ⚠️ **Miniatura de afiche: sólo donde la API la trae.** El registro del diario y el ítem del feed
  **no traen `aficheUrl` y no lo van a traer** (D77), así que las filas `diario` y `feed` son
  tipográficas puras — que es coherente con D71 y no una carencia. La miniatura existe en
  `resultado` y `admin`, que se arman con el resumen de producción.
- **Dónde:** diario (8), feed (2), búsqueda (7), artista (4), sala (5), colas del admin (12).

### 3 · `Afiche`

**Qué resuelve:** la imagen cuando está y **la placa tipográfica cuando no**, que es el mismo
componente y no un caso de error.

- **V:** `ficha` · `grilla` · `miniatura` (los tamaños están en la sección de dimensiones).
- **E:** *con imagen* → `object-cover` en `grilla` y `miniatura`, **`object-contain` en `ficha`**
  (ver dimensiones). *Sin imagen* (`aficheUrl: null`) → placa tipográfica. *Cargando* → el fondo
  de la placa, sin spinner: si la imagen entra, reemplaza; si no, ya está mostrando lo correcto.
  *Error de carga* (`onError`) → **cae a la placa**, nunca a un ícono roto. *Deshabilitado* no
  aplica.
- **Dónde:** ficha (3), en cartel (6), sala (5), artista (4), búsqueda (7), panel (12), y como
  `og:image`.

### 4 · `Puntaje`

**Qué resuelve:** mostrar el entero 1-10 de D9 y el promedio con un decimal de D20.

- **V:** `registro` → el entero solo, `tabular-nums`, en `tinta` sobre `superficie` con
  `border-borde` · `promedio` → el número con **un decimal y coma decimal** ("8,4"), acompañado
  una sola vez de "/10" y de la cantidad de personas ("17 personas puntuaron").
- **E:** *sin valor* (`rating: null`, `promedio: null`) → **no se renderiza nada**. El componente
  devuelve `null`, no un guión ni un "sin puntaje"; el que sabe qué decir en ese hueco es la
  pantalla (ver casos degradados). Sin estados de carga, error ni deshabilitado: no es un control.
- **Dos decisiones adentro de esto:**
  - **El "/10" aparece una sola vez por pantalla, con el promedio.** Repetirlo en veinte filas del
    feed es ruido, y el visitante que llega de WhatsApp lo aprende en la ficha, que es donde cae.
  - **El puntaje no se colorea según el valor.** Nada de rojo para el 3 y verde para el 9: es una
    opinión, no un indicador, y teñirla es que el producto opine sobre la opinión. Siempre neutro.
- **Dónde:** ficha (3), diario (8), feed (2), cola de reportes (12).

### 5 · `Chip`

**Qué resuelve:** una etiqueta corta de metadato. **Nunca es un control:** no se toca, no filtra,
no navega.

- **V:** `estado` (D8) → **EN CARTEL** = `bg-acento-suave text-acento-tinta`, el único chip con
  color, porque es el único que informa disponibilidad · **PRÓXIMAMENTE** = borde `acento-tinta`,
  fondo transparente · **CERRADA** = `text-tinta-tenue` con `border-borde`. `rol` (D17) →
  ACTUACIÓN / DIRECCIÓN / DRAMATURGIA, neutro, `text-xs`. `nota` → tenue, en minúscula, para los
  casos degradados ("ficha eliminada", "sin fecha").
- **E:** no tiene. Un chip sin valor no se dibuja.
- **Dónde:** ficha (3), en cartel (6), artista (4), sala (5), búsqueda (7), diario (8), panel (12).

### 6 · `Usuario` (avatar + username)

**Qué resuelve:** la firma de una persona, siempre igual, en las cuatro pantallas donde aparece
gente.

- **V:** `linea` (avatar 24 px + username `text-sm`) · `cabecera` (avatar 56 px + username
  **`text-3xl` → `sm:text-4xl`** + meta).
  ⚠️ Acá decía `text-lg`, y era un error: `cabecera` la usa **una sola pantalla, el perfil**
  (`SCREEN_SPECS.md` 8), donde el username **es el título de la pantalla** — y la tabla de la
  escala de este mismo documento asigna ese escalón a "título de ficha y de perfil, y nada
  más". Corregido al escribir el componente (paso 1 de la Fase 4).
- **E:** ***autor `null`*** → avatar neutro **sin letra**, texto "cuenta eliminada" en
  `tinta-tenue` y en cursiva, **sin link**. Es el caso de `API.md` para reseñas, ítems del feed y
  las dos colas del admin. *Cargando* → círculo y barra de esqueleto. *Error* y *deshabilitado*
  no aplican.
- **El avatar es un monograma, no una foto.** No hay subida de imágenes de perfil en el alcance
  congelado (D27) y ningún endpoint devuelve una: es la inicial del username en `tinta-suave`
  sobre `superficie` con `border-borde`.
- **Se descartó el color determinístico por username** (el clásico hash → tono): son doce valores
  más para verificar en dos temas, con riesgo de caer en combinaciones que no cumplan contraste, a
  cambio de una ayuda de escaneo que acá no hace falta — **el avatar nunca aparece solo: el nombre
  está siempre al lado**, que es la información que el color estaría duplicando.
- **Dónde:** feed (2), ficha (3, en cada reseña), perfil (8), colas del admin (12).

### 7 · `Boton`

**Qué resuelve:** todas las acciones. Incluido el de registrar, que está siempre presente (D71).

- **V:** `primario` (`bg-acento text-sobre-acento`) · `secundario` (`border-borde-control`, fondo
  transparente) · `fantasma` (sólo texto en `acento-tinta`) · `peligro` (`bg-peligro
  text-sobre-peligro`, con el borde de 1 px que pide el tema oscuro) · **`alternante`** (dos
  etiquetas y dos aspectos: "Seguir" secundario ↔ "Siguiendo" fantasma; el like igual).
- **E:** *normal* · *hover* (oscurece el fondo o aparece `bg-acento-suave`; no cambia de tamaño) ·
  *foco* (el anillo de la sección de tokens) · *activo* · **cargando** → se deshabilita y la
  etiqueta pasa al gerundio ("Publicando…", "Borrando…"), sin spinner: el cambio de texto dice más
  y cuesta menos · **deshabilitado** → opacidad 50 %, `cursor-not-allowed`, y **conserva el
  contraste del texto**, que es lo que casi siempre se rompe al deshabilitar.
- **Tamaños:** `md` = 44 px de alto (por defecto, es el del celular) · `sm` = 32 px, **sólo panel
  admin**.
- **La variante `alternante` cambia primero y pregunta después** (actualización optimista): el
  estado se pinta al tocar y, si la llamada falla, **vuelve al anterior y muestra el error con
  `Aviso`**. Es lo que hace que un like a las 23:30 con mala señal no se sienta roto. Se dibuja
  sólo cuando el dato **no es `null`** (`loSigo`, `leDiLike`): `null` es "no hay botón" y `false`
  es "hay botón y está apagado" (D67/D68).
- **Dónde:** todas las pantallas.

### 8 · `EstadoVacio`

**Qué resuelve:** los huecos que USER_FLOWS marcó como el lugar donde los MVPs de una persona
siempre quedan flojos. Título corto en serif, una línea de explicación, **como máximo un botón**.

- **V:** `invitacion` (diario vacío → **el texto** dice "Registrá lo último que viste" y **el
  botón** dice "Registrar", HU-12/Flujo 2 — el voseo vive en la frase, no en la etiqueta, D84) ·
  `sin-resultados` (búsqueda vacía → deriva a sugerir, con lo tipeado; ver el camino del catálogo
  cerrado) · `informativo` (nada que hacer todavía: "Nadie escribió una reseña todavía", sin
  botón) · `error` (5xx: "No pudimos cargar esto" + "Reintentar", **sin perder lo tipeado**) ·
  `no-encontrado` (404 de ficha o perfil, con **búsqueda embebida**, como pide USER_FLOWS).
- **E:** es un estado. No tiene sub-estados propios salvo el botón de `error`, que usa el estado
  *cargando* de `Boton` al reintentar.
- **Dónde:** perfil/diario (8), feed (2), búsqueda (7), ficha (3, sin reseñas), en cartel (6),
  colas del admin vacías (12), 404 (13).

### 9 · `Aviso`

**Qué resuelve:** decir algo sobre contenido que **sí está**. Banda en línea, no flotante, no
temporal.

- **V:** `info` (`bg-acento-suave`, borde izquierdo `acento`) → el aviso de `global: true` del
  feed (D22/D66) y la expectativa honesta de la sugerencia · `error` (`peligro-tinta` sobre
  `superficie` con borde `peligro`) → el mensaje general de un formulario: el `409` sin `errores`
  del alta y el `400` sin `errores` de todo el panel admin (`API.md`).
- **E:** no tiene estados. **Y no se puede cerrar**, a propósito: para recordar que alguien lo
  cerró hace falta guardar esa preferencia en algún lado, y ese lado no existe (misma razón que
  mató el interruptor de tema). Un aviso que vuelve en cada carga es peor que uno que nunca se
  fue.
- **Nunca es un `toast`.** Un mensaje que se va solo a los tres segundos es inaccesible para quien
  lee despacio y no deja rastro del error; además pide un sistema de notificaciones flotantes que
  nadie pidió.
- **Dónde:** feed (2), alta/login (11), sugerir (10), gesto (9), panel admin entero (12).

### 10 · `Confirmacion`

**Qué resuelve:** lo irreversible. Diálogo modal: título con la consecuencia, una línea de
detalle, dos botones.

- **V:** `peligro` (borrar registro HU-11, borrar reseña HU-22, **fusionar duplicados D63**, que
  es la más irreversible de todas) · `neutra` (confirmaciones sin destrucción, si aparecen).
- **E:** *cargando* → los dos botones bloqueados, el de confirmar en gerundio. *Error* → el
  mensaje aparece **adentro del diálogo con `Aviso` y el diálogo no se cierra**: cerrarlo dejaría
  al usuario sin saber si pasó o no.
- **Reglas:** el botón que confirma **nombra la acción** ("Borrar registro", "Fusionar en esta
  ficha"), nunca "Aceptar" · el foco entra en **Cancelar** · `Esc` y el clic afuera **cancelan**,
  nunca confirman · el foco vuelve al elemento que lo abrió · fondo con `bg-tinta/50`.
- ⚠️ **No es el acuse de una sugerencia.** "Confirmación" en HU-08 quiere decir otra cosa —una
  pantalla de recibido— y se dibuja con `EstadoVacio`/`Aviso`, no con este diálogo. Confundirlas
  pone un modal donde va una pantalla.
- **Dónde:** diario (8) y feed (2) al borrar propio, cola de reportes (12), producciones del
  admin (12).

## Las tres cosas que hay que diseñar a mano (D71)

### 1 · La ficha sin afiche, y su preview de Open Graph

**Es el problema de diseño más importante de la Fase 4** y su regla es una sola: **sin afiche la
ficha no tiene un hueco donde iría el afiche — tiene otra portada.**

**La placa tipográfica.** Ocupa el lugar del afiche y está construida con lo que la ficha sí trae
siempre: el título. Fondo `acento-suave` en tema claro y `superficie` en oscuro, borde `borde`,
**una regla horizontal de 3 px en `acento` arriba**, y el título en `--fuente-titulo` alineado
abajo a la izquierda, con **tres escalones de cuerpo según el largo** (≤24 caracteres, ≤60, y el
resto), cortando a 3 líneas con puntos suspensivos. Debajo del título, en `tinta-suave` y
`text-sm`, sólo lo que exista: obra original, o sala, o estado. **La placa no dice nunca "sin
afiche" ni muestra un ícono de imagen.**

**Y la ficha cambia de forma, no sólo de contenido:**

| | Con afiche | Sin afiche |
|---|---|---|
| ≥`md` | Dos columnas: afiche a la izquierda, datos a la derecha | **Una columna.** El título sube a `text-4xl`, ocupa el ancho, y los datos van debajo. **No hay placa** |
| base (celular) | Afiche full-width arriba, datos debajo | Igual: **una columna**, título grande arriba. Sin placa |

⚠️ **La placa existe donde hace falta rellenar una celda de tamaño fijo —la grilla, la miniatura,
el `og:image`—, no en la ficha**, que es la única pantalla que puede permitirse reacomodarse. Eso
es lo que hace la diferencia entre "una imagen que no cargó" y "esta ficha se ve así".

**El `og:image` cuando no hay afiche**: **1200×630, siempre en la versión oscura** (`#26231F` de
fondo, título en `#F5F2EC` — 14,00:1 — y regla `acento` — 7,21:1), con el título en serif hasta
3 líneas y una segunda línea con sala y estado. **Margen de seguridad de 60 px** por lado, porque
WhatsApp y Twitter recortan distinto. Abajo a la izquierda queda **un espacio reservado y vacío
para el logotipo del día que P2 cierre**: hoy no se dibuja nada ahí y la placa se sostiene igual,
que es exactamente lo que pide la regla 3.

✅ **Cerrado en D85** (paso 2 de la Fase 4), y salió como esta sección lo anticipaba. `next/og`
viene con Next —no es dependencia nueva— pero **necesita un archivo de fuente embebido**: satori,
su motor, **no lee fuentes del sistema** y por defecto trae una sans. Entra entonces un **Noto
Serif (OFL 1.1) subseteado a latín, 30 KB**, en `frontend/assets/`, con su licencia y el comando
que lo generó al lado. **No contradice "cero webfonts"**: el navegador no lo descarga nunca —se
usa sólo en el servidor, para generar esta imagen— y las dos familias del sitio siguen siendo las
del sistema. La placa vive en `app/og/{tipo}/{id}` y no en el `opengraph-image.tsx` convencional,
porque ese archivo ganaría sobre `generateMetadata` y **haría imposible escribir la regla de acá
arriba**: cuando haya afiche, el `og:image` es el afiche tal cual. El costo asumido del
subconjunto: cubre latín, Latin-1 y Latin Extended-A —castellano y los nombres europeos de un
catálogo de CABA—, y un glifo fuera de ese rango saldría como un cuadrito.

### 2 · El camino del catálogo cerrado

Tres momentos que no existen en ninguna referencia (R-d, D7/D24). La regla que los une: **lo
tipeado nunca se pierde y nunca se promete lo que MD-3 dijo que no hay.**

**a. Búsqueda sin resultados** (`200` con `[]`, D65 — no es un error). `EstadoVacio` variante
`sin-resultados`, y lo que se buscó se muestra **entre comillas y textual**: «no encontramos nada
para "hamllet"». Un botón, primario: **«Sugerir "hamllet"»** — el texto viaja en el botón, que es
lo que hace evidente que no hay que volver a escribirlo. Debajo, una línea tenue con la
expectativa: "el catálogo lo carga una persona a mano". Sirve igual en la pantalla de búsqueda (7)
y en el autocompletado del gesto (9), donde el `404` del `POST /api/registros` es **el mismo
camino feliz** y no una pantalla de error (`FRONTEND_ARCHITECTURE.md`).

**b. La pantalla de sugerir** (10) llega con **el título ya cargado** y el foco en el campo
siguiente, no en el primero. Un solo campo obligatorio, el título; los otros cuatro (sala, año,
elenco, comentario) van debajo de un encabezado tenue: "si te acordás, ayuda — todo esto es
opcional". El único que puede faltar es el título; los demás sólo pueden fallar por largo o por
un año fuera de 1800-2100 — y eso lo ataja el cliente antes de mandar, porque **sugerir es uno de
los cuatro endpoints que sí devuelven `errores` por campo** (`API.md`) y aun así el `400` es la
red de contención, no la fuente del mensaje.

**c. La confirmación** es **una pantalla, no un cartel que se va**. Muestra lo que se envió —que
es literalmente lo que devuelve la API (D69: la respuesta *es* la confirmación)— y dice la
expectativa sin adornarla:

> **Lo recibimos.** El catálogo lo revisa una persona, así que puede tardar. **No te vamos a
> avisar cuando entre**: cuando esté, vas a poder buscarla y registrarla.

Dos salidas: "Volver a lo que estaba haciendo" y "Sugerir otra". **Lo que no va:** un estado
"pendiente" que se pueda consultar (no existe: no hay endpoint y no habría qué mostrar), una barra
de progreso, un tilde verde, y cualquier forma de "te avisamos" (MD-3).

### 3 · "En cartel" como motor de descubrimiento

No hay a quién copiarle porque en cine y cerveza lo disponible ahora es un detalle, y acá es una
de las dos razones para volver (CORE_LOOP). Se diseña desde el uso: **alguien que quiere salir
esta semana y no sabe qué ver.**

- **Dos secciones, en el orden en que la API las manda: "En cartel" y "Próximamente"** (D8). La
  segunda es visiblemente secundaria: encabezado `text-xl`, misma grilla, chip de estado en cada
  celda.
- **Grilla de `Tarjeta` variante `grilla`**: 2 columnas en el celular, 3 en `sm`, 4 en `lg`. Cada
  celda es afiche o placa + título + sala/complejo. Nada más, porque nada más viene en el resumen
  de producción.
  ⚠️ **Con una precisión que sólo se vio al dibujarla** (D86): **el título va una sola vez por
  celda**. Con afiche va debajo de la imagen; **sin afiche ya está adentro de la placa**, grande
  y en la serif, y abajo queda sólo la sala. Escrito como estaba —"placa + título"— el título
  aparecía dos veces en 164 px y **se leía como un bug**, que es justo lo que el criterio de
  aceptación de esta grilla no perdona. Y **la caja de la imagen crece** para que la celda con
  menos texto no rellene con un rectángulo vacío la altura que le impone la fila.
- **Sin filtros, sin calendario, sin barrio, sin horarios.** No hay agenda de funciones (X4, P6) y
  el estado de la producción es el máximo nivel de vigencia que el producto mantiene. Un filtro
  por sala sería la primera pieza de una agenda.
- **En la home del visitante (1)**, "en cartel" es el gancho: las primeras 6 celdas y un link a la
  pantalla completa. Es contenido `apiPublic` con TTL de 300 s (`FRONTEND_ARCHITECTURE.md`), así
  que es la parte más barata de servir del producto y la que primero ve el que llega sin cuenta.
- ⚠️ **La prueba de aceptación de esta pantalla es la mezcla:** una grilla con **la mitad de las
  celdas sin afiche** tiene que leerse como una decisión de diseño. Si al mirarla se lee "faltan
  imágenes", el diseño falló, y la salida ya está escrita en D71 y en `UI_REFERENCES.md`: irse al
  extremo tipográfico y convertir la grilla en una lista de `Fila`. **Se mide al cargar las ~50
  fichas de D38**, que es cuando se sabe de verdad qué proporción tiene afiche. Las placas **no se
  agrupan ni se mandan al final**: el orden lo da el catálogo.

## Cómo se dibuja lo que la API sí devuelve nulo

`API.md` ya dice **cuándo** llega cada nulo. Acá se decide **cómo se ve**. Nada de esto es lógica
de negocio: es presentación, y `FRONTEND_ARCHITECTURE.md` la pone explícitamente del lado del
frontend.

| Caso | Cómo se dibuja |
|---|---|
| `autor: null` (cuenta borrada) | `Usuario` con avatar sin letra + "cuenta eliminada" en cursiva tenue, **sin link**. La reseña sigue entera: el texto no era de la cuenta, era de la reseña |
| `enCatalogo: false` (D62) | **El título se muestra igual, en texto plano y sin link**, con un `Chip` variante `nota`: "ficha eliminada". El registro conserva fecha, puntaje y reseña. No es un error y no se esconde |
| Reseña sin texto (`resenia: null`) | La fila queda en **una sola línea de cabecera** y no reserva alto de cuerpo. En el feed es un ítem legítimo: esa persona fue al teatro (D66/D70). **Sin botón de like ni de reportar** — coincide con que `leDiLike` viene `null` y con que la API responde `404` sobre un registro sin texto |
| `rating: null` | **No se dibuja nada.** Ni guión, ni "s/p", ni un espacio reservado. La fila se ve más corta y ya está: puntuar es opcional (D18) |
| `promedio: null` | "Todavía nadie puntuó" en `tinta-tenue`, en el lugar del promedio. **Nunca "0"**, que es un puntaje válido y mentiría |
| `loSigo: null` | **No se dibuja el botón de seguir.** Es el perfil propio o no hay sesión (D67) |
| `leDiLike: null` | **No se dibuja el corazón.** El contador de likes sí se muestra si es > 0: leer cuántos hay no pide sesión |
| `vecesQueLaVi: null` (⏳ D76) | No se dibuja nada de esa zona: no hay sesión |
| `vecesQueLaVi: 0` | CTA primario **"Registrar que la vi"** |
| `vecesQueLaVi: N ≥ 1` | "La viste **N** veces" (o "una vez") + botón secundario "Registrar de nuevo" — que es el re-visto de D19 y hay que ofrecerlo, no esconderlo |
| `granularidad: "SIN_FECHA"` | "sin fecha" en `Chip` variante `nota`. En el diario van en **su propia sección al final** (MD-2), con encabezado propio: no se mezclan ni se les inventa fecha |
| `sala`, `complejo`, `sinopsis`, `obraOriginal`, `autorOriginal` nulos | **La línea entera desaparece**, etiqueta incluida. No hay "Sala: —". La ficha se acorta |
| `aficheUrl: null` (⏳ D77) | Placa tipográfica o cambio de forma, según la pantalla (ver arriba). **Es el caso normal, no un error** |
| Feed `global: true` | `Aviso` variante `info` arriba del feed: "Estás viendo toda la plataforma. Seguí gente para armar tu feed" (D22) |
| Feed `items: []` con `global: true` | `EstadoVacio` informativo: no hay actividad todavía en ningún lado |
| Feed `items: []` con `global: false` | `EstadoVacio` informativo **distinto**: los que seguís no registraron nada. Es información honesta, no un feed roto (D66) |
| `siguienteCursor: null`, o una página final con `items: []` | Fin de la lista: se deja de pedir. **Sin cartel de "no hay más", sin hueco, sin error** — la página vacía de más es normal (`API.md`) |
| Cola de reportes: `texto: null` con `produccion` presente | La fila muestra "el texto ya no está" en `tinta-tenue` y cursiva, y **conserva las dos acciones**: es la única forma de vaciar la cola (D70) |
| Cola de reportes: `produccion: null` (registro entero borrado) | Fila mínima: "el registro completo ya no está", `autor` y `rating` también nulos. **Las dos acciones siguen vivas.** `produccion` es el discriminante, no `autor` ni `rating` (`API.md`) |
| `motivo: null` en un reporte | "sin motivo" en tenue. No tiene nada que ver con los otros nulos de la fila |
| Búsqueda con `[]` | El camino del catálogo cerrado (arriba). **Nunca una pantalla de error** |

## A qué tamaño se ve un afiche

✅ **P16 se cerró en D88** con esto como insumo, así que lo de abajo dejó de ser un pendiente y
pasó a ser la especificación de lo que el backend hace hoy: encaja el afiche en una caja de
1200×1600 **sin recortar**, no lo agranda nunca, y guarda un JPEG.

**Este era el insumo de P16** y por eso fue entregable obligatorio: P16 no podía decidir a qué
tamaño se guarda una imagen si no está escrito a qué tamaño se muestra. **Acá van los tamaños y
nada más: P16 sigue abierto.**

| Uso | Caja en CSS px | Proporción | Ajuste | Necesita (×2) |
|---|---|---|---|---|
| **Ficha, celular** | ancho de columna, hasta 328 (360 − `px-4`×2) | libre, alto máximo 60 vh | `contain` | 656 de ancho |
| **Ficha, ≥`md`** | 320 de ancho, alto máximo 480 | libre | `contain` | **640 × 960** |
| **Grilla (en cartel, sala, artista)** | celda de 164 a 240 de ancho | **2:3 fija** | `cover`, anclado arriba | **480 × 720** |
| **Miniatura de fila** (búsqueda, admin) | 48 × 72 | **2:3 fija** | `cover`, anclado arriba | 96 × 144 |
| **`og:image`** | **1200 × 630**, fijo | 1,91:1 | ver abajo | 1200 × 630 |

**Las dos decisiones de forma que salen de esta tabla:**

1. **La ficha no recorta: `contain`.** Los afiches de teatro independiente de CABA no son un
   formato: hay verticales de programa de mano, cuadrados de Instagram y flyers apaisados.
   Recortarlos a 2:3 corta títulos y créditos, que en teatro suelen estar impresos **en** el
   afiche. La ficha es la pantalla que puede permitirse alto variable, así que ahí se ve entero.
2. **La grilla sí recorta, y lo hace la pantalla, no la subida.** Una grilla necesita celdas
   parejas o no es una grilla. Se recorta con CSS **anclado arriba** (`object-position: top`),
   que es donde los afiches ponen el título. ⚠️ **Consecuencia directa para P16: el archivo se
   guarda sin recortar.** Guardar ya recortado a 2:3 destruiría la ficha para ganar algo que el
   navegador hace gratis.

**Lo que esto le entrega a P16** (y que P16 sigue teniendo que decidir, junto con la herramienta,
el formato, la calidad, la orientación EXIF y el tope de píxeles decodificados):

- **El lado mayor que se llega a mostrar es 1200 px** (el `og:image`). El segundo es 960
  (ficha ≥`md` en pantalla ×2).
- Por lo tanto **una caja de origen de 1200 × 1600, encajando sin recortar**, cubre todos los usos
  de la tabla con margen: cualquier afiche entra ahí y ninguna pantalla lo pide más grande.
- **Un solo archivo alcanza.** Ningún uso de la tabla necesita un recorte distinto del que hace
  CSS, así que no hacen falta variantes por tamaño — lo que además es lo único compatible con el
  contrato de D77, que es explícito: **un solo archivo por producción**.

**El `og:image` cuando sí hay afiche: se usa el archivo del afiche tal cual.** Ya es una URL
absoluta, estática e inmutable (D77) y no hay nada que generar. La contra, que se acepta y se
anota: un afiche vertical en una preview de WhatsApp se ve como miniatura al costado y no como
tarjeta grande. La alternativa —componer un 1200×630 con el afiche adentro— es una segunda imagen
generada por ficha y otra pieza que mantener; **se mide con el test literal de HU-04** (pegar el
link en WhatsApp) y si la miniatura resulta pobre, se revisa entonces. Sin afiche, la placa
1200×630 de la sección anterior, que ya es la proporción justa.

## Voz y microcopy

Sin i18n: **castellano rioplatense, voseo, una sola persona escribiendo** (`FRONTEND_ARCHITECTURE.md`).

- **Los botones son verbos en infinitivo**: "Registrar", "Sugerir", "Publicar", "Seguir",
  "Borrar", "Crear tu diario", "Enviar", "Guardar". **Sin excepciones y sin voseo**: el panel
  admin ya no es un caso aparte, porque ahora habla igual que el resto.
  ⚠️ **Acá decía lo contrario** —imperativo voseante, con el panel como excepción—, y eso era
  **P17/MD-6**, que estaba abierto: `SCREEN_SPECS.md` venía nombrando las etiquetas en
  infinitivo y este documento fijaba la regla al revés. **Cerrado en D84 a favor del
  infinitivo.** El voseo **no se va del producto**: sigue en todo lo demás —títulos, estados
  vacíos, avisos, errores—, que es donde la voz se escucha. Un botón es una etiqueta, no una
  frase.
- **Los estados vacíos hablan del usuario, no del sistema.** "Todavía no registraste nada", no
  "No hay datos".
- **Los errores dicen qué hacer.** "Revisá el email" antes que "Error de validación". Cuando no se
  sabe qué pasó, se dice: "Algo falló de nuestro lado. Probá de nuevo." — y **lo tipeado se queda
  donde está** (USER_FLOWS lo pide explícitamente).
- **Nunca se promete lo que no existe**: ni avisos, ni notificaciones, ni "te escribimos" (MD-3).
- **Enums traducidos a castellano y en minúscula**, salvo los chips de estado y de rol, que van
  en `uppercase text-xs tracking-wide`. La traducción vive una sola vez, en `lib/formato.ts`.
- **Números en formato local**: coma decimal ("8,4"), y fechas difusas según granularidad —
  "marzo de 2023", nunca "1 de marzo de 2023" (D59). También en `formato.ts`.

## Lo que este documento NO trae, y dónde está

- **Las pantallas una por una**, con su composición y sus llamadas: `SCREEN_SPECS.md` (v1.2,
  D80/D81/D83), que es el que cierra el paso 0 de la Fase 4 — y donde vive el armazón completo: el
  contenido del menú principal por sesión y por rol, y las celdas de la barra de destinos.
- **El código de los componentes**: se escribe con el esqueleto de `/frontend` (paso 1).
- **Cualquier herramienta nueva**: no hay ninguna. Ni librería de UI (D73), ni de íconos, ni de
  animación, ni webfonts, ni generador de tokens.
- **Cómo se procesa el afiche al subirlo**: **P16**, que este documento desbloquea con los tamaños
  de arriba pero no cierra.
