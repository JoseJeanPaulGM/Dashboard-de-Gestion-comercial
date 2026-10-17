# Seguridad local y sesiones — V10

## Configuración

Identity es dueño de usuarios, roles, permisos y sesiones. Spring Security aplica la autorización HTTP en una aplicación sin sesión de servidor ni autenticación por cookies. CSRF se deshabilita para este contrato Bearer; no se habilita una política CORS para el frontend de fases futuras.

`JWT_SECRET` es obligatorio en local/prod: contiene una clave aleatoria de al menos 32 bytes codificada en Base64. La aplicación rechaza una clave vacía, inválida o demasiado corta. Los valores ficticios y los nombres de variables están en `backend/.env.example`; ese archivo no se carga automáticamente. El secreto real se proporciona desde el entorno y se conserva fuera de Git. Las pruebas generan una clave efímera en memoria.

Para el primer arranque sobre una base sin usuarios, definir `BOOTSTRAP_ADMIN_USERNAME` y `BOOTSTRAP_ADMIN_PASSWORD` junto con el datasource y la clave JWT. El arranque crea una cuenta ADMIN con contraseña BCrypt. Si faltan las credenciales iniciales, el arranque falla explícitamente. Cuando ya hay usuarios, no vuelve a crear ni sobrescribe cuentas; retirar las variables de bootstrap después del primer arranque. En test el bootstrap está deshabilitado y las pruebas crean sus datos.

## Autenticación y revocación

El access token HS256 dura 15 minutos y solo transporta identidad interna, emisor, fechas y versión. Cada solicitud consulta el usuario y sus permisos actuales en PostgreSQL. La firma no concede acceso a una cuenta inexistente o desactivada. Un JWT incompleto, expirado, de otro emisor o con versión de tipo incorrecto se rechaza con 401.

El refresh token contiene 32 bytes aleatorios codificados en Base64 URL. Solo su hash SHA-256 se persiste. Su duración es de siete días por emisión y se rota en cada renovación. El anterior queda revocado; los reintentos no emiten sesiones adicionales. Logout revoca el refresh presentado y no incrementa la versión global del usuario; el access token conserva el límite de 15 minutos. Cambiar contraseña, roles o estado revoca todas las renovaciones y cambia la versión, invalidando también los access tokens anteriores.

Login, renovación y cambios de usuario bloquean la fila del usuario dentro de su transacción. La renovación bloquea después la sesión y revalida su estado. Este orden impide emitir una renovación vigente después de una desactivación o revocación concurrente. Los cambios de rol/estado serializan además la comprobación del último administrador mediante `pg_advisory_xact_lock(10100001)`.

## Permisos y límites

Los cinco roles son grupos internos predefinidos. Los permisos concretos y las reglas por ruta se documentan en [role-matrix.md](role-matrix.md). Una cuenta puede tener varios roles y recibe la unión de sus permisos. La API no permite editar directamente las tablas ni conceder permisos mediante el payload de login.

El health público no expone detalles internos. Los endpoints técnicos de System requieren autenticación. OpenAPI y Swagger están deshabilitados en prod. No se registran credenciales ni tokens desde Identity; los errores de autenticación son genéricos. Las pruebas con tokens reales deshabilitan la impresión automática de peticiones y respuestas de MockMvc.

V10 no incorpora registro público, recuperación de contraseña por correo, MFA, límites de intentos, roles personalizados, UI de sesión ni proveedores OIDC. El cambio de contraseña disponible es administrativo. Antes de exponer una demostración pública deberán revisarse los controles operativos y la configuración del despliegue en su fase correspondiente. La identidad externa continúa aplazada a V16/V17 y nunca sustituirá la autorización interna.

La decisión está registrada en [ADR 0008](../architecture/decisions/0008-local-identity-and-sessions-v10.md). El [contrato API](../api/identity-v10.md) y el [esquema](../database/identity-v10.md) describen las entradas y restricciones.
