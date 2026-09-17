# Plantilla de runbook

## Objetivo y señal de activación

Describe qué condición inicia este procedimiento y qué resultado seguro debe alcanzar.

## Requisitos

- Accesos necesarios:
- Herramientas:
- Riesgos:
- Punto de no retorno, si existe:

## Diagnóstico

1. Confirmar alcance y versión.
2. Comprobar frontend, backend, base y proveedor por separado.
3. Consultar health checks, métricas y logs mediante correlation ID.
4. Registrar evidencia antes de reiniciar o cambiar estado.

## Recuperación

Detallar pasos verificables, comandos con placeholders seguros y criterio para detener/escalar.

## Validación

- Health/readiness correctos.
- Smoke test funcional.
- Métricas y logs normales.
- Integridad de datos comprobada si aplica.

## Reversión

Indicar cómo deshacer el procedimiento y qué datos no se revierten automáticamente.

