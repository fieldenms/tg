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

### Testing Entity Centre criteria via `DynamicQueryBuilder`

When a synthetic `Re*` entity uses the declarative crit-only style (`@CritOnly(entityUnderCondition, propUnderCondition)` + `{propName}_` stem field — see *Declarative correlated filters* in @platform-doc/claude/entity-model.md), its `model_` is a bare passthrough and does **not** contain any `.critCondition(...)` clauses.
Running `co(ReEntity.class).count(select(ReEntity.class).model(), params)` therefore exercises none of the criteria — the test would produce the same result regardless of the filter values.

To test such criteria, replicate the Entity Centre runtime path: build `QueryProperty` instances for each criterion, then feed them into `DynamicQueryBuilder.createQuery(...)`, which is what translates the declarative hints into real `.critCondition(...)` clauses.

```java
// For each (path, value) pair, create a QueryProperty and index it in the params map
// under the conventional QP_ key that DynamicQueryBuilder expects.
final Map<String, Object> params = new HashMap<>();
for (int i = 0; i < paths.size(); i++) {
    final var qp = EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(ReEntity.class, paths.get(i));
    qp.setValue(values.get(i));
    params.put(queryPropertyParamName(paths.get(i)), qp);
}
final var queryProperties = params.values().stream()
    .mapMulti(typeFilter(QueryProperty.class)).toList();

// Assemble the query exactly as the Entity Centre runtime would.
final var count = co(ReEntity.class).count(
    DynamicQueryBuilder.createQuery(ReEntity.class, queryProperties, getInstance(IDates.class)).model(),
    params);
```

Relevant platform helpers:
- `EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(Class, CharSequence)` — creates an unconfigured `QueryProperty` ready to accept `setValue`/`setValue2`/mnemonic state. The `CharSequence` overload accepts metamodel path objects directly.
- `QueryProperty.queryPropertyParamName(CharSequence)` — produces the `QP_<path>` key that `DynamicQueryBuilder` expects in the params map.
- `DynamicQueryBuilder.createQuery(managedType, queryProperties, dates)` — assembles the final query (there is also a 4-arg overload that accepts an `IQueryEnhancer`).
- `StreamUtils.typeFilter(Class)` — filters a heterogeneous `Map<String, Object>.values()` stream down to just `QueryProperty` instances, useful when the params map mixes types.

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

### Reference data parity between production and tests

Production and test environments seed reference / lookup rows differently:
- **Production** populates them via SQL migration scripts.
- **Tests** populate them via `populate*` methods in the application's domain-data interface (commonly `IDomainData`).

These two paths must be kept in sync.
When you introduce a new constant for a reference table — typically a hand-curated enumeration of business values fetched at runtime via `findByKey(...)` — update **both** sides:
1. Add the row to the relevant SQL migration script.
2. Append the same row to the populate method that seeds the lookup table in tests.

If only the production migration is updated, tests run against a database where `findByKey(...)` returns `null` for the new constant.
Most code happily accepts that, persists `null` to dependent columns, and the symptom surfaces much later as a `NullPointerException` in an unrelated assertion or downstream query — far from the actual omission.

### Test clock control

When a test relies on `createdDate` / `lastUpdatedDate` ordering — e.g., a calculated property or query filters by `createdDate.lt(...)`, or assertions sort entities by their version timestamps — install the millisecond ticker so that consecutive saves get distinct timestamps.
Without it, fast successive saves can share the same millisecond, silently breaking ordering filters (the predicate is `false` instead of `true`, so rows drop out of subqueries with no error).

The pattern is two pieces:

1. **Field** — obtain the mutable test constants from the injector:
   ```java
   private final UniversalConstantsForTesting constants =
           (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
   ```
2. **`@Before` reset** — re-install the ticker before every test method:
   ```java
   @Before
   public void reset() {
       constants.setNow(<chosen-start-time>);
       // setNow clears the supplier — install the ticker again.
       constants.setTimeSupplier(constants.mkMillisTicker(1000));
   }
   ```
   `<chosen-start-time>` is a `DateTime` of the test's choosing.
   Some applications expose a base-class helper like `resetNowAndStartTimeTicking(constants, dateTime)` (which wraps the two calls above) and a `prePopulatedNow` constant for the populate baseline — use them if available, otherwise inline the two calls.

Why `@Before` and not just `populateDomain()`:
- `UniversalConstantsForTesting` is mutable and shared across tests within a JVM fork — a previous test may have advanced or removed the supplier.
- `populateDomain()` early-returns when `useSavedDataPopulationScript()` is `true`, so any clock setup done there is skipped in the fast-iteration path.

If `populateDomain()` itself runs save logic that needs distinct timestamps, mirror the same `setNow` + `setTimeSupplier` pair inside it (after the `useSavedDataPopulationScript()` early-return guard).

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

### Runtime-generated tokens for audit types

Security tokens for auditing (see @platform-doc/claude/auditing.md) are **not** hand-written.
`ISecurityTokenGenerator` produces them at application startup for the **synthetic** audit-entity side only — `Re{E}_a3t_CanRead_Token` and `Re{E}_a3t_CanReadModel_Token`.
Persistent audit-entity types (`E_a3t_{n}`) and audit-prop types do not receive tokens — users never query those directly.

Consequences:
- Do not commit hand-written `Re{E}_a3t_*_Token` classes. If you see them in an application module under `security/tokens/`, they are remnants of a pre-generic-auditing design and should be deleted.
- Dynamic token retrieval now goes through `ISecurityTokenProvider` rather than `Class.forName` — requests for tokens that are not in the provider are stricter than before, and an application using tokens outside the provider may break.
- To customise audit-token generation or inject extra tokens, extend `SecurityTokenProvider` and override the appropriate methods.

## Testing with auditing

Enabling auditing (`audit.mode=ENABLED`) adds per-save overhead: one audit-entity insert plus one audit-prop insert per dirty auditable property.
For DAO integration test suites that save many entities per run to exercise business logic, this overhead is significant and unnecessary — the DAO tests do not assert on audit rows.

The conventional TG-application pattern is to **disable auditing in the DAO test configuration** (the application's `IDomainDrivenTestCaseConfiguration` implementation used by `AbstractDaoTestCase`):

```java
props.setProperty(AuditingIocModule.AUDIT_MODE, AuditingMode.DISABLED.name());
// audit.path is not required when auditing is disabled
```

This is a deliberate speed trade-off, not a gap in coverage.
The application's data-population configuration and the webapp itself still run with `AUDIT_MODE = ENABLED`, so web-UI tests, data population, and manual testing do exercise the audit hook on save.
If a specific test genuinely needs auditing on, it should use a **separate** `IDomainDrivenTestCaseConfiguration` — do not flip the shared DAO test config.

When reviewing a TG-based application, `AUDIT_MODE = DISABLED` in the DAO test configuration is expected, not a regression.
In `DISABLED` mode `IAuditTypeFinder` is still injectable, but every `navigate*` method throws `AuditingModeException` — tests that need to reflect over audit types must use a separate configuration with `AUDIT_MODE = ENABLED` or `GENERATION`.