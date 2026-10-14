# Esquema Sale — V8

## Migración

`V5__create_sales.sql` agrega `sales` y `sale_lines`. Flyway aplica cinco migraciones en orden desde una base PostgreSQL vacía y Hibernate valida el esquema.

## Venta y líneas

- `sales`: PK `BIGINT`, FK restrictiva a `customers`, estado `DRAFT`/`CONFIRMED`, total `NUMERIC(20,2)`, creación y confirmación en UTC. Los checks exigen total positivo y coherencia entre estado e instante de confirmación.
- `sale_lines`: PK `BIGINT`, FK restrictivas a venta y producto, combinación venta–producto única, cantidad entre 1 y 1 000 000, precio unitario `NUMERIC(12,2)` y subtotal `NUMERIC(20,2)` igual a cantidad por precio.
- El precio unitario y el total son snapshots de la venta; cambiar el precio de Catalog no reescribe ventas anteriores.
- No hay borrado físico ni cascadas en la base. Las líneas pertenecen al agregado Sale.

## Descuento de saldo

Inventory conserva la propiedad de `inventory_balances`. Sale solicita el descuento mediante su servicio de aplicación, en la misma transacción que cambia el estado de la venta. Una actualización condicional `quantity >= cantidad` descuenta y bloquea la fila de saldo; si no actualiza ninguna fila, Inventory informa stock insuficiente. Las líneas se procesan por `product_id` ascendente para mantener un orden de bloqueos estable. El check existente `quantity >= 0` protege también la integridad de base. V9 añadirá movimientos, ledger, mínimos y alertas.

Hay índices para consultar ventas por cliente y por estado/fecha, y líneas por producto.
