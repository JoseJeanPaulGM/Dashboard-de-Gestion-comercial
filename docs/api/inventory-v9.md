# API Inventory — V9

La API usa `/api/v1/inventory`, JSON, `PageResponse<T>` y Problem Details. V9 aún no incorpora autenticación ni permisos; ese control corresponde a V10.

Desde V10, estas rutas requieren JWT: `INVENTORY_READ` para consultar y `INVENTORY_ADJUST` para ajustar o configurar mínimos. Véase la [matriz de roles](../security/role-matrix.md).

## Saldo, mínimo y alertas

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/balances/{productId}` | Saldo del producto, o 404 si no existe. Un producto sin fila de saldo devuelve cantidad y mínimo cero. |
| PUT | `/balances/{productId}/minimum` | Establece `minimumQuantity` no negativa sin alterar la cantidad. |
| GET | `/alerts` | Saldos con mínimo positivo y cantidad menor o igual al mínimo. |

El saldo contiene `productId`, `quantity`, `minimumQuantity`, `updatedAt` y `lowStock`. Un mínimo de cero desactiva la alerta. Las alertas se calculan al consultar; V9 no envía notificaciones.

`GET /alerts` admite `page`, `size` y `sort=productId,asc|desc` o `sort=quantity,asc|desc`; el valor predeterminado es `productId,asc`.

## Movimientos

`GET /balances/{productId}/movements` devuelve movimientos paginados con `id`, `productId`, `quantityChange`, `balanceAfter`, `sourceType`, `sourceId`, `requestKey`, `reason` y `createdAt`. Admite `sort=id,asc|desc` o `sort=createdAt,asc|desc`; el valor predeterminado es `id,desc`.

`sourceType` puede ser `OPENING`, `PURCHASE`, `SALE` o `ADJUSTMENT`. En compras y ventas, `sourceId` identifica la línea comercial. `OPENING` representa el saldo anterior a V9 y no reconstruye cada operación histórica.

## Ajustes

`POST /adjustments` recibe:

```json
{
  "productId": 42,
  "quantityChange": -3,
  "reason": "Tres unidades dañadas",
  "requestKey": "1317fd78-33e4-45e4-980c-cf853ae3bbed"
}
```

`quantityChange` es un entero entre -1 000 000 y 1 000 000, distinto de cero. `reason` se normaliza quitando espacios externos y debe tener entre 10 y 500 caracteres. Un ajuste negativo no puede dejar stock negativo. `requestKey` es un UUID único para la intención de ajuste: repetir la misma clave y los mismos datos devuelve el movimiento original sin cambiar el saldo; reutilizarla con datos distintos devuelve 409 `ADJUSTMENT_KEY_CONFLICT`.

Los errores de validación, paginación y orden siguen las convenciones transversales. Producto inexistente devuelve 404 `PRODUCT_NOT_FOUND`; stock insuficiente devuelve 409 `INSUFFICIENT_STOCK`. Los ajustes no sustituyen la confirmación de compras o ventas.
