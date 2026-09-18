# Gestión Comercial

Proyecto de portafolio Full Stack que evolucionará, en 18 fases controladas, desde una base documental hasta una aplicación comercial desplegada y operable.

## Estado actual

- Última fase cerrada: **V4 — Clientes** (2026-09-18).
- Fase activa: ninguna.
- Código funcional: backend Spring Boot con módulo Customer, API paginada, Flyway, PostgreSQL y pruebas de integración.
- Base de datos oficial: PostgreSQL.
- Moneda y zona horaria: PEN y `America/Lima`.
- Siguiente paso: V5 permanece pendiente y no está autorizada para iniciar.

La fuente breve de continuidad es [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md). El índice documental completo está en [docs/README.md](docs/README.md).

## Alcance del producto

El sistema gestionará progresivamente clientes, proveedores, categorías, productos, compras, ventas, inventario, usuarios, autorización, dashboard, reportes, auditoría e integraciones externas.

La evolución prevista incluye Java 17, Spring Boot, PostgreSQL, Angular 18, JWT, OAuth 2.0/OpenID Connect, Docker, GitHub Actions y despliegue en Render. Cada tecnología se introducirá cuando exista una necesidad concreta y no antes.

## Estructura del monorepo

```text
.
├── backend/                # Reservado para Spring Boot desde V2
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
PLAN → REVISAR → APROBAR → CHAT → IMPLEMENTAR → PROBAR
     → REVISAR → COMMIT → CERRAR FASE → ESPERAR
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
