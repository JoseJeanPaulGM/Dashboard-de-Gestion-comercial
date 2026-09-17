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
- Último trabajo realizado: cierre aprobado de V3, con PostgreSQL 17, Spring Data JPA, Hibernate, configuración por ambientes y pruebas Testcontainers de persistencia y transacciones.
- Estado de V3: cerrada y aprobada el 2026-09-17; versión de cierre `v3`; 9 pruebas en verde.
- Estado de V2: cerrada y aprobada el 2026-09-17; versión de cierre `v2`.
- Estado de V1: cerrada y aprobada el 2026-09-17; versión de cierre `v1`.
- Código funcional existente: backend Spring Boot con infraestructura JPA/PostgreSQL; sin entidades ni funcionalidad comercial.
- Migraciones existentes: ninguna.
- Pruebas automatizadas existentes: 4 pruebas MVC de V2 y 5 pruebas V3 contra PostgreSQL 17 para conexión, JPA, restricciones, commit y rollback.
- Bloqueos conocidos: Git exige `safe.directory` por comando debido a la diferencia de propietario entre el workspace y la cuenta de ejecución.
- Próximo paso: esperar aprobación explícita para planificar e iniciar V4. No comenzar V4 todavía.

## Decisiones vigentes

1. Monolito modular y organización por dominio.
2. Monorepo con `backend/`, `frontend/`, `docs/` e infraestructura compartida.
3. API futura bajo `/api/v1`, JSON y errores Problem Details.
4. Identificadores internos `Long`; importes `BigDecimal`; PEN como moneda base.
5. PostgreSQL es la única base oficial; Oracle queda excluido.
6. Flyway se incorporará en V4, cuando el esquema necesite evolución controlada.
7. Autorización interna mediante usuarios, roles y permisos; OIDC no la reemplazará.
8. Integración externa de negocio: tipo de cambio USD/PEN mediante un adaptador aislado.
9. Facturación electrónica queda como evolución posterior a V18.
10. Ninguna fase comienza sin aprobación explícita de la anterior.

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

Las pruebas V3 requieren Docker Desktop operativo y usan `postgres:17-alpine` mediante Testcontainers. En el entorno del agente deben ejecutarse fuera del sandbox para acceder al named pipe de Docker.

Para inspeccionar Git en este entorno:

```powershell
git -c safe.directory='D:/portfolio 2026' status --short --branch
```

No se debe añadir la excepción de forma global sin autorización del propietario del equipo.

## Documentos esenciales

- Arquitectura: `docs/architecture/overview.md`.
- API backend V2: `docs/api/backend-base.md`.
- Límites modulares: `docs/architecture/module-boundaries.md`.
- Roadmap: `docs/roadmap.md`.
- Modelo conceptual: `docs/database/conceptual-model.md`.
- Persistencia V3: `docs/database/persistence-v3.md`.
- Seguridad: `docs/security/role-matrix.md`.
- API: `docs/api/conventions.md`.
- Pruebas: `docs/testing/strategy.md`.
- Git: `docs/devops/git-workflow.md`.
- Ciclo por fase: `docs/operations/phase-workflow.md`.
- Evidencia de revisión V1: `docs/operations/v1-review.md`.
- Evidencia de revisión V2: `docs/operations/v2-review.md`.
- Evidencia de revisión V3: `docs/operations/v3-review.md`.
- Riesgos: `docs/risks.md`.

## Regla de actualización

Al cerrar cada fase, actualizar este archivo con estado, decisiones nuevas, tecnologías activas, comandos verificados, pruebas existentes, problemas pendientes y siguiente tarea concreta. Debe seguir siendo breve y útil como punto de reanudación.
