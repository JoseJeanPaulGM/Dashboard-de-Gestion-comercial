# Backend — Gestión Comercial API

Backend de Gestión Comercial con Customer, Catalog, Supplier, Purchase, Sale, Inventory e Identity. La API REST usa PostgreSQL y Flyway. V10 añade autenticación local, RBAC y JWT; fue aprobada para cierre el 2026-10-17 con la versión `v10`.

## Tecnologías activas

- Java 17.
- Spring Boot 3.5.16.
- Maven Wrapper 3.8.5.
- Spring Web, Validation, Actuator y springdoc-openapi 2.9.1.
- Spring Data JPA, Hibernate y driver PostgreSQL.
- Spring Security, JWT HS256 con Nimbus y BCrypt; persistencia de Identity mediante JDBC.
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

El perfil `local` se activa por defecto. Antes de ejecutar la aplicación, definir `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` mediante variables de entorno. La clave JWT contiene al menos 32 bytes aleatorios codificados en Base64. Para una base sin usuarios, proporcionar también `BOOTSTRAP_ADMIN_USERNAME` y `BOOTSTRAP_ADMIN_PASSWORD`; el arranque crea el primer administrador y exige una contraseña de al menos 12 caracteres y hasta 72 bytes UTF-8.

```powershell
.\mvnw.cmd spring-boot:run
```

Los nombres y valores ficticios están disponibles en `.env.example`; Spring Boot no carga ese archivo automáticamente. Los secretos reales no se guardan en Git. Retirar las variables de bootstrap después del primer arranque: con usuarios existentes no se crea ni modifica ninguna cuenta. Flyway aplica las migraciones y Hibernate valida el esquema JPA en todos los perfiles. El perfil test genera una clave efímera, deshabilita el bootstrap y usa Testcontainers.

La verificación del 2026-10-17 ejecutó 108 pruebas con 0 fallos, 0 errores y 0 omitidas contra PostgreSQL 17.11. `verify` generó el JAR y el informe informativo JaCoCo (97.13% de líneas, 77.29% de ramas). La evidencia se conserva en [revisión V10](../docs/operations/v10-review.md).

## Endpoints disponibles

| URL | Propósito |
|---|---|
| `GET /api/v1/system/info` | información técnica de la aplicación |
| `POST /api/v1/system/echo` | validación técnica de una petición no persistente |
| `GET /actuator/health` | health check básico |
| `GET /v3/api-docs` | documento OpenAPI JSON |
| `GET /swagger-ui/index.html` | interfaz Swagger local |
| `POST /api/v1/auth/login`, `/refresh`, `/logout` | login, rotación y revocación de renovación |
| `GET /api/v1/auth/me` | usuario autenticado |
| `/api/v1/users` y acciones por ID | administración de usuarios, roles, estado y contraseña |
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
| `/api/v1/suppliers` y `/api/v1/suppliers/{id}` | crear, consultar y listar proveedores; actualizar estado y vínculos con productos |
| `POST /api/v1/purchases` | crear compra borrador |
| `GET /api/v1/purchases` y `GET /api/v1/purchases/{id}` | listar y consultar compras |
| `POST /api/v1/purchases/{id}/confirm` | confirmar compra e incrementar stock |
| `POST /api/v1/sales` | crear venta borrador con precios de Catalog |
| `GET /api/v1/sales` y `GET /api/v1/sales/{id}` | listar y consultar ventas |
| `POST /api/v1/sales/{id}/confirm` | confirmar venta y descontar stock disponible |
| `GET /api/v1/inventory/balances/{productId}` | consultar saldo y mínimo de un producto |
| `PUT /api/v1/inventory/balances/{productId}/minimum` | configurar mínimo |
| `GET /api/v1/inventory/balances/{productId}/movements` | consultar movimientos paginados |
| `GET /api/v1/inventory/alerts` | consultar alertas de stock bajo |
| `POST /api/v1/inventory/adjustments` | aplicar ajuste justificado e idempotente |

La especificación base está en [backend-base.md](../docs/api/backend-base.md).

Los endpoints comerciales requieren Bearer JWT y los permisos de la [matriz de roles](../docs/security/role-matrix.md). Health y login/refresh/logout son públicos; System requiere autenticación. Swagger/OpenAPI permanecen habilitados solo en local/test. Los contratos y el primer arranque están en [Identity API](../docs/api/identity-v10.md) y [Seguridad V10](../docs/security/identity-v10.md).

La persistencia de V3 está documentada en [persistence-v3.md](../docs/database/persistence-v3.md).

El contrato Customer y el esquema V4 están documentados en [customers-v4.md](../docs/api/customers-v4.md) y [customers-v4.md](../docs/database/customers-v4.md).

El contrato Catalog y el esquema V5 están documentados en [catalog-v5.md](../docs/api/catalog-v5.md) y [catalog-v5.md](../docs/database/catalog-v5.md).

Los contratos de [Supplier V6](../docs/api/suppliers-v6.md), [Purchase V7](../docs/api/purchases-v7.md), [Sale V8](../docs/api/sales-v8.md) e [Inventory V9](../docs/api/inventory-v9.md) detallan las reglas comerciales y sus esquemas enlazados desde el [índice de documentación](../docs/README.md).
