# ADR-0003: Identidad externa y autorización interna

- Estado: aceptado.
- Fecha: 2026-09-16.

## Contexto

Google o Microsoft pueden autenticar a una persona, pero no conocen sus responsabilidades dentro de Gestión Comercial.

## Decisión

Conservar usuarios, roles y permisos internos. Una identidad OIDC se vinculará mediante la pareja estable `provider + subject` a un usuario interno. No se concederán permisos por email ni se usarán tokens externos como autorización de las APIs propias.

## Consecuencias

- Login local y login federado terminan en el mismo modelo interno de sesión.
- El cambio de roles pertenece a la aplicación.
- La vinculación exige un flujo explícito y auditable.
- Se evita convertir un cambio de email o coincidencia accidental en escalada de privilegios.

