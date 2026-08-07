# Roadmap

> Estado: v2.0 — reescrito tras el ajuste de ritmo D51-D55 (la v1.0 cargaba toda la
> deuda de aprendizaje en la Fase 0 y eso materializó el riesgo R3).
> Restricción real: ~5-6 hs/semana de desarrollo. Regla de la v2: **cada fase termina
> con algo que funciona y toca producto lo antes posible.** Las herramientas entran
> cuando aparece su problema (misma filosofía que ADR-002), no por adelantado.

## Lo que ya está hecho (no volver a tocar)

Repo con licencia y docs · esqueleto Spring con Modulith en el pom · Postgres corriendo
con un comando (`docker compose up -d postgres`) · CI que compila en cada push · CLAUDE.md.

## Fase 1 — El catálogo, a puro código conocido (HECHA)

**Objetivo:** el backend del catálogo completo, escrito con las capas de siempre
(entidad / repository / service / controller / DTOs record) adentro de los módulos.

1. Las 5 carpetas de módulos + package-info (2 minutos, a mano).
2. `ModulithArchitectureTest` (3 líneas, copy-paste, cuando sea en la semana).
3. **CRUD de Salas** (HU-19) — el más simple, para entrar en calor. Con `ddl-auto: update`:
   las tablas se crean solas, cero migraciones.
4. CRUD de Personas.
5. CRUD de Producciones con participaciones y estados (HU-20, sin afiche todavía).
6. Endpoints públicos de lectura: ficha, en cartel, página de artista (HU-04/05/06, versión JSON).

**Criterio de salida:** desde un cliente HTTP (curl/Postman) podés crear y consultar
el catálogo completo. **Herramientas nuevas usadas: cero.**

## Fase 2 — Identidad y Diario (el corazón) (HECHA)

- ~~Vuelve **Spring Security** (D52): registro, login con sesiones (HU-01/02).~~ Hecho
  en D56/D57, con el candado de `/api/admin/**` y los primeros tests del proyecto.
- ~~El gesto de registro completo (HU-09/10/11), promedio D20, diario y stats
  (HU-12/13/14).~~ Hecho **el backend**, que es lo único que esta fase puede cerrar:
  registrar / editar / borrar, el promedio de D20, el diario ordenado, las stats y las
  reseñas de la ficha, con tests de aceptación. Lo que a esas historias todavía les falta
  no se construye acá: el autocompletado es HU-07 (el punto que sigue), la derivación a
  sugerir es HU-08 y los likes que pide HU-14 son HU-17 — las dos, Fase 3 —, y la
  confirmación de borrado y los estados vacíos son pantallas (Fase 4). Las historias se
  cierran del todo cuando cierren sus fases; el backend ya no las bloquea.
  Decisiones que hicieron falta: D58, D59, D60 y D61.
- ~~Búsqueda pg_trgm (HU-07).~~ Hecha: tres endpoints de búsqueda —producciones y personas
  en Catálogo, usuarios en Identidad—, con tolerancia a typos y a títulos escritos a medias,
  que es lo que hace usable el autocompletado del gesto de registro (HU-09). Decisiones que
  hicieron falta: D64 (cómo entran la extensión y sus índices sin Flyway) y D65 (la forma).
  Lo que a HU-07 le falta es pantalla: el resultado vacío que deriva a sugerir necesita HU-08
  (Fase 3) y la pantalla de resultados es Fase 4.

## Fase 3 — Social (HECHA)

- ~~Follow y feed compuesto (HU-15/16).~~ Hecho: nace el módulo Social con el grafo
  —seguir, dejar de seguir, contadores en el perfil— y el feed, que es la composición que
  D29 venía anunciando: Social dice a quién sigo, Diario qué registró esa gente, Identidad
  cómo se llaman, y no se guarda nada. Con su fallback global para quien no sigue a nadie
  (D22) y paginado por cursor. Decisiones que hicieron falta: D66 y D67.
- ~~Likes a reseñas (HU-17), que además completa lo que a HU-14 le falta en la ficha.~~ Hecho:
  el toggle sobre `/api/resenias/{id}/like` y el contador donde se leen reseñas —la ficha, que
  con esto tiene todo lo que HU-14 pedía, y también el feed—. Social suma su segunda capacidad
  sin enterarse de qué es una reseña. Decisión que hizo falta: D68, que además amplía D66 (el
  feed pasa a siete consultas).
