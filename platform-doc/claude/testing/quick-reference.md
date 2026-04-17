# Testing & Security — Quick Reference

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

`AUDIT_MODE = DISABLED` in DAO test config is expected — see `auditing/reference.md`.

## Security Token Templates

| Template | Token Name Pattern | Purpose |
|---|---|---|
| `SAVE` | `Entity_CanSave_Token` | Standard save (most common) |
| `SAVE_NEW` / `SAVE_MODIFIED` | `Entity_CanSaveNew_Token` | Separate create vs update (rare) |
| `DELETE` | `Entity_CanDelete_Token` | Deletion |
| `READ` | `Entity_CanRead_Token` | Reading data |
| `READ_MODEL` | `Entity_CanReadModel_Token` | Reading data model |
| `EXECUTE` | `Entity_CanExecute_Token` | Action execution |
| `MODIFY` | `Entity_CanModify_Property_Token` | Property-level modification |
| `MASTER_OPEN` | `Entity_CanOpen_Token` | Opening entity masters |
| `MASTER_MENU_ITEM_ACCESS` | `Entity_CanAccess_Token` | Compound master menu items |

Apply via `@Authorise(Token.class)` on DAO methods and producers.
