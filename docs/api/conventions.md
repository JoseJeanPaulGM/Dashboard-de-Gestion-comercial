# Convenciones de API

Estas convenciones se activarán desde V2 y se concretarán con OpenAPI.

## Recursos y URLs

- Prefijo: `/api/v1`.
- Sustantivos plurales en inglés: `/customers`, `/products`, `/purchases`.
- Identificador como segmento: `/customers/{id}`.
- Acciones de transición solo cuando no sean CRUD natural: `/purchases/{id}/confirm`.

## Métodos y estados

| Operación | Método | Respuesta habitual |
|---|---|---|
| crear | POST | 201 y ubicación del recurso |
| consultar | GET | 200 |
| actualizar completo | PUT | 200 |
| transición parcial explícita | PATCH o POST de acción | 200 |
| desactivar | PATCH/DELETE según contrato | 204 o 200 |

Se usarán 400 para petición mal formada, 401 no autenticado, 403 no autorizado, 404 inexistente, 409 conflicto de estado/unicidad y 422 solo si se adopta de manera consistente para semántica inválida.

## Paginación

- Parámetros: `page` desde cero, `size` limitado y `sort=field,direction`.
- Solo se aceptan campos de orden declarados por el recurso.
- La respuesta contendrá `content`, página, tamaño, total de elementos y total de páginas.

Desde V4 el contrato concreto es `PageResponse<T>` con `content`, `page`, `size`, `totalElements` y `totalPages`. El tamaño permitido es de 1 a 100 y cada módulo declara sus campos de orden, filtros y búsqueda. V5 reutilizó esta base para productos y categorías.

## Errores

La respuesta seguirá Problem Details e incorporará:

- `type`, `title`, `status`, `detail` e `instance`;
- `code` estable para el cliente;
- `timestamp`, `path` y `correlationId`;
- colección de errores de campo cuando corresponda.

No contendrá stack traces, SQL, nombres internos de clases, tokens ni secretos.

## Datos

- JSON UTF-8 y nombres `camelCase`.
- Fechas/horas ISO 8601.
- Importes transmitidos como números decimales y moneda ISO 4217 cuando el contexto la requiera.
- El backend recalcula importes y no confía en totales enviados por Angular.

## Compatibilidad

Cambios aditivos mantienen `/v1`. Cambios incompatibles requieren primero valorar una migración compatible; una nueva versión de API será el último recurso y debe quedar documentada.
