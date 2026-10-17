# Persistencia Identity — V10

La migración `V7__identity.sql` añade seis tablas PostgreSQL sin modificar saldos, movimientos ni documentos comerciales. Flyway la aplica después de V6; Hibernate continúa en `validate` para las entidades JPA existentes. Identity usa un adaptador JDBC propio y no expone su persistencia por API.

| Tabla | Contenido y restricciones |
|---|---|
| `app_users` | ID BIGINT generado, nombre único normalizado, hash BCrypt, estado activo, versión no negativa e instantes UTC |
| `roles` | código de rol como PK; cinco grupos iniciales |
| `permissions` | código de permiso como PK; trece acciones de Identity y módulos comerciales existentes |
| `role_permissions` | PK compuesta rol/permiso y FK a ambos catálogos |
| `user_roles` | PK compuesta usuario/rol y FK; sin concesiones implícitas por identidad externa |
| `refresh_sessions` | UUID, FK a usuario, hash SHA-256 único, creación, expiración y revocación opcional |

El CHECK de nombre exige letra inicial y caracteres ASCII permitidos. La vigencia de una sesión debe ser posterior a su creación. El índice parcial de sesiones activas por usuario apoya su revocación. Las asociaciones no admiten duplicados y las FK impiden referencias desconocidas.

La migración carga únicamente roles y permisos técnicos, sin usuarios, contraseñas ni tokens. El administrador inicial se crea desde variables externas de bootstrap; su hash se calcula en la aplicación.

Cada emisión, rotación o revocación de sesión es transaccional. La fila del usuario se bloquea antes de emitir o renovar tokens y antes de cambiar contraseña, estado o roles. La renovación obtiene después un bloqueo de la fila de sesión. Los cambios de administración usan además un bloqueo advisory de transacción para conservar al menos un ADMIN activo. Ninguna operación HTTP elimina físicamente usuarios o sesiones.

La suite de persistencia verifica las siete migraciones desde una base vacía y conserva el caso de actualización desde V5 con stock existente hasta el esquema actual. Las pruebas de Identity comprueban hashes de refresh, rotación, revocación y las carreras de renovación y desactivación.
