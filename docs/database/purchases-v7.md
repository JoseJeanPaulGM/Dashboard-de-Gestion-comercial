# Esquema Purchase — V7

## Migración

`V4__create_purchases.sql` añade `purchases`, `purchase_lines` e `inventory_balances`. Flyway aplica las cuatro migraciones en orden desde una base PostgreSQL vacía; Hibernate mantiene `ddl-auto: validate`.

## Compra y líneas

- `purchases`: PK `BIGINT`, FK restrictiva a `suppliers`, estado `DRAFT`/`CONFIRMED`, total `NUMERIC(20,2)`, instante de creación e instante de confirmación nullable. Los checks aseguran total positivo y correspondencia entre estado e instante de confirmación.
- `purchase_lines`: PK `BIGINT`, FK restrictivas a compra y producto, combinación compra–producto única, cantidad positiva hasta 1 000 000, precio unitario `NUMERIC(12,2)` y subtotal `NUMERIC(20,2)` igual a cantidad por precio.
- Los importes son snapshots propios de la compra. Cambiar el precio de venta del producto no modifica el precio de compra ni los importes históricos.
- No hay borrado físico ni cascadas de base de datos. Las líneas pertenecen al agregado Purchase y se crean con su cabecera.

## Saldo mínimo

`inventory_balances` guarda una fila por producto: `product_id` como PK/FK restrictiva, `quantity BIGINT` no negativa y `updated_at` UTC. Inventory es dueño de la escritura. Purchase llama a su servicio para incrementar; el adaptador usa un upsert aditivo en PostgreSQL que participa en la transacción de confirmación. La fila de compra se bloquea para que dos confirmaciones concurrentes no dupliquen el efecto.

Este saldo permite comprobar la entrada de stock de V7. Movimientos, ledger, ajustes, mínimos y alertas quedan para V9; V8 añadirá la salida controlada por ventas. La decisión de adelantar únicamente el saldo está registrada en ADR 0005.

## Índices y restricciones

Hay índices para compras por proveedor y por estado/fecha, y para líneas por producto. Las FK impiden eliminar maestros referenciados. El stock tiene check `quantity >= 0`; un fallo de escritura revierte toda la confirmación.
