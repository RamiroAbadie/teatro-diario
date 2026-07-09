# Roadmap

> Estado: v1.0 — cierre de Etapa 10 y del proceso fundacional.
> Escrito contra la restricción real: **~5-6 hs/semana de desarrollo** (D37).
> Estimación total hasta lanzamiento: **4-6 meses**. Si los tiempos incomodan, se negocia
> alcance (el Decision Log tiene la lista de recortes), nunca los límites internos.

## Principio rector del plan: el esqueleto camina primero

El deploy NO es la última fase. La deuda de aprendizaje más riesgosa del proyecto es
operativa (Docker, VPS, primera vez en producción — STACK.md), y las deudas riesgosas se
pagan primero, cuando equivocarse es barato. Por eso el primer incremento es un
"walking skeleton": la tubería completa funcionando de punta a punta con contenido trivial.

## Fase 0 — Fundaciones y esqueleto que camina (semanas 1-3)

**Objetivo:** `docker compose up` local levanta todo; el mismo sistema corre en el VPS
con HTTPS; CI verde. Cero funcionalidad de producto.

- Monorepo (D47): `/backend`, `/frontend`, `/docs` (esta documentación entra al repo), 
  `docker-compose.yml`. Licencia AGPL-3.0 (D46). `.gitignore` + `.env.example` desde el
  commit cero (D48).
- Esqueleto Spring Boot con la estructura de paquetes por módulo (identidad, catalogo,
  diario, social + capa de aplicación) — los límites de ADR-001 existen desde el día uno.
- Esqueleto Next.js con una página SSR trivial que consume un endpoint trivial de Spring.
- Compose: Spring + Next + Postgres + Caddy.
- VPS contratado, Compose desplegado, HTTPS funcionando, UptimeRobot mirando.
- GitHub Actions: build + tests de ambas apps en cada PR.

**Criterio de salida:** una URL pública con HTTPS que muestra datos que salieron de
Postgres pasando por Spring y renderizados por Next en el servidor.

## Fase 1 — Catálogo e identidad (semanas 4-9)

**Objetivo:** el admin puede construir el catálogo; existe la ficha pública.

- Identidad mínima: registro, login (sesiones, D44), perfil básico.
- Módulo Catálogo: producciones, personas, participaciones, salas, estados.
- Panel de admin (CRUD completo, calidad de herramienta interna).
- Ficha pública de producción y página de artista con SSR + metadatos Open Graph (ADR-003
  rinde acá): compartir una ficha por WhatsApp ya muestra preview.
- Vista "en cartel".
- **En paralelo (horas de curaduría):** carga de salas y primeras fichas reales — el
  catálogo se construye durante el desarrollo, no después.

## Fase 2 — Diario, el corazón (semanas 10-15)

**Objetivo:** el gesto central completo. Al final de esta fase, el producto ya es un diario.

- El registro en un gesto (D18): buscar producción + fecha opcional/aproximada + rating +
  reseña. Editar/borrar. Re-visto (D19).
- Promedio por producción con la lógica D20 (⚠️ no AVG plano).
- Perfil/diario del usuario: historial + stats mínimas (D26).
- Búsqueda `pg_trgm` sobre producciones y personas (D42).

## Fase 3 — Social y válvulas (semanas 16-19)

- Follow/unfollow; feed como composición (D29) con fallback global (D22); likes.
- Búsqueda de usuarios.
- Sugerencias de producciones → cola de aprobación en panel admin (D24).
- Reportar reseña → cola en panel admin (D40).

## Fase 4 — Endurecimiento y beta (semanas 20-24)

- Backups nocturnos a R2 **con restauración probada** (D45).
- Catálogo a las ~50 fichas impecables (D38).
- **Beta cerrada con el puñado de amigos/conocidos espectadores** — acá se paga
  parcialmente la deuda de validación P1: son las primeras personas reales usando el
  loop completo. Observar contra las métricas de MVP_SCOPE.md (¿registran 3+ obras?
  ¿vuelven?).
- Correcciones de la beta → lanzamiento abierto.

## Después del lanzamiento (v1.1+, sin fechas)

En orden tentativo por valor/costo: listas (D25) → compartir registro como imagen para
stories (X3) → scraper de precarga interno (D39, con su revisión legal) → comentarios
(X5) → "tu año en teatro" (diciembre). Cada una entra por decisión explícita en el log.

## Reglas del plan

1. Las fases no se solapan en desarrollo: una feature de Fase 3 no empieza con Fase 2 abierta.
2. La curaduría corre en paralelo desde la Fase 1 con sus propias horas (D37).
3. Al cerrar cada fase: revisar el Decision Log — ¿alguna decisión quedó desactualizada
   por la realidad? Se actualiza el log, no la memoria.
