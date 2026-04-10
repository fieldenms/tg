# Testing and Security Reference

## Testing Approach

JUnit 4 with integration tests against PostgreSQL and SQL Server.
Test data fixtures using `DbDrivenTestCase` pattern with test-specific Guice modules.

### Assertions

Both JUnit and AssertJ are acceptable. Prefer AssertJ for:
- **Exceptions:** `assertThatThrownBy(() -> ...).isInstanceOf(X.class).hasMessageContaining("...")`
- **Collections:** `assertThat(list).hasSize(3).containsExactly("a", "b", "c")`
- **Descriptive messages:** `.as("context")` is cleaner than JUnit's message parameter

### Indirect Testing Pattern

Business logic (validators, definers) resides in `pojo-bl` but is tested **indirectly through DAO tests**:
- DAO tests manipulate entities → property setters automatically trigger validation chains
- Don't expect traditional unit tests in `pojo-bl` — this is intentional
- A single DAO test may validate multiple validators and definers
- Legacy tests may be `@Deprecated` in favor of newer `IDomainData` approach

### Test Data Population Script Caching

`saveDataPopulationScriptToFile()` and `useSavedDataPopulationScript()` control test data caching:
- **`saveDataPopulationScriptToFile = true`**: records all SQL INSERTs from `populateDomain()` to a file (first run).
- **`useSavedDataPopulationScript = true`**: replays the saved script instead of running `populateDomain()` from scratch (subsequent runs — much faster).
- They **must not both be true** simultaneously (throws `DomainDrivenTestException`).

**Local iteration workflow** (single test case only): set `saveDataPopulationScriptToFile = true` for one run, then switch to `useSavedDataPopulationScript = true` for fast re-runs.
This only works when running tests within a single test case — the saved script is specific to that test case's `populateDomain()`.
Running multiple test cases with `useSavedDataPopulationScript = true` will fail because each test case has different data requirements.

**IMPORTANT**: Both must return `false` before committing.
`useSavedDataPopulationScript = true` fails on CI and for other developers (no saved script file).
`saveDataPopulationScriptToFile = true` generates unnecessary files.
Always verify these return `false` in any test file being committed.

### Web Resource Testing

**Server-side web resource logic** is tested via `AbstractWebResourceWithDaoTestCase` (`platform-web-resources/src/test/`).
This base class extends `AbstractDaoTestCase` and uses `WebResourcesTestRunner`, which loads web UI bindings (`IWebUiConfig`, `IApplicationSettings`, etc.) in addition to standard DAO bindings.

Tests `@Inject` platform interfaces directly — no manual stubs needed for standard platform contracts:
```java
public class ApplicationConfigurationResourceTest extends AbstractWebResourceWithDaoTestCase {
    @Inject private IWebUiConfig webUiConfig;
    @Inject private IApplicationSettings appSettings;
    @Inject private IDates dates;
    @Inject private IUserProvider userProvider;
    // ...
}
```

**Testability pattern for Restlet resources:** Restlet resource classes (`AbstractWebResource` subclasses) require `Context`, `Request`, and `Response` for construction, making direct instantiation in tests impractical.
Extract business logic into `static` methods with explicit parameters, then call them directly in tests using injected dependencies:
```java
// In the resource — static, no Restlet dependencies
static LinkedHashMap<String, Object> buildConfiguration(
        final IWebUiConfig webUiConfig, final IApplicationSettings appSettings, ...) { ... }

// In the test — call directly with injected deps
final var config = buildConfiguration(webUiConfig, appSettings, dates, provider, user);
```

### Web Client Testing

Browser-based test suites using Web Component Tester (WCT):
- Main suite: `platform-web-ui/src/main/web/ua/com/fielden/platform/web/tests.html`
- Individual tests in `*/test/*.html` directories

## Security — Domain-Centric Authorization

### Security Token Templates

| Template | Token Name Pattern | Purpose |
|----------|-------------------|---------|
| `SAVE` | `Entity_CanSave_Token` | Standard save (most common) |
| `SAVE_NEW` | `Entity_CanSaveNew_Token` | Save new only (specific cases) |
| `SAVE_MODIFIED` | `Entity_CanSaveModified_Token` | Save existing only (specific cases) |
| `DELETE` | `Entity_CanDelete_Token` | Deletion |
| `READ` | `Entity_CanRead_Token` | Reading data |
| `READ_MODEL` | `Entity_CanReadModel_Token` | Reading data model |
| `EXECUTE` | `Entity_CanExecute_Token` | Action execution |
| `MODIFY` | `Entity_CanModify_Property_Token` | Property-level modification |
| `MASTER_OPEN` | `Entity_CanOpen_Token` | Opening entity masters |
| `MASTER_MENU_ITEM_ACCESS` | `Entity_CanAccess_Token` | Compound master menu items |

Generally only `SAVE` is used. `SAVE_NEW`/`SAVE_MODIFIED` are for specific cases needing separate create vs update permissions.

### Declarative Authorization

Apply via `@Authorise` annotation on DAO methods and producers:
```java
@Override @SessionRequired @Authorise(Project_CanDelete_Token.class)
public int batchDelete(final Collection<Long> entitiesIds) { ... }
```

**Infrastructure:** `AuthorisationInterceptor` (AOP), `IAuthorisationModel`, thread-local scoping prevents nested redundant checks.

### Authorization Levels

- **DAO:** CRUD operations with appropriate tokens
- **Producer:** Entity creation/opening
- **Property:** Fine-grained access for sensitive properties
- **Action:** Business process execution