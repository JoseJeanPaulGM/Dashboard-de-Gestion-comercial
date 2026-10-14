# Contexto permanente del proyecto

## Identidad

- Nombre: Gestión Comercial.
- Tipo: proyecto profesional de portafolio Full Stack Java + Angular.
- Objetivo formativo: adquirir experiencia práctica explicable en entrevistas, no solo producir una aplicación funcional.
- Arquitectura: monolito modular en un monorepo.
- Backend futuro: Java 17 y Spring Boot.
- Frontend futuro: Angular 18, TypeScript y RxJS.
- Persistencia futura: exclusivamente PostgreSQL.
- Convenciones regionales: PEN, persistencia temporal en UTC y presentación en `America/Lima`.

## Estado de continuidad

- Fase activa: ninguna.
- Último trabajo realizado: cierre aprobado de V8 con ventas, descuento atómico de stock y concurrencia controlada.
- Estado de V8: cerrada y aprobada el 2026-10-14; versión de cierre `v8`; 90 pruebas en verde.
- Estado de V7: cerrada y aprobada el 2026-10-13; versión de cierre `v7`; 83 pruebas en verde.
- Estado de V6: cerrada y aprobada el 2026-09-23; versión de cierre `v6`; 75 pruebas en verde.
- Estado de V5: cerrada y aprobada el 2026-09-18; versión de cierre `v5`; 52 pruebas en verde.
- Estado de V4: cerrada y aprobada el 2026-09-18; versión de cierre `v4`; 27 pruebas en verde.
- Estado de V3: cerrada y aprobada el 2026-09-17; versión de cierre `v3`; 9 pruebas en verde.
- Estado de V2: cerrada y aprobada el 2026-09-17; versión de cierre `v2`.
- Estado de V1: cerrada y aprobada el 2026-09-17; versión de cierre `v1`.
- Código funcional existente: backend Spring Boot con módulos Customer, Catalog, Supplier, Purchase, Sale y saldo mínimo de Inventory, endpoints REST, paginación compartida y persistencia JPA/PostgreSQL.
- Migraciones existentes: `V1__create_customers.sql` a `V5__create_sales.sql`, aplicadas por Flyway desde una base vacía.
- Pruebas automatizadas existentes: 90 pruebas en verde; incluyen compras, ventas concurrentes, stock insuficiente, rollback, PostgreSQL 17, restricciones y regresión V2–V7.
- Cobertura informativa V8: 97.49% de líneas y 77.63% de ramas; sin gate hasta V17.
- Cobertura informativa V5: 96.83% de líneas y 82.03% de ramas; sin gate hasta V17.
- Cobertura informativa V6: 97.36% de líneas y 83.15% de ramas; sin gate hasta V17.
- Cobertura informativa V7: 97.69% de líneas y 79.75% de ramas; sin gate hasta V17.
- Bloqueos conocidos: Git exige `safe.directory` por comando debido a la diferencia de propietario entre el workspace y la cuenta de ejecución.
- Próximo paso: esperar aprobación explícita para preparar V9. No iniciar V9 automáticamente.

## Decisiones vigentes

1. Monolito modular y organización por dominio.
2. Monorepo con `backend/`, `frontend/`, `docs/` e infraestructura compartida.
3. API futura bajo `/api/v1`, JSON y errores Problem Details.
4. Identificadores internos `Long`; importes `BigDecimal`; PEN como moneda base.
5. PostgreSQL es la única base oficial; Oracle queda excluido.
6. Flyway administra el esquema desde V4; Hibernate usa `validate` en todos los perfiles.
7. Autorización interna mediante usuarios, roles y permisos; OIDC no la reemplazará.
8. Integración externa de negocio: tipo de cambio USD/PEN mediante un adaptador aislado.
9. Facturación electrónica queda como evolución posterior a V18.
10. Ninguna fase comienza sin aprobación explícita de la anterior.
11. V4 establece `PageResponse<T>` y validación técnica de `page`, `size` y `sort`; cada módulo conserva sus filtros y campos de orden.
12. V5 reutilizó esta base de paginación para productos y categorías y añadió sus filtros propios.
13. V6 modela Supplier como dueño del vínculo lógico con Product y consume Catalog mediante su servicio de aplicación.
14. Catalog protege la desactivación de Product mediante un puerto implementado por Supplier; un producto o proveedor con vínculos activos no puede desactivarse.
15. V7 incorpora el saldo mínimo bajo propiedad de Inventory; Purchase lo incrementa dentro de la transacción de confirmación. ADR 0005 registra esta decisión.
16. V8 descuenta saldo mediante una actualización condicional de Inventory, con líneas ordenadas por producto y confirmación transaccional. ADR 0006 registra esta decisión.

