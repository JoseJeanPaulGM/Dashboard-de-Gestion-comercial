# Gestión Comercial

Proyecto de portafolio Full Stack que evolucionará, en 18 fases controladas, desde una base documental hasta una aplicación comercial desplegada y operable.

## Estado actual

- Última fase cerrada: **V10 — Identity, RBAC y JWT** (2026-10-17), versión `v10`.
- Fase activa: ninguna.
- Código funcional: backend Spring Boot con Customer, Catalog, Supplier, Purchase, Sale, Inventory e Identity; API paginada, Flyway y PostgreSQL. Las compras y ventas cambian stock y generan movimientos atómicos; Inventory ofrece ajustes idempotentes, mínimos y alertas. Identity incorpora login local, permisos y sesiones revocables.
- Verificación de V10: 108 pruebas automatizadas en verde contra PostgreSQL 17.11; `verify` completado el 2026-10-17.
- Base de datos oficial: PostgreSQL.
- Moneda y zona horaria: PEN y `America/Lima`.
- Siguiente paso: esperar una nueva aprobación antes de preparar V11. No se inicia la siguiente versión automáticamente.

La fuente breve de continuidad es [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md). El índice documental completo está en [docs/README.md](docs/README.md).
El contrato de compras está en [docs/api/purchases-v7.md](docs/api/purchases-v7.md) y su esquema en [docs/database/purchases-v7.md](docs/database/purchases-v7.md).
El contrato de ventas está en [docs/api/sales-v8.md](docs/api/sales-v8.md), su esquema en [docs/database/sales-v8.md](docs/database/sales-v8.md) y la evidencia de cierre en [docs/operations/v8-review.md](docs/operations/v8-review.md).
El contrato de inventario está en [docs/api/inventory-v9.md](docs/api/inventory-v9.md), su esquema en [docs/database/inventory-v9.md](docs/database/inventory-v9.md) y la evidencia de cierre en [docs/operations/v9-review.md](docs/operations/v9-review.md).
El contrato de seguridad está en [docs/api/identity-v10.md](docs/api/identity-v10.md), su configuración en [docs/security/identity-v10.md](docs/security/identity-v10.md) y la evidencia de cierre en [docs/operations/v10-review.md](docs/operations/v10-review.md).

## Alcance del producto

El sistema gestionará progresivamente clientes, proveedores, categorías, productos, compras, ventas, inventario, usuarios, autorización, dashboard, reportes, auditoría e integraciones externas.

La evolución prevista incluye Java 17, Spring Boot, PostgreSQL, Angular 18, JWT, OAuth 2.0/OpenID Connect, Docker, GitHub Actions y despliegue en Render. Cada tecnología se introducirá cuando exista una necesidad concreta y no antes.

## Estructura del monorepo

```text
.
├── backend/                # API Spring Boot activa desde V2
├── frontend/               # Reservado para Angular desde V11
├── docs/                   # Documentación viva por disciplina
├── .github/                # Plantillas y, desde V17, workflows
├── AGENTS.md               # Reglas operativas para futuros agentes
├── PROJECT_CONTEXT.md      # Memoria permanente y punto de reanudación
└── README.md               # Presentación y acceso rápido
```

## Forma de trabajo

Cada fase sigue este flujo sin saltos:

```text
RESUMEN → REVISAR → APROBAR INICIO → IMPLEMENTAR → PROBAR
        → DOCUMENTAR → REVISAR → APROBAR CIERRE
        → COMMIT/TAG → CERRAR FASE → ESPERAR
```

No se inicia una fase nueva automáticamente. Las reglas completas están en [docs/operations/phase-workflow.md](docs/operations/phase-workflow.md) y el flujo Git en [docs/devops/git-workflow.md](docs/devops/git-workflow.md).

## Arranque para una nueva conversación

1. Leer `AGENTS.md`.
2. Leer `PROJECT_CONTEXT.md`.
3. Confirmar la fase y el estado actuales.
4. Revisar los documentos enlazados por el contexto.
5. No implementar la siguiente fase sin aprobación explícita.

## Fuera de alcance

- Oracle y microservicios.
- Facturación electrónica y SUNAT.
- Contabilidad completa, pagos y logística.
- Despliegue simultáneo en AWS, Azure y GCP.
- Alta disponibilidad multirregión o Kubernetes durante V1–V18.
