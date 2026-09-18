# Revisión de V5 — Catalog

## Estado

V5 fue aprobada y cerrada el 2026-09-18. La versión de cierre es `v5`; V6 continúa sin autorización.

## Evidencia provisional de aceptación

| Criterio | Estado | Evidencia |
|---|---|---|
| Módulo Catalog | Cumplido | API, aplicación, dominio y persistencia dentro del módulo |
| Categorías y productos | Cumplido | alta, consulta, actualización y cambio de estado |
| Relación Category 1:N Product | Cumplido | FK obligatoria, sin cascada ni borrado físico |
| Reglas de ciclo de vida | Cumplido | categoría activa para alta/reactivación y bloqueo con productos activos |
| Precio PEN | Cumplido | `BigDecimal`, `NUMERIC(12,2)`, check positivo y moneda en respuesta |
| Paginación V4 | Cumplido | reutilización de contratos Shared con filtros y órdenes de Catalog |
| Flyway y PostgreSQL | Cumplido | migración V2 aplicada desde esquema vacío en PostgreSQL 17 |
| Problem Details | Cumplido | validación, inexistencia, unicidad, paginación y conflictos de estado |
| Regresión y build | Cumplido | 52 pruebas y `verify` en verde |
| Alcance | Cumplido | sin Supplier, compras, ventas, stock, seguridad o frontend |

## Comandos ejecutados

Desde `backend/`:

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd -DskipTests test
.\mvnw.cmd test
.\mvnw.cmd verify
```

La compilación principal, la suite y `verify` terminaron en `BUILD SUCCESS`. La verificación final ejecutó 52 pruebas: 0 fallos, 0 errores y 0 omitidas. JaCoCo midió 96.83% de líneas y 82.03% de ramas. Las pruebas de integración se ejecutaron fuera del sandbox para acceder al named pipe de Docker Desktop y levantaron `postgres:17-alpine` mediante Testcontainers.

Una ejecución intermedia de `verify` no pudo iniciar porque Docker Desktop estaba pausado; no reveló un defecto del código. Tras reactivarlo, la verificación completa terminó correctamente.

## Cierre registrado

- El módulo Catalog, su relación Category–Product y el tratamiento de importes PEN fueron aprobados.
- Se autorizó el commit convencional, la integración en `main` y la etiqueta anotada `v5`.
- V6 no se inicia automáticamente y permanece pendiente de un resumen y aprobación explícita.
