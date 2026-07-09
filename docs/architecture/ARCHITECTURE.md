# Architecture

> Estado: v1.0 — cierre de Etapa 6.
> Las dos decisiones mayores tienen ADR propio: ADR-001 (monolito modular),
> ADR-002 (sin mensajería: escalera de asincronía).

## Forma general

**Un monolito modular**: una sola aplicación desplegable, con los cuatro módulos de
MODULE_MAP.md como límites de código internos (interfaz pública explícita por módulo,
prohibido cruzar límites por los costados).

```
┌─────────────────────────────────────────────┐
│                 Capa web / API              │  ← detalle reemplazable
├─────────────────────────────────────────────┤
│   Capa de aplicación (casos de uso,         │  ← composición (feed, vistas),
│   autorización, transacciones)              │     autorización transversal
├──────────┬──────────┬──────────┬────────────┤
│ Identidad│ Catálogo │  Diario  │   Social   │  ← módulos con autoridad propia
│          │          │    │     │            │
│          │◄─────────┼────┘     │            │  ← única dependencia: Diario→Catálogo
├──────────┴──────────┴──────────┴────────────┤
│        Una base de datos relacional          │  ← esquema único; propiedad de tablas
│                                              │     por módulo (convención, no física)
└─────────────────────────────────────────────┘
```

## Reglas estructurales

1. **El núcleo no conoce sus caras.** Los casos de uso son invocables por igual desde la
   capa web actual y desde la futura API móvil (D5). Ninguna lógica de negocio vive en
   controladores ni en el frontend.
2. **Un módulo = un paquete/carpeta con interfaz pública.** El resto es privado. Las
   queries a tablas de otro módulo están prohibidas; se pasa por su interfaz.
3. **Una sola base relacional, un solo esquema.** Cada tabla pertenece a un módulo
   (convención documentada). Esto preserva transacciones ACID simples (registrar +
   actualizar promedio) que una arquitectura distribuida convertiría en problema.
4. **Comunicación entre módulos: llamadas síncronas in-process.** Ver ADR-002 para la
   escalera evolutiva de asincronía.
5. **La elección de frontend (Etapa 8) está condicionada por un requisito de producto:**
   fichas y perfiles se comparten por WhatsApp/Instagram y deben tener link previews
   (Open Graph) e indexabilidad en Google. Cualquier opción de frontend debe resolverlo.

## Forma operativa

- **Un proceso de aplicación + una base de datos + un reverse proxy**, todo en
  **Docker Compose** sobre un único servidor.
- Sin Kubernetes, sin orquestación, sin servicios administrados salvo justificación por P9.
- Los afiches/imágenes del catálogo necesitan almacenamiento de archivos: disco local con
  volumen en el MVP; almacenamiento de objetos si/cuando el costo o los backups lo
  justifiquen (se resuelve en Etapas 7–8).
- Backups de la base: obligatorios desde el día uno de tener usuarios reales. Estrategia
  concreta en Etapa 8.

## Restricciones de contexto que esta arquitectura respeta

- **P3:** una persona opera todo. Una app, una DB, un servidor, una factura.
- **P9:** presupuesto máximo USD 10/mes; todo gasto justificado contra la alternativa gratuita.
- **P4:** evolucionable sin reescritura — los límites de módulos son los futuros límites de
  servicios *si alguna vez hace falta*, y la escalera de ADR-002 cubre la asincronía.
- Experiencia operativa del fundador: primera vez operando producción. Docker Compose se
  justifica por paridad dev-prod y simplicidad de deploy, aceptando su curva de aprendizaje.

## Qué NO hay (deliberadamente)

Microservicios, message brokers (RabbitMQ incluido), Redis/caché externa, motor de búsqueda,
CDN, colas administradas, funciones serverless. Cada uno tiene su condición de entrada
definida (ADR-002 y Etapa 8); ninguno tiene un problema concreto que resolver hoy.
