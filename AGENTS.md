# Instrucciones del repositorio para agentes

## Orden de lectura

1. Leer este archivo completo.
2. Leer `PROJECT_CONTEXT.md`.
3. Consultar `docs/README.md` y los documentos de la fase activa.
4. Inspeccionar el estado del repositorio antes de modificar archivos.

## Regla de fases

- Antes de iniciar cada versión, presentar al usuario un resumen corto de qué se hará, qué quedará fuera, cómo se comprobará y qué documentos se actualizarán; esperar su aprobación explícita antes de crear ramas, modificar archivos o implementar.
- Trabajar exclusivamente en la fase aprobada por el usuario.
- No anticipar código, dependencias ni infraestructura de fases futuras.
- Durante una versión aprobada, si aparece cualquier corrección o mejora, detenerse, explicarla y solicitar aprobación explícita antes de aplicarla, aunque parezca necesaria o beneficiosa.
- No cerrar, etiquetar ni iniciar otra fase sin revisión y aprobación explícitas.
- Ante una petición amplia como “continuar”, confirmar el próximo elemento pendiente del contexto y mantener el alcance de la fase activa.

## Arquitectura y código futuro

- Mantener un monolito modular organizado por dominio.
- Evitar paquetes globales de `controller`, `service` o `repository`; cada módulo será dueño de sus capas internas.
- Las dependencias entre módulos deberán seguir `docs/architecture/module-boundaries.md`.
- No exponer entidades JPA directamente por API.
- Usar PostgreSQL en desarrollo, pruebas de persistencia y producción; no introducir H2.
- No introducir Oracle, microservicios, Kubernetes ni facturación electrónica en V1–V18.

## Seguridad y datos

- Nunca versionar secretos, credenciales, tokens, dumps reales ni datos personales.
- Mantener ejemplos en archivos `.example` con valores ficticios.
- No registrar contraseñas, JWT, refresh tokens, API keys ni payloads sensibles.
- La identidad OIDC externa nunca concede permisos por sí sola: siempre se vincula a un usuario interno.
- Evitar borrado físico de registros comerciales referenciados.

## Calidad

- Cada cambio funcional futuro debe incluir pruebas proporcionales y documentación actualizada.
- Ejecutar los checks relevantes antes de declarar una tarea terminada.
- No ocultar fallos ni bajar umbrales de calidad para hacer pasar una build.
- Mantener commits pequeños y usar Conventional Commits en inglés.
- Preservar cambios del usuario que no pertenezcan a la tarea actual.

## Cierre de trabajo

- Completar y documentar las pruebas relevantes y actualizar la documentación afectada antes de solicitar el cierre.
- Presentar el resultado final al usuario y esperar su aprobación explícita antes de cerrar la versión, crear su etiqueta o iniciar la siguiente.
- Actualizar `PROJECT_CONTEXT.md`.
- Registrar decisiones arquitectónicas nuevas como ADR.
- Documentar comandos ejecutados y resultado de pruebas.
- Enumerar pendientes reales, sin iniciar automáticamente la siguiente fase.
