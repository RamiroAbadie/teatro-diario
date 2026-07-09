# ADR-001 — Monolito modular como arquitectura inicial

**Estado:** aceptada — julio 2026
**Contexto de decisión:** Etapa 6 del proceso fundacional.

## Contexto

Producto nuevo, sin usuarios, desarrollado y operado por una sola persona a tiempo parcial
(P3), con presupuesto máximo de USD 10/mes (P9), open source, web primero con app móvil
futura (D5). Experiencia previa del fundador en un proyecto académico con módulos
comunicados por mensajería (RabbitMQ), cuya arquitectura se revisó críticamente en lugar
de heredarse.

## Alternativas consideradas

1. **Monolito tradicional** (sin límites internos): mínimo esfuerzo inicial, pero viola P4 —
   cuando el producto crece, obliga a reescribir porque no hay costuras por donde separar.
2. **Monolito modular**: una aplicación, cuatro módulos con interfaz pública explícita
   (MODULE_MAP.md), una base de datos.
3. **Microservicios**: resuelven problemas organizacionales (equipos independientes,
   deploys independientes) y de escala extrema. Ninguno de esos problemas existe: no hay
   equipos, no hay escala. Su costo es concreto e inmediato: múltiples deploys, red entre
   servicios (fallos parciales, timeouts, retries), observabilidad distribuida, y
   transacciones cross-servicio para operaciones hoy triviales (registrar + actualizar
   promedio). Viola P3 y P9 sin resolver nada.

## Decisión

Monolito modular. Los límites entre módulos son de código (paquetes con interfaz pública),
no de red. Una sola base de datos relacional con propiedad de tablas por módulo como
convención documentada.

## Consecuencias

- (+) Una unidad de deploy, una factura, transacciones ACID simples, debugging local completo.
- (+) Los límites de módulos son las futuras costuras de separación si algún día un módulo
  necesita escalar de forma independiente (hipótesis registradas en MODULE_MAP.md).
- (−) Exige disciplina: los límites internos no los defiende ninguna barrera física.
  Mitigación: interfaz pública explícita por módulo, prohibición de queries cruzadas,
  y revisión de esa regla en cada PR propio.
- (−) Si el proyecto sumara colaboradores intensivos, la falta de deploys independientes
  podría doler — problema deseable y lejano; se re-evalúa si ocurre.