- ~~Sugerencias: el formulario del usuario (HU-08) y la cola del admin (HU-21).~~ Hecho: la
  válvula del catálogo cerrado (D7/D24). El formulario pide una sola cosa —el título— y la
  cola del admin se vacía por dos puertas: aprobar, con la ficha que ya cargó el formulario
  de HU-20, o rechazar con un motivo que lee solo él. Con esto **HU-07 no tiene nada más que
  esperar del backend**: el resultado vacío ya tiene adónde derivar, y lo que queda es la
  pantalla (Fase 4). Decisión que hizo falta: D69.
- ~~Reportes: el botón (HU-18) y la cola del admin (HU-22).~~ Hecho: lo que D40 agregó al alcance
  congelado. Social suma su tercera capacidad —el aviso, que cuelga de la reseña como el like y
  tampoco sabe qué es— y la cola del admin se vacía por dos puertas: borrar el texto reportado o
  desestimar. Borrar se lleva la reseña y deja la salida al teatro con su fecha y su puntaje, así
  que el promedio de D20 no se entera. Decisión que hizo falta: D70, que además reemplaza
  `existeResenia` por `autorDeResenia` (amplía D68). **Con esto cierra la Fase 3, y con ella la
  capa social entera (D3).** De las 22 historias, a casi todas lo único que les falta son
  pantallas. Lo que todavía espera backend son dos cosas, y las dos entran en la Fase 4: la
  subida de afiches de HU-20 (D72, movida desde la Fase 5) y el `vecesQueLaVi` de HU-10 (D76),
  que apareció al escribir `architecture/API.md`.

## Fase 4 — Frontend (AHORA)

**Objetivo:** las pantallas de USER_FLOWS.md contra la API ya construida y probada. Next.js
sin cambios (ADR-003/D55, revisado al llegar y confirmado). Vale la misma regla que hizo
rápida la Fase 1: **antes de escribir pantallas, existe el documento que dice dónde va cada
cosa nueva** — en el backend eso era MODULE_MAP + las capas de D51; acá hay que escribirlo.

0. ~~**Los documentos que van antes de la primera pantalla**~~ **HECHO — el paso 0 está cerrado.**
   En este orden:
   ~~`architecture/API.md`~~ **hecho** (v1.2: el contrato HTTP, corregido contra el código en dos
   revisiones) → ~~`architecture/FRONTEND_ARCHITECTURE.md`~~ **hecho** (v1.1, D78: carpetas,
   rutas, servidor vs cliente, el único lugar donde vive el fetch, cómo viajan la sesión de D56 y
   el token de D57) → ~~`product/DESIGN_SYSTEM.md`~~ **hecho** (v1.0, D79: tokens con el contraste
   calculado, tema oscuro automático sin interruptor, los 10 componentes de `ui/`, los tres huecos
   de D71 diseñados, cómo se dibuja cada nulo de la API y **a qué tamaño se ve un afiche, que es
   lo que P16 estaba esperando**) → ~~`product/SCREEN_SPECS.md`~~ **hecho** (v1.0, D80: una entrada
   por pantalla con sus datos, su composición, sus islas cliente y **los cuatro estados
   obligatorios**; el gesto como hoja y no como ruta, y la matriz de cobertura de las 22 historias).
   ~~Referencia de interfaz~~: cerrada en D71. **Lo que sigue es el punto 1: código.**
