# Entity Model Reference

## Entity Hierarchy

All domain entities extend `AbstractEntity`. Common ancestors:

| Base Class | Purpose |
|------------|---------|
| `AbstractPersistentEntity` | Adds `createdBy`, `createdDate`, `createdTransactionGuid`, `lastUpdatedBy`, `lastUpdatedDate`, `lastUpdatedTransactionGuid` |
| `ActivatableAbstractEntity` | Adds `active` property with reference counting; values remain persisted but inactive ones shouldn't be used for new data |
| `AbstractFunctionalEntityWithCentreContext` | Action entities (non-persistent); `save` on companion "executes" the action |

Entities annotated with `@MapEntityTo` are persistent.

## Core Annotations

**Entity-level:** `@MapEntityTo`, `@KeyType`, `@CompanionObject`, `@EntityTitle(value, desc)`, `@DisplayDescription`, `@DescRequired`, `@DescTitle(value, desc)`, `@Subtitles(@PathTitle(path, title))`, `@DeactivatableDependencies({Dep1.class, Dep2.class})`, `@DomainEntity`, `@WithMetaModel`, `@SupportsEntityExistsValidation`

- `@DomainEntity` — marks synthetic entities (union entities, report entities) for metamodel generation and domain registration.
- `@WithMetaModel` — explicitly enables metamodel generation for non-standard entities (e.g., action entities that need metamodel references).
- `@SupportsEntityExistsValidation` — enables entity existence validation for non-persistent entities used as criteria grouping properties.

**Property-level:** `@IsProperty`, `@MapTo`, `@Title`, `@Observable` (required on all setters), `@Calculated`, `@Required`, `@Final`, `@Readonly`, `@UpperCase`, `@DateOnly`, `@Dependent(prop1, prop2, ...)`, `@CompositeKeyMember(n)`, `@SkipEntityExistsValidation(skipActiveOnly, skipNew)`, `@LeProperty(prop)`, `@GeProperty(prop)`, `@CritOnly`

**`@IsProperty` parameters:** `@IsProperty(Long.class)` for collection element type, `@IsProperty(length = 8000)` for string length, `@IsProperty(assignBeforeSave = true)` for auto-assigned values.

**`@CritOnly`** — marks properties as criteria-only (used for filtering, not displayed as result columns):
```java
@CritOnly(RANGE)                                    // Date/numeric range filter
private Date dateRangeCrit;

@CritOnly(SINGLE)                                   // Single entity picker
private Location locationCrit;

@CritOnly(value = MULTI, mnemonics = WITHOUT)        // Multi-select with "without" mnemonic
private InventoryPart inventoryPartCrit;

// Advanced: filter on a related entity's property
@CritOnly(value = Type.MULTI, entityUnderCondition = WorkOrderDetail.class,
    propUnderCondition = WorkOrderDetailMetaModel.comment_)
private String commentCrit;
```

## Composite Keys

Entities with multi-part business keys use `@KeyType(DynamicEntityKey.class)` and annotate each key member with `@CompositeKeyMember(n)` where `n` determines the order.
Key members are typically `@Final` to prevent modification after creation.

```java
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(PmTaskCo.class)
public class PmTask extends ActivatableAbstractEntity<DynamicEntityKey> {
    @IsProperty @MapTo @Final @CompositeKeyMember(1) @SkipActivatableTracking
    private PmRoutine pmRoutine;

    @IsProperty @MapTo @Final @CompositeKeyMember(2)
    private Task task;

    @IsProperty @MapTo @Final @CompositeKeyMember(3)
    private AssetClass assetClass;
}
```

## Activatable Entities and Deactivation Dependencies

`ActivatableAbstractEntity` supports reference counting — the platform tracks how many other entities reference an activatable entity.
Use `@DeactivatableDependencies` to declare which dependent entities should be cascade-deactivated when the parent is deactivated.

```java
@DeactivatableDependencies({ PmXref.class, PmTask.class, PmConsumable.class, PmToolClass.class })
public class PmRoutine extends ActivatableAbstractEntity<String> { ... }
```

When deactivating a `PmRoutine`, the DAO should check `isDirty()` on the `active` property and cascade:
```java
if (pm.getProperty(PmRoutine_.active()).isDirty() && !pm.isActive()) {
    // deactivate dependent PmTasks, PmConsumables, etc.
}
```

## Validators and Definers

