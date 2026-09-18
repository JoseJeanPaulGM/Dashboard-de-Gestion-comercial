# Esquema Customer y Flyway — V4

## Gestión del esquema

V4 incorpora Flyway como única autoridad de evolución. `V1__create_customers.sql` construye el esquema desde una base vacía y Hibernate usa `ddl-auto: validate` en `local`, `test` y `prod`. No se usa `baseline-on-migrate`, H2 ni generación automática de tablas.

## Tabla `customers`

La tabla utiliza PK `BIGINT` identity, tipo y número de documento, nombre, contacto opcional, estado lógico y timestamps UTC con precisión de microsegundos. PostgreSQL aplica `NOT NULL`, check de tipo documental y unicidad de `(document_type, document_number)`.

El índice `ix_customers_active` respalda el filtro de estado. La búsqueda contiene texto (`%valor%`) y escapa comodines aportados por el cliente; no se añadió un índice funcional que PostgreSQL no pudiera aprovechar para ese patrón.

Los cambios posteriores deberán añadirse mediante nuevas migraciones hacia adelante. Una migración aplicada no se reescribe.
