# Revisión de V6 — Supplier

## Estado

V6 fue aprobada y cerrada el 2026-09-23. La versión de cierre es `v6`; V7 continúa sin autorización.

## Evidencia provisional de aceptación

| Criterio | Estado | Evidencia |
|---|---|---|
| Módulo Supplier | Cumplido | API, aplicación, dominio y persistencia dentro del módulo |
| Proveedores | Cumplido | alta, consulta, actualización, listado y cambio de estado |
| Asociación Supplier–Product | Cumplido | vínculo único, código opcional, consulta y estado lógico |
| Límites modulares | Cumplido | Supplier depende del servicio de aplicación de Catalog, no de su repositorio |
| Reglas de ciclo de vida | Cumplido | partes activas para alta/reactivación y bloqueo al desactivar proveedor o producto con vínculos activos |
| Flyway y PostgreSQL | Cumplido | migración V3 aplicada desde esquema vacío en PostgreSQL 17 |
| Problem Details | Cumplido | validación, inexistencia, unicidad, paginación y conflictos de estado |
| Regresión | Cumplido | 75 pruebas en verde |
| Alcance | Cumplido | sin compras, inventario, ventas, seguridad o frontend |
| Regla Catalog–Supplier | Cumplido | puerto de Catalog implementado por Supplier, sin acceso a repositorios ajenos ni dependencia circular |

## Comandos ejecutados

Desde `backend/`:

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd "-Dtest=CatalogDomainTest,CustomerDomainTest,SupplierDomainTest,PaginationTest" test
.\mvnw.cmd test
.\mvnw.cmd verify
```

La compilación y `verify` terminaron en `BUILD SUCCESS`. La selección unitaria ejecutó 10 pruebas en verde. La suite completa ejecutó 75 pruebas contra PostgreSQL 17.11: 0 fallos, 0 errores y 0 omitidas. Testcontainers aplicó las tres migraciones Flyway desde un esquema vacío. JaCoCo midió 97.36% de líneas y 83.15% de ramas; continúa siendo informativo y sin gate hasta V17.

Dos intentos anteriores de la suite fallaron antes de ejecutar pruebas porque Docker Desktop no estaba disponible; no revelaron un defecto del código. Tras activar Docker, la ejecución completa terminó correctamente.

## Cierre registrado

- El módulo Supplier, sus vínculos con Product y las reglas simétricas de ciclo de vida fueron aprobados.
- Se autorizó el commit convencional, la integración en `main`, la publicación en GitHub y la etiqueta anotada `v6`.
- V7 no se inicia automáticamente y permanece pendiente de un resumen y aprobación explícita.
