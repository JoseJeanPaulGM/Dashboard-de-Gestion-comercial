# Revisión de V8 — Sale

## Estado

V8 fue autorizada el 2026-10-13 y aprobada para cierre el 2026-10-14. La implementación, las pruebas y la documentación fueron revisadas antes de integrar la versión en `main` y crear la etiqueta anotada `v8`.

## Evidencia de aceptación

| Criterio | Estado | Evidencia |
|---|---|---|
| Venta como agregado | Cumplido | cabecera, líneas privadas, precios de Catalog como snapshot, total PEN y estados `DRAFT`/`CONFIRMED` |
| API | Cumplido | alta, consulta, listado paginado y confirmación; DTO y Problem Details |
| Stock | Cumplido | Sale usa el servicio de Inventory; descuento condicional sin saldo negativo |
| Concurrencia | Cumplido | dos ventas compiten por el mismo saldo y solo una se confirma |
| Atomicidad | Cumplido | stock insuficiente en una línea posterior revierte los descuentos anteriores y conserva el borrador |
| Persistencia | Cumplido | migración Flyway V5 desde base vacía en PostgreSQL 17.11; Hibernate valida esquema |
| Alcance | Cumplido | sin ledger, movimientos, alertas, seguridad ni frontend |
| Regresión | Cumplido | 90 pruebas en verde, sin fallos, errores ni omitidas |

## Comandos y resultados

Desde `backend/`:

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd "-Dtest=SaleDomainTest,SaleApiIntegrationTest,PersistenceIntegrationTest" test
.\mvnw.cmd test
.\mvnw.cmd verify
```

La compilación terminó en `BUILD SUCCESS`. La primera ejecución de las pruebas seleccionadas dentro del sandbox falló por acceso denegado al named pipe de Docker; se repitió fuera del sandbox, como indica `PROJECT_CONTEXT.md`, y terminó con 13 pruebas en verde. La suite completa y `verify` terminaron en `BUILD SUCCESS` con 90 pruebas. JaCoCo midió 97.49% de líneas y 77.63% de ramas, sin gate hasta V17. `git diff --check` no detectó errores de espacios; solo informó la conversión de finales de línea en `backend/README.md`.

## Decisión y pendientes

ADR 0006 registra el descuento condicional y el orden de bloqueos. Permanecen para V9 el ledger, los movimientos, los mínimos y las alertas; la autorización por rol llegará en V10 y el frontend en V11. V8 está cerrada; V9 no se inicia automáticamente y requiere su propio resumen y aprobación.
