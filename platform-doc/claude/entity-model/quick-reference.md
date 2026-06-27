# Entity Model — Quick Reference

## Entity Hierarchy

| Base Class | Purpose |
|---|---|
| `AbstractEntity` | Root of all domain entities |
| `AbstractPersistentEntity` | Adds `createdBy`, `createdDate`, `createdTransactionGuid`, `lastUpdatedBy`, `lastUpdatedDate`, `lastUpdatedTransactionGuid` |
| `ActivatableAbstractEntity` | Adds `active` property with reference counting |
| `AbstractFunctionalEntityWithCentreContext` | Action entities (non-persistent); `save` "executes" the action |

Entities annotated with `@MapEntityTo` are persistent.

## Core Annotations

**Entity-level:** `@MapEntityTo`, `@KeyType`, `@CompanionObject`, `@EntityTitle(value, desc)`, `@DisplayDescription`, `@DescRequired`, `@DescTitle`, `@Subtitles`, `@DeactivatableDependencies`, `@DomainEntity`, `@WithMetaModel`, `@WithoutMetaModel`, `@SupportsEntityExistsValidation`, `@Audited`

**Property-level:** `@IsProperty`, `@MapTo`, `@Title`, `@Observable` (required on all setters), `@Calculated`, `@Required`, `@Final`, `@Readonly`, `@UpperCase`, `@DateOnly`, `@TimeOnly`, `@Dependent`, `@CompositeKeyMember(n)`, `@SkipEntityExistsValidation`, `@LeProperty`, `@GeProperty`, `@CritOnly`, `@DisableAuditing`

**`@IsProperty` parameters:** `(Long.class)` for collection element type, `(length = 8000)` for string length, `(assignBeforeSave = true)` for auto-assigned values.
`MaxLengthValidator` is implicit for `String` properties — do not declare it manually.

**`@Dependent` must use metamodel constants**, not string literals.

**`@CritOnly` on persistent entities is an anti-pattern** — use a synthetic `Re*` wrapper.
Exception: generative entities (implement `WithCreatedByUser<T>`).

## Property Declaration

```java
@IsProperty @MapTo @Title("Description") @Readonly
private String someProperty;

@Observable
public MyEntity setSomeProperty(final String value) {
    this.someProperty = value;
    return this;
}
```

Property setter calls are intercepted; validators and definers fire synchronously per setter call (no `save()` needed):
- Validators: `@BeforeChange(@Handler(ValidatorClass.class))` — run **before** the setter; chain in declaration order; can prevent the assignment by returning `Result.failure()`.
  Validators do **not** run at DB load (load uses direct field writes that bypass `ObservableMutatorInterceptor`).
  Use a definer with `metaProp.setDomainValidationResult(...)` to surface a load-time message.
- Definers: `@AfterChange(DefinerClass.class)` — run **after** the setter; also execute at DB retrieval, but *not* per-property during the load itself — TG sets every field directly, then `DefinersExecutor` walks the graph and fires each definer with all sibling properties already populated (so cross-property reads from a definer at load time are safe; check `entity.isInitialising()` to distinguish load from user mutation).
- **No-op setters short-circuit both.** When the new value equals the current (`equalsEx`), `ObservableMutatorInterceptor` skips validation and the `@AfterChange` handler entirely. Tests that try to provoke a validator on the loaded value must set a different value first to clear, then re-set.
- **Chained reads in a definer:** prefer the dot-path idiom — `Reflector.isPropertyProxied(entity, MetaModel_.a().b().c())` to short-circuit when programmatic flows passed a thinly-fetched ref, then `entity.get(MetaModel_.a().b().c())` for a null-safe traversal that returns `null` on any missing link. See `entity-model/reference.md` § *Chained reads in definers*.

**Anti-pattern: definer as save-time hook.**
A definer is not a save-time hook — it fires per setter call and per loaded property.
If you want a value computed *once per save* (e.g., expensive aggregation over a date range, multi-property snapshot), compute it in the DAO's `save()` method just before `super.save(entity)`.
In many situations it is worth defining the destination property `@Readonly` so the master form doesn't expose it as editable.
Symptom of misuse: the computation runs many times during edit, sees inconsistent intermediate states, or you find yourself reaching for `isInitialising()` to suppress unwanted firings.

