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

**Property-level:** `@IsProperty`, `@MapTo`, `@Title`, `@Observable` (required on all setters), `@Calculated`, `@Required`, `@Final`, `@Readonly`, `@UpperCase`, `@DateOnly`, `@Dependent`, `@CompositeKeyMember(n)`, `@SkipEntityExistsValidation`, `@LeProperty`, `@GeProperty`, `@CritOnly`, `@DisableAuditing`

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
- Definers: `@AfterChange(DefinerClass.class)` — run **after** the setter; also execute during DB retrieval (check `entity.isInitialising()` to distinguish).

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
