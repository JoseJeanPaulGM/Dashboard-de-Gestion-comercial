# API Sale — V8

## Alcance

Una venta se crea como borrador para un cliente activo, con entre 1 y 100 líneas de productos activos. Cada producto aparece una sola vez y su precio de venta vigente se copia en la línea al crearla. El backend calcula subtotales y total en PEN; no acepta importes como autoridad. La confirmación es una transición única. V8 no incluye edición ni anulación.

| Método | Ruta | Resultado |
|---|---|---|
| `POST` | `/api/v1/sales` | `201`, venta borrador y `Location` |
| `GET` | `/api/v1/sales/{id}` | `200`, cabecera y líneas |
| `GET` | `/api/v1/sales` | `200`, página de resúmenes |
| `POST` | `/api/v1/sales/{id}/confirm` | `200`, venta confirmada |

La creación recibe `customerId` y `lines` con `productId` y `quantity`; cada cantidad está entre 1 y 1 000 000. La respuesta incluye `status` (`DRAFT` o `CONFIRMED`), líneas con precio unitario y subtotal, `total`, `currency: "PEN"`, `createdAt` y `confirmedAt` cuando corresponda. Los importes se transmiten como números decimales. El precio histórico no cambia cuando cambia el precio de Catalog.

El listado acepta `page`, `size`, `sort`, `status` y `customerId`. Los campos ordenables son `id`, `customerId`, `status`, `total`, `createdAt` y `confirmedAt`; el orden predeterminado es `createdAt,desc`. Usa `PageResponse<T>` y un orden secundario por `id` para estabilizar las páginas.

## Confirmación y stock

El cliente y todos los productos deben seguir activos. Sale bloquea la fila de venta y solicita a Inventory los descuentos en orden de identificador de producto dentro de una única transacción. Inventory solo descuenta cuando el saldo disponible alcanza la cantidad solicitada; la actualización condicional en PostgreSQL serializa ventas concurrentes sobre el mismo saldo. Si falta stock en cualquier línea, todos los descuentos anteriores se revierten y la venta permanece en borrador. Confirmar dos veces devuelve `409` y no repite el descuento.

## Errores

- `VALIDATION_ERROR` (`400`): campos obligatorios, cantidad o número de líneas inválidos.
- `INVALID_REQUEST` (`400`): producto repetido, filtro de cliente inválido u otra regla de petición.
- `INVALID_PAGINATION` / `INVALID_SORT` (`400`): paginación u orden inválido.
- `CUSTOMER_NOT_FOUND` / `PRODUCT_NOT_FOUND` (`404`): maestro inexistente al crear.
- `SALE_NOT_FOUND` (`404`): venta inexistente.
- `SALE_PARTY_UNAVAILABLE` (`409`): cliente o producto inactivo al crear o confirmar.
- `SALE_STATE_CONFLICT` (`409`): venta ya confirmada.
- `INSUFFICIENT_STOCK` (`409`): saldo ausente o insuficiente para confirmar.

Los errores usan Problem Details con correlation ID. La autorización por rol llegará en V10.
