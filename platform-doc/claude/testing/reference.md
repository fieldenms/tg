# Testing — Detailed Reference

For fetch patterns and other common lookups, see `quick-reference.md` in this directory.
For security tokens and authorization, see `security/quick-reference.md`.

## Testing Approach

JUnit 4 with integration tests against PostgreSQL and SQL Server.
Test data fixtures using `DbDrivenTestCase` pattern with test-specific Guice modules.

### Assertions

Both JUnit and AssertJ are acceptable.
Prefer AssertJ for:
- **Exceptions:** `assertThatThrownBy(() -> ...).isInstanceOf(X.class).hasMessageContaining("...")`
- **Collections:** `assertThat(list).hasSize(3).containsExactly("a", "b", "c")`
- **Descriptive messages:** `.as("context")` is cleaner than JUnit's message parameter

**Query results against shared fixtures.**
A test class typically shares one `populateDomain()` dataset across all its tests, so an assertion over every row couples the test to fixtures added later for unrelated tests.
When a test depends on only some of the fixtures, scope the query to them, impose an explicit order when the assertion is order-sensitive, and assert exact values:
```java
final var qry = select(TgVehicle.class)
        .where().prop("key").in().values("CAR1", "CAR2")
        .yield().prop("id").as("id")
        .yield().prop("key").as("key")
        .yield().prop("price").as("price")
        .modelAsEntity(TgVehicle.class);

final var vehicles = co(TgVehicle.class).getAllEntities(from(qry)
                                                                .with(fetchKeyAndDescOnly(TgVehicle.class).with("price"))
                                                                .with(orderBy().prop("key").asc().model())
                                                                .model());
assertThat(vehicles)
        .extracting(TgVehicle::getKey, TgVehicle::getPrice)
        .containsExactly(tuple("CAR1", Money.of("20")), tuple("CAR2", Money.of("200")));
```
`containsExactly` checks the row count and every value together, so a missing, extra, or wrong row fails with a readable diff.
The database guarantees no result order: without an `orderBy`, use `containsExactlyInAnyOrder`.
Asserting over the whole result is appropriate when the dataset is designed as a whole for the test case and the test means to verify its full shape.
In that case a fixture change should update the test deliberately, so keep the assertion exact rather than conditional.
The pattern to avoid is an unscoped `allSatisfy` with a conditional on the key: it neither pins the rows nor fails clearly when a fixture is added.

### Indirect Testing Pattern

Business logic (validators, definers) resides in `pojo-bl` but is tested **indirectly through DAO tests**:
- DAO tests manipulate entities → property setters automatically trigger validation chains
- Don't expect traditional unit tests in `pojo-bl` — this is intentional
- A single DAO test may validate multiple validators and definers
- Legacy tests may be `@Deprecated` in favor of newer `IDomainData` approach

### Fetch Patterns in Tests

| Scenario | Pattern | Rationale |
|----------|---------|-----------|
| Read-only, basic properties | `co(E.class).findByKey(...)` | No fetch model needed; lightweight |
| Read-only, needs calculated properties | `co(E.class).findByKeyAndFetch(fetchAllInclCalc(E.class), ...)` | Calculated properties require explicit fetch |
| Write path (modify + save) | `co$(E.class).findByKeyAndFetch(ECo.FETCH_PROVIDER.fetchModel(), ...)` | Instrumented + full fetch for validators/definers |

Do not use `FETCH_PROVIDER` for read-only access to basic properties — it is unnecessary overhead and obscures intent.

### Testing Entity Centre criteria via `DynamicQueryBuilder`

When a synthetic `Re*` entity uses the declarative crit-only style (`@CritOnly(entityUnderCondition, propUnderCondition)` + `{propName}_` stem field — see *Declarative correlated filters* in `entity-model/reference.md`), its `model_` is a bare passthrough and does **not** contain any `.critCondition(...)` clauses.
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
- `EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(Class, CharSequence)` — creates an unconfigured `QueryProperty` ready to accept `setValue`/`setValue2`/mnemonic state.
  The `CharSequence` overload accepts metamodel path objects directly.
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

