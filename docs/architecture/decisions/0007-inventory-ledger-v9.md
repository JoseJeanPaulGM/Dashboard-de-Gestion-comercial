# ADR 0007 — Ledger e idempotencia de Inventory en V9

- Estado: aceptada en V9, fase cerrada el 2026-10-14.
- Fecha: 2026-10-14.

## Contexto

V7 y V8 guardan saldos y aplican cambios atómicos, pero el saldo no explica cada variación. V9 debe conservar el origen de cada cambio sin trasladar la propiedad del stock a Purchase ni Sale. Las bases existentes pueden contener saldos previos al ledger.

## Decisión

Inventory registra en su propia tabla un movimiento por línea de compra o venta confirmada y por ajuste. El cambio de saldo y el movimiento comparten la transacción comercial. Una restricción única sobre tipo y línea protege contra duplicados. Los ajustes requieren UUID y motivo; una restricción única y un bloqueo transaccional por clave serializan reintentos concurrentes. El saldo anterior a V9 se conserva mediante un movimiento de apertura por producto con cantidad positiva, sin reconstruir eventos que no constan en el modelo previo.

Los mínimos se guardan en el saldo. Las alertas se calculan al leer, con mínimo positivo y cantidad menor o igual al umbral.

## Consecuencias

Desde V9, cada variación aplicada por la aplicación tiene un movimiento comprobable. El ledger y el saldo se revierten juntos si falla una confirmación o ajuste. La apertura documenta el límite temporal de trazabilidad. Inventory sigue siendo dueño de sus tablas y de sus operaciones de escritura. La autorización de ajustes se añadirá en V10.
