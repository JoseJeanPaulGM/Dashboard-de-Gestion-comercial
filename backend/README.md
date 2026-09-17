# Backend — Gestión Comercial API

Backend base de Gestión Comercial, construido en V2. No incluye todavía persistencia, módulos comerciales ni seguridad.

## Tecnologías activas

- Java 17.
- Spring Boot 3.5.16.
- Maven Wrapper 3.8.5.
- Spring Web, Validation, Actuator y springdoc-openapi 2.9.1.

## Ejecutar localmente

Desde este directorio:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

El perfil `local` se activa por defecto. Para usar otro perfil:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=test"
```

## Endpoints V2

| URL | Propósito |
|---|---|
| `GET /api/v1/system/info` | información técnica de la aplicación |
| `POST /api/v1/system/echo` | validación técnica de una petición no persistente |
| `GET /actuator/health` | health check básico |
| `GET /v3/api-docs` | documento OpenAPI JSON |
| `GET /swagger-ui/index.html` | interfaz Swagger local |

La especificación de contrato está en `../docs/api/backend-base.md`.

