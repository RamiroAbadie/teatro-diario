# Decision Log

> Registro vivo de decisiones (D), supuestos (S), descartes (X) y pendientes (P).
> Regla: nada pasa de S a D sin una decisión explícita. Formato ligero; las decisiones
> arquitectónicas mayores tendrán ADRs propios en `docs/decisions/` cuando lleguemos a ellas.

## Decisiones tomadas

| ID | Decisión | Notas |
|----|----------|-------|
| D1 | Usuario primario: espectador intensivo de teatro de CABA | El ocasional puede usarlo; no se diseña para convertirlo |
| D2 | Núcleo del producto: diario/registro personal | Lo social es capa, no núcleo |
| D3 | Capa social mínima desde el MVP: perfil público + follow + actividad de seguidos + likes a reseñas | Nada más en etapa inicial |
| D4 | Alcance geográfico inicial: CABA | |
| D5 | Gratuito, open source, web primero, móvil futuro | |
| D6 | Sin venta de entradas ni links a venta en el MVP | Link externo: candidato post-MVP |
| D7 | Catálogo cerrado, curado por admin. Usuarios pueden sugerir producciones faltantes vía formulario que genera ficha semi-armada para aprobación | Carga abierta descartada por duplicados/calidad |
| D8 | Sin agenda de funciones. Cada producción tiene estado: `en cartel` / `cerrada` / `próximamente` (estreno o reestreno) | El usuario registra con fecha libre, no contra una función del sistema |
| D9 | Rating: entero de 1 a 10; los promedios se muestran con decimales | |
| D10 | Reseñas de texto en el MVP | |
| D11 | Likes a reseñas: sí en MVP. Comentarios en reseñas: pospuestos post-MVP | Comentarios implican moderación, reportes y notificaciones prematuros |
| D12 | Unidad central del dominio: la **producción** (texto + director + elenco). Distintas producciones del mismo texto son entidades distintas. El cambio de sala no crea una producción nueva | Preliminar; a consolidar en Etapa 2 |
| D13 | La producción lleva campos de texto simples `obra_original` y `autor/dramaturgo` (sin entidad Obra relacionada todavía) | Permite normalizar a entidad Obra en el futuro sin perder el dato |
| D14 | Elenco y equipo como entidades **Persona** reutilizables, con relación Participación (persona, producción, rol). Modelo de la "opción A" con tolerancia operativa de la "C": duplicados ocasionales se aceptan y se corrigen a mano | Habilita la página de artista (versión mínima: nombre + participaciones, sin foto ni bio) |
| D15 | La **Sala** es entidad de catálogo, curada por el admin (~100-200 salas en CABA) | Complejos multi-sala: detalle a resolver en el esquema |
| D16 | La compañía teatral **no** es entidad del MVP | Si hace falta, se menciona en la descripción de la producción |
| D17 | Roles de Participación: lista cerrada inicial — actor/actriz, dirección, dramaturgia. Una persona puede tener múltiples roles en la misma producción | Roles técnicos (iluminación, escenografía, música) quedan post-MVP |
| D18 | El registro es la acción central, en un solo gesto: producción + fecha **opcional/aproximada** + rating opcional + reseña opcional. No existe "rating sin registro" | La fecha difusa resuelve la carga de historial viejo sin caminos especiales |
| D19 | Re-visto permitido: múltiples registros por usuario-producción, cada uno con su rating | |
| D20 | El promedio de una producción se calcula sobre el **último rating de cada usuario**, no AVG plano de todos los registros | Lógica no obvia: dejar explícito en la implementación |
| D21 | Todo el contenido es público. Sin niveles de privacidad en el MVP | Mata una familia entera de complejidad transversal |
| D22 | Home logueado: feed de actividad de seguidos + accesos a "registrar" y "en cartel". Fallback sin seguidos: actividad global | |
| D23 | Búsqueda simple (texto) sobre producciones, personas y usuarios. Salas no se buscan: se navegan desde fichas. Sin motor de relevancia en MVP | |
| D24 | Onboarding mínimo (el usuario queda en la home). Historial viejo: bajo demanda vía sugerencias (D7) — el flujo de sugerencia **entra al MVP**. Mensaje de lanzamiento: "diario de acá en adelante" | Resuelve la tensión catálogo-actual vs diario-histórico sin precarga |
| D25 | **Listas fuera del MVP.** Van a v1.1: el loop cierra sin ellas y no prueban S1 ni S2 | Cierra P5 |
| D26 | Estadísticas del perfil con alcance quirúrgico: obras por año, promedio de ratings propios y poco más. Solo queries sobre registros propios; sin gráficos elaborados | "Tu año en teatro" (Wrapped) queda post-MVP |
| D27 | **MVP congelado** según `docs/product/MVP_SCOPE.md` v1.0. Cambios de alcance requieren decisión explícita en este log | Cierre de Etapa 4 |
| D28 | Cuatro módulos: **Identidad, Catálogo (incluye curaduría), Diario, Social**. Límites de código dentro de una sola aplicación, con interfaz pública explícita por módulo | Ver `docs/architecture/MODULE_MAP.md` |
| D29 | El **feed es composición** en la capa de aplicación (Social aporta el grafo, Diario la actividad, Identidad los nombres), no una capacidad de Social | Corrección surgida de revisión externa |
| D30 | Las referencias por **ID opaco no son dependencias**. Grafo real de dependencias: solo Diario → Catálogo. Autorización = concern transversal de la capa de aplicación | Corrección surgida de revisión externa |
| D31 | Eventos de dominio identificados (`RegistroCreado`, etc.) pero **no implementados**: cero consumidores reales en el MVP. Preparación futura = disciplina de límites, no infraestructura | La discusión de mensajería/brokers se cierra en Etapa 6 |
| D32 | **Monolito modular** como arquitectura inicial. Una app desplegable, cuatro módulos como límites de código, una base relacional con propiedad de tablas por módulo | **ADR-001** |
| D33 | **Sin mensajería asincrónica.** Escalera evolutiva: síncrono in-process → despachador in-process → cola en DB → broker externo; cada peldaño solo ante un problema concreto con evidencia | **ADR-002** |
| D34 | El núcleo de negocio no conoce sus caras: casos de uso invocables por igual desde la web actual y la futura API móvil. Ninguna lógica en controladores ni frontend | Habilita D5 sin rehacer nada |
| D35 | Forma operativa: un proceso + una DB + reverse proxy en **Docker Compose** sobre un único servidor. Sin Kubernetes ni orquestación | Docker justificado por paridad dev-prod y simplicidad de deploy; curva de aprendizaje aceptada |
| D36 | Presupuesto: máximo USD 10/mes. Todo gasto justificado; la alternativa gratuita se descarta solo con evidencia (seguridad, funcionalidad clave, o complejidad que agrega). Asciende a principio P9 | También protege del free tier que cuesta más en horas de lo que ahorra en dólares |
| D37 | Presupuesto de horas: 8 hs/semana totales; hasta 3 hs/semana de curaduría post-lanzamiento, con **sesión fija semanal** (rutina, no "cuando pueda"). Desarrollo ~5-6 hs/semana → MVP estimado en 4-6 meses | El roadmap se escribe contra estos números |
| D38 | Catálogo del día uno: **~50 producciones impecables** (comercial + oficial + independientes grandes). Carga inicial manual: 8-17 hs estimadas | Mejor 50 excelentes que 150 mediocres |
| D39 | Fuente de datos inicial: **carga manual**, con datos fácticos de fuentes públicas y sinopsis de redacción propia. Escalera de fuentes en DATA_STRATEGY.md (scraper → salas → curadores comunitarios), cada peldaño ante un problema real | Difiere la cuestión legal del scraping a si/cuando haga falta |
| D40 | **Firmada.** Modificación del MVP congelado: botón "reportar reseña" + cola de reportes en panel admin + borrado por admin | Primera modificación formal vía D27 |
| D41 | Backend: **Java 21 + Spring Boot** | Mayor solidez del fundador; fricción de arranque mínima; ver STACK.md |
| D42 | Base de datos: **PostgreSQL**. La búsqueda del MVP se resuelve con `pg_trgm` dentro de la base — sin motor de búsqueda aparte | |
| D43 | Frontend: **Next.js (React + SSR)** | **ADR-003** — resuelve P13 (previews/SEO) de fábrica |
| D44 | Auth: Spring Security + sesiones con cookie HTTP-only. JWT recién con la app móvil | D34 lo permite sin rediseño |
| D45 | Operación: VPS chico (~USD 5-6/mes) + Docker Compose (Spring, Next, Postgres, Caddy) + backups `pg_dump` nocturnos a R2 + UptimeRobot + GitHub Actions. Total ~USD 6-7/mes | Dentro de P9. Backups obligatorios desde el primer usuario real; restauración probada pre-lanzamiento |
| D46 | Licencia: **AGPL-3.0** | Protege contra clones cerrados tipo SaaS; coherente con producto gratuito y comunitario |
| D47 | **Monorepo**: `/backend`, `/frontend`, `/docs`, compose en raíz | Un solo lugar para issues; features full-stack viajan en un PR; docs junto al código |
| D48 | Secretos: variables de entorno; `.env` local ignorado + `.env.example` versionado; `.gitignore` desde el commit cero. Ninguna credencial en el repo jamás | Cierra P12 |
| D49 | Roadmap en 5 fases (esqueleto que camina → catálogo → diario → social → beta), ~4-6 meses. El deploy es Fase 0, no la última: la deuda operativa se paga primero. Beta cerrada con espectadores reales como pago parcial de P1 | Ver `docs/roadmap/` — cierre de Etapa 10 |
| D50 | 22 historias de usuario con criterios de aceptación (`docs/product/USER_STORIES.md`) + mapa de flujos y pantallas (`docs/product/USER_FLOWS.md`) como fuente de los issues de Fases 1-3. Regla: historia que no mapea a una decisión del log = cambio de alcance (D27) | Cuatro micro-decisiones por defecto (MD-1 a MD-4) atacables en USER_STORIES.md |
| D51 | **Ajuste de ritmo (no de arquitectura), tras sobrecarga real del fundador en Fase 0.** Se congela con condición de reentrada todo lo que no bloquea escribir producto. Los módulos (ADR-001) se mantienen: son estructura de carpetas, no herramienta | R3 se materializó; el plan se ajusta donde es barato |
| D52 | Spring Security **fuera del pom** hasta HU-01 (login, Fase 1) | Reentrada: al implementar auth |
| D53 | **Flyway diferido**: en desarrollo, `ddl-auto: update` (Hibernate crea el esquema desde las entidades). Condición innegociable de reentrada: **antes del primer deploy con datos reales**, migración baseline de Flyway + pasar a `validate` | El drift solo es riesgo con una base de producción que proteger; hoy no existe |
| D54 | Docker local reducido a **un comando**: `docker compose up -d postgres`. Dockerfiles, Caddy, Compose completo y VPS: congelados hasta la fase de deploy (post-catálogo funcionando). Tests pospuestos salvo `ModulithArchitectureTest` (3 líneas, esta semana). Testcontainers vuelve con los tests. CI existente se conserva (mantenimiento cero) | Pierde parte del argumento "deuda riesgosa primero" de D49; aceptado como precio de la motivación, que hoy es la restricción más dura |
| D55 | Frontend fuera de la mesa hasta backend de catálogo funcional. ADR-003 (Next.js) sin cambios; si el fundador quiere rediscutirlo al llegar, se rediscute entonces con decisión explícita. Nota: React Native es mobile — D5 sigue siendo web primero | No decidir en caliente lo que no bloquea hoy |
| D56 | **Reentrada de D52**: Spring Security vuelve con HU-01/02. Sesión con cookie HTTP-only y `SameSite=Lax` (D44), BCrypt, login por email o username. El rol vive en `Usuario` (`USUARIO`/`ADMIN`) y el candado de `/api/admin/**` se define en la capa de aplicación, no en Catálogo. Sin panel de roles: el primer admin se promueve con un `UPDATE` a mano, documentado en el README | El chequeo de admin sigue siendo autorización transversal (MODULE_MAP), así que Catálogo no pasa a depender de Identidad. `secure: true` en la cookie entra en la Fase 5, con HTTPS |
| D57 | CSRF activado desde el día uno: token en cookie legible (`XSRF-TOKEN`) + header `X-XSRF-TOKEN`, sin enmascarado BREACH porque la API nunca renderiza el token en el cuerpo | Se evaluó postergarlo hasta el frontend (Fase 4): se descartó porque la sesión con cookie ya es explotable y activarlo después es más caro que convivir con él en las pruebas por curl |

