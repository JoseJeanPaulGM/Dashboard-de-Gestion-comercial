# Esquema Catalog — V5

## Migración

`V2__create_catalog.sql` añade las tablas `categories` y `products` después del baseline Customer. La migración se aplica hacia adelante mediante Flyway; `V1__create_customers.sql` no se modifica y Hibernate continúa con `ddl-auto: validate`.

## Tabla `categories`

- PK `BIGINT` identity.
- `code VARCHAR(30)` obligatorio y único.
- `name VARCHAR(100)` obligatorio.
- `description VARCHAR(250)` opcional.
- estado lógico y timestamps UTC con precisión de microsegundos.
- índice por `active` para el filtro publicado.

## Tabla `products`

- PK `BIGINT` identity.
- `sku VARCHAR(50)` obligatorio y único.
- `name VARCHAR(150)` obligatorio.
- `description VARCHAR(500)` opcional.
- `sale_price NUMERIC(12,2)` obligatorio con check positivo.
- `category_id BIGINT` obligatorio con FK restrictiva hacia `categories`.
- estado lógico y timestamps UTC con precisión de microsegundos.
- índices por `active` y por `(category_id, active)` para los filtros publicados.

La aplicación usa `BigDecimal`, conserva dos decimales y expresa el precio exclusivamente en PEN. La moneda no se duplica en la tabla porque PEN es la moneda base vigente.

## Relación y ciclo de vida

La relación es Category 1:N Product y se modela desde Product como `ManyToOne` lazy. Los listados y consultas de producto cargan la categoría explícitamente para evitar N+1 y funcionar con `open-in-view: false`.

No existe borrado físico ni cascada. La aplicación impide desactivar una categoría con productos activos y evita crear o reactivar productos bajo una categoría inactiva. La FK conserva la relación incluso para registros inactivos.

No se añaden índices B-tree para búsquedas `%texto%`, porque no respaldarían ese patrón de forma eficiente.
