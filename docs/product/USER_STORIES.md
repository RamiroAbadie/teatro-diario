# User Stories — MVP

> Estado: v1.0. Las historias del MVP congelado (D27 + D40), con criterios de aceptación
> verificables. Cada historia es trazable a decisiones del log — **una historia que no
> mapea a ninguna decisión es scope colándose y requiere decisión explícita antes de
> implementarse.** Estas historias son la fuente de los issues de GitHub de las Fases 1-3.
>
> Actores: **Visitante** (sin cuenta), **Usuario** (con cuenta), **Admin** (el fundador).

---

## Épica A — Identidad (módulo Identidad · Fase 1)

**HU-01 · Crear cuenta** — Como visitante quiero crear una cuenta con email, username y
contraseña para tener mi diario.
Criterios: username único y validado (formato URL-safe: será parte de la URL del perfil);
email único; contraseña con mínimo razonable; errores claros por campo; al completar,
quedo logueado y en la home.

**HU-02 · Login / logout** — Como usuario quiero entrar y salir de mi cuenta.
Criterios: sesión con cookie HTTP-only (D44); login por email o username; mensaje genérico
ante credenciales inválidas (sin revelar cuál campo falló); logout invalida la sesión.

**HU-03 · Ver perfil público** — Como visitante quiero ver el perfil de cualquier usuario
(es la carta de presentación que se comparte).
Criterios: URL `/{username}`; muestra diario, stats y reseñas; renderizado SSR con
metadatos Open Graph (P13); accesible sin login (D21).

---

## Épica B — Catálogo público (módulo Catálogo · Fase 1)

**HU-04 · Ver ficha de producción** — Como visitante quiero ver todo sobre una producción.
Criterios: título, afiche, sinopsis, obra original y autor (D13), estado (D8), sala con
link, elenco y equipo por rol con links a artista (D14/D17), promedio (D20) con cantidad
de ratings, reseñas con likes; SSR + Open Graph — **test literal: pegar el link en
WhatsApp muestra título + afiche + sinopsis (ADR-003)**; accesible sin login.

**HU-05 · Ver página de artista** — Como visitante quiero ver todas las participaciones
de una persona.
Criterios: nombre + participaciones agrupadas o etiquetadas por rol, con links a fichas;
nada más (D14: sin foto, sin bio); SSR indexable.

**HU-06 · Ver "en cartel"** — Como visitante quiero ver qué se puede ir a ver ahora.
Criterios: lista de producciones con estado `en cartel`; `próximamente` visible como
sección o filtro; sin login.

**HU-07 · Buscar** — Como usuario quiero encontrar producciones, personas y usuarios.
Criterios: búsqueda por texto con tolerancia a typos vía `pg_trgm` (D42) sobre los tres
tipos (D23); resultado vacío ofrece el camino a "sugerir producción" (HU-08) cuando la
búsqueda era de producciones.