### Test configurations

A test case runs with a test configuration — the injector with all bindings — created from the DB connectivity properties that its runner produces with `mkDbProps(String)` (see `AbstractDomainDrivenTestCaseRunner`).
Configurations are created once per JVM for each runner type, as their creation is computationally intensive; test cases with the same runner share one.

A test case that depends on a different configuration, such as another authentication mode, uses a runner that adjusts the properties:
```java
public class SsoAuthModeContextSelector extends H2OrPostgreSqlOrSqlServerContextSelector {

    public SsoAuthModeContextSelector(final Class<?> klass) throws Exception {
        super(klass);
    }

    @Override
    protected Properties mkDbProps(final String dbUri) {
        final Properties props = super.mkDbProps(dbUri);
        props.setProperty("auth.mode", AuthMode.SSO.name());
        return props;
    }
}
```
```java
@RunWith(SsoAuthModeContextSelector.class)
public class UserInSsoModeTestCase extends AbstractDaoTestCase { ... }
```
Such test cases run in the normal build, alongside all others.

- All configurations in a JVM share the test database and its schema, which is created once; the properties may adjust the configuration (e.g., `auth.mode`, or `config.domain` for a configuration class with other bindings), but not the domain entity types.
- Each configuration keeps its injector and connection pool for the rest of the JVM, so a runner type is introduced only where a test case depends on a separate configuration.
- The static state of `AbstractDomainDrivenTestCase`, through which `co(...)`, `new_(...)` and `getInstance(...)` work, refers to the configuration of the running test case: a runner assigns it when created and again before its test case runs.

### Test clock control

Tests take the current time from `UniversalConstantsForTesting`, the test binding of `IUniversalConstants`, rather than from `new Date()` or `System.currentTimeMillis()`.
It delegates to `DatesForTesting`, the test binding of `IDates`, so code under test that reads the time through either sees the time set by the test.
The system clock is used only where a test depends on real elapsed time.

How `now()` is determined:
- `setNow(dateTime)` fixes the time, and removes any time supplier.
- `setTimeSupplier(supplier)` makes every call of `now()` return the next value of `supplier`, which takes precedence over the fixed time.
- `removeTimeSupplier()` returns to the time fixed by `setNow`.
- With neither set, `now()` falls back to the system clock — another reason to fix the time in every test.

The field that gives access to the test clock:
```java
private final UniversalConstantsForTesting constants =
        (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
```

#### Fixed time

Fix the time in a `@Before` method:
```java
@Before
public void setNow() {
    constants.setNow(dateTime("2026-10-21 10:00:00"));
}
```
`dateTime(...)` and `date(...)` of the test base class parse `yyyy-MM-dd`, optionally followed by `HH:mm`, `HH:mm:ss` or `HH:mm:ss.SSS`.
A fixed time makes every run assert the same instants, and lets boundary cases be written as concrete times — e.g., `PasswordResetCooldownTest` checks a reset link issued at 10:00 and expiring at 10:15 at 10:09:59 and at 10:10.
Code under test that takes the time as a parameter is passed `constants.now()` (`constants.now().toDate()` for a `Date`), and the test moves the time with further `setNow(...)` calls.

Production code that has to read the system clock directly — e.g., to compare with instants stored as epoch milliseconds, where `IDates.now()` would shift the instant in the independent time-zone mode — cannot be controlled by the test clock.
Such code takes the time as a parameter, and only its caller reads the clock, so that tests pass a fixed time — e.g., `LoginInitiateResetResource.findOrAssignPasswordResetUuid`.

#### Ticking time

