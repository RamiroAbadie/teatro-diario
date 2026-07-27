# Stack

> Estado: v1.0 — cierre de Etapa 8.
> Cada elección se justificó contra el contexto real (P3: una persona; P9: ≤ USD 10/mes;
> P13: link previews + SEO; fricción de arranque como criterio del fundador), no contra
> tendencias. La decisión abierta de verdad (frontend) tiene ADR propio: ADR-003.

## Aplicación

| Capa | Elección | Justificación / alternativas descartadas |
|---|---|---|
| **Backend** | **Java 21 + Spring Boot** | Tecnología de mayor solidez del fundador (menor fricción de arranque); ecosistema maduro para todo el MVP (Security, JPA, validación). FastAPI: segunda opción sin ventaja concreta acá. Costo aceptado: RAM (~400-600 MB) → el VPS no puede ser el mínimo absoluto |
| **Frontend** | **Next.js (React + SSR)** | Ver ADR-003. Resuelve P13 (previews/SEO) de fábrica; parte de React que el fundador ya tocó; el mejor asistido por IA. Costo: segundo contenedor + curva de conceptos de servidor |
| **Base de datos** | **PostgreSQL** | `pg_trgm` resuelve la búsqueda del MVP (D23) dentro de la base → **elimina la necesidad de un motor de búsqueda como tecnología aparte**. Familiaridad previa vía Supabase. MySQL no ofrece nada superior acá |
| **Auth** | **Spring Security + sesiones (cookie HTTP-only)** | Mismo origen detrás del proxy: no hace falta JWT. JWT entra cuando llegue la app móvil; D34 lo permite sin rediseño |
| **Búsqueda** | Postgres (`pg_trgm` + índices) | Sin Elasticsearch ni similares. Peldaño futuro solo con evidencia de que la DB no alcanza |
| **Imágenes (afiches)** | Disco local + volumen Docker, redimensionado al subir | Objeto (R2/S3) solo si el volumen o los backups lo justifican (P9) |

## Operación

| Pieza | Elección | Justificación |
|---|---|---|
| **Servidor** | VPS chico (referencia: Hetzner, ~USD 5-6/mes) | Justificado por P9 contra free tiers de PaaS con evidencia: apagado por inactividad (arranques de 30+ s) y bases gratuitas que expiran. Un servidor, una factura |
| **Contenedores** | Docker Compose: app Spring + app Next + Postgres + Caddy | D35. Sin orquestación |
| **Reverse proxy** | **Caddy** | HTTPS automático (Let's Encrypt), configuración mínima |
| **Backups** | **Dos, separados**: `pg_dump` nocturno → Cloudflare R2 para PostgreSQL, y una copia propia del **volumen de afiches**, que el `pg_dump` no toca (D77) | **Obligatorios desde el primer usuario real.** Las **dos** restauraciones —base y volumen— se prueban al menos una vez antes del lanzamiento: sin la segunda, restaurar deja el catálogo entero sin imágenes |
| **Observabilidad** | Logs a stdout (`docker logs`) + UptimeRobot (gratis) | Nada más hasta que duela. Métricas/APM: peldaño futuro con problema concreto |
| **CI** | GitHub Actions (gratis en repos públicos) | Build + tests en cada PR |

## Costo mensual total estimado

**~USD 6-7/mes** (VPS) + dominio (~USD 12/año). Dentro de P9 con margen.

## Deudas de aprendizaje aceptadas (explícitas)

1. Docker/Compose en producción (D35) — se aprende durante el desarrollo, no en el deploy.
2. Conceptos de servidor de Next.js (qué se renderiza dónde, rutas).
3. Operación básica de un VPS (SSH, actualizaciones de seguridad, disciplina de backups).
   Recurso disponible: consultas teóricas a colegas del trabajo; la ejecución es propia.