**`@BeforeChange(@Handler(ValidatorClass.class))`** — Validators (integrity constraints):
- Implements `IBeforeChangeEventHandler<T>`
- Returns `Result.failure()` / `Result.warning()` / `Result.informative()` / successful `Result`
- Executed only during property mutation phase
- Multiple validators chain in declaration order
- Validators can be parameterised via `@StrParam` and `@IntParam`:
  ```java
  @BeforeChange(@Handler(value = GreaterValidator.class, str = { @StrParam(name = "limit", value = "0") }))
  private BigDecimal daysDue;

  @BeforeChange(@Handler(value = IntegerRangeValidator.class, integer = {
      @IntParam(name = "rangeStart", value = 0), @IntParam(name = "rangeEnd", value = 24) }))
  private Integer cutoffHour;
  ```

**`@AfterChange(DefinerClass.class)`** — Definers (automatic value calculation):
- Implements `IAfterChangeEventHandler<T>`
- **Executes during database retrieval too** (unlike validators)
- Check `entity.isInitialising()` to distinguish DB load from user mutation
- Cannot reject values; runs after successful validation
- **Important:** Definer-initiated mutations go through `ObservableMutatorInterceptor` and trigger the full validation chain — they are **not** silent

**Validation Result Types:** Failure (rejects value), Warning (accepts + warning), Informative (accepts + info), Success (accepts silently)

## Union Entities (Polymorphic Associations)

Extend `AbstractUnionEntity`. Model polymorphic references where a property can reference different entity types.

**Constraints:**
- Only one union property can have a value at a time
- All properties must be entity types (unique, no duplicates)
- Must be created via companion `new_()`, never `new`

**Key methods:** `activeEntity()`, `setUnionProperty(entity)`, `isActivePropertyUnionMemberOf(OtherUnion.class)`

**Common properties** (exist on *all* members) can be accessed transparently through dot-notation in EQL, fetch models, and UI configurations — the platform auto-expands to each member.

**Interface contract pattern:** Union members implement a common interface; union entity delegates via `activeEntity()` cast.

**Cross-union conversion:** Companion default methods convert between overlapping union types using `isActivePropertyUnionMemberOf()` + `setUnionProperty()`.

**`@SkipActivatableTracking`:** Use on union members that should be allowed to become inactive while entities referencing the union remain active.

**Union `.id()` in EQL:** `union.id()` resolves to `CASE WHEN member1 IS NOT NULL THEN member1 WHEN member2 IS NOT NULL THEN member2 ... END` (not `COALESCE`).
Because TG uses contiguous entity IDs (globally unique across all tables), this is a collision-free scalar key usable in `groupBy`, `yield`, and JOIN conditions.
See @../eql-reference.md for examples.

## Entity Producer Pattern

Extend `DefaultEntityProducerWithContext<T>` for context-aware entity instantiation.

- Access master entity via `ofMasterEntity()` and selection criteria via `selectionCrit()`
- Set property editability and perform validation during creation
- Support `@Authorise` annotations

**Two usage patterns:**
- **Domain entity producers** — used in Web UI for Entity Masters/Centres to provide default values
- **Action entity producers** — used both in Web UI and programmatically in DAO methods

## Companion Object Pattern

**Naming:** Entity `Vehicle` → interface `VehicleCo` → implementation `VehicleDao`

| Method | Returns | Instrumented | Use Case |
|--------|---------|--------------|----------|
| `co(Type.class)` | `IEntityReader<T>` | No | Read-only queries, exports, lookups |
| `co$(Type.class)` | `IEntityDao<T>` | Yes | Full CRUD — create, update, save, delete |

**Critical:** `co$()` returns entities with full change tracking, validation, and meta-property support. `co()` returns lightweight, uninstrumented entities. Using the wrong one causes subtle bugs.

**Two-parameter `save()`:** `save(entity, Optional<fetch<T>>)` returns `Either<Long, T>`.
- `Optional.empty()` — entity is **not** refetched after persistence; returns `Either.left(id)` with just the ID. Use when the caller doesn't need the saved entity back.
- `Optional.of(fetchModel)` — entity is refetched with the provided fetch model; returns `Either.right(entity)`. Use when the caller needs the saved entity with specific properties populated.
```java
// No refetch — just persist and get the ID
final Either<Long, WorkOrder> result = super.save(wo, Optional.empty());
final Long savedId = result.left;

// Refetch with a custom fetch model
final Either<Long, WorkOrder> result = super.save(wo, Optional.of(customFetchModel));
final WorkOrder saved = result.right;
```

## Transaction Isolation for Batch Operations

For batch operations where each item must succeed or fail independently (e.g., generating PMs for multiple assets), use the wrapper + non-nested session pattern:

