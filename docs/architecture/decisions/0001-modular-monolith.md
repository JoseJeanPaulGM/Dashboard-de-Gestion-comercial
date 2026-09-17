# ADR-0001: Monolito modular

- Estado: aceptado.
- Fecha: 2026-09-16.

## Contexto

El proyecto debe enseñar desarrollo Full Stack profesional a una escala manejable por una sola persona. Microservicios añadirían red, despliegues, consistencia distribuida y observabilidad antes de existir una necesidad de negocio.

## Decisión

Construir un único backend Spring Boot dividido en módulos de dominio, un frontend Angular y una base PostgreSQL.

## Consecuencias

- Desarrollo, pruebas y despliegue iniciales más simples.
- Transacciones locales para compras, ventas e inventario.
- Los límites internos se documentan para evitar un monolito desorganizado.
- Una separación futura solo se considerará con evidencia de escala, autonomía o aislamiento insuficientes.

