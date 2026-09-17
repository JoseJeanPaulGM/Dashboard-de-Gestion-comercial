# ADR-0002: PostgreSQL como base exclusiva

- Estado: aceptado.
- Fecha: 2026-09-16.

## Contexto

Mantener compatibilidad artificial con varias bases reduce el tiempo disponible para aprender modelado, transacciones, índices, backups y operación real.

## Decisión

Usar PostgreSQL en desarrollo, pruebas de persistencia y producción. No introducir Oracle ni H2.

## Consecuencias

- Las consultas y tipos pueden aprovechar PostgreSQL conscientemente.
- Las pruebas de persistencia detectarán diferencias reales del motor.
- El entorno de prueba necesita una instancia PostgreSQL aislada.
- Flyway controlará el esquema desde V4.