Los motivos están registrados en `docs/architecture/decisions/`.

## Módulos previstos

| Módulo | Responsabilidad | Primera fase |
|---|---|---:|
| Shared/API | contratos técnicos, errores y paginación | V2 |
| Customer | clientes y estado comercial | V4 |
| Catalog | categorías y productos | V5 |
| Supplier | proveedores | V6 |
| Purchase | compras y confirmación | V7 |
| Sale | ventas y control de stock | V8 |
| Inventory | saldos, movimientos y alertas | V9 |
| Identity | usuarios, roles, permisos y sesión | V10 |
| Dashboard | indicadores e integración cambiaria | V13 |
| Report | reportes y exportaciones | V14 |
| Audit | trazabilidad de acciones | V15 |

## Contratos transversales acordados

- Endpoints principales: `/api/v1/{resource}`.
- Listados: `page`, `size`, `sort` y filtros permitidos explícitamente.
- Errores: Problem Details con código funcional, timestamp, path y errores de campo.
- Fechas: ISO 8601; instantes almacenados en UTC.
- Registros comerciales referenciados: desactivación lógica, no borrado arbitrario.
- Logs: correlation ID y exclusión de tokens, contraseñas y secretos.

## Comandos verificados

Desde `backend/`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

En V8 ambos comandos terminaron en `BUILD SUCCESS` con 90 pruebas contra PostgreSQL 17.11. La compilación y las 13 pruebas seleccionadas de Sale y persistencia también terminaron correctamente.

Las pruebas de integración requieren Docker Desktop operativo y usan `postgres:17-alpine` mediante Testcontainers. En el entorno del agente deben ejecutarse fuera del sandbox para acceder al named pipe de Docker.

Para inspeccionar Git en este entorno:

```powershell
git -c safe.directory='D:/portfolio 2026' status --short --branch
```

No se debe añadir la excepción de forma global sin autorización del propietario del equipo.

## Documentos esenciales

- Arquitectura: `docs/architecture/overview.md`.
- API backend V2: `docs/api/backend-base.md`.
- API Customer V4: `docs/api/customers-v4.md`.
- Límites modulares: `docs/architecture/module-boundaries.md`.
- Roadmap: `docs/roadmap.md`.
- Modelo conceptual: `docs/database/conceptual-model.md`.
- Persistencia V3: `docs/database/persistence-v3.md`.
- Esquema Customer V4: `docs/database/customers-v4.md`.
- API Catalog V5: `docs/api/catalog-v5.md`.
- Esquema Catalog V5: `docs/database/catalog-v5.md`.
- API Supplier V6: `docs/api/suppliers-v6.md`.
- Esquema Supplier V6: `docs/database/suppliers-v6.md`.
- API Purchase V7: `docs/api/purchases-v7.md`.
- Esquema Purchase V7: `docs/database/purchases-v7.md`.
- API Sale V8: `docs/api/sales-v8.md`.
- Esquema Sale V8: `docs/database/sales-v8.md`.
- Seguridad: `docs/security/role-matrix.md`.
- API: `docs/api/conventions.md`.
- Pruebas: `docs/testing/strategy.md`.
- Git: `docs/devops/git-workflow.md`.
- Ciclo por fase: `docs/operations/phase-workflow.md`.
- Evidencia de revisión V1: `docs/operations/v1-review.md`.
- Evidencia de revisión V2: `docs/operations/v2-review.md`.
- Evidencia de revisión V3: `docs/operations/v3-review.md`.
- Evidencia de revisión V4: `docs/operations/v4-review.md`.
- Evidencia de revisión V5: `docs/operations/v5-review.md`.
- Evidencia de revisión V6: `docs/operations/v6-review.md`.
- Evidencia de revisión V7: `docs/operations/v7-review.md`.
- Evidencia de revisión V8: `docs/operations/v8-review.md`.
- Riesgos: `docs/risks.md`.

## Regla de actualización

Al cerrar cada fase, actualizar este archivo con estado, decisiones nuevas, tecnologías activas, comandos verificados, pruebas existentes, problemas pendientes y siguiente tarea concreta. Debe seguir siendo breve y útil como punto de reanudación.
