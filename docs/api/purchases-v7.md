# API Purchase — V7

## Alcance

Una compra se crea como borrador con un proveedor y entre 1 y 100 líneas. Cada producto aparece una sola vez. El backend calcula subtotales y total en PEN; no acepta totales como autoridad. La confirmación es una transición explícita y una compra confirmada conserva sus importes. No hay edición ni anulación en V7.

| Método | Ruta | Resultado |
|---|---|---|
| `POST` | `/api/v1/purchases` | `201`, compra borrador y `Location` |
| `GET` | `/api/v1/purchases/{id}` | `200`, cabecera y líneas |
| `GET` | `/api/v1/purchases` | `200`, página de resúmenes |
| `POST` | `/api/v1/purchases/{id}/confirm` | `200`, compra confirmada |

La creación recibe `supplierId` y `lines` con `productId`, `quantity` y `unitPrice`. Cantidad: de 1 a 1 000 000 por línea. Precio de compra: positivo, máximo diez enteros y dos decimales. El precio de venta de Catalog no determina el precio de compra. La respuesta incluye `status` (`DRAFT` o `CONFIRMED`), líneas con subtotales, `total`, `currency: "PEN"`, `createdAt` y `confirmedAt` cuando aplique. Los importes usan JSON numérico decimal.

El listado acepta `page`, `size`, `sort`, `status` y `supplierId`. `status` usa los valores del enum en mayúsculas. Se puede ordenar por `id`, `supplierId`, `status`, `total`, `createdAt` y `confirmedAt`; el predeterminado es `createdAt,desc`. Se reutiliza `PageResponse<T>` y un orden secundario por `id` estabiliza las páginas.

## Reglas de confirmación

El proveedor, cada producto y cada vínculo proveedor–producto deben existir y estar activos al crear y al confirmar. La confirmación bloquea la fila de compra, comprueba su estado y actualiza todos los saldos dentro de la misma transacción. Un segundo intento devuelve `409` sin aumentar el stock. Si falla cualquier incremento, la compra continúa en borrador y todos los saldos conservan sus valores anteriores.

## Errores

- `VALIDATION_ERROR` (`400`): campos obligatorios, cantidades, precios o número de líneas inválidos.
- `INVALID_REQUEST` (`400`): producto repetido, filtro de proveedor inválido u otra regla de petición.
- `INVALID_PAGINATION` / `INVALID_SORT` (`400`): paginación u orden inválido.
- `SUPPLIER_NOT_FOUND` / `PRODUCT_NOT_FOUND` (`404`): maestro inexistente al crear.
- `PURCHASE_NOT_FOUND` (`404`): compra inexistente.
- `PURCHASE_SUPPLY_UNAVAILABLE` (`409`): proveedor, producto o vínculo inactivo o ausente.
- `PURCHASE_STATE_CONFLICT` (`409`): compra ya confirmada.

Los errores conservan el formato Problem Details y correlation ID de la API. La autorización por rol se implementará en V10.
