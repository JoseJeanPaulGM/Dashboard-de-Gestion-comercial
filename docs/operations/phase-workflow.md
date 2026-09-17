# Ciclo de trabajo por fase

## Flujo obligatorio

```text
PLAN → REVISAR → APROBAR → CHAT → IMPLEMENTAR → PROBAR
     → REVISAR → COMMIT → CERRAR FASE → ESPERAR
```

## Entrada a una fase

- Fase anterior cerrada y aprobada.
- Objetivo, alcance, exclusiones y aceptación entendidos.
- Riesgos y dependencias revisados.
- Rama o estrategia de cambio acordada.

## Definition of Done

- Funcionalidad de la fase completa o exclusión aprobada.
- Pruebas relevantes ejecutadas y en verde.
- Documentación, OpenAPI y diagramas afectados actualizados.
- Migraciones reproducibles desde una base vacía cuando existan.
- Sin secretos, datos reales, warnings críticos ni fallos ocultos.
- `PROJECT_CONTEXT.md` refleja el estado verificable.
- Revisión realizada y observaciones resueltas.
- Commit/tag solo después de aprobación del cierre.

## Cierre y handoff

Registrar:

1. Qué se construyó.
2. Qué decisiones se tomaron y dónde están documentadas.
3. Qué comandos y pruebas se ejecutaron.
4. Qué funciona y qué no.
5. Qué deuda o riesgo permanece.
6. Cuál es la siguiente tarea, sin ejecutarla.

## Regla de parada

Después de cerrar una fase se espera una nueva aprobación. La disponibilidad de tiempo o herramientas no autoriza a avanzar.

