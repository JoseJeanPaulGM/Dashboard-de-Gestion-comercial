# API Catalog — V5

## Alcance

Catalog administra categorías, productos y el precio de venta vigente en PEN. Category y Product son maestros con estado activo/inactivo; no existe borrado físico ni se exponen entidades JPA por la API.

## Categorías

| Método | Ruta | Resultado |
|---|---|---|
| `POST` | `/api/v1/categories` | `201`, cuerpo y cabecera `Location` |
| `GET` | `/api/v1/categories/{id}` | `200` con categoría |
| `GET` | `/api/v1/categories` | `200` con página de categorías |
| `PUT` | `/api/v1/categories/{id}` | `200` con categoría actualizada |
| `PATCH` | `/api/v1/categories/{id}/status` | `200` con categoría actualizada |

El código y el nombre son obligatorios. El código se normaliza con trim y mayúsculas y es único. La descripción es opcional. Una categoría no puede desactivarse mientras tenga productos activos.

El listado acepta `page`, `size`, `sort`, `active` y `search`. La búsqueda parcial ignora mayúsculas, consulta código o nombre y trata `%`, `_` y `\` literalmente. Los campos ordenables son `id`, `code`, `name`, `active`, `createdAt` y `updatedAt`; el orden predeterminado es `name,asc`.

## Productos

| Método | Ruta | Resultado |
|---|---|---|
| `POST` | `/api/v1/products` | `201`, cuerpo y cabecera `Location` |
| `GET` | `/api/v1/products/{id}` | `200` con producto |
| `GET` | `/api/v1/products` | `200` con página de productos |
| `PUT` | `/api/v1/products/{id}` | `200` con producto actualizado |
| `PATCH` | `/api/v1/products/{id}/status` | `200` con producto actualizado |

SKU, nombre, `salePrice` y `categoryId` son obligatorios. El SKU se normaliza con trim y mayúsculas y es único. El precio es positivo, acepta como máximo diez enteros y dos decimales y se devuelve junto con `currency: "PEN"`. Cada producto pertenece a una categoría existente. Un producto nuevo o reactivado requiere una categoría activa. Desde V6, un producto tampoco puede desactivarse mientras tenga vínculos activos con proveedores; después de inactivar esos vínculos puede desactivarse normalmente.

El listado acepta `page`, `size`, `sort`, `active`, `categoryId`, `search`, `minPrice` y `maxPrice`. Los límites de precio son inclusivos y `minPrice` no puede superar `maxPrice`. La búsqueda consulta SKU o nombre con las mismas reglas literales de categorías.

Los campos ordenables son `id`, `sku`, `name`, `salePrice`, `categoryName`, `active`, `createdAt` y `updatedAt`; el orden predeterminado es `name,asc`.

## Paginación

Ambos recursos reutilizan `PageResponse<T>`, `PageCriteria`, `PageResult<T>` y la validación de V4. `page` comienza en cero, `size` admite de 1 a 100 y `sort` usa `field,direction`. Un orden secundario por `id` estabiliza las páginas.

## Errores

La API conserva Problem Details y los códigos transversales. Catalog añade:

- `CATEGORY_NOT_FOUND`;
- `CATEGORY_CODE_CONFLICT`;
- `CATEGORY_HAS_ACTIVE_PRODUCTS`;
- `PRODUCT_NOT_FOUND`;
- `PRODUCT_SKU_CONFLICT`;
- `PRODUCT_CATEGORY_INACTIVE`;
- `PRODUCT_HAS_ACTIVE_SUPPLIERS` desde V6.

Las validaciones devuelven `400`, los recursos inexistentes `404` y los conflictos de unicidad o estado `409`.
