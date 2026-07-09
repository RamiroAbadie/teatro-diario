# Glossary

> Vocabulario común del proyecto. Si un término se usa en código, documentación o conversación
> con un significado distinto al de acá, o se corrige el uso o se actualiza el glosario.

| Término | Definición en este proyecto |
|---|---|
| **Producción** (montaje) | Unidad central del producto. Un texto teatral puesto en escena por una dirección y un elenco determinados. Contra esto se registra, puntúa y reseña. |
| **Obra / texto original** | El texto teatral en sí (*Hamlet*, *La casa de Bernarda Alba*). En el MVP no es entidad: es un campo de texto de la producción. |
| **Registro / log** | El acto de un usuario de asentar "vi esta producción", con fecha libre. Núcleo del diario. |
| **Rating** | Puntaje entero 1–10 de un usuario a una producción. |
| **Reseña** | Texto de opinión de un usuario sobre una producción. |
| **Persona** | Artista (actor/actriz, dirección, dramaturgia) como entidad reutilizable del catálogo. |
| **Participación** | Vínculo de una persona con una producción en un rol determinado. |
| **Rol** | Función de una persona en una producción. Lista cerrada inicial: actor/actriz, dirección, dramaturgia. |
| **Sala** | Espacio teatral físico de CABA. Entidad de catálogo curada por el admin. |
| **Complejo** | Edificio/institución que agrupa varias salas (Teatro San Martín, Paseo La Plaza). Detalle de modelado pendiente. |
| **Estado de producción** | `en cartel` / `cerrada` / `próximamente` (cubre estreno y reestreno). Máximo nivel de "vigencia" que mantiene el sistema (P6). |
| **Catálogo** | Conjunto de producciones, personas y salas. Cerrado: solo el admin crea y edita (D7). |
| **Sugerencia** | Propuesta de un usuario de una producción faltante, que genera una ficha semi-armada para aprobación del admin (D7). |
| **Ficha** | La página/datos completos de una producción en el catálogo. |
| **Página de artista** | Vista de una persona con todas sus participaciones. Versión MVP: nombre + lista, nada más. |
| **Diario** | El historial completo de registros de un usuario. El núcleo del producto (P1). |
| **Actividad** | Los registros, ratings y reseñas recientes de los usuarios que alguien sigue (D3). |
| **Lista** | Colección personal y pública de producciones, ordenada manualmente. Candidata a MVP (P5). |
| **Admin** | El fundador, en su rol de curador del catálogo. Restricción de diseño: es una sola persona (P3). |
