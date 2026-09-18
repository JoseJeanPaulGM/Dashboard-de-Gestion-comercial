# API Customer — V4

## Recurso

`Customer` representa una persona o empresa con documento comercial, datos opcionales de contacto y estado activo/inactivo. La API usa DTO; la entidad JPA no se expone.

| Método | Ruta | Resultado |
|---|---|---|
| `POST` | `/api/v1/customers` | `201`, cuerpo y cabecera `Location` |
| `GET` | `/api/v1/customers/{id}` | `200` con cliente |
| `GET` | `/api/v1/customers` | `200` con página de clientes |
| `PUT` | `/api/v1/customers/{id}` | `200` con cliente actualizado |
| `PATCH` | `/api/v1/customers/{id}/status` | `200` con cliente actualizado |

Los tipos documentales admitidos son `DNI`, `RUC`, `CE`, `PASSPORT` y `OTHER`. Tipo, número y nombre son obligatorios; email, teléfono y dirección son opcionales. El documento se normaliza con trim y mayúsculas y es único junto con su tipo.

## Listado

`GET /api/v1/customers` acepta:

| Parámetro | Predeterminado | Regla |
|---|---|---|
| `page` | `0` | entero mayor o igual a cero |
| `size` | `20` | entero entre 1 y 100 |
| `sort` | `name,asc` | un campo permitido y `asc` o `desc` |
| `active` | sin filtro | booleano opcional |
| `search` | sin búsqueda | coincidencia parcial por nombre o documento |

Los campos de orden permitidos son `id`, `name`, `documentNumber`, `active`, `createdAt` y `updatedAt`. Un orden secundario por `id` estabiliza las páginas. La búsqueda ignora mayúsculas y trata `%`, `_` y `\` literalmente. Al combinar filtros se aplica `active AND (name OR documentNumber)`.

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

`PageResponse<T>` y la validación técnica de paginación viven en Shared. Los módulos conservan sus filtros, búsqueda y lista permitida de orden; V5 reutilizará este contrato en Catalog.

## Errores

La API conserva el formato Problem Details transversal y añade `INVALID_REQUEST`, `INVALID_PAGINATION`, `INVALID_SORT`, `CUSTOMER_NOT_FOUND` y `CUSTOMER_DOCUMENT_CONFLICT`. Los errores no exponen SQL, entidades ni nombres de restricciones.
