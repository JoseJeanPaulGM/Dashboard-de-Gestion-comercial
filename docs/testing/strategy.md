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

## Escenarios críticos acumulativos

- Compra confirmada incrementa stock una sola vez.
- Venta concurrente no produce stock negativo.
- Anulación crea el efecto compensatorio permitido.
- Usuario sin permiso recibe 403 aunque manipule la interfaz.
- Refresh token reutilizado o revocado no abre sesión.
- Fallo del tipo de cambio no impide mostrar información interna.
- Auditoría no expone secretos y conserva actor/correlación.
- Backup restaurado reproduce integridad y conteos acordados.

## Datos y aislamiento

Las pruebas crearán sus propios datos y no dependerán del orden de ejecución. Desde V3, las integraciones PostgreSQL usan una base descartable de Testcontainers. No se copiarán datos de producción ni se usará la base local de desarrollo.

## Cobertura

JaCoCo comienza en V4 como medición. En V17 el gate inicial será 70% de líneas y 60% de ramas para dominio/servicios backend, y 70% de líneas frontend. Toda exclusión debe corresponder a código generado o infraestructura trivial y quedar justificada.

## Evidencia de fase

El cierre registrará comandos ejecutados, resultado, pruebas omitidas con motivo y cualquier requisito manual pendiente. Una prueba inestable se trata como defecto, no se ignora silenciosamente.
