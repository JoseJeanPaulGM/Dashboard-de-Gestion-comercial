# Backend — Gestión Comercial API

Backend de Gestión Comercial con los módulos Customer y Catalog, API REST paginada y esquema PostgreSQL administrado por Flyway. La seguridad permanece reservada para V10.

## Tecnologías activas

- Java 17.
- Spring Boot 3.5.16.
- Maven Wrapper 3.8.5.
- Spring Web, Validation, Actuator y springdoc-openapi 2.9.1.
- Spring Data JPA, Hibernate y driver PostgreSQL.
- Flyway para migraciones y Hibernate en modo `validate`.
- PostgreSQL 17 y Testcontainers para pruebas de persistencia.
- JaCoCo 0.8.13 como medición informativa de cobertura.

## Requisitos

- Java 17.
- Docker Desktop operativo para ejecutar las pruebas automatizadas.
- PostgreSQL 17 accesible para ejecutar la aplicación con los perfiles `local` o `prod`.

Las pruebas levantan una base descartable con `postgres:17-alpine`. No utilizan H2 ni la base configurada para desarrollo local.

## Ejecutar localmente

Desde este directorio:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

El perfil `local` se activa por defecto. Antes de ejecutar la aplicación, definir el datasource mediante variables de entorno:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/gestion_comercial"
$env:DB_USERNAME = "gestion_comercial"
$env:DB_PASSWORD = "local-only-password"
.\mvnw.cmd spring-boot:run
```

Los nombres y valores ficticios están disponibles en `.env.example`; Spring Boot no carga ese archivo automáticamente. `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` son obligatorios en producción. Flyway aplica las migraciones y Hibernate valida el esquema en todos los perfiles.

## Endpoints disponibles

| URL | Propósito |
|---|---|
| `GET /api/v1/system/info` | información técnica de la aplicación |
| `POST /api/v1/system/echo` | validación técnica de una petición no persistente |
| `GET /actuator/health` | health check básico |
| `GET /v3/api-docs` | documento OpenAPI JSON |
| `GET /swagger-ui/index.html` | interfaz Swagger local |
| `POST /api/v1/customers` | crear cliente |
| `GET /api/v1/customers/{id}` | consultar cliente |
| `GET /api/v1/customers` | listar, filtrar y buscar clientes con paginación |
| `PUT /api/v1/customers/{id}` | actualizar cliente |
| `PATCH /api/v1/customers/{id}/status` | activar o desactivar cliente |
| `POST /api/v1/categories` | crear categoría |
| `GET /api/v1/categories/{id}` | consultar categoría |
| `GET /api/v1/categories` | listar, filtrar y buscar categorías con paginación |
| `PUT /api/v1/categories/{id}` | actualizar categoría |
| `PATCH /api/v1/categories/{id}/status` | activar o desactivar categoría |
| `POST /api/v1/products` | crear producto con precio en PEN |
| `GET /api/v1/products/{id}` | consultar producto y su categoría |
| `GET /api/v1/products` | listar, filtrar y buscar productos con paginación |
| `PUT /api/v1/products/{id}` | actualizar producto |
| `PATCH /api/v1/products/{id}/status` | activar o desactivar producto |

La especificación base está en [backend-base.md](../docs/api/backend-base.md).

La persistencia de V3 está documentada en [persistence-v3.md](../docs/database/persistence-v3.md).

El contrato Customer y el esquema V4 están documentados en [customers-v4.md](../docs/api/customers-v4.md) y [customers-v4.md](../docs/database/customers-v4.md).

El contrato Catalog y el esquema V5 están documentados en [catalog-v5.md](../docs/api/catalog-v5.md) y [catalog-v5.md](../docs/database/catalog-v5.md).
