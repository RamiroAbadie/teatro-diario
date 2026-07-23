# Roadmap

> Estado: v2.0 — reescrito tras el ajuste de ritmo D51-D55 (la v1.0 cargaba toda la
> deuda de aprendizaje en la Fase 0 y eso materializó el riesgo R3).
> Restricción real: ~5-6 hs/semana de desarrollo. Regla de la v2: **cada fase termina
> con algo que funciona y toca producto lo antes posible.** Las herramientas entran
> cuando aparece su problema (misma filosofía que ADR-002), no por adelantado.

## Lo que ya está hecho (no volver a tocar)

Repo con licencia y docs · esqueleto Spring con Modulith en el pom · Postgres corriendo
con un comando (`docker compose up -d postgres`) · CI que compila en cada push · CLAUDE.md.

## Fase 1 — El catálogo, a puro código conocido (AHORA)

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

## Fase 2 — Identidad y Diario (el corazón)

- Vuelve **Spring Security** (D52): registro, login con sesiones (HU-01/02).
- El gesto de registro completo (HU-09/10/11), promedio D20, diario y stats (HU-12/13/14).
- Búsqueda pg_trgm (HU-07).

## Fase 3 — Social

- Follow, feed compuesto, likes, sugerencias, reportes (HU-15..18, 21, 22).

## Fase 4 — Frontend

- Recién acá se abre la discusión de front (D55). Plan vigente: Next.js (ADR-003);
  se revisa al llegar si el fundador lo pide, como decisión explícita.
- Las pantallas de USER_FLOWS.md contra la API ya construida y probada.

## Fase 5 — Deploy y beta

- **Entra Flyway** (D53): baseline del esquema + `ddl-auto: validate`. Innegociable
  antes de datos reales.
- Descongelar Dockerfiles + Caddy + Compose completo (ya escritos, están en el repo).
  VPS, HTTPS, backups probados (D45).
- Subida de afiches (lo único de HU-20 que quedó pendiente), panel admin pulido.
- Carga de las ~50 fichas (D38) + beta cerrada con espectadores reales (métricas de
  MVP_SCOPE.md).

## Deudas congeladas con condición de reentrada (resumen)

| Qué | Vuelve cuando |
|---|---|
| Spring Security | HU-01 (Fase 2) |
| Flyway | Antes del primer deploy con datos (Fase 5) — innegociable |
| Tests + Testcontainers | Progresivamente desde Fase 2; sí o sí antes de la beta |
| Dockerfiles/Caddy/VPS | Fase 5 |
| Frontend | Fase 4 |
