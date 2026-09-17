# Persistencia inicial — V3

## Propósito

V3 incorpora PostgreSQL, Spring Data JPA, Hibernate y transacciones sin anticipar entidades comerciales. La primera entidad de negocio y el baseline de Flyway permanecen en V4.

## Dependencias y límites

- PostgreSQL es el único motor admitido en desarrollo, pruebas y producción.
- El driver PostgreSQL se carga en runtime.
- `open-in-view` está deshabilitado.
- Hibernate usa UTC para JDBC.
- H2 no forma parte del proyecto.
- No existen todavía migraciones ni tablas comerciales.

La entidad, el repositorio y el servicio transaccional utilizados para verificar el mapeo viven exclusivamente en las fuentes de prueba. Por tanto, V3 no introduce tablas técnicas permanentes ni API nuevas.

## Configuración por ambiente

| Perfil | Datasource | Gestión de esquema |
|---|---|---|
| `local` | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `none` |
| `test` | conexión dinámica de Testcontainers | `create-drop` |
| `prod` | variables obligatorias externas | `none` |

El perfil `test` inicia `postgres:17-alpine` mediante una `ServiceConnection`. La base `gestion_comercial_test`, el usuario y la contraseña de prueba solo existen dentro del contenedor descartable.

## Pruebas de persistencia

La suite verifica contra PostgreSQL 17:

- conexión real y versión mayor del motor;
- persistencia y lectura JPA;
- identificador `Long` generado;
- instante UTC con precisión compatible con PostgreSQL;
- restricción `NOT NULL` aplicada por la base;
- commit atómico de dos escrituras;
- rollback total después de una excepción;
- health check y regresión completa de los contratos de V2.

Testcontainers requiere Docker Desktop operativo. La ausencia del daemon produce un fallo explícito al iniciar el contexto; no existe fallback a H2 ni a una base compartida.

## Evolución en V4

V4 incorporará la primera entidad comercial, establecerá el baseline de Flyway y cambiará Hibernate a validación del esquema gestionado por migraciones. Este documento no autoriza iniciar esas tareas antes del cierre aprobado de V3.
