# ADR-003 — Next.js (SSR) como frontend

**Estado:** aceptada — julio 2026
**Contexto de decisión:** Etapa 8. La única elección de stack genuinamente abierta.

## Contexto

Requisito duro heredado de la arquitectura (P13, derivado de D34 y del análisis de
producto): las fichas de producción y los perfiles **se comparten por WhatsApp e
Instagram** — es la mecánica de crecimiento natural dado dónde vive el público objetivo —
y necesitan link previews (Open Graph) e indexabilidad en Google. El fundador conoce
React superficialmente y declara debilidad en frontend (mucho código asistido por IA).

## Alternativas

1. **React SPA pura**: la opción de mayor familiaridad. Falla el requisito P13 de fábrica
   (previews e indexación pobres); mitigarlo (prerendering, SSR casero) cuesta más que
   usar una herramienta que lo trae resuelto. Descartada con la propia regla del proyecto.
2. **Spring + Thymeleaf (HTML del servidor) + JS mínimo**: la más simple en términos
   absolutos — un solo contenedor, SEO perfecto, cero framework nuevo. Costos: front
   interactivo artesanal (búsqueda con autocompletado del gesto de registro), habilidad
   menos transferible, y migración dolorosa si el producto pide interactividad creciente.
   Defendible; descartada por decisión del fundador priorizando React como base.
3. **Next.js**: React con renderizado en servidor. P13 resuelto de fábrica; parte del
   React ya conocido; el ecosistema mejor asistido por IA (relevante para un fundador
   débil en front). Costos: un segundo contenedor (proceso Node) y la curva de los
   conceptos de servidor de Next.

## Decisión

Next.js. El frontend consume la API del backend Spring; ninguna lógica de negocio vive
en Next (D34). Las páginas públicas (fichas, perfiles, "en cartel") se renderizan en
servidor con sus metadatos Open Graph; lo interactivo (gesto de registro) es React normal.

## Consecuencias

- (+) Compartir una ficha por WhatsApp muestra título + afiche + sinopsis. La mecánica
  de crecimiento funciona desde el día uno.
- (+) Google indexa el catálogo: "nombre de la obra" encuentra al producto.
- (−) Dos aplicaciones que buildear y desplegar (mitigado: mismo Compose, misma CI).
- (−) Curva de Next.js aceptada como deuda de aprendizaje explícita (STACK.md).
- Cláusula de salida: si Next resultara desproporcionado en la práctica, la alternativa 2
  sigue disponible — el núcleo agnóstico (D34) hace que el frontend sea reemplazable.
