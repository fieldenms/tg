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

**Behaviour that relies on dependent revalidation needs a restored-entity test.**
An in-memory test mutates one instance whose sibling properties were setter-assigned, so it cannot distinguish that path from the web validation cycle, which restores a fresh entity and applies only the touched properties.
To pin the restored path: persist the entity, refetch it fresh via an instrumented companion with the companion's fetch model, apply the change via `entity.getProperty(prop).setValue(value, true)` — the same enforced application that `EntityResourceUtils.processPropertyValue` performs — and assert on the dependent property's metastate.
If an interactive check contradicts such a passing test, suspect the environment first (server build predating the change, stale client bundle) before redesigning.

## Fetch Patterns

| Scenario | Pattern |
|---|---|
| Read-only, basic | `co(E.class).findByKey(...)` |
| Read-only, needs calc props | `co(E.class).findByKeyAndFetch(fetchAllInclCalc(E.class), ...)` |
| Write path (modify + save) | `co$(E.class).findByKeyAndFetch(ECo.FETCH_MODEL, ...)` |

Do not use `FETCH_PROVIDER` for read-only access to basic properties.

## Asserting on Query Results

A test class typically shares one `populateDomain()` dataset across all its tests, so assert over the rows a test depends on rather than over every row by default.
Scope the query to fixtures the test owns (`where().prop("key").in().values("CAR1", "CAR2")`), order explicitly (`orderBy().prop("key").asc()`), and assert exact values with `extracting(...).containsExactly(tuple(...), ...)`.
Asserting over the whole result is appropriate when the dataset is designed as a whole for the test case and the test means to verify its full shape; keep the assertion exact so that a fixture change updates the test deliberately.
An unordered `containsExactly` is flaky either way; an unscoped `allSatisfy` with a conditional on the key neither pins the rows nor fails clearly.
See `reference.md`, Assertions, for the worked example.

## Asserting Fixture Assumptions

Assert each assumption that a test's scenario depends on, after building the fixture and before exercising the code under test.
A fixture that fails to set up its premise turns the test into one about another case, which may well pass.
Typical assumptions:
- a type has, or does not have, a property: `Finder.isPropertyPresent(TgVehicle.class, "price")`;
- a type is, or is not, a domain entity type: `IApplicationDomainProvider.entityTypes()`;
- a time relationship, such as an expiry being in the past: compare with the time that the code under test reads (e.g., `IDates.serverNow()`), not with a literal;
- a setter assigned its value: a setter whose validator rejects the value, or whose value equals the current one, leaves the property unchanged.

Take an assumption from the same source as the code under test, and describe it with `.as(...)` so that its failure reads as a broken fixture rather than as broken code.
Mutation-check an assumption by breaking the fixture rather than the code: the test must fail at the assumption, with its description.
See `reference.md`, Assertions, for a worked example.

## Current Time in Tests

Take the current time from `UniversalConstantsForTesting` (the test binding of `IUniversalConstants`), fixed with `setNow(...)` in a `@Before` method, rather than from `new Date()` or `System.currentTimeMillis()`.
It shares its time with the test binding of `IDates`, so code under test that reads the time through either sees the test's time; code that takes the time as a parameter is passed `constants.now()`.
Production code that operates in server time uses `serverNow()`, which returns the test's time as well; to test that it does, model a request time zone (see `reference.md`, Modelling a request time zone).
Production code that reads a clock directly (e.g., a `Ticker`) takes the time, or the clock, as a parameter instead, so that tests can control it.
A fixed time makes every run assert the same instants, and lets boundary cases be written as concrete times.
Where successive operations need distinct times (e.g., ordering by `createdDate`, audit records of successive saves), install a ticker after `setNow` — `constants.setTimeSupplier(constants.mkMillisTicker(stepInMillis))` — which advances the time on every call of `now()`, and remove it in an `@After` method with `constants.removeTimeSupplier()`; `setNow` itself removes any ticker.
Use the system clock only where a test depends on real elapsed time.
See `reference.md`, Test clock control, for the details of fixed and ticking time.

## Test Data Caching

`saveDataPopulationScriptToFile()` and `useSavedDataPopulationScript()` — both must return `false` before committing.

## Auditing in Tests

The platform's DAO test config sets `AUDIT_MODE = ENABLED`, so auditing is exercised by DAO tests — see `auditing/quick-reference.md`.
Test databases are created without FK constraints, so tests cannot exercise the FKs that audit tables carry.

## Authorization in Tests

DAO methods and producers are guarded by `@Authorise(Token.class)` — see `security/quick-reference.md` for the token templates.
