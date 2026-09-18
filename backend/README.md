# Backend — Gestión Comercial API

Backend de Gestión Comercial con el módulo Customer de V4, API REST paginada y esquema PostgreSQL administrado por Flyway. La seguridad permanece reservada para V10.

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

La especificación de contrato está en `../docs/api/backend-base.md`.

La persistencia de V3 está documentada en `../docs/database/persistence-v3.md`.

El contrato Customer y el esquema V4 están documentados en `../docs/api/customers-v4.md` y `../docs/database/customers-v4.md`.
