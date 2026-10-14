# Revisión de V7 — Purchase

## Estado

V7 fue autorizada el 2026-10-09 y aprobada para cierre el 2026-10-13. La implementación, las pruebas y la documentación fueron revisadas antes de integrar la versión en `main` y crear la etiqueta anotada `v7`.

## Evidencia de aceptación

| Criterio | Estado | Evidencia |
|---|---|---|
| Compra como agregado | Cumplido | cabecera, líneas privadas, importes calculados y estados `DRAFT`/`CONFIRMED` |
| API | Cumplido | alta, consulta, listado paginado y confirmación; DTO y Problem Details |
| Maestros y vínculo | Cumplido | proveedor, producto y vínculo activos al crear y confirmar |
| Confirmación única | Cumplido | bloqueo de compra; repetición y dos llamadas concurrentes dejan un solo incremento |
| Atomicidad | Cumplido | fallo en un incremento posterior revierte el primero y mantiene el borrador |
| Persistencia | Cumplido | migración Flyway V4 desde base vacía en PostgreSQL 17.11; Hibernate valida esquema |
| Límites modulares | Cumplido | Purchase usa servicios de Supplier, Catalog e Inventory; no accede a repositorios ajenos |
| Alcance | Cumplido | saldo mínimo sin ledger, ventas, seguridad, frontend ni capacidades de V9 |
| Regresión | Cumplido | 83 pruebas en verde, sin fallos, errores ni omitidas |

## Comandos y resultados

Desde `backend/`:

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd "-Dtest=PurchaseDomainTest,PurchaseApiIntegrationTest,PersistenceIntegrationTest" test
.\mvnw.cmd "-Dtest=PurchaseApiIntegrationTest,PurchaseDomainTest" test
.\mvnw.cmd test
.\mvnw.cmd verify
```

Todos terminaron en `BUILD SUCCESS`. Las selecciones ejecutaron 13 y 8 pruebas, respectivamente. La suite completa y la verificación final del 2026-10-13 ejecutaron 83 pruebas contra PostgreSQL 17.11. JaCoCo midió 97.69% de líneas y 79.75% de ramas; no hay gate hasta V17. `git diff --check` no detectó errores de espacios. El caso de rollback provoca deliberadamente un desbordamiento `BIGINT` en el segundo saldo y comprueba que PostgreSQL revierte el primer incremento y el estado de compra.

## Decisión y pendientes

ADR 0005 documenta el saldo mínimo de Inventory adelantado para cumplir la confirmación atómica de V7. El ledger y los movimientos quedan para V9; la salida de stock por ventas queda para V8. V7 está cerrada; V8 no se inicia automáticamente y requiere su propio resumen y aprobación.
