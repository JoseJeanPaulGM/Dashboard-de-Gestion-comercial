# Revisión de V1 — Preparación y arquitectura

## Evidencia de aceptación

| Criterio | Estado | Evidencia |
|---|---|---|
| Repositorio Git en rama `main` | Cumplido | `.git/` inicializado; sin commits todavía |
| Monorepo definido | Cumplido | `backend/`, `frontend/`, `docs/` y `.github/` |
| Memoria permanente | Cumplido | `PROJECT_CONTEXT.md` y `AGENTS.md` |
| Arquitectura explicada | Cumplido | `docs/architecture/overview.md` |
| Límites entre módulos | Cumplido | `docs/architecture/module-boundaries.md` |
| Decisiones registradas | Cumplido | ADR-0001 a ADR-0004 |
| Roadmap de 18 fases | Cumplido | `docs/roadmap.md` |
| Estrategia de ramas y commits | Cumplido | `docs/devops/git-workflow.md` |
| Contratos API preliminares | Cumplido | `docs/api/conventions.md` |
| Modelo conceptual | Cumplido | `docs/database/conceptual-model.md` |
| Roles preliminares | Cumplido | `docs/security/role-matrix.md` |
| Pruebas progresivas | Cumplido | `docs/testing/strategy.md` |
| Riesgos y glosario | Cumplido | `docs/risks.md` y `docs/glossary.md` |
| Plantillas operativas | Cumplido | plantillas de incidente, runbook y PR |
| Sin código funcional | Cumplido | solo Markdown y `.gitignore` |
| Enlaces documentales | Cumplido | verificación local sin enlaces rotos |
| Ausencia de secretos evidentes | Cumplido | escaneo por patrones sin hallazgos |
| Revisión y aprobación del usuario | Cumplido | aprobación explícita recibida el 2026-09-17 |

## Decisiones que deben conservarse

- PostgreSQL exclusivo; Oracle fuera del proyecto.
- Monolito modular y monorepo.
- Dominio genérico en PEN y zona `America/Lima`.
- Facturación electrónica posterior a V18.
- Autorización interna independiente de OIDC.
- No avanzar automáticamente entre fases.

## Cierre registrado

V1 fue aprobada y cerrada el 2026-09-17. El alcance entregado es exclusivamente documental: arquitectura, decisiones, roadmap, convenciones, matrices y plantillas operativas. No se creó código funcional ni se inició V2.

La siguiente fase continúa pendiente de autorización explícita.
