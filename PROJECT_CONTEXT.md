# Contexto permanente del proyecto

## Identidad

- Nombre: Gestión Comercial.
- Tipo: proyecto profesional de portafolio Full Stack Java + Angular.
- Objetivo formativo: adquirir experiencia práctica explicable en entrevistas, no solo producir una aplicación funcional.
- Arquitectura: monolito modular en un monorepo.
- Backend activo: Java 17 y Spring Boot.
- Frontend activo desde V11: Angular 18, TypeScript y RxJS; datos ficticios en memoria hasta la integración de V12.
- Persistencia: exclusivamente PostgreSQL.
- Convenciones regionales: PEN, persistencia temporal en UTC y presentación en `America/Lima`.

## Estado de continuidad

- Fase activa: ninguna.
- Último trabajo realizado: cierre aprobado de V11 con interfaz Angular 18, datos ficticios en memoria, formularios reactivos y vistas de los módulos existentes. El usuario autorizó integrar en `main` y publicar `main` y `v11` en GitHub.
- Estado de V11: cerrada y aprobada el 2026-10-17; versión de cierre `v11`; build y 10 pruebas frontend en verde.
- Estado de V10: cerrada y aprobada el 2026-10-17; versión de cierre `v10`; `verify` exitoso con 108 pruebas contra PostgreSQL 17.11.
- Estado de V9: cerrada y aprobada el 2026-10-14; versión de cierre `v9`; `verify` exitoso con 96 pruebas contra PostgreSQL 17.11.
- Estado de V8: cerrada y aprobada el 2026-10-14; versión de cierre `v8`; 90 pruebas en verde.
- Estado de V7: cerrada y aprobada el 2026-10-13; versión de cierre `v7`; 83 pruebas en verde.
- Estado de V6: cerrada y aprobada el 2026-09-23; versión de cierre `v6`; 75 pruebas en verde.
- Estado de V5: cerrada y aprobada el 2026-09-18; versión de cierre `v5`; 52 pruebas en verde.
- Estado de V4: cerrada y aprobada el 2026-09-18; versión de cierre `v4`; 27 pruebas en verde.
- Estado de V3: cerrada y aprobada el 2026-09-17; versión de cierre `v3`; 9 pruebas en verde.
- Estado de V2: cerrada y aprobada el 2026-09-17; versión de cierre `v2`.
- Estado de V1: cerrada y aprobada el 2026-09-17; versión de cierre `v1`.
- Código funcional existente: backend Spring Boot con Customer, Catalog, Supplier, Purchase, Sale, Inventory e Identity. Spring Security protege la API por permisos; login local, JWT HS256, refresh rotatorio y revocable, administración de usuarios, roles predefinidos, estado y contraseña. Identity usa JDBC propio; los módulos comerciales conservan JPA/PostgreSQL. El frontend Angular 18 muestra estos módulos con estado ficticio en memoria; no conecta aún con la API.
- Migraciones existentes: `V1__create_customers.sql` a `V7__identity.sql`, aplicadas por Flyway desde una base vacía; se mantiene la actualización desde V5 con saldo existente y movimiento de apertura. V7 añade seis tablas de Identity sin credenciales de seed.
- Pruebas automatizadas existentes: 108 backend en verde hasta V10 y 10 frontend en verde en V11. La suite backend incluye matriz completa con JWT reales, validación, revocación, renovación concurrente, renovación frente a desactivación y protección concurrente del último ADMIN.
- Cobertura informativa V10: 97.13% de líneas y 77.29% de ramas; sin gate hasta V17.
- Cobertura informativa V9: 97.60% de líneas y 76.19% de ramas; sin gate hasta V17.
- Cobertura informativa V8: 97.49% de líneas y 77.63% de ramas; sin gate hasta V17.
- Cobertura informativa V5: 96.83% de líneas y 82.03% de ramas; sin gate hasta V17.
- Cobertura informativa V6: 97.36% de líneas y 83.15% de ramas; sin gate hasta V17.
- Cobertura informativa V7: 97.69% de líneas y 79.75% de ramas; sin gate hasta V17.
- Bloqueos conocidos: Git exige `safe.directory` por comando debido a la diferencia de propietario entre el workspace y la cuenta de ejecución.
- Próximo paso: esperar una nueva solicitud, resumen de alcance y aprobación explícita antes de preparar V12. No iniciar V12 automáticamente. Antes de publicar el frontend o conectar datos reales, decidir cómo tratar los 8 hallazgos de `npm audit` en dependencias de producción de Angular 18.

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
17. V9 registra un movimiento por variación de stock desde la aplicación, conserva saldos previos como aperturas y serializa reintentos de ajustes por UUID; ADR 0007 registra esta decisión.
18. V10 aplica RBAC interno en la frontera HTTP, access JWT de 15 minutos y refresh opaco de siete días guardado como hash. Cambios de cuenta revocan renovaciones y aumentan una versión que invalida access tokens anteriores. Bloqueos transaccionales coordinan las carreras de sesión y administración; ADR 0008 fue aceptada con el cierre de V10.

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

En V9, `compile`, la prueba de migración V5→V6, `test` y `verify` terminaron en `BUILD SUCCESS`. La verificación final ejecutó 96 pruebas contra PostgreSQL 17.11. `git diff --check` no detectó errores de espacios. Se ejecutaron las pruebas de integración fuera del sandbox por el acceso al named pipe de Docker.

En V10, compilación, compilación de pruebas y pruebas seleccionadas pasaron después de correcciones aprobadas explícitamente. `verify` terminó en `BUILD SUCCESS` el 2026-10-17: 108 pruebas, 0 fallos, 0 errores y 0 omitidas, contra PostgreSQL 17.11; generó JAR y JaCoCo. La revisión documenta los fallos previos y sus correcciones. El bootstrap y la clave externa tienen pruebas automatizadas; no se efectuó un arranque manual local/prod con credenciales externas. Para arrancar fuera de test, consultar `docs/security/identity-v10.md` y las variables de `backend/.env.example`.

En V11, desde `frontend/`, `npm run build` terminó correctamente y `npm test -- --watch=false --browsers=ChromeHeadless` ejecutó 10 pruebas sin fallos fuera del sandbox. Playwright verificó alta local de cliente, vistas de compras/inventario y ausencia de errores de consola. `npm audit --omit=dev --json` informó 8 hallazgos en dependencias de producción (4 moderados, 4 altos); la estrategia de actualización de Angular 18 sigue pendiente de decisión. La evidencia detallada está en `docs/operations/v11-review.md`.

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
- API Inventory V9: `docs/api/inventory-v9.md`.
- Esquema Inventory V9: `docs/database/inventory-v9.md`.
- API Identity V10: `docs/api/identity-v10.md`.
- Frontend V11: `frontend/README.md`.
- Esquema Identity V10: `docs/database/identity-v10.md`.
- Seguridad y configuración V10: `docs/security/identity-v10.md`.
- Decisión de Identity: `docs/architecture/decisions/0008-local-identity-and-sessions-v10.md`.
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
- Evidencia de revisión V9: `docs/operations/v9-review.md`.
- Evidencia de cierre V10: `docs/operations/v10-review.md`.
- Evidencia de revisión V11: `docs/operations/v11-review.md`.
- Riesgos: `docs/risks.md`.

## Regla de actualización

Al cerrar cada fase, actualizar este archivo con estado, decisiones nuevas, tecnologías activas, comandos verificados, pruebas existentes, problemas pendientes y siguiente tarea concreta. Debe seguir siendo breve y útil como punto de reanudación.
