# API Supplier — V6

## Alcance

Supplier administra proveedores y sus vínculos comerciales con productos de Catalog. Los proveedores y vínculos usan estado activo/inactivo; no existe borrado físico ni se exponen entidades JPA por la API.

## Proveedores

| Método | Ruta | Resultado |
|---|---|---|
| `POST` | `/api/v1/suppliers` | `201`, cuerpo y cabecera `Location` |
| `GET` | `/api/v1/suppliers/{id}` | `200` con proveedor |
| `GET` | `/api/v1/suppliers` | `200` con página de proveedores |
| `PUT` | `/api/v1/suppliers/{id}` | `200` con proveedor actualizado |
| `PATCH` | `/api/v1/suppliers/{id}/status` | `200` con proveedor actualizado |

El RUC y la razón social son obligatorios. El RUC contiene exactamente 11 dígitos, es único y se conserva como texto. Nombre comercial, correo, teléfono y dirección son opcionales. El correo, cuando existe, debe tener formato válido.

El listado acepta `page`, `size`, `sort`, `active` y `search`. La búsqueda parcial ignora mayúsculas, consulta RUC, razón social o nombre comercial y trata `%`, `_` y `\` literalmente. Los campos ordenables son `id`, `ruc`, `businessName`, `tradeName`, `active`, `createdAt` y `updatedAt`; el orden predeterminado es `businessName,asc`.

## Vínculos de catálogo

| Método | Ruta | Resultado |
|---|---|---|
| `POST` | `/api/v1/suppliers/{supplierId}/products` | `201` con vínculo y `Location` |
| `GET` | `/api/v1/suppliers/{supplierId}/products` | `200` con página de vínculos |
| `PUT` | `/api/v1/suppliers/{supplierId}/products/{productId}` | `200` con código actualizado |
| `PATCH` | `/api/v1/suppliers/{supplierId}/products/{productId}/status` | `200` con vínculo actualizado |

Cada combinación proveedor–producto es única. El vínculo puede guardar un código opcional de hasta 80 caracteres usado por el proveedor. Crear o reactivar un vínculo requiere que proveedor y producto estén activos. Un proveedor o producto no puede desactivarse mientras conserve vínculos activos. Tras inactivar todas sus relaciones, cualquiera de las partes puede desactivarse.

El listado acepta `page`, `size`, `sort`, `active` y `search`. La búsqueda consulta SKU, nombre del producto o código del proveedor. Los campos ordenables son `id`, `sku`, `name`, `supplierProductCode`, `active`, `createdAt` y `updatedAt`; el orden predeterminado es `name,asc`.

La respuesta del vínculo expone `productActive` además del estado propio de la relación. Catalog consulta el puerto de ciclo de vida implementado por Supplier antes de desactivar un producto, sin acceder a repositorios ajenos.

## Errores

La API conserva Problem Details y añade:

- `SUPPLIER_NOT_FOUND`;
- `SUPPLIER_RUC_CONFLICT`;
- `SUPPLIER_HAS_ACTIVE_PRODUCTS`;
- `SUPPLIER_PRODUCT_NOT_FOUND`;
- `SUPPLIER_PRODUCT_CONFLICT`;
- `SUPPLIER_PRODUCT_INACTIVE_PARTY`.

La desactivación de productos añade `PRODUCT_HAS_ACTIVE_SUPPLIERS` al contrato de Catalog.

Las validaciones devuelven `400`, los recursos o vínculos inexistentes `404` y los conflictos de unicidad o estado `409`.
