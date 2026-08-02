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
   ficha con el afiche a 60 vh contra el cromo del armazón — no se puede medir hasta que haya
   un afiche (P16), y hoy la ficha sin afiche cambia de forma y entra sobrada.
3. **Lo que le queda al backend**, todo salido de escribir `API.md` y sin lo cual hay pantallas
   que no cierran: la subida de afiches con redimensionado y versionado (HU-20, D72/D77,
   adelantada desde la Fase 5), el `vecesQueLaVi` que cierra HU-10 (D76) y un `@ControllerAdvice`
   que unifique las respuestas de error — hoy no hay ninguno y los formularios del admin no
   devuelven errores por campo.
   ⚠️ **La subida de afiches todavía no se puede empezar**: le falta una decisión, no código.
   Con qué se decodifica, redimensiona y codifica la imagen es **P16**, y es una dependencia
   nueva que D51 exige decidir y no adoptar — el JDK no escribe WebP y el `pom.xml` no tiene
   ninguna librería de imágenes. **La mitad que dependía del diseño ya está**: D79 fijó las
   dimensiones (caja de origen 1200×1600 **sin recortar**, un solo archivo, el recorte de la
   grilla lo hace CSS), así que lo que queda de P16 es herramienta, formato, calidad, EXIF y
   tope de píxeles decodificados.
4. Cuentas y el gesto: alta/login (HU-01/02), búsqueda con su resultado vacío (HU-07), el
   gesto de registro con autocompletado y el desvío a sugerir sin perder lo tipeado
   (HU-08/09/10), editar y borrar con confirmación (HU-11).
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
