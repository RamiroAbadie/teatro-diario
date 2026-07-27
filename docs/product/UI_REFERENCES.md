# Referencias de interfaz

> Estado: v1.0 — exploración previa a la Fase 4. **Cerrada en D71**, que adoptó la
> recomendación del final tal cual. Este documento no decidía: preparaba la decisión. Se
> conserva porque guarda lo descartado y las alternativas vivas, que es lo que hace falta el
> día que alguien —incluido vos-en-seis-meses— quiera rediscutir la estética.
> Compara productos existentes contra las 13 pantallas de `USER_FLOWS.md`.
>
> Regla que lo justifica: el frontend no tiene el equivalente del MODULE_MAP —el documento
> que decide dónde va cada cosa nueva—, y la estética es la mitad de ese problema. La otra
> mitad la resuelve `FRONTEND_ARCHITECTURE.md`, que se escribe después de esto.

## Qué se está eligiendo (y qué no)

**Sí:** de dónde salen los patrones de interacción y la arquitectura de información —qué
forma tiene el gesto de registro, cómo se lee una lista de registros, qué muestra un perfil,
cuál es la pantalla que es "la casa"—.

**No:** la identidad visual. Paleta, tipografía, logo y nombre (P2) son propios en cualquiera
de las opciones. Copiar patrones de interacción probados es economía; copiar identidad es
otra cosa.

## Contra qué se evalúa (las restricciones ya decididas)

| # | Restricción | De dónde sale |
|---|---|---|
| R-a | El gesto de registro compite en fricción contra subir una story | P8, CORE_LOOP |
| R-b | El escenario real del gesto es un celular a las 23:30 saliendo del teatro | USER_FLOWS, Flujo 3 |
| R-c | Fichas y perfiles se comparten por WhatsApp/IG: previews OG + SEO | P13, ADR-003 |
| R-d | El catálogo es **cerrado y curado**: el usuario no carga, sugiere | D7, P5 |
| R-e | **Muchas fichas no van a tener afiche decente** (teatro independiente de CABA) | D38, D39 |
| R-f | Rating entero 1-10, no estrellas | D9 |
| R-g | Sin listas, sin comentarios, sin notificaciones, sin agenda | D25, X5, MD-3, X4 |
| R-h | Una sola persona construye y opera | P3 |

R-e es la que más manda y la que menos se ve venir: casi todas las referencias del rubro
están construidas sobre la existencia garantizada de una imagen buena.

## Los dos ejes de la decisión

La pregunta útil no es "¿qué app copio?" sino dónde nos paramos en dos ejes. Cada referencia
es un punto en este plano, no un paquete que se toma entero.

**Eje 1 — De qué está hecha la pantalla: póster ↔ tipografía.**
En un extremo, la grilla de imágenes es la interfaz (Letterboxd). En el otro, la densidad de
texto lo es (RateYourMusic). R-e empuja fuerte hacia el medio: una grilla de placeholders
grises no es un diseño con datos faltantes, es un diseño roto.

**Eje 2 — Cuál es la casa: feed ↔ catálogo ↔ diario.**
Qué ve el usuario logueado al entrar. D22 ya lo resolvió a nivel producto (home logueada =
feed + accesos a "registrar" y "en cartel"), pero de cuánto peso relativo se lleva cada cosa
depende toda la jerarquía visual.

## Las referencias

### Letterboxd — el análogo directo

**Qué copiar:** el modal de registro es prácticamente HU-09 ya diseñada (fecha + puntaje +
texto + "la vi antes", que es D19). El diario como lista cronológica compacta y densa, con
la fila como unidad. El perfil = números arriba, actividad reciente abajo. La reseña como
contenido con página propia y likes (D11/HU-17).

**Qué no:** identidad visual completa. Y las listas, que estructuran media interfaz de
Letterboxd y están fuera del MVP (D25) — copiar la jerarquía sin ellas deja huecos.

**Dónde no mapea:**
- Catálogo abierto vía TMDB. **El camino "no está → sugerir" (HU-08) no existe ahí**, y en
  nuestro producto es primera clase (D24: es la válvula del historial viejo). No hay de dónde
  copiarlo.
- Todo el sistema visual asume póster (R-e).
- Estrellas con medios puntos vs. nuestro entero 1-10 (R-f): el widget hay que rediseñarlo,
  no reescalarlo.

### Untappd — el análogo del *gesto*, no del catálogo

**Qué copiar:** es el mejor exponente de R-a y R-b que existe. El check-in se hace parado en
un bar, en segundos, desde un botón central que está siempre. Feed-first sin disculpas. La
"venue" es análoga a nuestra Sala.

**Qué no:** la estética, que es de app y no de web.

**Dónde no mapea:**
- **Catálogo abierto y cargado por usuarios → duplicados masivos.** Es evidencia a favor de
  D7/P5, no un modelo a seguir.
