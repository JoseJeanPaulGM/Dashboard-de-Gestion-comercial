# Matriz de roles y permisos

V10 aplica esta matriz a los endpoints existentes, con denegación por defecto. Las pruebas usan JWT reales para los cinco roles. Dashboard, reportes y auditoría permanecen como intención de sus fases futuras y no tienen permisos activos en V10.

| Capacidad | ADMIN | VENDEDOR | ALMACEN | SUPERVISOR | AUDITOR |
|---|:---:|:---:|:---:|:---:|:---:|
| Administrar usuarios/roles | Sí | No | No | No | No |
| Gestionar clientes | Sí | Sí | No | Lectura | Lectura |
| Gestionar categorías/productos | Sí | Lectura | Sí | Lectura | Lectura |
| Gestionar proveedores | Sí | Lectura | Sí | Lectura | Lectura |
| Crear/confirmar compras | Sí | No | Sí | Lectura | Lectura |
| Crear/confirmar ventas | Sí | Sí | No | Lectura | Lectura |
| Ajustar inventario | Sí | No | Sí | Lectura | Lectura |
| Ver dashboard | Sí | Sí limitado | Sí limitado | Sí | Sí lectura |
| Ver/exportar reportes | Sí | Limitado | Limitado | Sí | Sí |
| Consultar auditoría | Sí | No | No | Limitado | Sí |

## Principios

- Los permisos son acciones concretas; los cinco roles predefinidos las agrupan. Una cuenta recibe la unión de sus roles.
- Una cuenta desactivada no puede renovar sesión aunque conserve roles.
- Cambiar roles, contraseña o estado revoca refresh tokens e incrementa la versión de sesión. Los access tokens anteriores se rechazan en las solicitudes posteriores al cambio.
- El rol ADMIN no se utilizará para tareas operativas cotidianas en las pruebas de autorización.
- Un login Google/Microsoft solo identifica; los permisos proceden de esta matriz interna.

## Permisos activos en V10

| Recurso | Lectura (`GET`) | Escritura en endpoints existentes |
|---|---|---|
| `/api/v1/users` | `IDENTITY_MANAGE` | `IDENTITY_MANAGE` |
| `/api/v1/customers` | `CUSTOMER_READ` | `CUSTOMER_WRITE` |
| `/api/v1/categories`, `/api/v1/products` | `CATALOG_READ` | `CATALOG_WRITE` |
| `/api/v1/suppliers`, incluidos vínculos | `SUPPLIER_READ` | `SUPPLIER_WRITE` |
| `/api/v1/purchases`, incluida confirmación | `PURCHASE_READ` | `PURCHASE_WRITE` |
| `/api/v1/sales`, incluida confirmación | `SALE_READ` | `SALE_WRITE` |
| `/api/v1/inventory` | `INVENTORY_READ` | `INVENTORY_ADJUST`, para ajustes y mínimos |

ADMIN recibe los trece permisos; los demás roles reciben únicamente los indicados en la matriz. SUPERVISOR y AUDITOR tienen lectura comercial. System y `/auth/me` requieren autenticación. Health y login/refresh/logout son públicos. Swagger/OpenAPI están disponibles en local/test y deshabilitados en prod. Las rutas sin regla se deniegan.

La API administra asignaciones de estos roles a usuarios, sin crear roles personalizados. El detalle de sesión y los códigos 401/403 están en [Identity V10](../api/identity-v10.md).

