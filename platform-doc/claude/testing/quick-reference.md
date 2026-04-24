# Testing — Quick Reference

## Indirect Testing Pattern

Business logic in `pojo-bl` is tested through DAO integration tests, not unit tests.
This is intentional — don't expect unit tests in `pojo-bl`.

## Validation and Definer Testing Patterns

Trigger validation and definer logic through the entity's own API, not through a DAO `save()` whose exception you then catch.
Property setter calls are intercepted, so calling `entity.setProp(value)` is enough to fire both validators and definers — no save needed.
For the underlying interception mechanism see *Property Declaration* in `entity-model/quick-reference.md` (or *Validators and Definers* in `entity-model/reference.md` for the detailed flow).

| Logic kind | Test via |
|---|---|
| Property validator (`@BeforeChange`) — failure | After `entity.setProp(value)`: `entity.getProperty(prop).isValid()` (boolean) or `.getFirstFailure().getMessage()` for exact-match |
| Property validator (`@BeforeChange`) — warning | After `entity.setProp(value)`: `entity.getProperty(prop).hasWarnings()` or `.getFirstWarning().getMessage()` |
| Entity-level (`validate()` override) | `entity.isValid()` — directly returns the `Result` of `validate()`. Use `.isSuccessful()` and `.getMessage()` |
| Definer (`@AfterChange`) | After `entity.setProp(value)`: assert side effects on other properties (e.g., computed `shiftDuration`) or on metaproperty state (`isRequired()`, `isEditable()`, `isVisible()`) |

`AbstractEntity.isValid()` is `return validate();` — calling it runs property-level validators (via `super.validate()`) and then the override's cross-property check.

Avoid `assertThatThrownBy(() -> save(entity))` for these tests: it tests platform plumbing (save propagates failure as a thrown exception) rather than the logic itself, and substring-matches messages instead of exact-matching them.
Reach for `save()` only when the test genuinely depends on persistence (e.g., to verify that a persistently-calculated property is stored, or that a cascading DAO behaviour fires).

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
