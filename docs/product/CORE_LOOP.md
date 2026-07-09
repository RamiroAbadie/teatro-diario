# Core Loop

> Estado: v0.1 — cierre de Etapa 3 (experiencia y acciones centrales)
> Define el sistema de comportamiento del producto: la acción central, el loop que la
> alimenta y las superficies mínimas que lo sostienen. No es diseño de pantallas.

## El loop

```
ver teatro (fuera del producto)
        │
        ▼
REGISTRAR ─────────────── la acción central, un solo gesto:
        │                 producción + fecha (opcional/aproximada)
        │                 + rating (opcional) + reseña (opcional)
        ▼
el diario propio crece ── valor individual (P1): historial, estadísticas
        │
        ▼
la actividad aparece en el feed de los seguidores
        │
        ▼
otros descubren producciones ── por actividad de seguidos y por catálogo "en cartel"
        │
        ▼
van a ver teatro → registran → (loop)
```

- **Acción central:** el registro. Su fricción se mide contra subir una story (P8).
- **Mecanismo social:** el feed de actividad de seguidos. No hay otro en el MVP.
- **Motores de descubrimiento:** (1) actividad de quienes seguís, (2) catálogo navegable
  con estado `en cartel`. Sin algoritmos (X2).
- **Razón para volver:** registrar lo último que viste; ver qué vieron los tuyos;
  ver qué hay en cartel.

## El registro, en detalle (D18)

- Un solo gesto: elegir producción → fecha opcional o aproximada (día exacto, solo año,
  o sin fecha) → rating opcional (1–10) → reseña opcional.
- La fecha opcional/difusa resuelve la carga de historial viejo sin caminos especiales.
- **Re-visto (D19):** se permite registrar la misma producción múltiples veces. Cada
  registro puede tener su propio rating.
- **Promedio (D20):** el promedio público de una producción se calcula sobre el
  **último rating de cada usuario**, no sobre todos los ratings históricos.
  ⚠️ No es un `AVG()` plano — no "simplificar" esto en la implementación.
- **Privacidad (D21):** todo es público. No existe contenido privado en el MVP.

## Superficies mínimas

| Superficie | Qué es | Notas |
|---|---|---|
| **Home (logueado)** | Feed de actividad de tus seguidos + acceso directo a "registrar" y a "en cartel" | Fallback si no seguís a nadie: actividad global de la plataforma (D22) |
| **Ficha de producción** | Datos, elenco/equipo, estado, promedio, reseñas | El destino de la navegación |
| **Diario / perfil** | Historial de registros del usuario, sus reseñas, sus stats básicas | El valor individual |
| **Página de artista** | Nombre + participaciones | Versión mínima (D14) |
| **En cartel** | Catálogo filtrado por estado | Descubrimiento sin algoritmo |
| **Búsqueda** | Texto simple sobre producciones, personas y usuarios (D23) | Sin motor de relevancia; las salas se navegan desde fichas |
| **Sugerir producción** | Formulario que genera ficha semi-armada para el admin | **Dentro del MVP**: es la válvula del historial viejo (D24) |

## Arranque en frío

- **De la plataforma:** el fallback de actividad global evita el feed vacío.
- **Del usuario nuevo:** onboarding mínimo — queda en la home (decisión del fundador,
  camino simple). La carga de historial viejo se resuelve **bajo demanda** vía
  sugerencias: solo se cura lo que alguien quiere registrar (D24).
- **Mensaje de lanzamiento honesto:** "el diario de tu teatro de acá en adelante,
  más lo viejo que quieras sugerir". La visión completa ("todo el teatro que viste
  en tu vida") es la dirección, no la promesa del día uno.