## Supuestos (hipótesis, no requisitos)

| ID | Supuesto | Evidencia | Riesgo si es falso |
|----|----------|-----------|--------------------|
| S1 | Existe un segmento de espectadores intensivos en CABA que quiere registrar y compartir lo que ve, y sus soluciones actuales (Instagram + memoria + entradas) le resultan insuficientes | Conversaciones informales con un puñado de amigos/conocidos espectadores. Sin validación formal, por decisión del fundador | El producto no tiene demanda. Contramedida: MVP mínimo |
| S2 | La capa social mínima es necesaria desde el día uno para la adopción | Intuición del fundador; la contraparte técnica hubiera pospuesto lo social | Se construye capa social para una plataforma sin masa crítica |
| S3 | El comportamiento de los usuarios objetivo (publicar en Instagram, seguir influencers teatrales) se traslada a una plataforma estructurada | Observación indirecta | El hábito no migra; el registro no compite con la story |

## Descartado por ahora

| ID | Descarte | Razón |
|----|----------|-------|
| X1 | Gamificación para convertir espectadores ocasionales en intensivos | Hipótesis sin evidencia; la identidad "teatrera" no la crea una app. Estadísticas personales simples sí entran como parte del diario |
| X2 | Recomendación algorítmica | Complejidad sin masa de datos que la justifique |
| X3 | Competir con Instagram en lo visual/efímero | Explorar a futuro integración (compartir logs como imagen para stories) en lugar de competir |
| X4 | Agenda de funciones con fechas y horarios | Dato que caduca a diario; deuda operativa insostenible para una persona (ver principio P6). Posiblemente nunca se incorpore |
| X5 | Comentarios en reseñas (en MVP) | Ver D11 |

