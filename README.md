# teatro-diario

**El diario personal del teatro de Buenos Aires.** Un lugar donde queda registrado,
ordenado y valorado todo el teatro que viste, y donde podés ver qué vieron y qué opinan
las personas que te importan.

Algo así como lo que Letterboxd es para el cine, pero para el teatro porteño (por ahora) — que es un
circuito enorme, vivo, con salas independientes en cada barrio, y sin ninguna herramienta
que te deje llevar la cuenta.

> `teatro-diario` es el nombre del repo, no el del producto. El nombre real todavía no
> existe (P2 en el decision log). Si se te ocurre uno bueno, abrí un issue.

---

## Por qué existe

El espectador intensivo de teatro de CABA —el que ve 15, 30, 50 obras por año— hoy
registra lo que ve en **stories de Instagram**, entradas guardadas en un cajón y memoria.
Las tres se borran. La story se va a las 24 horas, la entrada se pierde, y a los dos años
no te acordás si viste ese Chéjov en el San Martín en 2019 o en 2021, ni quién lo dirigía,
ni qué te había parecido.

Y no es que falten plataformas: Alternativa Teatral y Plateanet resuelven cartelera y
venta de entradas, muy bien. Pero ninguna resuelve la otra mitad, que es la personal:
**tu historial, tus puntajes, lo que escribiste, lo que vieron los tuyos.** El competidor
real de este proyecto es Instagram, no la cartelera.

Entonces la apuesta es concreta: que registrar una obra al salir del teatro sea igual de
fácil que subir una story, y que te devuelva lo que la story estructuralmente no puede
darte — un historial buscable, la producción como ficha con su elenco y las opiniones de
otros, y las estadísticas de tu propio año teatral.

### La parte honesta

Yo no soy el usuario de este producto. Voy poco al teatro; la idea sale de conversaciones
con gente que sí va muchísimo y que se queja de exactamente esto. Eso es un riesgo real y
está anotado como tal, con nombre y apellido, en
[docs/roadmap/RISKS.md](docs/roadmap/RISKS.md) (R1) y como supuesto S1 en el
[decision log](docs/decisions/DECISION_LOG.md).

La contramedida no es convencerme de que tengo razón: es mantener el MVP chico para que,
si el supuesto es falso, la pérdida sea de meses y no de años. Y una beta cerrada con
espectadores de verdad antes de cantar victoria.

Es un proyecto de **una persona, ~5-6 horas por semana**, en paralelo a trabajo y
facultad. Eso no es un disclaimer: es una restricción de diseño que aparece en casi todas
las decisiones técnicas del repo (principio P3).

---

## Por qué es open source

Es gratuito y open source desde la decisión número cinco del proyecto, antes de que
existiera una línea de código (D5). Los motivos, sin épica:

**El código no es la parte difícil.** Lo difícil acá es el catálogo: alguien tiene que
cargar a mano y mantener las fichas del teatro de CABA, porque no existe un TMDb del
teatro porteño. Ese trabajo no lo copia nadie con un `git clone`. Así que cerrar el
código no protegería nada, y abrirlo sí aporta algo.

**Es un registro público de cómo se construye algo así.** El repo no tiene solo código:
tiene el proceso entero de fundación en [docs/](docs/) — visión, dominio, alcance,
arquitectura, stack, riesgos — y sobre todo un
[decision log](docs/decisions/DECISION_LOG.md) donde cada decisión está anotada con su
porqué, junto con lo que se descartó y por qué se descartó. Incluidas las decisiones
incómodas y las veces que hubo que corregir el rumbo (ver D51, el ajuste de ritmo después
de una sobrecarga real). Si alguna vez te preguntaste cómo se piensa un proyecto antes de
escribirlo, está todo acá adentro.

**Un producto sobre cultura pública debería poder auditarse.** Todo el contenido de la
plataforma es público por diseño (D21): no hay perfiles privados, no hay contenido oculto.
Que el código que lo maneja también lo sea es coherente.

**Licencia AGPL-3.0** (D46), y la elección es deliberada: protege contra el clon cerrado
tipo SaaS. Si alguien levanta este código como servicio, tiene que publicar sus cambios.
Copyleft fuerte, no "open source de vidriera".

Sin monetización, sin telemetría, sin dark patterns. Presupuesto de infraestructura:
**máximo USD 10 por mes**, y cada dólar hay que defenderlo (P9).

---

## Estado actual

**Fase 4 en curso — el frontend.** El backend cubre las 22 historias y está probado; lo que
se está escribiendo ahora son las pantallas. **Ya están las públicas**: en cartel, la ficha de
una obra, la página de artista, la de sala y la home del visitante — con SSR y con el preview
que se ve al compartir el link. Lo que todavía no podés hacer es tener cuenta y usarla desde
el navegador: entrar, registrar lo que viste y ver tu diario son los pasos que siguen. **Al
backend ya no le queda nada de esta fase**: las tres cosas que faltaban —la subida de afiches
(D77/D88), el `vecesQueLaVi` de la ficha (D76) y un manejo global de errores (D87)— están
hechas y probadas.

