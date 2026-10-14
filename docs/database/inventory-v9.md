# Esquema Inventory — V9

## Migración

`V6__inventory_ledger.sql` añade `minimum_quantity BIGINT NOT NULL DEFAULT 0` a `inventory_balances` y crea `inventory_movements`. Flyway aplica seis migraciones desde una base PostgreSQL vacía; Hibernate mantiene `validate`.

Para una base que ya contiene saldos de V7/V8, la migración inserta un movimiento `OPENING` por cada saldo positivo. Su cambio y `balance_after` coinciden con el saldo existente y `created_at` conserva el `updated_at` disponible. Los saldos cero no necesitan movimiento de apertura. El historial individual anterior a V9 sigue en compras y ventas; no se infiere un orden de movimientos históricos que el saldo antiguo no registra.

## Ledger

Cada movimiento conserva producto, cambio con signo, saldo resultante, tipo de origen, referencia, clave de idempotencia opcional, motivo opcional e instante UTC. La aplicación solo inserta movimientos; no expone operaciones para editarlos o eliminarlos.

- `PURCHASE` y `SALE` referencian la línea comercial mediante `source_id`. Un índice único impide registrarla dos veces.
- Un `CHECK` exige referencia no nula para apertura, compra y venta, además de cambio positivo para `PURCHASE` y negativo para `SALE`, también frente a inserciones SQL directas.
- `ADJUSTMENT` usa `request_key UUID` único y exige un motivo. El servicio serializa solicitudes con la misma clave en PostgreSQL antes de verificar si son una repetición.
- `OPENING` representa el saldo de entrada a V9.

El cambio de saldo, la inserción del movimiento y la confirmación comercial participan en una sola transacción. Las ventas y ajustes negativos usan descuento condicional para impedir saldos negativos. Los índices sirven a consultas por producto y a las restricciones de unicidad. `minimum_quantity` no altera el ledger porque cambiar un umbral no cambia existencias.

Una consulta de alerta considera `minimum_quantity > 0 AND quantity <= minimum_quantity`. No hay tabla de alertas ni entrega de notificaciones en V9.