## Companion Objects

| Method | Returns | Use Case |
|---|---|---|
| `co(Type.class)` | `IEntityReader<T>` (uninstrumented) | Read-only queries, exports |
| `co$(Type.class)` | `IEntityDao<T>` (instrumented) | Full CRUD with change tracking |

**Use `FETCH_PROVIDER` / `FETCH_MODEL` when retrieving for editing:**
```java
co$(Vehicle.class).findByKeyAndFetch(VehicleCo.FETCH_MODEL, key);
```
Narrower fetches are fine for read-only access but never for the write path.

## Calculated Properties

```java
@IsProperty @Readonly @Calculated
private BigDecimal totalCost;
protected static final ExpressionModel totalCost_ = expr().prop(X_.hours()).mult().prop(X_.rate()).model();
```

Used in aggregations, EQL expands the expression inline (e.g., `SUM(hours * rate)`).

For `BigDecimal` calculated properties, any literal default (`then().val(...)`) — and any consumer literal such as a test assertion — must match the declared `@IsProperty(scale = N)`.
`BigDecimal.equals` is scale-sensitive, so `BigDecimal.ZERO` (scale 0) does not equal `0.00` (scale 2) returned from the DB.
See *Calculated Properties — Subquery Patterns* in `reference.md` for the recommended scale-matching constant pattern.

## Synthetic / Report Entities

Non-persistent entities with a `model_` query.
Annotated with `@DomainEntity` (+ often `@WithMetaModel`), not `@MapEntityTo`.

```java
protected static final EntityResultQueryModel<T> model_ = select(Source.class)
    .groupBy().prop(Source_.key())
    .yield().prop(Source_.key()).as(T_.key())
    .yield().sumOf().prop(Source_.amount()).as(T_.total())
    .modelAsEntity(T.class);
```

**Synthetic grouping property entities** — fixed-option selectors for report grouping/distribution.
Use `models_` (plural `List`), inner `enum`, `@SupportsEntityExistsValidation`.
See `entity-model/reference.md` § *Synthetic Grouping Property Entities*.

## Fixed Entity Instances

Protect specific records of a user-maintained *persistent* entity that business logic references by key.
An inner `enum Fixed` lists the protected keys and is the single source of truth; the rows are created by migration/population, not by the enum.
Four parts: (1) the `Fixed` enum with `matches` / `fromValue` / `isOneOf`; (2) a key+desc `@BeforeChange` validator — **fail** on key rename, **warn** on desc change, guarded by `isPersisted() && Fixed.isOneOf(entity)`; (3) an `IRenderingCustomiser` that italicises fixed rows; (4) a `batchDelete` override rejecting fixed rows.
See `entity-model/reference.md` § *Fixed Entity Instances*.

## Metamodel References

Always use metamodel references instead of string literals:
```java
.prop(Vehicle_.model())                              // Simple
.prop(PmXref_.asset().equipment().avgReading())      // Deep chain
```

Generated in `target/generated-sources/` by `MetaModelProcessor`.

## Topic-Specific Gotcha

**DeleteOperations patterns:**
Pessimistic locking with `UPGRADE` lock mode for activatable entities.
Deliberately catches only `PersistenceException` for referential integrity violations.
`case null, default -> null` in switch expressions is conventional TG shorthand.

**`@DateOnly` and `@TimeOnly` are semantic markers, not data transformations.**
Both apply to `java.util.Date` properties and signal the UI to render only the relevant portion.
The other portion of the underlying `Date` is **not** truncated or zeroed automatically — it is up to domain logic to handle it as appropriate.
A `@TimeOnly` property whose setter has been given a full `Date` retains both the date and time parts in storage; reading the value returns the full datetime.
Do not write date-composition or anchoring code (e.g. `entity.getDate()` + `entity.getTimeOnlyField()`) to "reconstruct" a datetime — the field already has it.
The reverse holds for `@DateOnly`: the time portion is preserved unless the upstream code zeroed it.
