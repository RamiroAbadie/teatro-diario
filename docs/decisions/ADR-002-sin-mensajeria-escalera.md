# ADR-002 — Sin mensajería asincrónica: escalera evolutiva

**Estado:** aceptada — julio 2026
**Contexto de decisión:** Etapa 6, revisando críticamente la experiencia previa del
fundador con RabbitMQ en un proyecto académico.

## Contexto

El proyecto académico de referencia usaba mensajería entre módulos. La revisión del
dominio actual (D31) enumeró los eventos de dominio conceptuales (`RegistroCreado`,
`ProduccionPublicada`, `SugerenciaEnviada`, `UsuarioSeguido`) y buscó consumidores reales
en el MVP: **no existe ninguno**. El feed es una query compuesta (D29), las estadísticas
son queries en vivo (D26), y no hay emails, notificaciones, proyecciones materializadas
ni procesamiento pesado post-escritura.

## Qué resolvería un broker (RabbitMQ u otro) hoy

Nada. Su costo, en cambio, es inmediato: un servicio más que instalar, operar, monitorear
y respaldar (viola P3); semántica de entrega (mensajes perdidos/duplicados, reintentos,
DLQs); debugging asincrónico. Todo para transportar eventos que nadie consume.

## Decisión: la escalera

Cada peldaño se sube **solo cuando aparece el problema que resuelve**, nunca antes:

1. **Llamadas síncronas in-process** — hoy. Cubre todo el MVP.
2. **Despachador de eventos in-process** (observer simple dentro de la app) — cuando exista
   el primer consumidor real de un evento (ej.: email de bienvenida, contador desnormalizado).
3. **Cola de trabajo respaldada en la propia base de datos** (tabla de jobs + worker) —
   cuando algo necesite reintentos, diferimiento o proceso en background (candidatos:
   el scraper de precarga, futuras notificaciones).
4. **Broker externo** — cuando la cola en base de datos demuestre quedarse corta con
   evidencia (volumen, latencia, contención). Con este producto, posiblemente nunca.

## Consecuencias

- (+) Cero infraestructura de mensajería que operar en la etapa donde cada hora del
  fundador es el recurso más escaso.
- (+) Los eventos de dominio quedan **nombrados** en el diseño: introducir el peldaño 2 es
  agregar un despachador, no rediseñar módulos.
- (−) Riesgo de que una llamada síncrona lenta bloquee una request. Aceptado: no existe
  hoy ninguna operación de ese tipo en el MVP; si aparece, es la señal del peldaño 3.
- Cláusula de revisión: cualquier propuesta de subir un peldaño debe citar el problema
  concreto (con evidencia) que lo exige.
