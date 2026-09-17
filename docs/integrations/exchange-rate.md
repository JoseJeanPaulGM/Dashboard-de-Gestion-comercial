# Integración futura de tipo de cambio

## Propósito

En V13 el dashboard mostrará una referencia USD/PEN y servirá para practicar consumo de una API externa. La tasa no decidirá precios, ventas, pagos ni registros contables.

## Diseño previsto

- Un puerto interno solicitará una cotización; el dominio no conocerá el JSON del proveedor.
- Un adaptador llamará ExchangeRate-API y transformará su respuesta a un DTO interno.
- La API key y URL vivirán en configuración externa.
- Se configurarán timeouts, reintentos solo para fallos transitorios y circuit breaker.
- La última cotización válida podrá actuar como fallback indicando antigüedad.
- Respuestas incompletas, moneda ausente o valor no positivo se rechazarán.

## Datos que no se registran

No se registrará la API key ni la URL si contiene credenciales. Los logs incluirán proveedor, resultado, latencia y correlation ID.

## Estado

Diseño previsto; no implementado en V1.