1. ~~Esqueleto de `/frontend` + Tailwind (D73) + el layout con la navegación y el botón de
   registrar siempre presente (D71).~~ **HECHO.** Next 16 con App Router y TypeScript, **sin
   ESLint ni ninguna otra herramienta**: las únicas dependencias son Next, React, TypeScript y
   **Tailwind v4**, que es lo que D73 permitía y nada más. Los **tokens de D79 entran tal cual**
   en `app/globals.css` —único lugar del frontend con un hex, con el tema oscuro por
   `prefers-color-scheme` y sin interruptor—, el **rewrite local de `/api`** le da a desarrollo
   el mismo origen único que Caddy da en producción, y **el armazón de cuatro piezas de D81 está
   en pie y verificado en sus tres variantes**: visitante ("Crear tu diario" · Inicio), con sesión
   ("Registrar" · Feed · Mi diario · Salir) y admin (la sección Panel). Están además los dos
   clientes de la API con la caché separada por llamada, el arranque perezoso del token CSRF y
   `lib/rutas.ts` con la regex del id. Decisión que hizo falta: **D82** (`lib/api/` se parte en
   `<modulo>.servidor.ts` / `<modulo>.cliente.ts`, porque un archivo con las dos mitades no
   compila), que además **cierra la comprobación que D78 había dejado pendiente**: el `401` de
   `/api/auth/yo` sí trae `Set-Cookie: XSRF-TOKEN`. Y **D83**, que salió de auditar el paso: el
   armazón vive en el grupo `app/(sitio)/` y no en la raíz —los layouts se anidan, así que con
   el armazón arriba el panel no podría sacárselo nunca (D81)—, `yo()` va memoizada con
   `cache()` porque **Next no pasa datos de un layout a sus hijos** y la ficha y el perfil
   necesitan la sesión para elegir cliente, y el `5xx` de `/auth/yo` dibuja el armazón del
   visitante en vez de tirar la pantalla entera.
   ⚠️ **Lo que este paso deja anotado y no resuelve**, porque es de los pasos que siguen: la
   home de `/` es provisoria (pantalla 1 en el paso 2, pantalla 2 en el paso 5), `not-found.tsx`
   y `error.tsx` esperan a `EstadoVacio` (pantalla 13), el panel necesita su propio layout **sin
   armazón** (paso 6), y el botón persistente con sesión **dispara un evento que todavía nadie
   escucha**: la hoja del gesto es el paso 4.
2. ~~Pantallas públicas con SSR y Open Graph: ficha, artista, sala, en cartel, home visitante
   (HU-04/05/06).~~ **HECHO.** Las cinco existen y son **Server Components enteros**, con una
   sola isla cliente en todo el paso: el **"Reintentar"**, que usan el bloque de opiniones de
   la ficha y el error de "en cartel" —reintentar es volver a pedirle la pantalla al servidor,
   y eso es `router.refresh()`—. Entran con ellas los seis
   componentes de `ui/` que faltaban —`Tarjeta`, `Fila`, `Afiche`, `Puntaje`, `Chip`,
   `EstadoVacio`—, `lib/formato.ts` (fecha difusa de D59, enums, coma decimal) y los clientes
   de Catálogo y Diario del lado servidor. La **ficha hace sus dos llamadas** (D60) y
   **degrada por bloque**: si fallan las opiniones, la ficha se muestra igual. La URL de D74
   está entera: el id se valida con la regex, un slug viejo o ausente redirige `308` a la
   canónica, y un id que no es entero positivo es `notFound()`. **Cierra de paso la pantalla
   13**, que el paso 1 había dejado esperando a `EstadoVacio`: el `404` de `(sitio)`, el
   `error.tsx` —que vive adentro del grupo, porque en la raíz se dibujaría sin armazón— y el
   **404 global, que compone las cuatro piezas a mano** como exigía D83. Decisiones que
   hicieron falta: **D85** (la placa de `og:image` con `next/og` + una Noto Serif subseteada
   de 30 KB embebida, ruta propia `/og/{tipo}/{id}` en vez del `opengraph-image.tsx`
   convencional, y `metadataBase`, que es lo que hace absolutas las URLs del preview) y
   **D86** (dos correcciones de la celda de la grilla que sólo se vieron renderizadas).
   ⚠️ **Y una limitación medida, anotada como P18**: el `notFound()` de una ficha y el
   `error.tsx` **sólo se pintan con JavaScript** —el estado HTTP es correcto y con JS se ve la
   pantalla entera, pero sin JS el cuerpo llega vacío—. El 404 global sí llega en el HTML.
   ⚠️ **El test de aceptación literal —pegar el link en WhatsApp— no se puede correr todavía**:
   necesita una URL pública, o sea la Fase 5. Lo que sí está verificado es lo que lo sostiene:
   los `<meta>` con URL absoluta y la placa de 1200×630 generada y mirada en los dos casos
   (título corto y título de 79 caracteres). **Y sigue pendiente la verificación de D81**: la
   ficha con el afiche a 60 vh contra el cromo del armazón. **Ya no la bloquea P16**, que cerró
   en D88: el endpoint existe y se puede subir un afiche de prueba. Lo que falta para medirla es
   la pantalla que lo suba (paso 6) o una subida a mano por `curl`; hoy la ficha sin afiche cambia
   de forma y entra sobrada.
