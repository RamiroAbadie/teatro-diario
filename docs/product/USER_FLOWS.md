# User Flows

> Estado: v1.0. El primo pragmático del journey map: qué pantallas existen, cómo se
> conectan, y los **estados vacíos y de error** — donde los MVPs de una persona siempre
> quedan flojos. Sirve para verificar las historias: si un flujo pasa por una pantalla
> que ninguna historia construye, hay un hueco.

## Inventario de pantallas (13 públicas + panel admin)

> Las rutas dejaron de ser tentativas: las fijan **D74** (el id es lo único que se parsea, el
> slug es decorativo y su ausencia redirige `308` a la forma canónica) y **D75** (el perfil va
> bajo `/usuario/`, porque la raíz colisiona con las rutas del producto).

| # | Pantalla | Ruta | SSR/OG | Historias |
|---|---|---|---|---|
| 1 | Home visitante (landing + en cartel destacado) | `/` | Sí | HU-06 |
| 2 | Home logueado (feed + accesos) | `/` | No hace falta | HU-16 |
| 3 | Ficha de producción | `/obra/{id}-{slug}` | **Sí (crítico)** | HU-04, HU-14 |
| 4 | Página de artista | `/artista/{id}-{slug}` | Sí | HU-05 |
| 5 | Página de sala | `/sala/{id}-{slug}` | Sí | HU-04 (link) |
| 6 | En cartel | `/en-cartel` | Sí | HU-06 |
| 7 | Búsqueda / resultados | `/buscar?q=` | No | HU-07 |
| 8 | Perfil / diario de usuario | `/usuario/{username}` | **Sí (crítico)** | HU-03, HU-12, HU-13 |
| 9 | Gesto de registro (modal o página) | — | No | HU-09, HU-10 |
| 10 | Sugerir producción | `/sugerir` | No | HU-08 |
| 11 | Alta de cuenta / login | `/registro`, `/login` | No | HU-01, HU-02 |
| 12 | Panel admin (salas, producciones, sugerencias, reportes) | `/admin/...` | No | HU-19..22 |
| 13 | Error 404 / genérico | — | — | transversal |

> ⚠️ **Tres caminos son del armazón y no de una pantalla, así que no están en esta tabla y no
> hay que buscarlos acá: el acceso al diario propio, a sugerir una obra y a salir.** Los tres
> viven en el menú principal que abre la cabecera; **"Mi diario" está además en la barra de
> destinos**, y **a sugerir se llega además por la última opción del autocompletado del gesto**
> (D7/D24). Lo especifica D81, en `SCREEN_SPECS.md` → "El armazón". Queda
> escrito porque son historias que existen —HU-12, HU-08, HU-02— y ninguna pantalla del
> inventario construye su entrada: sin esta línea, parecen huecos y no lo son. **El inventario
> sigue siendo de 13** y el hueco 3 sigue cerrado: no hay pantalla de ajustes.

## Flujo 1 — El visitante que llega por un link compartido (el flujo de crecimiento)

```
WhatsApp/IG → preview de ficha → Ficha de producción (3)
   → lee promedio y reseñas
   → toca una reseña → Perfil del autor (8) → ve su diario
   → CTA "creá tu diario" → Alta de cuenta (11) → Home logueado (2)
```
**Por qué importa:** es la mecánica de adquisición completa (ADR-003). Cada pantalla
pública necesita un camino visible hacia "crear cuenta" que no moleste al que solo mira.

## Flujo 2 — El usuario nuevo (arranque en frío individual)

```
Alta de cuenta → Home logueado (2)
   → feed = actividad GLOBAL con aviso "estás viendo toda la plataforma;
     seguí gente para armar tu feed" (D22)
   → diario propio VACÍO → estado vacío con CTA único: "registrá lo último que viste"
   → Gesto de registro (9)
```
**Estados vacíos que hay que diseñar (no dejar en blanco):**
- Diario vacío (HU-12): invitación al primer registro.
- Feed sin seguidos: fallback global + explicación (HU-16).
- Stats con <2 registros: se ocultan o muestran versión mínima, no un dashboard vacío.

## Flujo 3 — El usuario intensivo, saliendo del teatro (el gesto que forma hábito)

```
[celular, 23:30, acaba de ver una obra]
Home → botón "registrar" (grande, siempre visible)
   → busca la obra (autocompletado, HU-07)
   ├─ EXISTE → fecha (default: hoy, un tap) → rating → reseña opcional → publicar
   │            [criterio P8: todo el camino feliz < 1 minuto]
   └─ NO EXISTE → "no está en el catálogo" → deriva a Sugerir (10)
                  SIN perder lo tipeado → confirmación con expectativa honesta (MD-3)
```
**Por qué importa:** este flujo ES el producto. El camino feliz se optimiza a mano;
el camino triste (obra inexistente) es la primera impresión del usuario intensivo con
historial — si lo frustra, no vuelve (R2).

## Flujo 4 — El admin, rutina semanal (D37)

```
/admin → Cola de sugerencias (HU-21): aprobar (form precargado) / rechazar
       → Cola de reportes (HU-22): borrar / desestimar
       → Barrido de estados: listado de producciones `en cartel`
         → marcar `cerrada` en un clic (HU-20)
       → Altas de estrenos de la semana
       → Duplicados que aparecieron al aprobar sugerencias: elegir la ficha canónica
         → fusionar la otra en ella, con confirmación (HU-20 ampliada por D63)
         [los registros de la duplicada se mudan solos; no hay nada que reconstruir]
[presupuesto total: ≤ 3 horas]
```
**Por qué importa:** si esta rutina no es rápida, el catálogo se pudre (R2). El panel
se diseña para minimizar clics del admin, no para verse bien.

## Errores transversales (definir una vez, usar en todos lados)

| Caso | Comportamiento |
|---|---|
| Búsqueda sin resultados (producciones) | Mensaje + CTA a sugerir (HU-07) |
| Acción que requiere login desde visitante | Redirige a login y **vuelve** a donde estaba con lo tipeado |
| **Visitante anónimo, a secas** | **No es un error y no redirige a ningún lado**: la pantalla se dibuja en su versión sin sesión. El `401` con el que el frontend averigua si hay alguien no se muestra nunca (D78) |
| 404 de ficha/perfil | Página propia con búsqueda embebida |
| Error de servidor en el gesto de registro | El contenido tipeado NO se pierde (guardar en estado del cliente hasta confirmar) |
| Sesión expirada en medio de una acción | Igual que login requerido: volver con contexto |

## Huecos detectados al mapear (verificación historias ↔ flujos)

1. La **página de sala** (pantalla 5) no tenía historia propia — queda cubierta como parte
   de HU-04 (link desde ficha) con contenido mínimo: nombre, complejo, producciones en
   cartel ahí. Sin historia nueva: es una vista más del Catálogo.
2. El **CTA de crear cuenta** en pantallas públicas (Flujo 1) no es una feature sino un
   criterio transversal de las pantallas 1, 3, 4, 6 y 8 — se agrega como criterio, no
   como historia.
3. Confirmado que **no hay** pantalla de configuración/ajustes en el MVP: no hay nada que
   configurar (todo público D21, username inmutable MD-4). Cambio de contraseña: el único
   candidato real — ⚠ queda como micro-decisión abierta MD-5 (ver log).
