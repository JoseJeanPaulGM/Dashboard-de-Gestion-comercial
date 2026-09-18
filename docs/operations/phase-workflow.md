# Ciclo de trabajo por fase

## Flujo obligatorio

```text
RESUMEN → REVISAR → APROBAR INICIO → IMPLEMENTAR → PROBAR
        → DOCUMENTAR → REVISAR → APROBAR CIERRE
        → COMMIT/TAG → CERRAR FASE → ESPERAR
```

## Entrada a una fase

- Fase anterior cerrada y aprobada.
- Resumen corto presentado al usuario con alcance, exclusiones, verificación y documentos que se actualizarán.
- Aprobación explícita del resumen antes de crear ramas, modificar archivos o implementar.
- Objetivo, alcance, exclusiones y aceptación entendidos.
- Riesgos y dependencias revisados.
- Rama o estrategia de cambio acordada.

## Cambios surgidos durante una fase

- Si durante una versión aparece cualquier corrección o mejora, se detiene su aplicación.
- Se informa al usuario del motivo, alcance, impacto y verificación prevista.
- La corrección o mejora solo se aplica después de recibir aprobación explícita, aunque parezca necesaria o beneficiosa.

## Definition of Done

- Funcionalidad de la fase completa o exclusión aprobada.
- Pruebas relevantes ejecutadas y en verde.
- Documentación, OpenAPI y diagramas afectados actualizados.
- Migraciones reproducibles desde una base vacía cuando existan.
- Sin secretos, datos reales, warnings críticos ni fallos ocultos.
- `PROJECT_CONTEXT.md` refleja el estado verificable.
- Revisión realizada y observaciones resueltas.
- Resultado final presentado al usuario después de completar pruebas y documentación.
- Aprobación explícita del usuario antes del commit, tag y cierre de la versión.

## Cierre y handoff

Registrar:

1. Qué se construyó.
2. Qué decisiones se tomaron y dónde están documentadas.
3. Qué comandos y pruebas se ejecutaron.
4. Qué funciona y qué no.
5. Qué deuda o riesgo permanece.
6. Cuál es la siguiente tarea, sin ejecutarla.

## Regla de parada

Antes de iniciar una fase, ante cualquier corrección o mejora surgida durante ella y antes de cerrarla, se espera la aprobación explícita correspondiente. Después del cierre no se inicia la fase siguiente automáticamente. La disponibilidad de tiempo o herramientas no autoriza a avanzar.
