# API Identity — V10

V10 incorpora autenticación local y autorización interna. Su cierre fue aprobado el 2026-10-17, con la versión `v10`. Las respuestas son DTO y los errores mantienen Problem Details y correlation ID.

## Sesión

| Método y ruta | Acceso | Resultado |
|---|---|---|
| `POST /api/v1/auth/login` | público | 200 con tokens al validar `username` y `password` |
| `POST /api/v1/auth/refresh` | público | 200 con tokens nuevos al validar `refreshToken` |
| `POST /api/v1/auth/logout` | público | 204; revoca el refresh token recibido; repetirlo es idempotente |
| `GET /api/v1/auth/me` | JWT válido | 200 con `id`, `username`, `active` y `roles` del usuario actual |

Login normaliza el nombre con trim y minúsculas. Credenciales incorrectas, usuario inexistente o cuenta inactiva producen el mismo 401 `INVALID_CREDENTIALS`. Los campos obligatorios vacíos producen 400 de validación.

Login y refresh devuelven `accessToken`, `refreshToken`, `tokenType` (`Bearer`), `accessExpiresAt` y `refreshExpiresAt`, con fechas UTC. El access token dura 15 minutos; el refresh token dura siete días desde su emisión. Cada renovación revoca el refresh anterior y emite uno nuevo: dos renovaciones concurrentes del mismo token producen un único éxito. Un refresh expirado, revocado o reutilizado devuelve 401. Logout revoca la renovación; el access token ya emitido conserva su vigencia hasta expirar o hasta invalidarse la cuenta o su versión de sesión.

El JWT usa HS256, `iss=gestion-comercial`, `sub` con ID interno, `iat`, `exp` y `version` entera. El cliente envía `Authorization: Bearer <accessToken>`. El backend comprueba firma, emisor, fechas, formato, estado activo y versión del usuario. Los permisos se consultan en PostgreSQL; no se confía en roles enviados por el cliente.

## Administración de usuarios

Todos estos endpoints requieren `IDENTITY_MANAGE`, concedido únicamente a ADMIN en V10.

| Método y ruta | Entrada | Resultado |
|---|---|---|
| `POST /api/v1/users` | `username`, `password`, `roles` | 201 y `Location` |
| `GET /api/v1/users/{id}` | ID | 200 con usuario, o 404 |
| `GET /api/v1/users` | `page`, `size`, `sort` | 200 con `PageResponse<UserResponse>` |
| `PUT /api/v1/users/{id}/roles` | `roles` | 200 con usuario actualizado |
| `PATCH /api/v1/users/{id}/status` | `active` booleano | 200 con usuario actualizado |
| `PUT /api/v1/users/{id}/password` | `password` | 204 |

El nombre normalizado tiene de 3 a 64 caracteres, comienza con letra y admite letras ASCII minúsculas, dígitos, punto, guion y guion bajo. Es único. La contraseña requiere al menos 12 caracteres y como máximo 72 bytes UTF-8, por el límite de BCrypt. Nunca se devuelve la contraseña ni su hash.

`roles` es un conjunto no vacío sin valores nulos. Los roles disponibles son ADMIN, VENDEDOR, ALMACEN, SUPERVISOR y AUDITOR. V10 permite asignar estos grupos predefinidos; sus definiciones y permisos se mantienen mediante migraciones, sin CRUD de roles personalizados.

La lista usa página cero y tamaño 20 por defecto; admite tamaños de 1 a 100 y `sort=id,asc|desc` o `sort=username,asc|desc`, con `id,asc` por defecto. No incorpora filtros adicionales.

Cambiar roles, estado o contraseña incrementa la versión del usuario y revoca sus refresh tokens activos. Los access tokens anteriores dejan de ser válidos en las siguientes solicitudes. La última cuenta ADMIN activa no puede desactivarse ni perder ese rol. Las operaciones concurrentes preservan esta regla mediante un bloqueo transaccional de administración.

## Autorización y errores

La [matriz de roles](../security/role-matrix.md) asigna los permisos a los endpoints comerciales existentes. System requiere autenticación. El health básico y los endpoints de sesión indicados son públicos; Swagger y OpenAPI están disponibles en local/test y deshabilitados en prod. Las rutas sin regla explícita se deniegan por defecto.

| Estado/código | Caso |
|---|---|
| 400 `INVALID_USERNAME`, `INVALID_PASSWORD`, `INVALID_ROLES` | datos de usuario fuera del contrato |
| 400 `VALIDATION_ERROR` | campos obligatorios ausentes o vacíos |
| 400 `INVALID_PAGINATION`, `INVALID_SORT` | listado inválido |
| 401 `INVALID_CREDENTIALS` | login o renovación rechazados |
| 401 `UNAUTHENTICATED` | falta JWT válido para una ruta protegida |
| 403 `FORBIDDEN` | usuario autenticado sin permiso |
| 404 `USER_NOT_FOUND` | usuario inexistente |
| 409 `USERNAME_CONFLICT` | nombre ya utilizado |
| 409 `LAST_ADMIN` | operación que dejaría sin ADMIN activo |

OpenAPI declara el esquema `bearerAuth`; login, refresh y logout se documentan sin requisito Bearer. La configuración y el primer arranque se detallan en [Seguridad V10](../security/identity-v10.md).
