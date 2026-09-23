# Esquema Supplier — V6

## Migración

`V3__create_supplier.sql` añade `suppliers` y `supplier_products` después de Customer y Catalog. Las tres migraciones se aplican hacia adelante desde una base vacía y Hibernate continúa con `ddl-auto: validate`.

## Tabla `suppliers`

- PK `BIGINT` identity.
- RUC `VARCHAR(11)` obligatorio, único y restringido a 11 dígitos.
- razón social `VARCHAR(150)` obligatoria.
- nombre comercial, correo, teléfono y dirección opcionales.
- estado lógico y timestamps UTC con precisión de microsegundos.
- índice por `active` para el filtro publicado.

## Tabla `supplier_products`

- PK `BIGINT` identity.
- FK obligatoria y restrictiva hacia `suppliers`.
- FK obligatoria y restrictiva hacia `products`.
- combinación `(supplier_id, product_id)` única.
- código de producto del proveedor `VARCHAR(80)` opcional.
- estado lógico y timestamps UTC con precisión de microsegundos.
- índices `(supplier_id, active)` y `(product_id, active)` para búsquedas y validaciones conocidas.

No hay borrado físico ni cascadas. La aplicación impide duplicar asociaciones, exige partes activas para crear o reactivar un vínculo y bloquea la desactivación de un proveedor o producto que aún tenga vínculos activos. Una vez inactivadas las relaciones, cualquiera de las partes puede desactivarse.

Supplier valida la existencia y estado del producto mediante el servicio de aplicación de Catalog; no usa repositorios ajenos. Para la transición inversa, Catalog define un puerto de desactivación que Supplier implementa consultando sus propios vínculos. Así se evita una dependencia circular. La asociación JPA carga producto y categoría explícitamente para producir respuestas con `open-in-view: false` y evitar N+1.