3. **Lo que le queda al backend**, todo salido de escribir `API.md` y sin lo cual hay pantallas
   que no cierran. Va en dos entregas y la primera está hecha:
   ~~el `vecesQueLaVi` que cierra HU-10 (D76) y un `@ControllerAdvice` que unifique las
   respuestas de error~~ **HECHO.** `vecesQueLaVi` sale de `opinionesDe` como pregunta aparte
   —una consulta que solo se paga con sesión— con los tres estados de la convención de D67/D68:
   nulo sin sesión, `0` con sesión y sin registros —que es lo que habilita el CTA— y el
   re-visto de D19 cuando la vio varias veces. Y **las tres familias de error de `API.md` pasan a
   ser una sola** (D87): `ProblemDetail` con `detail` en castellano siempre, `errores` por campo
   en **todos** los formularios y no en cuatro —que es lo que al panel admin le faltaba—, el
   `401`/`403` de la cadena de filtros con la misma forma, y el fallo inesperado como un `500`
   con forma y sin tripas. Los errores de dominio no se movieron: la forma se unifica, el
   significado lo sigue decidiendo el endpoint. **79 tests en verde**, `ModulithArchitectureTest`
   incluido: el advice no nombra una sola clase interna de un módulo.
   ~~La segunda entrega: la subida de afiches~~ **HECHO también, y con eso cierra el paso 3.**
   `POST` y `DELETE /api/admin/producciones/{id}/afiche` con el contrato de D77 entero: el
   **contador monótono** que nunca se reinicia ni se reutiliza —lo que hace que el `immutable` de
   un año no sea una mentira—, `afiche_actual` separado de él, el orden de las cuatro operaciones
   (reservar → escribir → publicar → borrar el viejo) con **la reserva en una sola sentencia
   atómica**, **la publicación con la fila bloqueada** y **el borrado del archivo después del
   commit y fuera de la transacción**, y el `DELETE` idempotente. Los tests que el contrato
   exigía son **15**, y los tres que importan más que el camino feliz son el del contador que no
   se reinicia y los **dos solapamientos con dos transacciones de verdad** —una tiene la fila
   tomada medio segundo y la otra tiene que esperarla—, que **fallan si se le saca el bloqueo al
   repositorio**: comprobado sacándolo. Los otros dos, que corren los pasos intercalados en un
   solo hilo, prueban el orden y no el bloqueo, y así están nombrados. **94 tests en verde.**
   ⚠️ **Esos dos tests de bloqueo entraron después, en D89**, junto con la orientación EXIF en los
   tres formatos y el movimiento atómico que dejó de degradar en silencio: los encontró una
   auditoría del paso, y los tres eran defectos reales.
   ✅ **P16 quedó cerrada en D88**: **TwelveMonkeys** para leer (Java puro, sin binarios nativos;
   acepta los tres formatos que promete `API.md` y no se planta con los JPEG CMYK de imprenta) y
   **JPEG a la salida**, porque no existe un escritor de WebP en Java puro. Eso **cambia la URL
   pública de `.webp` a `.jpg` y enmienda D77 en ese punto y en ningún otro**. Lo demás de P16
   también: 1200×1600 sin recortar y sin agrandar (D79), calidad 0,82, EXIF aplicado al subir y
   archivo guardado sin metadatos, y un **tope de 50 MP comprobado leyendo la cabecera antes de
   decodificar** —lo único de la lista que es de seguridad: los 5 MB son del archivo comprimido y
   no acotan la memoria—.
   ⚠️ **Lo que de los afiches sigue pendiente es de la Fase 5 y está anotado allá**: que Caddy
   sirva `/afiches` desde el volumen, y que ese volumen tenga su propio backup (D45/D77). En
   desarrollo ya funciona sin nada de eso: Spring escribe en `frontend/public/afiches/` y lo
   sirve Next como estático (D78).
