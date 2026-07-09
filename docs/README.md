# Documentación fundacional

Producto: plataforma de registro y descubrimiento de teatro — "el diario personal del
teatro de Buenos Aires". Esta documentación es el resultado de un proceso fundacional
iterativo (julio 2026) que recorrió: visión → dominio → loop del usuario → MVP →
módulos → arquitectura → datos → stack → open source → plan de ejecución.

## Mapa de lectura

**Para entender el producto:**
1. `product/PRODUCT_VISION.md` — qué es, para quién, qué NO es.
2. `product/PRODUCT_PRINCIPLES.md` — las reglas con las que se decide (P1-P9).
3. `product/CORE_LOOP.md` — la acción central y el sistema de comportamiento.
4. `product/MVP_SCOPE.md` — el alcance congelado y qué prueba.

**Para entender el dominio:**
5. `domain/DOMAIN_MODEL.md` — entidades y relaciones conceptuales.
6. `domain/GLOSSARY.md` — vocabulario común obligatorio.

**Para entender el sistema:**
7. `architecture/MODULE_MAP.md` — los cuatro módulos y su (mínimo) grafo de dependencias.
8. `architecture/ARCHITECTURE.md` — la forma general y las reglas estructurales.
9. `architecture/DATA_STRATEGY.md` — de dónde salen los datos (⚠️ riesgo #2 del proyecto).
10. `architecture/STACK.md` — tecnologías elegidas y justificadas.

**Para entender por qué:**
11. `decisions/DECISION_LOG.md` — TODAS las decisiones (D), supuestos (S), descartes (X)
    y pendientes (P). **El documento vivo más importante del repo.**
12. `decisions/ADR-001..003` — las tres decisiones arquitectónicas mayores.

**Para ejecutar:**
13. `roadmap/ROADMAP.md` — fases contra las horas reales disponibles.
14. `roadmap/INITIAL_BACKLOG.md` — issues de Fase 0-1 y definición del primer incremento.
15. `roadmap/RISKS.md` — riesgos honestos, en orden de gravedad.

## Regla de mantenimiento

Si la realidad contradice un documento, se actualiza el documento — el log registra el
cambio. Un documento desactualizado es peor que ninguno.
