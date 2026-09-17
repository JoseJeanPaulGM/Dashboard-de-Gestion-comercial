# ADR-0004: Monorepo

- Estado: aceptado.
- Fecha: 2026-09-16.

## Contexto

Backend, frontend, contratos, infraestructura y documentación evolucionarán en fases coordinadas.

## Decisión

Mantenerlos en un único repositorio con directorios independientes y verificaciones específicas por ruta.

## Consecuencias

- Un PR puede actualizar contrato, cliente, pruebas y documentación de forma atómica.
- El pipeline futuro evitará trabajos innecesarios mediante filtros por ruta.
- Backend y frontend conservarán builds y dependencias independientes.

