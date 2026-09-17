# Límites modulares

## Regla general

Cada módulo será propietario de su modelo, casos de uso, contratos y persistencia. La interacción entre módulos ocurrirá mediante servicios de aplicación o eventos internos definidos, nunca mediante repositorios ajenos.

## Responsabilidades y dependencias permitidas

| Módulo | Es propietario de | Puede depender de |
|---|---|---|
| Customer | clientes y su estado | Shared |
| Catalog | categorías, productos y precios | Shared |
| Supplier | proveedores y vínculos de catálogo | Catalog, Shared |
| Purchase | compra, líneas y estados | Supplier, Catalog, Inventory, Shared |
| Sale | venta, líneas y estados | Customer, Catalog, Inventory, Shared |
| Inventory | saldo, movimientos y mínimo | Catalog, Shared |
| Identity | usuarios, roles, permisos y sesión | Shared |
| Dashboard | consultas agregadas y cambio referencial | contratos de lectura de módulos, Shared |
| Report | proyecciones de lectura | contratos de lectura de módulos, Shared |
| Audit | eventos de auditoría | Identity y eventos publicados |

`Shared` se limitará a conceptos técnicos estables: errores, paginación, reloj, correlation ID y tipos realmente compartidos. No será un contenedor de lógica sin dueño.

## Reglas de interacción

1. Purchase y Sale solicitan cambios de stock a Inventory; no escriben el saldo directamente cuando V9 esté completada.
2. Dashboard y Report consumen proyecciones de lectura y no modifican entidades comerciales.
3. Audit recibe hechos; no controla el éxito de la operación principal ni almacena secretos.
4. Identity decide autoridades internas. Los proveedores OIDC solo prueban identidad externa.
5. Una dependencia circular obliga a revisar límites antes de introducir soluciones técnicas.

## Estructura futura orientativa

```text
com.example.commercialmanagement
├── shared
├── customer
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── catalog
├── supplier
├── purchase
├── sale
├── inventory
├── identity
├── dashboard
├── report
└── audit
```

El nombre base definitivo se confirmará en V2 antes de generar el proyecto; el ejemplo no autoriza crear código durante V1.