**HU-08 · Sugerir producción faltante** — Como usuario quiero proponer una obra que no
está para poder registrarla (la válvula del historial viejo, D24).
Criterios: formulario mínimo (título obligatorio; sala, año, elenco y comentario
opcionales); requiere login; confirmación de recibido con expectativa honesta ("el
catálogo es curado; si se aprueba, va a aparecer");
⚠ **micro-decisión MD-3:** no hay notificación de aprobación (no existen notificaciones
en el MVP) — la producción simplemente aparece. Aceptado como límite consciente.

---

## Épica C — Diario (módulo Diario · Fase 2) — el corazón

**HU-09 · Registrar en un gesto** — Como usuario quiero registrar que vi una producción,
con puntaje y reseña opcionales, en un solo flujo (D18).
Criterios: buscar producción con autocompletado (reusa HU-07); fecha con granularidad
elegible — ⚠ **micro-decisión MD-1:** cuatro niveles: día exacto / mes y año / solo año /
"no me acuerdo" (sin fecha); rating opcional entero 1-10 (D9); reseña opcional; el gesto
completo toma menos de un minuto (P8: compite contra una story); si la producción no
existe, el flujo ofrece HU-08 sin perder lo tipeado.

**HU-10 · Re-visto** — Como usuario quiero registrar la misma producción otra vez (D19).
Criterios: registros múltiples conviven en el diario; cada uno puede tener su rating;
la ficha del usuario sobre esa producción evidencia que la vio N veces.

**HU-11 · Editar / borrar mi registro** — Como usuario quiero corregir o eliminar un
registro propio.
Criterios: solo el dueño (quién sos lo resuelve la capa de aplicación; que el registro sea
tuyo lo hace cumplir el módulo Diario — D61, que matiza a D30); borrar un registro
recalcula el promedio si correspondía (D20); confirmación antes de borrar.

**HU-12 · Ver mi diario** — Como usuario quiero ver mi historial completo.
Criterios: cronológico descendente; ⚠ **micro-decisión MD-2:** los registros con fecha
difusa ordenan por su granularidad (un "2023" se ubica al final de 2023; los sin fecha,
en una sección propia al final); estado vacío con invitación clara al primer registro.

**HU-13 · Mis estadísticas** — Como usuario quiero ver mis números (D26).
Criterios: bloque en el perfil: total de obras, obras por año, promedio de mis ratings;
solo queries sobre registros propios; nada más (alcance quirúrgico).

**HU-14 · Promedio y reseñas en la ficha** — Como visitante quiero ver qué opina la gente.
Criterios: promedio = último rating de cada usuario (D20 — **no AVG plano**), mostrado
con un decimal; reseñas listadas con autor, fecha, rating de ese registro y likes.

---

## Épica D — Social (módulo Social + composición · Fase 3)

**HU-15 · Seguir / dejar de seguir** — Como usuario quiero seguir personas cuyo criterio
me interesa (D3).
Criterios: botón en el perfil ajeno; contadores de seguidos/seguidores en el perfil;
sin aprobación (todo público, D21).

**HU-16 · Feed de actividad** — Como usuario quiero ver qué registraron los que sigo (D22).
Criterios: la home logueada muestra registros/reseñas de seguidos, descendente por
creación; **fallback:** si no sigo a nadie, actividad global con un aviso breve que
explica qué estoy viendo; feed compuesto en capa de aplicación (D29); paginación simple;
accesos directos visibles a "registrar" y "en cartel".

**HU-17 · Like a reseña** — Como usuario quiero destacar reseñas que valen la pena (D11).
Criterios: toggle like/unlike; contador visible; requiere login.

**HU-18 · Reportar reseña** — Como usuario quiero avisar sobre una reseña ofensiva (D40).
Criterios: botón en cada reseña ajena; motivo opcional; entra a la cola del admin
(HU-22); sin feedback posterior al reportante (mismo límite que MD-3).

---

## Épica E — Panel de admin (módulo Catálogo, cara de escritura · Fase 1 y 3)

**HU-19 · CRUD de salas** — Como admin quiero gestionar el catálogo de salas (D15).
Criterios: alta/edición/baja; campo complejo opcional (P9 del modelo); es el CRUD molde
para el resto del panel.

**HU-20 · CRUD de producciones** — Como admin quiero crear y mantener fichas completas
en el menor tiempo posible (mi presupuesto es 15 min/ficha, D37/D38).
Criterios: alta con todos los campos del modelo; participaciones con **buscar-o-crear
persona inline** (D14) y múltiples roles por persona (D17); subida de afiche con
redimensionado (D45); cambio de estado en un clic desde el listado (el barrido semanal
de estados debe ser trivial); el flujo completo de una ficha típica toma ≤15 minutos.

**HU-21 · Cola de sugerencias** — Como admin quiero aprobar o rechazar sugerencias (D24).
Criterios: listado de pendientes; aprobar abre el formulario de HU-20 precargado con lo
sugerido; rechazar pide motivo interno (para tu propio registro); el `user_id` del
sugerente queda como referencia (D30).

**HU-22 · Cola de reportes** — Como admin quiero resolver reseñas reportadas (D40).
Criterios: listado con la reseña, el motivo y el contexto; acciones: borrar reseña o
desestimar reporte; ambas sacan el ítem de la cola.

---

## Micro-decisiones tomadas al escribir estas historias (atacables)

| ID | Decisión por defecto | Razón |
|---|---|---|
| MD-1 | Fecha del registro con 4 granularidades: día / mes-año / año / sin fecha | Cubre historial viejo (D18) sin complejidad de rangos |
| MD-2 | Orden del diario: fechas difusas ordenan por su granularidad; sin-fecha va en sección aparte al final | Evita inventar fechas falsas |
| MD-3 | Sin notificación de sugerencia aprobada ni de reporte resuelto: no existen notificaciones en el MVP | El primer consumidor real de eventos (ADR-002, peldaño 2) probablemente sea esto, en v1.1 |
| MD-4 | El username es parte de la URL del perfil (`/{username}`) → validación URL-safe e inmutable en MVP | Simplifica; cambiar username queda post-MVP |

## Cobertura

Estas 22 historias cubren los 11 puntos de MVP_SCOPE.md + D40. Cualquier historia nueva
que aparezca durante el desarrollo se contrasta primero contra el log: si no mapea a una
decisión, es cambio de alcance (D27).
