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
5. `product/USER_STORIES.md` — las 22 historias con criterios de aceptación.
6. `product/USER_FLOWS.md` — pantallas, flujos, estados vacíos y de error.
7. `product/UI_REFERENCES.md` — exploración de referencias de interfaz, previa a la Fase 4.
8. `product/DESIGN_SYSTEM.md` — tokens, los 10 componentes de `ui/` y cómo se dibuja lo que
   la API devuelve nulo.
9. `product/SCREEN_SPECS.md` — una entrada por pantalla: datos, composición, islas cliente y
   los cuatro estados obligatorios.

**Para entender el dominio:**
5. `domain/DOMAIN_MODEL.md` — entidades y relaciones conceptuales.
6. `domain/GLOSSARY.md` — vocabulario común obligatorio.

**Para entender el sistema:**
7. `architecture/MODULE_MAP.md` — los cuatro módulos y su (mínimo) grafo de dependencias.
8. `architecture/ARCHITECTURE.md` — la forma general y las reglas estructurales.
9. `architecture/DATA_STRATEGY.md` — de dónde salen los datos (⚠️ riesgo #2 del proyecto).
10. `architecture/STACK.md` — tecnologías elegidas y justificadas.
11. `architecture/API.md` — el contrato HTTP que consume el frontend.
12. `architecture/FRONTEND_ARCHITECTURE.md` — dónde va cada cosa en `/frontend`.

**Para entender por qué:**
13. `decisions/DECISION_LOG.md` — TODAS las decisiones (D), supuestos (S), descartes (X)
    y pendientes (P). **El documento vivo más importante del repo.**
14. `decisions/ADR-001..003` — las tres decisiones arquitectónicas mayores.

**Para ejecutar:**
15. `roadmap/ROADMAP.md` — fases contra las horas reales disponibles.
16. `roadmap/INITIAL_BACKLOG.md` — issues de Fase 0-1 y definición del primer incremento.
17. `roadmap/RISKS.md` — riesgos honestos, en orden de gravedad.

## Regla de mantenimiento

Si la realidad contradice un documento, se actualiza el documento — el log registra el
cambio. Un documento desactualizado es peor que ninguno.
