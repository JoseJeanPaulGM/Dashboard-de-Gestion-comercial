# ADR 0008 — Identity local, RBAC y sesiones en V10

- Estado: aceptada en V10, fase cerrada el 2026-10-17.
- Fecha: 2026-10-17.

## Contexto

Los endpoints comerciales de V2–V9 todavía no tenían autenticación. V10 necesita identificar usuarios internos, aplicar acciones permitidas por rol y revocar sesiones cuando cambian sus privilegios, manteniendo el monolito y sin anticipar OIDC.

## Decisión

Identity conserva usuarios y sus contraseñas BCrypt, cinco roles predefinidos, permisos concretos y sesiones de renovación. Un adaptador JDBC es dueño exclusivo de estas tablas; las API comerciales conservan sus servicios y modelos. Spring Security comprueba las autoridades en la frontera HTTP y deniega rutas no contempladas.

El acceso usa JWT HS256 de 15 minutos con clave externa, ID interno y versión. Los permisos se consultan en PostgreSQL por solicitud. La renovación usa tokens opacos aleatorios, guardados únicamente como SHA-256, con duración de siete días y rotación de un solo uso. Los cambios de contraseña, roles o estado incrementan la versión y revocan las renovaciones.

Un bloqueo de fila por usuario serializa emisión, renovación y cambios de cuenta. La renovación bloquea después su sesión. Un bloqueo advisory transaccional serializa los cambios de administración para proteger al último ADMIN activo. La base se inicializa sin credenciales; un bootstrap explícito crea el primer administrador desde variables de entorno.

## Consecuencias

La autorización se hace efectiva en las solicitudes posteriores al cambio de privilegios, sin esperar a que expire el JWT. Cada solicitud protegida depende de PostgreSQL y realiza lecturas de Identity. Logout revoca la renovación presentada, mientras el access token mantiene su límite de 15 minutos. Una rotación de la clave de firma invalida los access tokens anteriores; no se incorpora un conjunto de claves con transición en V10.

Los roles personalizados, la UI de sesión y la autenticación externa requieren fases posteriores y nuevas decisiones. El modelo interno seguirá siendo la autoridad de permisos cuando se añada OIDC, conforme a ADR 0003.
