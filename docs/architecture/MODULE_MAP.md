# Module Map

> Estado: v1.0 — cierre de Etapa 5.
> Módulos = capacidades del negocio con autoridad sobre sus datos, no agrupaciones de tablas.
> En el MVP son **límites de código dentro de una sola aplicación** (paquetes/carpetas con
> interfaz pública explícita), no servicios. Ver ARCHITECTURE.md (Etapa 6) para el porqué.

## Los cuatro módulos

### Identidad
- **Responsabilidad:** cuentas, autenticación, perfil.
- **Autoridad sobre:** usuarios (credenciales, datos de perfil).
- **Expone:** alta/login, consulta de perfiles (para composición en lecturas).
- **Depende de:** nadie.

### Catálogo
- **Responsabilidad:** las entidades teatrales y su curaduría (panel admin + sugerencias).
- **Autoridad sobre:** producciones, personas, participaciones, salas, estados. Es el único
  módulo que decide la **identidad** de las entidades (¿dos Hamlets son la misma producción?).
- **Expone:** fichas, "en cartel", búsqueda de producciones/personas, escrituras solo-admin,
  recepción de sugerencias (guarda el `user_id` del sugerente como referencia opaca).
- **Depende de:** nadie. (El chequeo "es admin" es autorización: concern transversal de la
  capa de aplicación, no una dependencia hacia Identidad.)
- **Nota de diseño:** la curaduría vive adentro y no como módulo "Admin" separado — es la
  cara de escritura de la misma capacidad, no una capacidad distinta.

### Diario
- **Responsabilidad:** todo lo que un usuario dice de una producción. El corazón del producto.
- **Autoridad sobre:** registros (con re-visto, D19), ratings, reseñas, el cálculo del
  promedio por producción (último rating por usuario, D20), estadísticas personales (D26).
- **Expone:** crear/editar/borrar registro; diario de un usuario; actividad de un conjunto de
  usuarios (insumo del feed); reseñas y promedio de una producción; stats propias.
- **Depende de:** **Catálogo** (validar que la producción existe; leer datos básicos para
  mostrar). Única dependencia módulo-a-módulo del sistema. Guarda `user_id` opaco.

### Social
- **Responsabilidad:** el grafo social y las interacciones sobre contenido.
- **Autoridad sobre:** follows y likes a reseñas.
- **Expone:** follow/unfollow; "IDs seguidos por X" (insumo del feed); like/unlike;
  conteo de likes.
- **Depende de:** nadie (referencia `user_id` y `reseña_id` como IDs opacos).
- **Límite fino asumido:** la reseña (contenido) es de Diario; el like (interacción) es de
  Social. Decidido conscientemente.

## Lo que NO es módulo

| Cosa | Dónde vive |
|---|---|
| **Feed** (de seguidos y global) | Caso de uso de **composición** en la capa de aplicación: pide a Social los seguidos, a Diario su actividad, a Identidad los nombres. No es un lugar del código con datos propios |
| **Búsqueda** | Cada módulo expone búsqueda sobre lo suyo (D23). Módulo propio solo si algún día hay motor dedicado |
| **Autorización** (admin, dueño-del-registro) | Concern transversal de la capa de aplicación |
| **Notificaciones, emails** | No existen en el MVP |

## Grafo de dependencias (real, no estético)

```
Diario ──► Catálogo

Identidad, Catálogo, Social: sin dependencias salientes.
Capa de aplicación: compone todos (feed, vistas de lectura, autorización).
```

Regla: las referencias por ID opaco **no** son dependencias. Dependencia = invocar la
interfaz pública de otro módulo.

## Eventos de dominio: identificados, no implementados

Conceptualmente existen: `RegistroCreado`, `ProduccionPublicada`, `SugerenciaEnviada`,
`UsuarioSeguido`. **En el MVP no tienen ni un consumidor real** (el feed es query, las stats
son queries, no hay notificaciones ni contadores). No se implementa bus ni mensajería.

La preparación para el futuro es disciplina de límites, no infraestructura:
1. Cada módulo expone una interfaz pública explícita; el resto es privado.
2. Nada cruza límites "por los costados" (queries directas a tablas de otro módulo, prohibidas).
3. Si mañana un evento necesita un consumidor (email de bienvenida, contador desnormalizado),
   se agrega un despachador de eventos **in-process** primero. Brokers externos, recién cuando
   haya un problema concreto que lo exija (ver Etapa 6).

## Razones futuras de escalado independiente (hipotéticas, no requisitos)

- **Catálogo:** casi solo lectura; cacheable agresivamente; el primer candidato a réplica de lectura.
- **Diario:** el volumen de escritura crece con los usuarios; dueño de los datos más valiosos.
- **Social:** los feeds a gran escala se materializan (fan-out); hoy sería sobre-ingeniería.

Ninguna de estas razones existe hoy. Se registran para que los límites las contemplen, no
para actuar sobre ellas.
