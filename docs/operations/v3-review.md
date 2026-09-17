# Revisión de V3 — Persistencia inicial

## Evidencia de aceptación

| Criterio | Estado | Evidencia |
|---|---|---|
| PostgreSQL como único motor | Cumplido | driver PostgreSQL y `postgres:17-alpine`; sin H2 |
| Spring Data JPA e Hibernate | Cumplido | dependencias activas y contexto de persistencia cargado |
| Configuración por ambientes | Cumplido | `local`, `test` y `prod` con datasource y política de esquema explícitos |
| Secretos fuera de Git | Cumplido | credenciales reales externas; `.env.example` contiene valores ficticios |
| Aislamiento de pruebas | Cumplido | base PostgreSQL descartable administrada por Testcontainers |
| Persistencia JPA | Cumplido | escritura, ID `Long`, lectura e instante UTC verificados |
| Restricción de base | Cumplido | `NOT NULL` provoca `DataIntegrityViolationException` |
| Commit transaccional | Cumplido | dos escrituras confirmadas como una operación |
| Rollback transaccional | Cumplido | ninguna escritura permanece después de la excepción |
| Regresión V2 | Cumplido | información, validación, Problem Details, correlation ID, health y OpenAPI en verde |
| Pruebas automatizadas | Cumplido | 9 pruebas, 0 fallos, 0 errores y 0 omitidas |
| Build verificable | Cumplido | `.\\mvnw.cmd verify` finalizó con `BUILD SUCCESS` |
| Funcionalidad comercial fuera de alcance | Cumplido | sin entidades ni endpoints comerciales |
| Flyway reservado para V4 | Cumplido | sin migraciones ni dependencia Flyway |
| Aprobación de cierre | Cumplido | aprobación explícita recibida el 2026-09-17 |

## Incidencias observadas

La primera ejecución dentro del sandbox del agente compiló el proyecto, pero Java no pudo abrir `\\.\pipe\docker_engine`. La misma suite ejecutada fuera del sandbox accedió correctamente a Docker Desktop y quedó en verde. No se añadió ningún fallback ni se omitieron pruebas.

## Cierre registrado

V3 fue aprobada y cerrada el 2026-09-17. La base de persistencia queda disponible para el primer vertical slice, pero V4 permanece pendiente de autorización explícita y no se inicia automáticamente.
