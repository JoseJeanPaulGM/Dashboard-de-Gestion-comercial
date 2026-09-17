# Índice de documentación

La documentación es parte del producto y evoluciona en la misma fase que el código relacionado.

## Orientación

- [Roadmap V1–V18](roadmap.md)
- [Glosario](glossary.md)
- [Riesgos](risks.md)
- [Visión de arquitectura](architecture/overview.md)
- [Límites modulares](architecture/module-boundaries.md)
- [Modelo conceptual](database/conceptual-model.md)
- [Convenciones de API](api/conventions.md)
- [Backend base V2](api/backend-base.md)
- [Matriz de roles](security/role-matrix.md)
- [Estrategia de pruebas](testing/strategy.md)
- [Flujo Git](devops/git-workflow.md)
- [Ciclo de fases](operations/phase-workflow.md)
- [Revisión de V1](operations/v1-review.md)
- [Revisión de V2](operations/v2-review.md)

## Directorios por disciplina

| Directorio | Contenido previsto |
|---|---|
| `architecture/` | visión, límites, diagramas y ADR |
| `database/` | modelo, migraciones, índices y operación PostgreSQL |
| `security/` | JWT, RBAC, OAuth 2.0 y OIDC |
| `api/` | convenciones, OpenAPI y contratos |
| `integrations/` | proveedores externos y resiliencia |
| `testing/` | estrategia, cobertura y evidencias |
| `devops/` | Git, Docker y CI/CD |
| `deployment/` | ambientes y Render |
| `monitoring/` | logs, métricas, alertas y health checks |
| `backups/` | backup, retención, restauración y RPO/RTO |
| `operations/` | runbooks, incidentes y cierre de fases |

Los documentos de fases futuras definen intención y restricciones, pero no declaran capacidades como implementadas.