## Pendientes

| ID | Pendiente | Etapa donde se resuelve |
|----|-----------|------------------------|
| P1 | Entrevistas con 5-10 espectadores intensivos de CABA | Recomendado antes de congelar el MVP; pospuesto por decisión del fundador |
| P2 | Nombre del producto | Libre |
| P15 | MD-5: ¿cambio de contraseña en el MVP o post-MVP? (única "configuración" candidata; sin email transaccional, el "olvidé mi contraseña" es otro problema encadenado) | Antes de Fase 1 |
| ~~P3~~ | **Resuelto**: ~50 producciones impecables (D38). Lista concreta de títulos: tarea pre-lanzamiento | Cerrado en Etapa 7 |
| ~~P4~~ | **Resuelto en diferido**: escalera de fuentes en DATA_STRATEGY.md; el scraper es el peldaño 2, con revisión legal en su momento (D39) | Cerrado en Etapa 7 |
| ~~P5~~ | **Resuelto**: listas fuera del MVP (D25) | Cerrado en Etapa 4 |
| ~~P6~~ | **Resuelto**: reemplazos/reestrenos los decide el admin caso por caso, con sesgo a "misma producción" si se mantienen texto y dirección | Cerrado en Etapa 2 |
| ~~P7~~ | **Resuelto**: rating 1–10 confirmado (D9, D18) | Cerrado en Etapa 3 |
| ~~P8~~ | **Resuelto**: ver `docs/domain/GLOSSARY.md` y `docs/domain/DOMAIN_MODEL.md` | Cerrado en Etapa 2 |
| P9 | Modelado de complejos multi-sala (campo texto vs auto-relación) | Diseño de esquema |
| ~~P10~~ | **Resuelto**: ver D18–D21 y `docs/product/CORE_LOOP.md` | Cerrado en Etapa 3 |
| ~~P11~~ | **Resuelto**: AGPL-3.0 (D46) | Cerrado en Etapa 9 |
| ~~P12~~ | **Resuelto**: ver D48 | Cerrado en Etapa 9 |
| ~~P13~~ | **Resuelto**: Next.js (D43, ADR-003) | Cerrado en Etapa 8 |
| ~~P14~~ | **Resuelto**: afiches en disco local con volumen; backups `pg_dump` → R2 (D45) | Cerrado en Etapa 8 |