- Su web es débil: compartir y ser indexado no son su mecánica. Falla R-c de lleno, que es
  justamente lo que decidió ADR-003.

### RateYourMusic — el contramodelo tipográfico

**Qué copiar:** la respuesta al problema de R-e. Es denso, tabular, jerarquizado por
tipografía, y **no se cae cuando falta la imagen** porque nunca dependió de ella. Sus fichas
soportan una cantidad de metadatos que la nuestra también tiene (obra original, autor,
elenco por rol, sala, estado).

**Qué no:** la densidad extrema y la interfaz de escritorio de los 2000. Falla R-b (el
celular a las 23:30) sin adaptación fuerte.

### Goodreads — la anti-referencia útil

No se copia nada. Sirve para ver qué pasa cuando el catálogo crece sin curaduría y la
interfaz se acumula por capas: fichas duplicadas, jerarquía ilegible, tres formas de hacer
lo mismo. Es el destino que D7 y P5 están evitando, dibujado.

## Las 13 pantallas y de dónde saldría cada patrón

| # | Pantalla | Referencia más útil | Cuánto sirve |
|---|---|---|---|
| 1 | Home visitante | Letterboxd | Media: su home de deslogueado vende otra cosa |
| 2 | Home logueado (feed) | Untappd + Letterboxd | Alta |
| 3 | **Ficha de producción** | Letterboxd (estructura) + RYM (densidad sin imagen) | Alta, pero es la que más hay que resolver a mano (R-e) |
| 4 | Página de artista | Letterboxd (página de director) | Alta |
| 5 | Página de sala | Untappd (venue) | Media |
| 6 | En cartel | — | **Baja: el equivalente es marginal en todas.** Ver abajo |
| 7 | Búsqueda / resultados | Letterboxd | Alta, salvo el resultado vacío |
| 8 | **Perfil / diario** | Letterboxd (diary) | Alta — es lo mejor que tiene para copiar |
| 9 | **Gesto de registro** | Letterboxd (forma) + Untappd (fricción) | Alta |
| 10 | Sugerir producción | — | **Ninguna: no existe en ninguna referencia** |
| 11 | Alta / login | cualquiera | Trivial |
| 12 | Panel admin | — | Ninguna, y no hace falta: es herramienta, no producto |
| 13 | 404 | Letterboxd | Trivial |

## Lo que ninguna referencia resuelve (hay que diseñarlo)

Son tres, y las tres son consecuencia directa de decisiones nuestras. Van al
`DESIGN_SYSTEM.md` como problemas propios:

1. **La ficha sin afiche (R-e).** Necesita un tratamiento tipográfico que se lea como una
   decisión de diseño y no como una imagen que no cargó. Y necesita resolver también el caso
   OG: qué preview se genera cuando no hay imagen (R-c), que es la mecánica de crecimiento
   entera. Este es el problema de diseño más importante de la Fase 4.
2. **El camino del catálogo cerrado (R-d).** Búsqueda sin resultados → sugerir sin perder lo
   tipeado → confirmación con expectativa honesta y sin promesa de aviso (MD-3). Tres momentos
   que en Letterboxd y Untappd simplemente no existen, y que USER_FLOWS marca como la primera
   impresión del usuario intensivo con historial (R2).
3. **"En cartel" como motor de descubrimiento.** En cine y cerveza, lo disponible ahora es un
   detalle; acá es una de las dos razones para volver (CORE_LOOP) y una de las tres cosas que
   hacen que el teatro no sea cine. No hay a quién copiarle: se diseña desde el uso.

## Recomendación

**Letterboxd como referencia estructural, corregida por RateYourMusic en la ficha y por
Untappd en el gesto.** En los dos ejes: **a mitad de camino en el eje 1** (la imagen se usa
cuando está y la fila tipográfica funciona sola cuando no), y **feed-first con el botón de
registrar siempre presente en el eje 2**, que es lo que ya dijo D22 llevado a jerarquía visual.

Las tres cosas de la sección anterior se diseñan a mano y son el trabajo real de diseño de la
fase; el resto se copia y eso es tiempo ahorrado, no falta de originalidad (R-h).

**Alternativas que quedan vivas si esto se descarta:** irse al extremo tipográfico completo
(más honesto con R-e, peor con R-b) o al extremo póster (mejor si la carga de afiches sale
bien y R-e resulta ser menos grave de lo estimado — se sabrá al cargar las ~50 fichas de D38).

## Cómo se cerró

**D71**: la recomendación de arriba, tal cual, con sus dos posiciones en los ejes. La
identidad visual queda propia y los tres huecos de la sección anterior son el trabajo de
diseño real de la fase. `DESIGN_SYSTEM.md` es el que baja esto a tokens y a los ~8 componentes
que se repiten en las 13 pantallas.
