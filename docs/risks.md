# Registro inicial de riesgos

| Riesgo | Prob. | Impacto | Mitigación | Señal de revisión |
|---|---|---|---|---|
| Ampliar el alcance antes de dominar la fase | Alta | Alta | aprobación y cierre por fase | tareas de fases futuras en el PR actual |
| Monolito sin límites reales | Media | Alta | módulos por dominio y revisión de dependencias | repositorios usados desde módulos ajenos |
| Inconsistencia de stock concurrente | Media | Alta | transacciones, locking y pruebas V7–V9 | stock negativo o diferencias de reconciliación |
| Secretos versionados | Media | Alta | `.gitignore`, ejemplos ficticios y escaneo en CI | API key o contraseña en diff |
| Dependencia excesiva de proveedor externo | Media | Media | puerto/adaptador, timeout, caché y fallback | dashboard falla con el proveedor |
| Cobertura alta pero poco útil | Media | Media | escenarios de negocio y mutation/review selectivo | pruebas solo de getters o mocks excesivos |
| Consultas lentas y N+1 | Media | Media | proyecciones, métricas y `EXPLAIN` | crecimiento de queries o latencia |
| Roles demasiado amplios | Media | Alta | matriz explícita y deny-by-default | rol operativo accede a administración |
| Confundir login OIDC con permisos | Media | Alta | vinculación explícita a usuario interno | autorización basada en email/claim externo |
| Costos o límites de servicios cloud | Media | Media | revisar planes al iniciar V17/V18 | cuota, suspensión o función de pago |
| Documentación desactualizada | Alta | Media | incluirla en Definition of Done | contexto contradice código o migraciones |
| Restore nunca probado | Media | Alta | restore drill obligatorio en V18 | existen backups sin evidencia de restauración |
| Docker no disponible para pruebas de persistencia | Media | Media | preflight, imagen PostgreSQL fijada y error explícito | Testcontainers no puede acceder al daemon |

El registro se revisará al planificar y cerrar cada fase. Los riesgos resueltos se conservan con su resultado para mantener trazabilidad.