Lo que funciona hoy:

- CRUD de **salas**, **personas** y **producciones** (con participaciones, roles y estados)
- Endpoints públicos de lectura: ficha de producción, página de artista, página de sala,
  y "en cartel"
- **Cuentas**: alta, login y logout con sesión en cookie HTTP-only. Escribir el catálogo
  ahora pide rol de admin; leerlo sigue sin pedir nada
- **El diario**: registrar en un gesto (con fecha difusa y re-visto), editar y borrar lo
  propio, y el perfil público con el historial y las estadísticas
- **Promedio y reseñas** de cada producción, con el promedio de D20 —el último rating de
  cada usuario— y no un `AVG()`
- **Búsqueda** de producciones, personas y usuarios con `pg_trgm`: aguanta el error de
  tipeo y el título escrito a medias, que es lo que hace usable buscar la obra al momento
  de registrarla
- **Seguir gente y el feed**: seguir y dejar de seguir, contadores en el perfil, y la home
  logueada con lo que registraron los que seguís —o la actividad de toda la plataforma, si
  todavía no seguís a nadie—. El feed no es una tabla: se arma al leer, componiendo tres
  módulos que no se conocen entre sí
- **Likes a reseñas**, **sugerencias** —la válvula del catálogo cerrado: el usuario propone,
  el admin aprueba o rechaza— y **reportes**, con su cola de moderación
- **Las pantallas públicas** (Next, con render en el servidor): "en cartel", la ficha con su
  promedio y sus reseñas, la página de artista, la de sala y la home del visitante — más el
  404 y la pantalla de error, con sus salidas. Cada link compartido lleva su propia imagen de
  preview, generada con el título cuando la ficha no tiene afiche todavía

Lo que todavía no existe, a propósito y en este orden:

| | Cuándo |
|---|---|
| **Las pantallas que piden cuenta**: alta y login, el gesto de registro, el diario, el feed, la búsqueda y el panel | Fase 4 (lo que queda) |
| Migraciones con Flyway, deploy al VPS, backups | Fase 5 |

La regla del roadmap es que las herramientas entran **cuando aparece su problema**, no por
adelantado. Por eso todavía no hay Flyway: el esquema lo genera Hibernate con
`ddl-auto: update` (D53) y sin una base de producción que proteger sería ceremonia; su
condición de reentrada está escrita y es innegociable. Spring Security estuvo fuera del pom
por el mismo criterio hasta que apareció su problema — el login — y volvió con él (D52/D56).

Detalle completo en [docs/roadmap/ROADMAP.md](docs/roadmap/ROADMAP.md).

---

## Cómo está construido

| Capa | Elección |
|---|---|
| Backend | Java 21 + Spring Boot (+ Spring Modulith para verificar los límites) |
| Base de datos | PostgreSQL — la búsqueda del MVP se resuelve con `pg_trgm`, sin motor aparte |
| Frontend | Next.js (App Router, SSR) + Tailwind — **única dependencia de UI**: los componentes se escriben a mano |
| Infra | Docker Compose sobre un VPS chico + Caddy para HTTPS |

Cuatro módulos dentro de un **monolito modular** (ADR-001): `identidad`, `catalogo`,
`diario`, `social`, más una capa de `aplicacion` que los compone. Cada módulo expone una
interfaz pública y esconde el resto en `internal/`. Nada cruza límites por los costados:
ni imports internos ajenos, ni queries a tablas de otro módulo. El único acoplamiento
permitido de todo el sistema es `Diario → Catálogo`.

Sin colas, sin brokers, sin mensajería asincrónica (ADR-002). Todo síncrono e in-process,
con una escalera escrita de qué haría falta para justificar el próximo peldaño. "Escala"
no es un argumento válido en este repo si no viene con un problema concreto adelante.

Dos reglas de dominio que parecen detalles y no lo son:

- **El promedio de una producción no es un `AVG()`**: es el promedio del *último* rating de
  cada usuario, porque se permite registrar la misma obra varias veces (D19/D20).
- **La fecha de un registro es opcional y difusa** — día exacto, mes y año, solo año, o sin
  fecha. Así se puede cargar historial viejo sin caminos especiales.

---

## Correrlo local

Hace falta Java 21, Node 20+ y Docker.

```bash
git clone https://github.com/RamiroAbadie/teatro-diario.git
cd teatro-diario

cp .env.example .env      # completá DB_PASSWORD con algo largo y aleatorio

docker compose up -d postgres

cd backend
DB_PASSWORD=$(grep DB_PASSWORD ../.env | cut -d= -f2) ./mvnw spring-boot:run
```

