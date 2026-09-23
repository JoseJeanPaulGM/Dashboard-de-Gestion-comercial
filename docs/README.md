# Índice de documentación

La documentación es parte del producto y evoluciona en la misma fase que el código relacionado.

## Orientación

- [Roadmap V1–V18](roadmap.md)
- [Glosario](glossary.md)
- [Riesgos](risks.md)
- [Visión de arquitectura](architecture/overview.md)
- [Límites modulares](architecture/module-boundaries.md)
- [Modelo conceptual](database/conceptual-model.md)
- [Persistencia inicial V3](database/persistence-v3.md)
- [Esquema Customer V4](database/customers-v4.md)
- [Esquema Catalog V5](database/catalog-v5.md)
- [Esquema Supplier V6](database/suppliers-v6.md)
- [Convenciones de API](api/conventions.md)
- [Backend base V2](api/backend-base.md)
- [API Customer V4](api/customers-v4.md)
- [API Catalog V5](api/catalog-v5.md)
- [API Supplier V6](api/suppliers-v6.md)
- [Matriz de roles](security/role-matrix.md)
- [Estrategia de pruebas](testing/strategy.md)
- [Flujo Git](devops/git-workflow.md)
- [Ciclo de fases](operations/phase-workflow.md)
- [Revisión de V1](operations/v1-review.md)
- [Revisión de V2](operations/v2-review.md)
- [Revisión de V3](operations/v3-review.md)
- [Revisión de V4](operations/v4-review.md)
- [Revisión de V5](operations/v5-review.md)
- [Revisión de V6](operations/v6-review.md)

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
