# CLAUde.md — Contexto para asistentes de IA

> Este archivo va en la raíz del monorepo. Resume las reglas innegociables del proyecto
> y apunta a `docs/` para el detalle. Si una instrucción de un prompt contradice este
> archivo, frenar y avisar en lugar de obedecer.

## Qué es este proyecto

Plataforma social de registro de teatro ("diario personal del teatro de Buenos Aires").
Web app. Un solo desarrollador. Toda la documentación fundacional vive en `docs/`
(empezar por `docs/README.md`, el índice). Las decisiones y su porqué están en
`docs/decisions/DECISION_LOG.md` — **es la fuente de verdad**.

## Stack (no proponer alternativas: ya está decidido y justificado en docs/architecture/STACK.md)

- Backend: Java 21 + Spring Boot, en `/backend`
- Base de datos: PostgreSQL (corre local con `docker compose up -d postgres`)
- Frontend: **Next.js (App Router) + Tailwind, en `/frontend`** — se abre en la Fase 4, que es
  la actual. ADR-003 (Next) y D73 (Tailwind como única dependencia nueva: los componentes se
  escriben a mano, sin librería de UI). D55 —"frontend fuera de la mesa"— **está cumplida y
  cerrada**: era hasta tener el backend de catálogo funcionando, y lo está. Sigue vigente lo
  otro: no proponer frameworks alternativos. **Dónde va cada cosa lo dice
  `docs/architecture/FRONTEND_ARCHITECTURE.md` (D78), y el contrato con el backend,
  `docs/architecture/API.md`** — los dos son de lectura obligatoria antes de escribir una pantalla
- Esquema de DB: por ahora `ddl-auto: update` (D53). NO introducir Flyway hasta la fase
  de deploy. Spring Security ya está (reentró en D56/D57 con HU-01/02): sesión con cookie
  HTTP-only y CSRF activo

## Reglas innegociables de arquitectura

1. **Monolito modular** (ADR-001). Cuatro módulos en el backend: `identidad`, `catalogo`,
   `diario`, `social`, más capa `aplicacion`. Cada módulo expone una interfaz pública;
   el resto es privado.
2. **PROHIBIDO cruzar límites de módulos por los costados**: ni imports de clases internas
   de otro módulo, ni queries a tablas de otro módulo. Única dependencia permitida entre
   módulos: Diario → Catálogo (`docs/architecture/MODULE_MAP.md`).
3. **Sin mensajería, sin brokers, sin colas** (ADR-002). Llamadas síncronas in-process.
   No introducir RabbitMQ/Kafka/Redis bajo ninguna justificación de "escalabilidad".
4. **Ninguna lógica de negocio en Next ni en controladores** (D34). El frontend consume
   la API; los casos de uso viven en la capa de aplicación del backend.
5. **El feed es una composición en la capa de aplicación** (D29), no un módulo ni una
   tabla materializada.
6. **Páginas públicas (fichas, perfiles, en-cartel) van con SSR + metadatos Open Graph**
   (ADR-003). Es requisito de producto, no detalle.

## Reglas de dominio que la IA suele romper si no se le avisa

- El **promedio de una producción NO es AVG(rating)**: es el promedio del ÚLTIMO rating
  de cada usuario (D20). Ver `docs/product/CORE_LOOP.md`.
- Un usuario puede registrar la misma producción **varias veces** (re-visto, D19).
- La **fecha del registro es opcional y difusa**: día / mes-año / año / sin fecha (MD-1).
- El catálogo es **cerrado**: solo el admin crea producciones/personas/salas (D7).
  Los usuarios solo sugieren.
- Rating: entero 1-10 (D9). Todo el contenido es público, no hay privacidad (D21).
- No existen: notificaciones, emails, comentarios, listas, agenda de funciones (ver
  `docs/product/MVP_SCOPE.md` — el alcance está CONGELADO, D27).

## Flujo de trabajo

- El trabajo sale de `docs/roadmap/INITIAL_BACKLOG.md` y de las historias con criterios
  de aceptación en `docs/product/USER_STORIES.md`. Una historia = la referencia de
  "terminado".
- Cambios chicos y revisables. Nada de generar el proyecto entero de una vez.
- Si una tarea parece requerir algo fuera del alcance congelado o contra una decisión
  del log: **frenar y avisar**, no implementar.
- Secretos SOLO por variables de entorno; jamás en el código ni en el repo (D48).
- MODO ESENCIAL (D51): no proponer herramientas, dependencias ni infraestructura nuevas.
  Las capas dentro de cada módulo son las clásicas: entidad / repository (solo interface,
  Spring Data genera la impl) / service / controller / DTOs como records, bajo
  `<modulo>/internal/`. Lo público del módulo va en la raíz del paquete del módulo.
  Excepción registrada (D60): un controlador que necesita datos de dos módulos —traducir
  la sesión a un `usuarioId`, ponerle nombre a un id opaco— va en la capa de aplicación,
  porque los módulos no pueden depender entre sí para conseguirlos. Es el caso de los
  endpoints del diario. La regla que manda es el grafo de dependencias, no la carpeta.

## Definición de "hecho"

Código en main vía PR con CI verde + funciona en el VPS + si tocó una decisión,
`docs/decisions/DECISION_LOG.md` actualizado.
