# Revisión de V9 — Inventory

## Estado

V9 fue autorizada y aprobada para cierre el 2026-10-14. La implementación, las pruebas y la documentación fueron revisadas antes de integrar la versión en `main` y crear la etiqueta anotada `v9`. V10 no se ha iniciado.

## Entrega

| Criterio | Evidencia |
|---|---|
| Ledger | Movimiento único por línea confirmada de compra o venta; cambio y saldo resultante; transacción compartida con la operación comercial |
| Ajustes | Motivo obligatorio, límite de cantidad, stock no negativo, UUID idempotente y reintentos concurrentes serializados |
| Mínimos y alertas | Umbral por producto, consulta de saldos y alerta calculada para cantidad menor o igual a un mínimo positivo |
| Compatibilidad | Migración V6 desde base vacía y actualización de V5 con saldo existente mediante movimiento de apertura; `CHECK` de signo y referencia por origen comercial |
| API | Consulta de saldo, mínimo, movimientos paginados, alertas paginadas y ajustes; Problem Details |
| Exclusiones | Sin RBAC/JWT, interfaz Angular ni notificaciones externas |

## Comandos y pruebas

Desde `backend/`:

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd '-Dtest=InventoryApiIntegrationTest,PurchaseApiIntegrationTest,SaleApiIntegrationTest,PersistenceIntegrationTest' test
.\mvnw.cmd '-Dtest=PersistenceIntegrationTest' test
.\mvnw.cmd test
.\mvnw.cmd verify
```

La compilación terminó en `BUILD SUCCESS`. El primer intento de pruebas seleccionadas dentro del sandbox falló por acceso denegado al named pipe de Docker; repetido fuera del sandbox, permitió ejecutar los casos. Una prueba detectó que faltaba `lowStock` en JSON; la corrección recibió aprobación explícita. La prueba específica de migración V5→V6 terminó en `BUILD SUCCESS` con siete casos. La suite completa terminó en `BUILD SUCCESS` con 94 pruebas. Las mejoras del `CHECK` de signos y referencias no nulas recibieron aprobación explícita. La verificación final terminó en `BUILD SUCCESS`: **96 pruebas**, 0 fallos, 0 errores y 0 omitidas, contra PostgreSQL 17.11. JaCoCo midió 97.60% de líneas y 76.19% de ramas, sin gate hasta V17. `git diff --check` no detectó errores de espacios.

## Decisiones y pendientes

ADR 0007 explica el ledger, la apertura de saldos anteriores y la idempotencia de ajustes. La apertura no reconstruye cada movimiento previo a V9. La autorización de ajustes pertenece a V10 y requiere una nueva aprobación de inicio. V9 quedó cerrada; no se inicia automáticamente la siguiente fase.