4. ~~Cuentas y el gesto: alta/login (HU-01/02), búsqueda con su resultado vacío (HU-07), el
   gesto de registro con autocompletado y el desvío a sugerir sin perder lo tipeado
   (HU-08/09/10), editar y borrar con confirmación (HU-11).~~ **HECHO**, en tres entregas.
   Están las **pantallas 7, 9, 10 y 11**, y con ellas **el camino del catálogo cerrado
   entero** —los tres momentos que D71 mandó diseñar a mano: la búsqueda sin resultados que
   deriva con lo tipeado adentro del botón, el formulario de un solo campo obligatorio, y el
   acuse que **es una pantalla y dice que no va a haber aviso** (MD-3)—. **El gesto quedó como
   lo pedía P8**: seis toques y ningún tipeo más que el título, con la última opción del
   autocompletado siempre en "sugerirla" —esperar la lista vacía no sirve: con `pg_trgm` casi
   siempre vuelve *algo*— y con el `404` del `POST` tratado como camino y no como error.
   **Cierra de paso el hueco que el paso 2 había dejado anotado**: el CTA de la ficha, que
   ahora dibuja los tres estados de `vecesQueLaVi` (D76). Entran `Aviso` (ui/9),
   `Confirmacion` (ui/10) —con lo que **los diez de `DESIGN_SYSTEM.md` están completos**—,
   `Campo`/`CampoLargo`, `usarBorrador`, la escala 1-10 de `components/diario/` y los tres
   clientes de navegador que faltaban. Decisión que hizo falta: **D90**, que además recoge
   **cuatro defectos que el paso encontró**, uno de ellos con el navegador y no leyendo:
   **sin hidratar, un formulario sin `method` mandaba la contraseña en la URL**.
   ⚠️ **Lo que este paso deja anotado y no resuelve**: **editar y borrar (HU-11) no tienen
   punto de entrada todavía** —los menús de tres puntos del diario y del feed son el paso 5,
   así que lo único que se probó de esa mitad es el contrato— y **el borrador que sobrevive al
   `401` es una pieza del gesto**, con su propia marca, no una regla general del armazón.
5. Perfil/diario con estados vacíos y stats (HU-03/12/13), home logueada con el feed y su
   aviso de fallback (HU-16), seguir (HU-15), likes y reportar (HU-17/18).
6. Panel admin: las cuatro caras que ya tienen backend —salas, producciones con estados,
   cola de sugerencias, cola de reportes (HU-19/21/22)— más el botón de fusionar duplicados
   y su confirmación (D63).

**Criterio de salida:** las 13 pantallas de USER_FLOWS.md existen con sus estados vacíos y
de error; el flujo 3 (celular, 23:30, registrar en menos de un minuto) se puede hacer de
punta a punta; el flujo 1 (link compartido → ficha → perfil → crear cuenta) también.

## Fase 5 — Deploy y beta

- **Entra Flyway** (D53): baseline del esquema + `ddl-auto: validate`. Innegociable
  antes de datos reales.
- Descongelar Dockerfiles + Caddy + Compose completo (ya escritos, están en el repo).
  VPS, HTTPS, backups probados (D45). **En Caddy entra `/afiches`** (D77): hoy el `Caddyfile`
  reparte `/api` y nada más, así que hay que agregarle el `file_server` y montar el volumen
  `uploads` **solo lectura** en el servicio `caddy` del compose, donde hoy solo lo monta
  `backend`. En desarrollo esa ruta no la sirve Caddy ni Spring: los afiches salen de
  `frontend/public/` (D78), y el rewrite local es solo para `/api`.
- **Backup del volumen de afiches** (D77), que el `pg_dump` de D45 no cubre — son **dos backups
  separados**: `pg_dump` respalda PostgreSQL, el volumen tiene el suyo. Sin el segundo, restaurar
  deja el catálogo entero sin imágenes y la mecánica de compartir de ADR-003 no funciona.
  **Las dos restauraciones se prueban antes de la beta**, no después. **La rutina nocturna para
  el backend mientras corren las dos copias** (D77): con mutaciones de afiches en el medio,
  ningún orden alcanza —un reemplazo entre el `pg_dump` y la copia del volumen borra el archivo
  que el dump referencia—. Las dos copias se guardan y se restauran **como una pareja**, con la
  misma marca de tiempo.
- ~~Lo que le queda a HU-20: subida de afiches, y las pantallas de lo que ya tiene backend
  —entre ellas el botón de fusionar duplicados y su confirmación (D63)—.~~ Todo eso se movió
  a la Fase 4: los afiches por D72, las pantallas porque son pantallas. Acá queda el panel
  admin pulido contra el uso real de la rutina semanal (D37).
- Carga de las ~50 fichas (D38) + beta cerrada con espectadores reales (métricas de
  MVP_SCOPE.md).

## Deudas congeladas con condición de reentrada (resumen)

| Qué | Vuelve cuando |
|---|---|
| ~~Spring Security~~ | Reingresó con HU-01/02 (D56) |
| Flyway | Antes del primer deploy con datos (Fase 5) — innegociable |
| Tests + Testcontainers | Progresivamente desde Fase 2; sí o sí antes de la beta |
| Dockerfiles/Caddy/VPS | Fase 5 |
| Frontend | Fase 4 |
