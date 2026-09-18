# Roadmap V1–V18

El roadmap define el orden de aprendizaje. “Previsto” no significa implementado.

| Fase | Entrega principal | Hito de aprendizaje | Estado |
|---:|---|---|---|
| V1 | arquitectura, documentación y gobierno | decisiones, Git y continuidad | Cerrada (2026-09-17) |
| V2 | backend HTTP mínimo | Spring Boot, DTO y errores | Cerrada (2026-09-17) |
| V3 | persistencia inicial | PostgreSQL, JPA y transacciones | Cerrada (2026-09-17) |
| V4 | clientes | primer vertical slice, Flyway y base reutilizable de paginación | Cerrada (2026-09-18) |
| V5 | productos y categorías | relaciones, dinero y ampliación de la paginación de V4 | Cerrada (2026-09-18) |
| V6 | proveedores | asociaciones y reglas de ciclo de vida | Pendiente |
| V7 | compras | agregados y atomicidad | Pendiente |
| V8 | ventas | stock, locking y concurrencia | Pendiente |
| V9 | inventario | ledger, trazabilidad e idempotencia | Pendiente |
| V10 | usuarios, RBAC y JWT | seguridad de API | Pendiente |
| V11 | interfaz Angular | componentes, RxJS y formularios | Pendiente |
| V12 | integración Full Stack | HTTP, sesión e interceptores | Pendiente |
| V13 | dashboard y tipo de cambio | agregaciones y resiliencia externa | Pendiente |
| V14 | reportes | optimización, filtros y CSV | Pendiente |
| V15 | auditoría | trazabilidad y logs estructurados | Pendiente |
| V16 | OAuth 2.0/OIDC genérico | federación y vinculación | Pendiente |
| V17 | Google/Microsoft, Docker, GitHub Actions y CI/CD | builds reproducibles, publicación de imágenes y calidad | Pendiente |
| V18 | Render, autodeploy condicionado a checks exitosos, monitoreo, backups y operación | deploy, recuperación y operación verificable | Pendiente |

## Reglas del roadmap

- Las 18 fases se conservan; un cambio estructural requiere explicación y aprobación.
- Cada fase produce un estado estable, probado y documentado.
- Las tareas no terminadas no se ocultan: se registran y se decide si bloquean el cierre.
- No se incorporan herramientas de una fase posterior solo por conveniencia.
- El detalle de la fase siguiente se revisa al cerrarse la fase actual para incorporar lo aprendido.

V4 establece el contrato paginado transversal y la validación de `page`, `size` y `sort`. V5 deberá reutilizar esa base para productos y categorías, manteniendo en Catalog la propiedad de sus filtros, búsquedas y campos de orden.

## Criterio de producto final

Al cerrar V18 deberá existir una aplicación desplegada con flujo compra–inventario–venta, autorización interna, frontend integrado, reportes, auditoría, autenticación externa, CI/CD, monitoreo y procedimientos de recuperación verificables.