Queda escuchando en `http://localhost:8080`. Las tablas se crean solas al arrancar.

Y en otra terminal, el frontend:

```bash
cd frontend
npm install
npm run dev
```

Queda en `http://localhost:3000`. En desarrollo no hay Caddy, así que Next reenvía `/api` a
Spring con un rewrite: el navegador ve **un solo origen**, igual que en producción, y la
cookie de sesión y el token CSRF se comportan como se van a comportar de verdad. Todo lo de
abajo también funciona contra `localhost:3000` en vez de `:8080`.

Leer el catálogo no pide nada (todo el contenido es público, D21):

```bash
curl localhost:8080/api/en-cartel
```

Escribir sí. La sesión va en cookie y las escrituras piden token CSRF, así que con `curl`
conviene usar un frasco de cookies. El token llega en la cookie `XSRF-TOKEN` y se devuelve
en el header `X-XSRF-TOKEN`:

```bash
curl -s -c cookies.txt -o /dev/null localhost:8080/api/en-cartel   # trae el token
token() { awk '$6=="XSRF-TOKEN"{print $7}' cookies.txt; }

# crear una cuenta (queda logueada)
curl -b cookies.txt -c cookies.txt -H "X-XSRF-TOKEN: $(token)" \
  -H 'Content-Type: application/json' \
  -d '{"username":"tuusuario","email":"vos@example.com","password":"unaClaveLarga"}' \
  localhost:8080/api/auth/registro

curl -b cookies.txt localhost:8080/api/auth/yo    # quién sos
```

El catálogo lo escribe solo el admin (D7) y no hay pantalla para repartir ese rol: se
promueve a mano contra la base, una vez por entorno. Después hay que **volver a entrar**,
porque la sesión ya abierta sigue con el rol viejo:

```bash
docker compose exec postgres psql -U teatro -d teatro \
  -c "update usuario set rol='ADMIN' where username='tuusuario';"

curl -b cookies.txt -c cookies.txt -H "X-XSRF-TOKEN: $(token)" \
  -H 'Content-Type: application/json' \
  -d '{"identificador":"tuusuario","password":"unaClaveLarga"}' \
  localhost:8080/api/auth/login

curl -b cookies.txt -c cookies.txt -H "X-XSRF-TOKEN: $(token)" \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Sala Casacuberta","complejo":"Teatro San Martín"}' \
  localhost:8080/api/admin/salas
```

Con una producción cargada (`/api/admin/producciones`), el gesto de registro es un POST y
lo demás se lee sin cuenta, como todo (D21):

```bash
curl -b cookies.txt -c cookies.txt -H "X-XSRF-TOKEN: $(token)" \
  -H 'Content-Type: application/json' \
  -d '{"produccionId":1,"fecha":"2026-07-12","granularidad":"DIA","rating":9,"resenia":"Salí flotando"}' \
  localhost:8080/api/registros

curl localhost:8080/api/usuarios/tuusuario        # tu diario y tus números
curl localhost:8080/api/producciones/1/opiniones  # promedio y reseñas de esa ficha
```

Buscar tampoco pide cuenta, y perdona bastante: el `q` va como parámetro y las tres
búsquedas viven en la misma familia de rutas, una por tipo de cosa (las salas no se buscan,
se navegan desde las fichas, D23).

```bash
curl "localhost:8080/api/buscar/producciones?q=terenal"   # con el typo y todo
curl "localhost:8080/api/buscar/personas?q=marini"
curl "localhost:8080/api/buscar/usuarios?q=tuusu"
```

La granularidad de la fecha es `DIA`, `MES`, `ANIO` o `SIN_FECHA`: es la que decide hasta
dónde se lee la fecha que mandaste, así cargar algo que viste "en 2019" no necesita
inventarle un día.

Seguir a alguien es un POST al perfil ajeno, y el feed es el único `GET` que pide cuenta —
sin ella no hay "los que sigo":

```bash
curl -b cookies.txt -c cookies.txt -H "X-XSRF-TOKEN: $(token)" -X POST \
  localhost:8080/api/usuarios/otrousuario/seguir    # y DELETE para dejar de seguir

curl -b cookies.txt "localhost:8080/api/feed?tamanio=20"
```

Si todavía no seguís a nadie, el feed viene con `"global": true` y la actividad de toda la
plataforma. Para seguir leyendo, mandá el `siguienteCursor` que trae la respuesta como
`?cursor=...`: es el último ítem entregado, así lo que se cargue mientras tanto no te
repite ni te saltea nada.

Destacar una reseña es otro toggle, sobre el id del registro que la tiene (un registro sin
texto no es una reseña y responde 404):

```bash
curl -b cookies.txt -c cookies.txt -H "X-XSRF-TOKEN: $(token)" -X POST \
  localhost:8080/api/resenias/1/like        # y DELETE para sacarlo
```

