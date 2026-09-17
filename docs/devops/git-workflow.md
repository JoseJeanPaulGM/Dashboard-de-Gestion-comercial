# Flujo Git

## Modelo

- `main` representa el último estado estable aprobado.
- Ramas cortas: `feature/<scope>`, `fix/<scope>`, `docs/<scope>` y `chore/<scope>`.
- Cada PR pertenece a una fase y tiene alcance revisable.
- No se mantienen ramas permanentes `develop` ni ramas por cada ambiente.

## Commits

Se utilizarán Conventional Commits en inglés:

```text
docs(architecture): record modular monolith decision
feat(customer): add paginated customer search
fix(inventory): prevent negative stock during concurrent sales
test(security): cover expired refresh token
```

Tipos principales: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `build`, `ci` y `perf`.

## Pull requests

Un PR debe indicar objetivo, fase, cambios, verificación, evidencia y pendientes. Requiere:

- build y pruebas relevantes en verde;
- documentación y contexto actualizados;
- ausencia de secretos;
- migraciones revisadas cuando existan;
- compatibilidad de API o cambio documentado;
- revisión de autorización si afecta un endpoint protegido.

## Tags y releases

Al cerrar cada fase se creará un tag anotado `v1`, `v2`, …, `v18`, únicamente después de aprobación. Correcciones posteriores pueden usar `vN.1` si es necesario, sin reutilizar tags.

## Protección futura de `main`

Al conectar GitHub se exigirá PR, checks de backend/frontend aplicables y conversación resuelta. Desde V17 las imágenes se etiquetarán por commit SHA y por release; `latest` no será evidencia suficiente para rollback.

## Estado especial del entorno actual

El workspace tiene un propietario distinto de la cuenta que ejecuta comandos. Las verificaciones locales usarán `git -c safe.directory='D:/portfolio 2026' ...`. No se alterará `safe.directory` global sin aprobación.