Where successive operations need distinct times, install a ticker that advances the time on every call of `now()`.
Typical cases: ordering by `createdDate` / `lastUpdatedDate` — e.g., a calculated property or query filters by `createdDate.lt(...)`, or assertions sort entities by their version timestamps — and audit records of successive saves, which applications keep apart this way.
Without a ticker, fast successive saves can share the same millisecond, silently breaking ordering filters (the predicate is `false` instead of `true`, so rows drop out of subqueries with no error).

```java
@Before
public void setNow() {
    constants.setNow(<chosen-start-time>);
    // setNow removes the time supplier, so the ticker is installed after it.
    constants.setTimeSupplier(constants.mkMillisTicker(1000));
}

@After
public void removeTicker() {
    constants.removeTimeSupplier();
}
```
`mkMillisTicker(step)` starts from the current `getNow()` — hence it is created after `setNow` — and advances by `step` milliseconds on every call, so the first call already returns the start time plus `step`.
Choose `step` to suit the domain, e.g., a minute (`60000`) where times are compared with one-minute precision.
Every call of `now()` advances the time, including calls made by the code under test, so tests assert the order of times rather than exact values; to assert exact values, capture `constants.now()` once, or remove the ticker for that part of the test.
A test can also tick only for a part of its work, calling `setTimeSupplier(...)` before and `removeTimeSupplier()` after it — e.g., data population scripts tick while saving and then remove the ticker.
Some applications expose a base-class helper like `resetNowAndStartTimeTicking(constants, dateTime)` (which wraps `setNow` and `setTimeSupplier`) and a `prePopulatedNow` constant for the populate baseline — use them if available, otherwise inline the two calls.

#### Where to set the clock

Why `@Before` (and `@After`) and not just `populateDomain()`:
- `UniversalConstantsForTesting` is mutable and shared across tests within a JVM fork — a previous test may have set a different time, or left a ticker installed.
- `populateDomain()` runs only for the first test of a test case — the others restore the recorded data population script.
- `populateDomain()` early-returns when `useSavedDataPopulationScript()` is `true`, so any clock setup done there is skipped in the fast-iteration path.

If `populateDomain()` itself runs save logic that needs a particular time or distinct timestamps, set the clock inside it as well (after the `useSavedDataPopulationScript()` early-return guard), removing a ticker once the data is saved.

#### Modelling a request time zone

`IDates.now()` / `IUniversalConstants.now()` return the current time in the user's time zone during a web request — with `independent.time.zone` enabled, the user's wall-clock time relabelled as the server's time zone.
`serverNow()` returns the current time in the server's time zone on any thread; logic that must operate in server time (e.g., computing when a scheduled job is next executed) uses it.

Without a modelled request time zone, `now()` and `serverNow()` both return the modelled time.
To test that such logic uses `serverNow()`, model a request time zone:
```java
constants.setRequestTimeZone(DateTimeZone.forID("Pacific/Kiritimati"));
```
After that:
- `serverNow()` returns the modelled time;
- `now()` returns the modelled time as `DefaultDates.now()` returns it during a request from that time zone with `independent.time.zone` enabled;
- `IDates.requestTimeZone()` returns that time zone, while `IDates.timeZone()` and `IDates.zoned(Date)` stay in the server's time zone, as in independent mode.

Unlike a real request time zone, which is thread-local, the modelled one is shared by all threads, and stays in effect until `removeRequestTimeZone()` is invoked.
`AbstractDaoTestCase.populateDomain()` removes it, but, as for the clock, this does not reset it between tests — remove it in `@After` (or in the `@Before` reset).

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

## Testing with auditing

Disable auditing in DAO test configurations — `AUDIT_MODE = DISABLED` is expected, not a regression:
```java
props.setProperty(AuditingIocModule.AUDIT_MODE, AuditingMode.DISABLED.name());
```

The webapp and data-population configs still run with `ENABLED`, so audit behaviour is exercised there.
For the full rationale and mode details, see `auditing/reference.md`.