El contador llega donde se leen reseñas —la ficha y el feed— como `likes`, con `leDiLike` al
lado: `true`/`false` es el estado de tu botón y `null` es que no hay botón, porque estás sin
cuenta o porque ese ítem del feed no tiene reseña.

Cuando la obra que querés registrar no está —la búsqueda no la encuentra, el catálogo es
cerrado (D7)—, se sugiere. Es un POST con sesión y el título es lo único obligatorio:

```bash
curl -b cookies.txt -c cookies.txt -H "X-XSRF-TOKEN: $(token)" \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Una que vi en 2014","sala":"Un sótano de Almagro","anio":2014}' \
  localhost:8080/api/sugerencias
```

Del otro lado está la cola del admin. Se aprueba **después** de cargar la ficha con
`/api/admin/producciones`, diciendo en cuál terminó: así, si dejás el formulario por la
mitad, la sugerencia sigue esperando en vez de haberse perdido.

```bash
curl -b cookies.txt localhost:8080/api/admin/sugerencias   # con el username de quien sugirió

curl -b cookies.txt -c cookies.txt -H "X-XSRF-TOKEN: $(token)" \
  -H 'Content-Type: application/json' -d '{"produccionId":1}' \
  localhost:8080/api/admin/sugerencias/1/aprobar

curl -b cookies.txt -c cookies.txt -H "X-XSRF-TOKEN: $(token)" \
  -H 'Content-Type: application/json' -d '{"motivo":"Ya está cargada"}' \
  localhost:8080/api/admin/sugerencias/2/rechazar
```

Al que sugirió no le llega nada: no hay notificaciones en el MVP (MD-3). Si se aprueba, la
obra simplemente aparece en el catálogo.

Ojo igual: esto corre sin HTTPS y la cookie de sesión todavía no es `secure`. Sigue sin ser
algo para exponer a internet — el endurecimiento va con el deploy, en la Fase 5.

Los secretos van siempre por variables de entorno; el `.env` está en `.gitignore` desde el
primer commit y nunca hubo una credencial en el repo (D48).

---

## El repo por dentro

```
backend/         Spring Boot: los cuatro módulos + la capa de aplicación
frontend/        Next (App Router) + Tailwind: app/ rutas, components/, lib/ y assets/
docs/            toda la documentación fundacional — empezá por docs/README.md
caddy/           config del reverse proxy (congelada hasta la fase de deploy)
docker-compose.yml
CLAUDE.md        contexto y reglas innegociables para asistentes de IA
```

Es un monorepo (D47): backend, frontend y docs en un solo lugar, un solo tablero de issues,
las features full-stack viajan en un PR.

### Por dónde empezar a leer

| Si querés entender... | Leé |
|---|---|
| el producto | [PRODUCT_VISION.md](docs/product/PRODUCT_VISION.md) y [CORE_LOOP.md](docs/product/CORE_LOOP.md) |
| qué entra y qué no en el MVP | [MVP_SCOPE.md](docs/product/MVP_SCOPE.md) (está congelado) |
| el sistema | [MODULE_MAP.md](docs/architecture/MODULE_MAP.md) y [ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md) |
| **por qué todo es como es** | [DECISION_LOG.md](docs/decisions/DECISION_LOG.md) — la fuente de verdad |

Regla de mantenimiento de los docs: si la realidad contradice un documento, se actualiza
el documento y el log registra el cambio. Un documento desactualizado es peor que ninguno.

---

## Contribuir

Sí, aunque con una advertencia amable: el alcance del MVP está **congelado** y las
decisiones están tomadas y justificadas. Un PR que agregue Redis, Kafka, un framework
nuevo o una feature que no está en el scope va a ser rechazado, aunque el código sea
impecable. No es soberbia — es la única forma de que un proyecto de una persona a 5 horas
por semana llegue a algún lado.

Lo que sí sirve muchísimo:

- **Issues.** Si algo del diseño no cierra, decilo. Ya hubo dos decisiones (D29, D30) que
  salieron de una revisión externa y que corrigieron errores reales.
- **Bugs y PRs chicos** contra una historia existente de
  [USER_STORIES.md](docs/product/USER_STORIES.md).
- **Saber de teatro porteño.** Esto vale más que el código: la calidad del catálogo es el
  activo central del producto y su riesgo número dos.

Antes de mandar un PR, pasá por el [decision log](docs/decisions/DECISION_LOG.md) y por
[CLAUDE.md](CLAUDE.md) — ese último archivo son las reglas innegociables, escritas para
asistentes de IA pero igual de válidas para humanos.

---

## Licencia

[AGPL-3.0](LICENSE). Usalo, estudialo, modificalo, corrélo. Si lo ofrecés como servicio,
publicá tus cambios.
