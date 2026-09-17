# Backend base — V2

## Propósito

V2 entrega un backend Spring Boot ejecutable sin persistencia ni funciones comerciales. Sus endpoints son técnicos y establecen el contrato base que los módulos de negocio reutilizarán.

## Perfiles

| Perfil | Uso | Swagger/OpenAPI |
|---|---|---|
| `local` | desarrollo local, perfil por defecto | habilitado |
| `test` | pruebas automatizadas | habilitado |
| `prod` | configuración de producción futura | deshabilitado |

No existe datasource en esta fase. PostgreSQL, JPA y Flyway pertenecen a V3/V4.

## Endpoints

### `GET /api/v1/system/info`

Devuelve el nombre, versión y timestamp UTC de la aplicación.

```json
{
  "name": "Gestión Comercial API",
  "version": "0.1.0-SNAPSHOT",
  "timestamp": "2026-09-17T07:00:00Z"
}
```

### `POST /api/v1/system/echo`

Endpoint técnico no persistente para demostrar DTO y validación. Acepta un `message` no vacío de hasta 120 caracteres y responde el mismo valor.

```json
{
  "message": "backend base"
}
```

Una petición inválida devuelve HTTP 400 y `application/problem+json`:

```json
{
  "type": "https://gestion-comercial.dev/problems/validation-error",
  "title": "Request validation failed",
  "status": 400,
  "detail": "One or more request fields are invalid.",
  "instance": "/api/v1/system/echo",
  "code": "VALIDATION_ERROR",
  "timestamp": "2026-09-17T07:00:00Z",
  "path": "/api/v1/system/echo",
  "correlationId": "generated-or-supplied-value",
  "errors": [
    { "field": "message", "message": "message must not be blank" }
  ]
}
```

## Correlation ID

Cada respuesta incluye `X-Correlation-Id`. Si el cliente envía un valor de 1 a 64 caracteres alfanuméricos, punto, guion o guion bajo, se conserva; en otro caso se genera un UUID. El identificador entra en el MDC para enlazar logs de una petición. Los logs estructurados y la auditoría completa siguen previstos para V15.

## Health y OpenAPI

- `GET /actuator/health` expone salud básica sin detalles internos.
- `GET /v3/api-docs` publica el contrato JSON en perfiles local/test.
- `GET /swagger-ui/index.html` permite explorar el contrato en local/test.

