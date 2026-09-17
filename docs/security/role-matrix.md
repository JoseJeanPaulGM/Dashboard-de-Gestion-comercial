# Matriz preliminar de roles y permisos

La matriz se validará con endpoints reales en V10. El backend aplicará deny-by-default; la interfaz solo reflejará permisos y nunca será la barrera principal.

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

- Los permisos serán acciones concretas; los roles solo las agrupan.
- Una cuenta desactivada no puede renovar sesión aunque conserve roles.
- Cambios de privilegios revocan refresh tokens activos o se hacen efectivos en la siguiente renovación, según el diseño final de V10.
- El rol ADMIN no se utilizará para tareas operativas cotidianas en las pruebas de autorización.
- Un login Google/Microsoft solo identifica; los permisos proceden de esta matriz interna.