```java
/// Wrapper method — handles exceptions and returns Result for user feedback.
/// Does NOT have @SessionRequired, so it runs outside the caller's transaction.
@Override
public Result generateSinglePm(final PmXref pmXref) {
    try {
        return generateSinglePmInNonNestedSession(pmXref);
    } catch (final Exception ex) {
        return Result.failure(ex);
    }
}

/// Actual implementation — runs in its own transaction.
/// allowNestedScope = false ensures a separate transaction that rolls back independently on failure.
@SessionRequired(allowNestedScope = false)
protected IPm generateSinglePmInNonNestedSession(final PmXref pmXref) {
    // ... business logic that may throw
    return generatedPm;
}
```

**Why this matters:** Without `allowNestedScope = false`, the inner method would join the caller's transaction, and any exception would roll back the entire batch.
The wrapper catches exceptions so that failures for individual items don't prevent processing of remaining items.

## MetaProperty System

Access via `entity.getProperty(Entity_.propName())`. Key methods:

| Method | Purpose |
|--------|---------|
| `isDirty()` | Modified since last save |
| `isValid()` / `validationResult()` | Validation state |
| `setDomainValidationResult(result)` | Set result from definer/DAO |
| `isEditable()` / `setEditable(boolean)` | Control editability |
| `getValue()` / `getOriginalValue()` / `getPrevValue()` | Value tracking |
| `isChangedFromOriginal()` | Changed since entity retrieval |

## Calculated Properties

Read-only, database-computed fields defined with `@Calculated`.
Use a companion `protected static final ExpressionModel propertyName_` field with EQL's `expr()` API.

```java
// Simple dereferencing
protected static final ExpressionModel assetClass_ = expr().prop(Vehicle_.vehicleClass().assetClass()).model();

// Subquery returning an entity — use modelAsEntity()
protected static final ExpressionModel location_ = expr().model(
    select(LocationTimeline.class).where()
        .prop(LocationTimeline_.capable().vehicle()).eq().extProp(Vehicle_.id())
        // ...
        .yield().prop(LocationTimeline_.location()).modelAsEntity(Location.class)).model();

// Subquery returning a scalar — use modelAsPrimitive()
protected static final ExpressionModel numberOfTasks_ = expr()
    .ifNull().model(select(PmTask.class).where()
            .prop(PmTask_.pmRoutine()).eq().extProp(PmRoutine_.id()).and()
            .prop(PmTask_.active()).eq().val(true)
            .yield().countAll()
            .modelAsPrimitive())
    .then().val(0).model();

// Inline (trivial cases only)
@Calculated("price.amount + purchasePrice.amount")
private BigDecimal calc0;
```

**SQL expansion:** When a `@Calculated` property is used in aggregations (e.g., `sumOf().prop(cost)` where `cost = hours * rate`), EQL expands it to the underlying expression in SQL (e.g., `SUM(hours * rate)`).
The calculated property has no physical column — database covering indexes must include the expression's operand columns, not the calculated property itself.

## Synthetic / Report Entities

Non-persistent entities synthesised from queries over other entities.
Define a `protected static final EntityResultQueryModel<T> model_` field that provides the query.

```java
@DomainEntity
@WithMetaModel
@CompanionObject(WholeLifeCostCo.class)
public class WholeLifeCost extends AbstractEntity<String> {
    protected static final EntityResultQueryModel<WholeLifeCost> model_ = select(WorkOrderCost.class)
        .where().prop(WorkOrderCost_.workOrder().status()).ne().val(CANCELLED)
        .groupBy().prop(WorkOrderCost_.workOrder().asset())
        .yield().prop(WorkOrderCost_.workOrder().asset()).as(WholeLifeCost_.asset())
        .yield().sumOf().prop(WorkOrderCost_.actualTotal()).as(WholeLifeCost_.totalCost())
        .modelAsEntity(WholeLifeCost.class);

    @IsProperty @CritOnly(RANGE)
    private Date dateRangeCrit;

    @IsProperty @Readonly @Calculated
    private Money totalCost;
    // ...
}
```

Key characteristics:
- Annotated with `@DomainEntity` (for domain registration) and often `@WithMetaModel` (for metamodel generation)
- The `model_` field defines the base query; the platform applies criteria and fetch models on top
- Commonly use `@CritOnly` properties for filtering and `@Calculated` properties for derived values
- Not annotated with `@MapEntityTo` (not directly persisted)

## MetaModel Annotation Processor

`MetaModelProcessor` generates type-safe metamodel classes at compile time.

**Generated output** (in `target/generated-sources/`):
- `metamodels/MetaModels.java` — static fields for all entity metamodels
- `{package}/meta/{Entity}MetaModel.java` — individual metamodel
- `{package}/meta/{Entity}MetaModelAliased.java` — for self-joins

```java
import static metamodels.MetaModels.*;
.prop(Vehicle_.model())                              // Simple property
.prop(PmXref_.asset().equipment().avgReading())      // Deep chain
```