# Frontend — V11

Aplicación Angular 18 de Gestión Comercial. V11 presenta la interfaz de clientes, categorías, productos, proveedores, compras, ventas, inventario y usuarios. El estado se conserva **solo en memoria** mediante `DemoStoreService` y RxJS: recargar la página repone los datos ficticios.

Los formularios usan Reactive Forms. Es posible crear y editar maestros, crear borradores de compra o venta con varias líneas, confirmarlos para cambiar saldos de ejemplo, configurar mínimos y ensayar ajustes. La interfaz no envía solicitudes HTTP ni abre una sesión real. La integración con la API, JWT e interceptores corresponde a V12. Las validaciones y reglas simuladas sirven para explorar la interfaz; el backend seguirá siendo la autoridad de negocio y permisos.

## Ejecutar

Se verificó con Node.js 20.12.2 y npm 10.5.0. Desde `frontend/`:

```powershell
npm ci
npm run start
```

Abrir `http://localhost:4200/`. No se requieren credenciales ni PostgreSQL para esta demostración.

## Comprobar

```powershell
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
```

Las pruebas de V11 cubren formularios y componentes, observación RxJS de registros, compras y ventas locales, stock insuficiente y mínimos. ChromeHeadless necesita acceso a un navegador instalado. La revisión visual y el estado de la fase constan en [la revisión de V11](../docs/operations/v11-review.md).

## Organización

- `src/app/demo-store.service.ts`: datos ficticios y operaciones reactivas en memoria.
- `src/app/records-page.component.ts`: pantallas de maestros y usuarios.
- `src/app/orders-page.component.ts`: borradores de compras y ventas.
- `src/app/inventory-page.component.ts`: saldos, alertas, mínimos y ajustes.
- `src/app/app.routes.ts`: rutas de la interfaz.

No se guardan contraseñas de demostración. El proyecto no incluye secretos ni datos reales.
