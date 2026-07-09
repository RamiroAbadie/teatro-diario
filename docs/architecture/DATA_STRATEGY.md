# Data Strategy

> Estado: v1.0 — cierre de Etapa 7.
> **Este documento describe el riesgo #2 del proyecto** (después de S1, la demanda).
> Letterboxd existe porque TMDb existe. Acá no hay TMDb del teatro porteño: el admin
> es la base de datos. Toda la estrategia deriva de esa realidad.

## El riesgo, enunciado sin anestesia

El loop se rompe en el primer eslabón si el usuario no encuentra la producción que quiere
registrar. El catálogo es un activo que se **pudre en silencio**: nadie avisa cuando queda
desactualizado; se nota cuando un usuario choca contra el hueco. Mitigaciones ya decididas:
catálogo cerrado y curado (D7), sugerencias como válvula (D24), alcance CABA (D4),
estados en vez de agenda (D8, P6).

## Origen de cada tipo de dato

| Dato | Fuente | Quién lo mantiene |
|---|---|---|
| Producciones, participaciones, estados | Carga manual del admin desde fuentes públicas (Alternativa Teatral, webs de teatros) — **datos fácticos** (título, elenco, sala, fechas de temporada); las sinopsis se redactan propias, no se copian | Admin |
| Personas (artistas) | Derivadas de la carga de fichas (buscar-o-crear) | Admin; duplicados como deuda aceptada (D14) |
| Salas | Carga única inicial (~100-200 en CABA), muy estable | Admin |
| Sugerencias de producciones | Usuarios (formulario → ficha semi-armada) | Aprobación del admin |
| Registros, ratings, reseñas, likes, follows | Usuarios | Los usuarios; el admin puede borrar contenido reportado |
| Afiches/imágenes | Subida del admin al crear ficha | Almacenamiento: disco local con volumen (revisión en Etapa 8) |

## Presupuesto de horas (la restricción real)

- **Total del proyecto: 8 hs/semana** (posiblemente más a futuro).
- **Curaduría post-lanzamiento: hasta 3 hs/semana** → ~12 fichas/semana a 15 min/ficha,
  entre altas y sugerencias, más mantenimiento de estados.
- **Desarrollo: ~5-6 hs/semana** → el MVP congelado es un proyecto de **4-6 meses**.
  El roadmap (Etapa 10) se escribe contra este número.

## Catálogo del día uno

- **~50 producciones en cartel, impecables** (mejor 50 excelentes que 150 mediocres:
  la ficha es la cara del producto). Mezcla de comercial, oficial e independientes de
  salas grandes; cerrar la lista concreta es tarea previa al lanzamiento.
- Costo estimado de carga inicial: **8-17 horas**, repartibles en 2-3 semanas en paralelo
  al desarrollo.
- Crecimiento posterior: sugerencias de usuarios (bajo demanda, D24) + altas del admin.

## Rutina operativa (formalizada, no "cuando pueda")

**Una sesión fija semanal de curaduría**, con checklist: aprobar/rechazar sugerencias,
altas de estrenos relevantes, barrido de estados (marcar `cerrada` lo que bajó de cartel),
revisar reseñas reportadas.

## Moderación de contenido de usuarios

Las reseñas son públicas y opinan, por elevación, sobre personas reales con nombre y
apellido. Mecanismo mínimo: botón **reportar** → cola en el panel de admin → el admin
borra o desestima. Sin flujos de apelación ni automatización en el MVP.
(Sujeto a confirmación como modificación explícita del MVP congelado — ver D37 propuesta.)

## Escalera de fuentes de datos (análoga a ADR-002: cada peldaño ante un problema real)

1. **Carga manual** — hoy. Decidido.
2. **Scraper de precarga como herramienta interna** (borradores que el admin aprueba) —
   cuando la carga manual demuestre no alcanzar. Requiere revisar términos de uso de las
   fuentes; los datos fácticos y las sinopsis propias reducen el riesgo, pero la decisión
   legal se toma en ese momento, no se hereda.
3. **Colaboración directa con salas/compañías** (te pasan su programación) — trabajo de
   relaciones, no de código; de paso valida el producto con el otro lado del mercado.
   Candidata fuerte para la etapa de crecimiento.
4. **Curadores voluntarios de la comunidad** (permisos de edición limitados) — solo con
   comunidad real y demanda demostrada; reabre problemas de calidad que D7 cerró, así
   que exigiría diseño propio.

## Qué NO hacemos con datos

- No copiamos sinopsis ni textos de otras plataformas: los datos fácticos se toman,
  la redacción es propia.
- No prometemos exhaustividad: el mensaje del producto es honesto sobre el alcance
  del catálogo (D24).
- No abrimos la edición del catálogo a usuarios (D7) — cualquier cambio a esto es una
  decisión mayor con ADR propio.
