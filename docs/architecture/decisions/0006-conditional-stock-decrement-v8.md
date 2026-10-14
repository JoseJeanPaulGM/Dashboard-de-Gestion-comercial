# ADR 0006 — Descuento condicional de stock en V8

- Estado: aceptada en V8, fase cerrada el 2026-10-14.
- Fecha: 2026-10-13.

## Contexto

Dos ventas distintas pueden intentar consumir el mismo saldo. Leer primero el saldo y escribir después permitiría decisiones basadas en un valor obsoleto. Sale tampoco debe escribir la tabla de Inventory.

## Decisión

Inventory expone a Sale una operación interna de descuento que exige una transacción existente. Su adaptador ejecuta un único `UPDATE` sobre `inventory_balances` con condición `quantity >= cantidad`; cero filas actualizadas significa stock ausente o insuficiente. PostgreSQL bloquea la fila durante la actualización y vuelve a comprobar la condición cuando una venta concurrente libera el bloqueo. Sale bloquea su propia fila para evitar confirmaciones repetidas y descuenta las líneas por identificador de producto ascendente para mantener un orden estable de bloqueos. Todos los descuentos y el cambio de estado participan en una transacción local.

## Consecuencias

Las ventas concurrentes no pueden dejar saldo negativo ni consumir dos veces la misma existencia. Si falla una línea, la transacción revierte los descuentos previos y la venta queda en borrador. Inventory conserva la propiedad del saldo. El historial explicativo de movimientos sigue previsto para V9.
