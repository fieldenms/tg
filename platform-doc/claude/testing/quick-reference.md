# Testing — Quick Reference

## Indirect Testing Pattern

Business logic in `pojo-bl` is tested through DAO integration tests, not unit tests.
This is intentional — don't expect unit tests in `pojo-bl`.

## Fetch Patterns

| Scenario | Pattern |
|---|---|
| Read-only, basic | `co(E.class).findByKey(...)` |
| Read-only, needs calc props | `co(E.class).findByKeyAndFetch(fetchAllInclCalc(E.class), ...)` |
| Write path (modify + save) | `co$(E.class).findByKeyAndFetch(ECo.FETCH_MODEL, ...)` |

Do not use `FETCH_PROVIDER` for read-only access to basic properties.

## Test Data Caching

`saveDataPopulationScriptToFile()` and `useSavedDataPopulationScript()` — both must return `false` before committing.

## Auditing in Tests

`AUDIT_MODE = DISABLED` in DAO test config is expected — see `auditing/quick-reference.md`.

## Authorization in Tests

DAO methods and producers are guarded by `@Authorise(Token.class)` — see `security/quick-reference.md` for the token templates.
