# ADR 0005 — Saldo mínimo de Inventory en V7

- Estado: aceptada para V7.
- Fecha: 2026-10-09.

## Contexto

V7 debe confirmar compras de forma atómica e incrementar existencias una sola vez. El módulo Inventory completo está previsto para V9, pero guardar el saldo dentro de Purchase daría a Purchase la propiedad de datos ajenos y complicaría V8 y V9.

## Decisión

Crear en V7 una capacidad mínima de Inventory: saldo por producto y una operación interna de incremento. Purchase solo usa el servicio de Inventory; no escribe su tabla. El incremento se ejecuta en la misma transacción local que la confirmación de la compra. La compra se bloquea al confirmar y el saldo se incrementa mediante un upsert aditivo de PostgreSQL.

## Consecuencias

V7 entrega una entrada de stock comprobable sin introducir ledger ni API de inventario. V8 podrá consumir el saldo para ventas. V9 ampliará el modelo con movimientos, mínimos y trazabilidad, conservando esta propiedad del dato dentro de Inventory. El saldo por sí solo no explica históricamente cada variación; las compras confirmadas son la evidencia de entradas hasta V9.
