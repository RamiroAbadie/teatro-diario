# Product Vision

> Estado: v0.2 — cierre de Etapa 1 (visión del producto)
> Última actualización: julio 2026

## Declaración de visión

Para el **espectador frecuente de teatro de Buenos Aires**, que hoy registra lo que ve de forma
fragmentada (stories de Instagram, entradas guardadas, memoria) y descubre obras por boca a boca
e influencers, **[nombre pendiente]** es **el diario personal del teatro**: un lugar donde queda
registrado, estructurado y valorado todo el teatro que viste en tu vida, y donde podés ver qué
vieron y qué opinan las personas que te importan.

## El problema

El espectador intensivo de teatro no tiene hoy una herramienta estructurada para:

- registrar su historial teatral completo (qué vio, cuándo, qué le pareció);
- consultar y compartir ese historial;
- ver qué vieron y qué opinan las personas de su círculo y los reseñadores que sigue.

Sus soluciones actuales son Instagram (efímero, no estructurado, no consultable), entradas
guardadas y memoria. **El competidor real del producto es Instagram**, no las plataformas de
cartelera existentes (Alternativa Teatral, Plateanet), que resuelven cartelera y venta pero no
la capa de registro personal y social.

## Usuario primario

El **espectador intensivo** del teatro de CABA (referencia: 15+ obras por año). El espectador
ocasional puede usar el producto, pero **no diseñamos para convertirlo en intensivo**.

## Núcleo del producto

El **diario personal**. El producto debe ser valioso para un usuario que está solo en la
plataforma: su historial, sus puntajes, sus reseñas, sus estadísticas.

La vara de calidad: registrar una obra debe ser **igual o más satisfactorio que subir una story**,
y debe ofrecer lo que Instagram estructuralmente no puede dar: historial ordenado y buscable,
la producción como entidad (ficha, elenco, otras opiniones sobre *esa* producción), estadísticas
del año teatral propio.

## Capa social (mínima, presente desde el inicio)

- Perfiles públicos.
- Seguir personas.
- Ver la actividad (registros, puntajes, reseñas) de quienes seguís.
- Likes a reseñas.

Nada más en la etapa inicial: sin algoritmos de recomendación, sin feed rankeado, sin
comentarios, sin mensajería.

## Comportamiento que queremos generar

Que registrar una obra después de verla se vuelva un **hábito placentero**, análogo a loguear
una película en Letterboxd.

## Unidad central del dominio (definición preliminar, a profundizar en Etapa 2)

La unidad contra la que se registra, puntúa y reseña es la **producción** (montaje): un texto
teatral puesto en escena por un director y un elenco determinados. Dos producciones distintas
del mismo texto son entidades distintas. Una producción que cambia de sala sigue siendo la
misma producción.

## Alcance geográfico inicial

**CABA.** Densidad de oferta y de público suficiente para que el producto tenga sentido sin
escala nacional.

## Qué es y qué NO es el producto

| Es | No es (por ahora o nunca) |
|---|---|
| Diario personal de teatro | Plataforma de venta de entradas |
| Catálogo curado de producciones de CABA | Cartelera con agenda de funciones, fechas y horarios |
| Red social mínima (follow + actividad + likes) | Red social completa (comentarios, mensajería, feed algorítmico) |
| Registro contra producciones con estado (en cartel / cerrada / próximamente-estreno/reestreno) | Base de datos colaborativa de carga abierta |
| Gratuito y open source | Producto con modelo de monetización definido |

## Riesgo principal reconocido

El fundador **no es usuario del producto**. La evidencia del problema proviene de conversaciones
informales con un puñado de espectadores (ver supuesto S1 en DECISION_LOG.md). Por decisión del
fundador, no se hace validación formal previa; la contramedida es mantener el MVP lo más chico
posible para minimizar la pérdida si el supuesto resulta falso.
