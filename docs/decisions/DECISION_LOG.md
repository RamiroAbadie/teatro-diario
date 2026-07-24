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
| D30 | Las referencias por **ID opaco no son dependencias**. Grafo real de dependencias: solo Diario → Catálogo. Autorización = concern transversal de la capa de aplicación | Corrección surgida de revisión externa. ⚠️ **Matizada por D61**: transversal es saber quién sos y si sos admin; que un registro sea tuyo lo hace cumplir Diario, que es quien tiene el dato |
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
| D58 | **El "último rating" de D20 es el de la función más reciente**, no el del registro cargado más recientemente: ordena por fecha del registro (los sin fecha al fondo) y desempata por orden de carga. Se resuelve en la base con `DISTINCT ON` de Postgres, en una sola query | La alternativa —último cargado, más simple y portable— rompe el promedio justo en el caso que D24 fomenta: quien sube hoy una salida de 2019 pisaría lo que opinó el mes pasado |
| D59 | **La fecha difusa (MD-1) se guarda normalizada al comienzo de su período** (`marzo de 2023` → `2023-03-01`) junto con la granularidad, y los registros sin fecha viajan en una lista aparte de la respuesta, no mezclados. El orden de MD-2 es **fecha, después precisión, después carga**: la fecha sola no alcanza porque el 1 de enero de 2023 y "2023" a secas se guardan iguales, y ahí el día exacto va primero | Ni se inventan fechas falsas ni se ordena a ojo. La granularidad es además lo que el frontend necesita para mostrar "marzo de 2023" y no "1 de marzo de 2023". El empate lo encontró una revisión externa después de la primera implementación, que ordenaba solo por fecha y carga. El orden termina en el id para que sea total: hay un test con las tres colisiones de granularidad (día-mes, día-año y mes-año) |
| D60 | **Los controladores del diario viven en la capa de aplicación**, no en `diario/internal/` como el resto del proyecto: registrar exige traducir la sesión a un `usuarioId` y firmar reseñas exige usernames, y las dos cosas son de Identidad, de la que Diario no puede depender. Por lo mismo, el promedio y las reseñas se sirven en `/api/producciones/{id}/opiniones` y no adentro de la ficha, que es de Catálogo | Es exactamente el concern transversal que describe MODULE_MAP (autorización + composición). Se evaluó componer una ficha única en la capa de aplicación: se descartó por costo (refactor de todos los DTOs de Catálogo) y porque dos pedidos desde una pantalla no son lógica de negocio en el front (D34). Los casos de uso siguen en el módulo: en los controladores no hay ninguna regla |
| D61 | **La autorización se parte en dos** y MODULE_MAP se corrige para decirlo: la identidad y el rol de admin son transversales (capa de aplicación), pero "este registro es tuyo" lo hace cumplir Diario, que es quien tiene el dato, con el `user_id` que la aplicación le pasa | Surgido de una revisión externa que marcó que el código y el documento no describían lo mismo. Se corrige el documento y no el código: mover el chequeo afuera obliga a exponer el dueño de cada registro en la interfaz pública y a leerlo antes de escribir —dos consultas y una ventana entre una y otra— para quedar más débil que ahora |
| D62 | **El registro guarda una copia del título de la producción**, para que el historial siga siendo legible después de que el admin borre una ficha. Mientras la ficha exista manda el título vivo del catálogo; la respuesta marca con `enCatalogo` si todavía hay adónde ir. **No es fusión de duplicados**: los registros de la ficha borrada siguen apuntando a su id muerto, y reasignarlos a la ficha buena sería otra operación, que hoy no existe y que ninguna historia pide | También de la revisión externa: el registro huérfano conservaba fecha, puntaje y reseña, y perdía lo único que lo hace un historial. Se descartó impedir el borrado —Catálogo no puede consultar a Diario (MODULE_MAP), así que obliga a mover el DELETE de producciones a la capa de aplicación, y le saca al admin la única herramienta que tiene para limpiar una ficha equivocada— y la baja lógica, que toca todas las consultas del catálogo y agrega un estado paralelo a los de D8. Límite aceptado: la copia es del momento de registrar, así que corregir el título de una ficha y borrarla después deja en el diario el título viejo. Refrescar copias al corregir una ficha necesita que Catálogo avise a Diario, y eso son eventos (D31) |

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
