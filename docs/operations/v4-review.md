# Revisión de V4 — Customer y Flyway

## Estado

V4 fue aprobada y cerrada el 2026-09-18. La versión de cierre es `v4`; V5 continúa sin autorización.

## Evidencia de aceptación

| Criterio | Estado | Evidencia |
|---|---|---|
| Primer vertical slice Customer | Cumplido | API, aplicación, dominio y persistencia dentro del módulo |
| Ciclo de vida sin borrado físico | Cumplido | alta, consulta, actualización, desactivación y reactivación |
| Listado paginado | Cumplido | `page`, `size`, `sort`, totales y página vacía fuera de rango |
| Filtro y búsqueda | Cumplido | `active` y búsqueda literal por nombre o documento |
| Base reutilizable | Cumplido | contratos técnicos en Shared; filtros permanecen en Customer |
| Flyway | Cumplido | migración V1 aplicada desde esquema vacío |
| Hibernate no administra DDL | Cumplido | `ddl-auto: validate` en todos los perfiles |
| PostgreSQL exclusivo | Cumplido | pruebas con `postgres:17-alpine`; sin H2 |
| Problem Details | Cumplido | validación, petición, paginación, orden, 404 y conflicto |
| JaCoCo informativo | Cumplido | reporte generado sin gate |
| Regresión y build | Cumplido | 27 pruebas y `verify` en verde |
| Alcance V4 | Cumplido | sin Catalog, seguridad, frontend ni infraestructura futura |

## Comandos ejecutados

Desde `backend/`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

Resultado final de ambos comandos: `BUILD SUCCESS`, 27 pruebas, 0 fallos, 0 errores y 0 omitidas. JaCoCo: 95.04% de líneas y 90% de ramas.

La primera ejecución dentro del sandbox compiló el proyecto, pero no pudo abrir `\\.\pipe\docker_engine`. Las verificaciones finales se ejecutaron fuera del sandbox, como ya estaba documentado desde V3.

## Cierre registrado

- El contrato Customer y la base paginada compartida fueron aprobados.
- Se autorizó el commit convencional, la integración en `main` y la etiqueta anotada `v4`.
- V5 reutilizará la paginación para productos y categorías, pero no se inicia automáticamente.
