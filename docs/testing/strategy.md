# Estrategia de pruebas

## Principios

- Las pruebas acompañan a la funcionalidad; no se aplazan hasta V18.
- Se prueba comportamiento observable y reglas, no detalles accidentales.
- PostgreSQL es el motor de integración; H2 no forma parte del proyecto.
- Los fallos externos se simulan de forma determinista.
- Cobertura informa y, desde V17, aplica un umbral; nunca reemplaza la revisión de escenarios.

## Pirámide prevista

| Nivel | Responsabilidad | Inicio |
|---|---|---:|
| unitaria | reglas, cálculos, validadores y mappers | V2/V4 |
| slice | MVC, repositorios y seguridad aislada | V2/V3 |
| integración | Spring + PostgreSQL + transacciones | V3 |
| contrato/API | request, status, headers y JSON | V2 |
| frontend | componentes, servicios, formularios e interceptores | V11 |
| E2E | flujos críticos desde navegador | V12 |
| smoke | salud del artefacto o despliegue | V17/V18 |
| operación | rollback, backup/restore e incidentes | V18 |

## Evidencia de V2

La suite MVC de V2 cubre carga de contexto, `GET /api/v1/system/info`, preservación/generación de correlation ID, validación de `POST /api/v1/system/echo`, Problem Details, health de Actuator, OpenAPI JSON y Swagger UI. Se ejecuta con `.\\mvnw.cmd test` desde `backend/`.

## Evidencia de V3

V3 ejecuta la suite contra `postgres:17-alpine` mediante Testcontainers y una conexión dinámica de Spring Boot. Las pruebas cubren conexión PostgreSQL 17, persistencia JPA, ID generado, instante UTC, restricción `NOT NULL`, commit, rollback y toda la regresión MVC de V2. La ejecución verificada contiene 9 pruebas, sin fallos, errores ni pruebas omitidas.

## Evidencia de V4

V4 reemplaza el probe artificial de persistencia por pruebas sobre el esquema Flyway y el módulo Customer real. La suite cubre creación, consulta, actualización, estado lógico, conflictos de documento, paginación, orden permitido, filtro `active`, búsqueda literal por nombre o documento, Problem Details, migración desde base vacía, restricciones, commit, rollback y regresión V2/V3.

La verificación del 2026-09-18 ejecutó 27 pruebas contra PostgreSQL 17: 0 fallos, 0 errores y 0 omitidas. JaCoCo midió 95.04% de líneas y 90% de ramas para el backend completo; estos valores son informativos y no constituyen un gate antes de V17.

## Evidencia de V5

V5 añade pruebas de dominio, API y persistencia para Category y Product. Cubren normalización, precios PEN, relación obligatoria, ciclos de estado, conflictos, filtros, rangos de precio, búsqueda literal, todos los campos de orden publicados, paginación compartida y carga de categorías con `open-in-view: false`.

La verificación final del 2026-09-18 ejecutó 52 pruebas contra PostgreSQL 17: 0 fallos, 0 errores y 0 omitidas. También verificó dos migraciones Flyway desde un esquema vacío, constraints, FK, checks y toda la regresión V2–V4. JaCoCo midió 96.83% de líneas y 82.03% de ramas para el backend completo; continúa siendo informativo y sin gate hasta V17.

## Escenarios críticos acumulativos

- Compra confirmada incrementa stock una sola vez.
- Venta concurrente no produce stock negativo.
- Anulación crea el efecto compensatorio permitido.
- Usuario sin permiso recibe 403 aunque manipule la interfaz.
- Refresh token reutilizado o revocado no abre sesión.
- Fallo del tipo de cambio no impide mostrar información interna.
- Auditoría no expone secretos y conserva actor/correlación.
- Backup restaurado reproduce integridad y conteos acordados.

## Evidencia provisional de V6

V6 añade pruebas de dominio, API y persistencia para Supplier y su vínculo con Product. Cubren normalización, RUC, unicidad, datos opcionales, filtros, búsqueda literal, paginación, campos de orden, FK, asociación única, estados lógicos, reglas de alta/reactivación y bloqueo simétrico de desactivación mientras existan vínculos activos. También verifican que el producto pueda desactivarse después de inactivar sus relaciones.

La verificación del 2026-09-23 ejecutó 75 pruebas contra PostgreSQL 17.11: 0 fallos, 0 errores y 0 omitidas. También aplicó tres migraciones Flyway desde un esquema vacío y conservó la regresión V2–V5. JaCoCo midió 97.36% de líneas y 83.15% de ramas; estos valores son informativos y no constituyen un gate antes de V17.

## Datos y aislamiento

Las pruebas crearán sus propios datos y no dependerán del orden de ejecución. Desde V3, las integraciones PostgreSQL usan una base descartable de Testcontainers. No se copiarán datos de producción ni se usará la base local de desarrollo.

## Cobertura

JaCoCo comienza en V4 como medición. En V17 el gate inicial será 70% de líneas y 60% de ramas para dominio/servicios backend, y 70% de líneas frontend. Toda exclusión debe corresponder a código generado o infraestructura trivial y quedar justificada.

## Evidencia de fase

El cierre registrará comandos ejecutados, resultado, pruebas omitidas con motivo y cualquier requisito manual pendiente. Una prueba inestable se trata como defecto, no se ignora silenciosamente.
