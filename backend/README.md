# Backend — Gestión Comercial API

Backend de Gestión Comercial con base HTTP de V2 e infraestructura de persistencia incorporada en V3. No incluye todavía módulos comerciales, migraciones Flyway ni seguridad.

## Tecnologías activas

- Java 17.
- Spring Boot 3.5.16.
- Maven Wrapper 3.8.5.
- Spring Web, Validation, Actuator y springdoc-openapi 2.9.1.
- Spring Data JPA, Hibernate y driver PostgreSQL.
- PostgreSQL 17 y Testcontainers para pruebas de persistencia.

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

Los nombres y valores ficticios están disponibles en `.env.example`; Spring Boot no carga ese archivo automáticamente. `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` son obligatorios en producción. Hibernate no crea ni modifica el esquema en `local` o `prod`.

## Endpoints V2

| URL | Propósito |
|---|---|
| `GET /api/v1/system/info` | información técnica de la aplicación |
| `POST /api/v1/system/echo` | validación técnica de una petición no persistente |
| `GET /actuator/health` | health check básico |
| `GET /v3/api-docs` | documento OpenAPI JSON |
| `GET /swagger-ui/index.html` | interfaz Swagger local |

La especificación de contrato está en `../docs/api/backend-base.md`.

La persistencia de V3 está documentada en `../docs/database/persistence-v3.md`.
