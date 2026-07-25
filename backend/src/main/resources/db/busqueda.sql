-- Lo único que la búsqueda de HU-07 necesita de la base y que Hibernate no sabe crear desde
-- las entidades: la extensión pg_trgm (D42) y sus índices GIN.
--
-- Corre en cada arranque, después de que `ddl-auto: update` creó o actualizó las tablas
-- (D53), y por eso todo acá es idempotente. No es una migración ni el principio de una:
-- cuando entre Flyway en la Fase 5, esto se muda a la migración baseline y el archivo
-- desaparece con su configuración.
--
-- Los índices son lo que hace que los operadores de similitud (`%`, `<%`) y el `ILIKE` sin
-- ancla de las tres búsquedas no terminen leyendo la tabla entera. Con 50 fichas da igual;
-- con el catálogo crecido, no.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS produccion_titulo_trgm ON produccion USING gin (titulo gin_trgm_ops);
CREATE INDEX IF NOT EXISTS persona_nombre_trgm ON persona USING gin (nombre gin_trgm_ops);
CREATE INDEX IF NOT EXISTS usuario_username_trgm ON usuario USING gin (username gin_trgm_ops);
