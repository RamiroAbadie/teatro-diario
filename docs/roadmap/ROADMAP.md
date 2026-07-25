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

## Fase 3 — Social (AHORA)

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
- Reportes: el botón (HU-18) y la cola del admin (HU-22).

## Fase 4 — Frontend

- Recién acá se abre la discusión de front (D55). Plan vigente: Next.js (ADR-003);
  se revisa al llegar si el fundador lo pide, como decisión explícita.
- Las pantallas de USER_FLOWS.md contra la API ya construida y probada.

## Fase 5 — Deploy y beta

- **Entra Flyway** (D53): baseline del esquema + `ddl-auto: validate`. Innegociable
  antes de datos reales.
- Descongelar Dockerfiles + Caddy + Compose completo (ya escritos, están en el repo).
  VPS, HTTPS, backups probados (D45).
- Lo que le queda a HU-20: subida de afiches, y las pantallas de lo que ya tiene backend
  —entre ellas el botón de fusionar duplicados y su confirmación (D63)—. Panel admin pulido.
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
