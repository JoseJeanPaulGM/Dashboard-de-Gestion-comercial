# Revisión de V10 — Identity, RBAC y JWT

## Estado

V10 fue autorizada para implementación el 2026-10-14 y presentada para revisión el 2026-10-17 en `feature/identity-v10`. **El usuario aprobó su cierre el 2026-10-17**, después de revisar el resultado, las pruebas y los documentos. El cierre autoriza guardar los cambios, integrarlos en `main`, crear la etiqueta anotada `v10` y publicar `main` y `v10` en GitHub. V11 permanece pendiente y no se inicia.

## Resultado

| Criterio | Evidencia |
|---|---|
| Usuarios internos | alta, consulta paginada, asignación de roles, cambio de estado y contraseña administrativa; DTO sin hash |
| RBAC | cinco roles y trece permisos; matriz probada con JWT reales; denegación por defecto |
| Login | BCrypt, JWT HS256 de 15 minutos, clave externa y errores genéricos |
| Sesión | refresh opaco de siete días, hash SHA-256, rotación, revocación y logout idempotente |
| Cambio de privilegios | versión de usuario y revocación de renovaciones; rechazo de JWT anteriores |
| Concurrencia | una sola renovación por token, coordinación con desactivación y preservación del último ADMIN |
| Persistencia | migración V7 aditiva con seis tablas de Identity y roles/permisos técnicos; sin credenciales de seed |
| Contrato | Problem Details para seguridad, correlation ID y esquema Bearer en OpenAPI |
| Regresión | 96 casos comerciales/técnicos anteriores conservados y 12 pruebas nuevas de Identity |

## Comandos y pruebas

Desde `backend/`, a lo largo de la implementación:

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd -DskipTests test-compile -q
.\mvnw.cmd '-Dtest=SystemApiIntegrationTest,PersistenceIntegrationTest' test -q
.\mvnw.cmd '-Dtest=IdentityApiIntegrationTest' test -q
.\mvnw.cmd '-Dtest=IdentityApiIntegrationTest#enforcesAuthenticationAndRolePermissionsUsingRealTokens' test -q
.\mvnw.cmd '-Dtest=IdentityApiIntegrationTest#enforcesAuthenticationAndRolePermissionsUsingRealTokens' '-Dlogging.level.org.springframework.security=TRACE' test -q
.\mvnw.cmd '-Dtest=IdentityApiIntegrationTest,IdentityInfrastructureTest' test -q
.\mvnw.cmd verify -q
.\mvnw.cmd verify
```

Desde la raíz:

```powershell
git status --short --branch
git diff --check
```

La compilación inicial detectó un tipo HTTP incorrecto; la corrección recibió aprobación. Las pruebas seleccionadas dentro del sandbox fallaron por acceso denegado al named pipe de Docker; se repitieron con autorización fuera del sandbox y pasaron 13 casos. Las primeras pruebas con JWT real detectaron 401 tras login. Se aprobó desactivar el registro servlet adicional del filtro; esa medida no resolvió el fallo. La prueba diagnóstica confirmó que `getIssuer()` intentaba convertir el emisor textual en URL; la comparación de `iss` como texto fue aprobada y resolvió el problema.

La revisión y las pruebas posteriores incorporaron, siempre con aprobación explícita, la separación de errores del filtro y del endpoint, rechazo de JWT incompletos y tipos incorrectos, límite UTF-8 de BCrypt, bloqueos de sesión/administración, validación de roles nulos, offset de paginación de 64 bits y supresión de la impresión automática de peticiones sensibles en pruebas. Una ejecución completa detectó 20 fallos de fixtures por permisos insuficientes en Supplier/Purchase/Sale; se aprobaron sus permisos concretos de preparación sin modificar la matriz de autorización del producto.

La verificación final del **2026-10-17** terminó en **BUILD SUCCESS** con **108 pruebas**, **0 fallos**, **0 errores** y **0 omitidas**, contra **PostgreSQL 17.11**. Incluye las siete migraciones desde una base vacía y la actualización desde V5 con saldo existente. Maven generó el JAR ejecutable y el informe JaCoCo: **97.13% de líneas**, **77.29% de ramas**; sin gate hasta V17. `git diff --check` no detectó errores de espacios; Git puede emitir avisos de normalización LF/CRLF según la configuración local.

La comprobación de enlaces relativos en los 16 documentos Markdown creados o modificados no encontró enlaces rotos.

## Documentación

- [API de Identity](../api/identity-v10.md): endpoints, datos, sesiones y errores.
- [Seguridad V10](../security/identity-v10.md) y [matriz](../security/role-matrix.md): configuración, bootstrap, permisos y límites.
- [Esquema](../database/identity-v10.md), [modelo conceptual](../database/conceptual-model.md), [límites modulares](../architecture/module-boundaries.md) y [ADR 0008](../architecture/decisions/0008-local-identity-and-sessions-v10.md).
- [Pruebas](../testing/strategy.md), [roadmap](../roadmap.md), índice, README raíz/backend y `PROJECT_CONTEXT.md` actualizados con el cierre aprobado de V10.
- La nota desactualizada que mantenía V9 pendiente de cierre en la estrategia de pruebas se corrigió con la aprobación del resumen inicial.

## Límites y pendientes reales

La validación de sesión/API se realizó mediante MockMvc con Spring y PostgreSQL real de Testcontainers. El bootstrap y la validación de claves tienen pruebas automatizadas; no se efectuó un arranque manual local/prod con credenciales externas. El logout revoca la renovación y permite que el access token existente llegue a su expiración, salvo invalidación global por cambios de cuenta.

No se implementaron Angular, OIDC, auditoría completa ni infraestructura de fases posteriores. V10 utiliza roles predefinidos y no incluye registro público, recuperación por correo, MFA o limitación de intentos. Estos límites se detallan en el documento de seguridad y no se presentan como capacidades existentes.

V10 queda cerrada con aprobación explícita. No se inicia V11; el siguiente trabajo requiere una nueva solicitud, resumen de alcance y aprobación.

## Integración y publicación autorizadas

Los cambios se guardan en commits separados de implementación, pruebas y documentación, con Conventional Commits en inglés. La integración en `main` usa fast-forward y la etiqueta de cierre es anotada. La publicación envía únicamente `main` y `v10`, sin forzar referencias ni publicar otras ramas o etiquetas.

```powershell
git fetch origin --tags
git switch main
git merge --ff-only feature/identity-v10
git tag -a v10 -m "Release V10: identity, RBAC and JWT sessions"
git push --atomic origin main refs/tags/v10
git ls-remote origin refs/heads/main refs/tags/v10 'refs/tags/v10^{}'
```

La verificación de publicación compara `main` y el commit al que apunta `v10` con las referencias remotas. Los resultados de las pruebas descritos arriba siguen correspondiendo al código aprobado; el cierre solo actualiza el estado documental.
