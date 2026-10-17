# Revisión de V11 — interfaz Angular

**Estado:** cierre aprobado por el usuario el 2026-10-17, después de revisar resultado, pruebas y documentos. Autorizó integrar en `main` y publicar `main` y la etiqueta anotada `v11` en GitHub.

## Entrega

- Aplicación Angular 18 independiente en `frontend/`, con navegación para Customer, Catalog, Supplier, Purchase, Sale, Inventory e Identity.
- Listados, filtros y formularios reactivos para maestros; borradores de compra y venta de varias líneas; confirmación local que modifica saldos ficticios; mínimos, alertas y ajustes.
- `DemoStoreService` usa RxJS y datos ficticios en memoria. Una recarga restablece el estado. No existe HTTP, sesión real ni autorización de interfaz en V11.
- La corrección de inicialización de dependencias en tres componentes fue aprobada expresamente por el usuario después de detectar cinco errores en la primera compilación.
- El usuario aprobó también ajustar el título y el idioma del HTML generado y reflejar el estado «En revisión» de V11 en el roadmap.
- El usuario aprobó corregir la tabla de estrategia de pruebas para asignar servicios HTTP, sesión e interceptores a V12.

## Comandos y resultado

Ejecutados desde `frontend/` salvo la inspección Git:

| Comando | Resultado |
|---|---|
| `npm install --cache <ruta local>` | Instalación completa; generó `package-lock.json`. npm informó 64 hallazgos de seguridad en el árbol total. |
| `npm run build` | Correcto después de la corrección aprobada; bundle inicial 348.06 kB bruto. |
| `npm test -- --watch=false --browsers=ChromeHeadless` | 10 pruebas, 0 fallos, fuera del sandbox. Dentro del sandbox ChromeHeadless no pudo iniciar por restricciones del entorno. |
| Playwright CLI sobre `http://127.0.0.1:4200/` | Alta local de cliente comprobada; vistas de compras e inventario abiertas; 0 errores de consola. |
| `npm audit --omit=dev --json` | 8 hallazgos en dependencias de producción: 4 moderados y 4 altos; la sugerencia de npm implica subir Angular de versión mayor. No se cambian dependencias fuera del alcance aprobado. |

## Límites y pendientes tras el cierre

- Los datos son ficticios y no persisten. V12 conectará HTTP, sesión e interceptores y añadirá E2E de flujos integrados.
- Los hallazgos de `npm audit` quedan registrados como riesgo aceptado para esta demostración local, sin cambiar el Angular 18 acordado. Antes de publicar o conectar datos reales, se necesita decidir una estrategia de actualización de dependencias.
- V12 no se inicia automáticamente; requiere nueva solicitud, resumen y aprobación.
