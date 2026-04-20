# Entity Model — Detailed Reference

Detailed patterns and advanced topics for the TG entity model.
For the hierarchy table, annotation lists, companion basics, calculated property syntax, synthetic entity pattern, and metamodel usage, see `quick-reference.md` in this directory.

## Annotation Details

- `@DomainEntity` — marks synthetic entities (union entities, report entities) for metamodel generation and domain registration.
- `@WithMetaModel` — explicitly enables metamodel generation for non-standard entities (e.g., action entities that need metamodel references).
- `@WithoutMetaModel` — explicitly disables compile-time metamodel generation for the annotated entity.
  Used on generated audit types.
- `@SupportsEntityExistsValidation` — enables entity existence validation for non-persistent entities used as criteria grouping properties.
- `@Audited` — marks a persistent entity as audited; the platform generates audit-entity types for it and automatically creates audit records on save.
  See `auditing/reference.md` for the full facility.
- `@DisableAuditing` — on a field of an `@Audited` entity, excludes the property from auditing.
  To opt `key` or `desc` out, redeclare them in the subclass first.

**`@IsProperty` parameters:** `@IsProperty(Long.class)` for collection element type, `@IsProperty(length = 8000)` for string length, `@IsProperty(assignBeforeSave = true)` for auto-assigned values.

**`MaxLengthValidator` is implicit for `String` properties.**
The platform automatically adds `MaxLengthValidator` based on `@IsProperty(length = ...)` — do not declare `@BeforeChange(@Handler(MaxLengthValidator.class))` manually; it is redundant.

**`@Dependent` parameters must use metamodel constants**, not string literals.
Import the constants from the entity's own `MetaModel` class:
```java
import static fielden.personnel.roster.meta.RosterProfileDayMetaModel.shiftFinish_;
import static fielden.personnel.roster.meta.RosterProfileDayMetaModel.shiftStart_;

@Dependent(shiftFinish_)
private Date shiftStart;

@Dependent(shiftStart_)
private Date shiftFinish;
```

**`@CritOnly`** — marks properties as criteria-only (used for filtering, not displayed as result columns):
```java
@CritOnly(RANGE)                                    // Date/numeric range filter
private Date dateRangeCrit;

@CritOnly(SINGLE)                                   // Single entity picker
private Location locationCrit;

@CritOnly(value = MULTI, mnemonics = WITHOUT)        // Multi-select with "without" mnemonic
private InventoryPart inventoryPartCrit;
```

**`@CritOnly` on persistent entities is an anti-pattern.**
A persistent (`@MapEntityTo`) entity models domain state; crit-only properties are filtering criteria for the UI and do not belong there.
Wrap the persistent entity in a synthetic `Re*` entity (inheriting from it or yielding over it) and host the crit-only properties on the synthetic wrapper — see *Synthetic / Report Entities* below.

**Exception — generative entities.**
*Generative entities* (`@MapEntityTo` + `implements WithCreatedByUser<T>`, driven by an `IGenerator<T>` at the Entity Centre `run` phase) legitimately host `@CritOnly` properties.
Their crit-only fields carry generation parameters that drive an on-the-fly computation; they never touch the schema.
See *Generative Entities* below.

### Declarative correlated filters (the `{propName}_` stem pattern)

For filters that correlate against cross-reference tables on Entity Centres, the model-driven style is `@CritOnly(entityUnderCondition = ..., propUnderCondition = ...)` paired with a companion `protected static final ICompoundCondition0<?> {propName}_` stem field on the synthetic entity.
This replaces the older approach of hand-wiring `.critCondition(...)` clauses inside the synthetic entity's `model_`, and it unlocks the full mnemonic matrix (`missing`, `not missing`, negation, empty/non-empty, per-value matching) uniformly for every criterion.

Two cooperating pieces are required on the synthetic entity:

1. The annotation on the `@CritOnly` property, with **both** `entityUnderCondition` and `propUnderCondition` supplied.
2. A companion static field named `{propName}_` holding the **stem** — a subquery "start" that scopes the xref lookup and anchors it to the outer row.

```java
@IsProperty
@CritOnly(value = MULTI,
          entityUnderCondition = Document2WorkActivityXref.class,
          propUnderCondition = Document2WorkActivityXrefMetaModel.workActivity_)
private WorkActivity workActivityCrit;

protected static final ICompoundCondition0<Document2WorkActivityXref> workActivityCrit_ =
    select(Document2WorkActivityXref.class).where()
        .prop(Document2WorkActivityXref_.document()).eq().prop(createConditionProperty(ReDocument_.id()));
```

**How the platform resolves it.**
At Entity Centre query-assembly time, `DynamicQueryBuilder.QueryProperty.findCritOnlySubmodelField` reflects on the entity class looking for a static field whose name is `{propertyName}_`.
If found — and the annotation hints are present and `propUnderCondition` actually resolves on `entityUnderCondition` — the stem is combined with the user-entered values into a real `.critCondition(collectionQueryStart, propName, critPropName)` clause in the final query.
If the criterion has no value and no mnemonic set, it is skipped entirely — nothing lands in the SQL.

**`createConditionProperty(...)` vs `extProp(...)`.**
The stem is built at class-init time, before any outer query exists, so it cannot use `extProp(...)` (which correlates against the *immediate enclosing* query).
Use `createConditionProperty(SyntheticEntity_.id())` from `DynamicQueryBuilder` instead — it resolves to the canonical alias that `DynamicQueryBuilder` always imposes around the managed type when building the Entity Centre query.
Application code references the helper, not the internal alias string — this is the supported encapsulation boundary.

**`entityUnderCondition` rule.**
It must be the source type the stem's `.critCondition(...)` operator iterates over — for a direct xref stem, the xref entity itself; for a *union stem* (multiple xref tables yielding the same type), the **yielded type**, not either underlying table.

**Union stem idiom.**
When one criterion must span two or more xref sources yielding the same type, compose them via `select(query1, query2).where().condition(emptyCondition())`.
The trailing `.where().condition(emptyCondition())` is the no-op continuation that exposes the required `ICompoundCondition0<YieldedType>`:

```java
// ReDocument.locationCrit spans both Document2LocationSubsystemXref and Document2LocationXref,
// both yielding Location.
private static ICompoundCondition0<Location> makeUnifiedLocationModel() {
    final var query1 = select(Document2LocationSubsystemXref.class).where()
            .prop(Document2LocationSubsystemXref_.document()).eq().prop(createConditionProperty(ReDocument_.id()))
            .yield().prop(Document2LocationSubsystemXref_.locationSubsystem().location())
            .modelAsEntity(Location.class);
    final var query2 = select(Document2LocationXref.class).where()
            .prop(Document2LocationXref_.document()).eq().prop(createConditionProperty(ReDocument_.id()))
            .yield().prop(Document2LocationXref_.location())
            .modelAsEntity(Location.class);
    return select(query1, query2).where().condition(emptyCondition());
}

@IsProperty
@CritOnly(value = MULTI, entityUnderCondition = Location.class, propUnderCondition = LocationMetaModel.key_)
private Location locationCrit;
protected static final ICompoundCondition0<Location> locationCrit_ = makeUnifiedLocationModel();
```

**Silent-typo gotcha.**
If `propUnderCondition` does not resolve to a real property on `entityUnderCondition`, `findCritOnlySubmodelField` silently skips the stem and the criterion is disabled — there is no runtime error.
Use metamodel constants for every path segment (e.g., `Document2LocationSubsystemXrefMetaModel.locationSubsystem_ + "." + LocationSubsystemMetaModel.subsystem_`) — compile-time safety for each segment closes most of the gap.
Invalid *chaining* across segments (each segment valid on its own, but the chain not forming a real path on `entityUnderCondition`) must still be caught by interactive testing.

**Mnemonic matrix.**
Because the resolved clause routes through the `.critCondition(collectionQueryStart, propName, critPropName)` overload, every declarative criterion automatically supports the full v/n/m matrix — values × negation × mnemonics — documented on `EntityQueryProgressiveInterfaces.critCondition`.

**See also.** For a "when to use which" between this declarative style and `IQueryEnhancer`, see *Query Enhancer Pattern* in `web-ui/reference.md`.
For testing such criteria without an actual Entity Centre, see *Testing Entity Centre criteria via `DynamicQueryBuilder`* in `testing/reference.md`.

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

**`.getKey()` on composite-key entities returns `DynamicEntityKey`, not a scalar.**
For an entity with `@KeyType(DynamicEntityKey.class)`, `getKey()` returns the composite-key object whose `toString()` joins the members with the key-member separator.
It is **not** interchangeable with any single member's value, even when there is only one composite member.
In code — and especially in test assertions — compare against the named member accessor of the composite-key property (e.g., `Priority.getValue()` for `@CompositeKeyMember(1) Integer value`, `Severity.getCode()` for `@CompositeKeyMember(1) String code`), not `getKey()`.
Only entities declared with `@KeyType(String.class)` / `@KeyType(Long.class)` / etc. can have their `getKey()` compared directly to a scalar.

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

**`@SkipEntityExistsValidation(skipActiveOnly = true)` for references to activatable entities.**
When an entity has a property referencing an activatable entity, the standard `EntityExistsValidator` rejects inactive values.
If the referencing entity must accept inactive values for that property (e.g., to populate dependent data before activating the referenced entity), use `@SkipEntityExistsValidation(skipActiveOnly = true)`:
```java
@IsProperty
@SkipEntityExistsValidation(skipActiveOnly = true)
private RosterProfile rosterProfile;
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

**Result factory conventions:**
- Use `Result.successful()` (no-arg) — not `Result.successful(newValue)`.
- Use `Result.warningf(format, args...)` — not `Result.warning(format.formatted(args...))`.

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
See `eql/reference.md` for examples.

## Entity Producer Pattern

Extend `DefaultEntityProducerWithContext<T>` for context-aware entity instantiation.

- Access master entity via `ofMasterEntity()` and selection criteria via `selectionCrit()`
- Set property editability and perform validation during creation
- Support `@Authorise` annotations

**Two usage patterns:**
- **Domain entity producers** — used in Web UI for Entity Masters/Centres to provide default values
- **Action entity producers** — used both in Web UI and programmatically in DAO methods

## Companion Object Pattern — Advanced

For basic `co()` vs `co$()` and `FETCH_MODEL` usage, see `quick-reference.md` in this directory.

**`FETCH_PROVIDER` on companion interfaces.**
Each companion interface declares `IFetchProvider<Entity> FETCH_PROVIDER` — the fetch model required for editing.
To change any property of an entity, it must be fetched with this fetch model.
It includes all properties accessed in validators and definers.
The corresponding DAO class should use `FETCH_PROVIDER` to implement `createFetchProvider()`.

**Why `FETCH_PROVIDER` matters over `fetchAll*`.**
A companion's `FETCH_PROVIDER` / `FETCH_MODEL` enumerates exactly the properties — including the nested paths — that the DAO's save-time logic, validators, and definers touch.
Retrieving with a narrower fetch (e.g., `findByKey` without an explicit fetch) or with a flat `fetchAll*` / `fetchAllInclCalc*` (which does not follow dot-notation into nested references) leaves deeper properties unfetched; any later access on the save path raises `StrictProxyException`.
The rule applies everywhere an entity is loaded for mutation: tests, DAO code, producers.

**`ISaveWithFetch<T>`.**
Prefer `ISaveWithFetch<T>` on companion interfaces.
The DAO then overrides the two-parameter `save(entity, maybeFetch)` instead of the one-parameter `save(entity)`.

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

## Canonical `batchDelete` Pattern

Custom deletion logic goes in `batchDelete(Collection<Long>)`.
The `batchDelete(List<T>)` overload calls `defaultBatchDelete(entities)`, which delegates to the ID-based overload — so custom logic runs for both paths.
Use `Result.ifFailure(Result::throwRuntime)` to throw on validation failure:
```java
@Override @SessionRequired @Authorise(Entity_CanDelete_Token.class)
public int batchDelete(final Collection<Long> entitiesIds) {
    validateSomething(entitiesIds).ifFailure(Result::throwRuntime);
    return defaultBatchDelete(entitiesIds);
}
```

Only override `batchDelete(Collection<Long>)` — skip `batchDelete(List<T>)`.
The generic delete action in Entity Centres invokes the `Collection<Long>` overload, and having both overrides tends to confuse developers without adding behaviour.

The original intent for `batchDelete(List<T>)` was a marginal convenience in cases where a list of entities is present.
This method will likely be deprecated.
If you must override it, simply invoke `defaultBatchDelete(entities)` as per the example below:
```java
@Override @SessionRequired @Authorise(Entity_CanDelete_Token.class)
public int batchDelete(final List<Entity> entities) {
    return defaultBatchDelete(entities); // delegates to batchDelete(Collection<Long>)
}
```

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

## Calculated Properties — Subquery Patterns

For basic `@Calculated` syntax and `ExpressionModel` usage, see `quick-reference.md` in this directory.

```java
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

// Simple dereferencing
protected static final ExpressionModel assetClass_ = expr().prop(Vehicle_.vehicleClass().assetClass()).model();

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

**Wrapping a persistent entity as a synthetic `Re*`.**
A common use of synthetic entities is to host crit-only filters that would otherwise pollute a persistent entity.
The wrapper typically inherits from the persistent entity and yields over it (`select(PersistentEntity.class).yieldAll().modelAsEntity(ReEntity.class)`), then adds `@CritOnly` properties with the declarative stem pattern (see *Declarative correlated filters* above).
This is the preferred remedy when a persistent entity otherwise carries `@CritOnly` properties — **except** for generative entities, which have their own pattern (see below).

## Generative Entities

A **generative entity** is a persistent entity whose rows are computed ad hoc at Entity Centre `run` phase rather than maintained by normal CRUD.
The user enters selection criteria; when the centre is run, a data generator clears any previously generated rows for the current user, computes new ones from the criteria, persists them, and the centre then queries the persistent table as usual.

**Typical use cases** (from `IGenerator`'s javadoc):
- Analysis / reporting where the data must be derived on demand from user-supplied parameters (date ranges, grouping choices, filters).
- "Wizard" flows where a later step operates over data computed from earlier selection criteria.

### Anatomy

**Entity side.** A generative entity is persistent, implements `WithCreatedByUser<T>`, and mixes two kinds of fields: `@MapTo` fields holding the *shape of the generated data*, and `@CritOnly` fields holding the *generation parameters*.
The composite key conventionally places `createdBy` as the first member so rows are naturally scoped per user.

```java
import ua.com.fielden.platform.data.generator.WithCreatedByUser;

@EntityTitle("Reliability Analysis")
@KeyType(DynamicEntityKey.class)
@CompanionObject(IReReliability.class)
@MapEntityTo
public class ReReliability extends AbstractPersistentEntity<DynamicEntityKey>
                           implements WithCreatedByUser<ReReliability> {

    // Composite key member 1: the user for whom the rows were generated.
    @IsProperty(assignBeforeSave = true)
    @MapTo
    @CompositeKeyMember(1)
    @SkipEntityExistsValidation    // assigned by the platform at save time
    private User createdBy;

    @IsProperty @MapTo @CompositeKeyMember(2)
    private String group;

    // ... more @MapTo fields describing the generated-data shape:
    // MTBF, MTTR, MDT, waCount, etc.

    // Selection-criteria fields — drive generation, never persisted.
    @IsProperty @CritOnly(SINGLE) @DateOnly
    private Date dateFrom;

    @IsProperty @CritOnly(MULTI)
    private Service serviceCrit;
    // ...
}
```

**Marker interface.** `WithCreatedByUser<T>` (in `ua.com.fielden.platform.data.generator`) is a minimal type-safety marker declaring a `User getCreatedBy()` contract.
Implementing it signals to the platform that the entity's rows are per-user and that a `createdBy:User` property exists for scoping.

**Generator side.** The generator implements `IGenerator<T extends AbstractEntity<?> & WithCreatedByUser<T>>`.
**Important:** the `IGenerator<T>` contract is almost always inherited via the companion interface, not declared on the DAO class directly:

```java
public interface IReReliability extends IEntityDao<ReReliability>, IGenerator<ReReliability> {}

@EntityType(ReReliability.class)
public class ReReliabilityDao extends CommonEntityDao<ReReliability> implements IReReliability {

    @Override @SessionRequired
    public Result gen(final Class<ReReliability> type, final Map<String, Optional<?>> params) {
        // 1. Remove any previously generated rows for the current user.
        // 2. Read selection-criteria values from params.
        // 3. Compute the analysis and persist new rows.
        return successful("Generated.");
    }
}
```

The generator is responsible for both **clearing** this user's previously generated rows and **writing** fresh ones.
`@SessionRequired` is expected (per the javadoc of `IGenerator`) so clear + insert happen in a single transaction.

**Centre side.** The Entity Centre config registers the generator via `.withGenerator(entityTypeToBeGenerated, generatorType)` on the criteria-layout stage (see `ILayoutConfigWithResultsetSupport.withGenerator`):

```java
EntityCentreBuilder.centreFor(ReReliability.class)
    .addCrit(ReReliability_.dateFrom()).asSingle().date().also()
    .addCrit(ReReliability_.serviceCrit()).asMulti().autocompleter(Service.class)
    // ... more criteria ...
    .setLayoutFor(Device.DESKTOP, ...)
    .withGenerator(ReReliability.class, ReReliabilityDao.class)   // wire the generator
    // ... result set definition ...
    .build();
```

### Runtime flow

1. User populates selection criteria on the centre and clicks *Run*.
2. The centre's `run` phase locates the registered `IGenerator` (resolved from the Guice injector via the generator class passed to `.withGenerator`) and invokes `gen(Class, params)`.
   `params` is a `Map<String, Optional<?>>` whose keys follow the `<entityTypeCamelCase>_<criteriaPropertyName>` convention (e.g., `leaveRequest_payrollCodeCrit`), and whose values are the criteria values wrapped in `Optional`.
3. The generator clears any previously generated rows for the current user, computes the analysis from the criteria, and persists new rows to the entity's table.
4. The centre then queries the persistent table as for any other entity.
   Rows are automatically filtered to the current user because `createdBy` is a composite-key member and the centre's query includes the standard per-user restriction.

**Forced regeneration.** `IGenerator.shouldForceRegeneration(params)` returns `true` when the params map contains the key `IGenerator.FORCE_REGENERATION_KEY` (`"@@forceRegeneration"`).
Use this when the generator should regenerate even if it would otherwise consider the data up-to-date.

### `@CritOnly` on generative entities is legitimate

Generative entities are the **sole exemption** from the "no `@CritOnly` on persistent entities" anti-pattern.
Their `@CritOnly` fields carry user-entered parameters that flow from the centre to the generator and are discarded — they are not `@MapTo` and never touch the schema.
The class deliberately co-locates the *shape of the generated data* (the `@MapTo` fields) and the *parameters that drive generation* (the `@CritOnly` fields) because both are facets of the same analysis.

When auditing a codebase for the `@CritOnly`-on-persistent-entities anti-pattern, `implements WithCreatedByUser<T>` on the entity itself is a cheap and definitive marker for "exempt" — no need to trace companion-interface inheritance or DAO generator implementations.

### Relationship to synthetic / report entities

Generative entities and synthetic entities both present computed data via Entity Centres but differ structurally:

| | Synthetic / report | Generative |
|---|---|---|
| `@MapEntityTo` | No | Yes |
| `model_` / `models_` | Required (defines the base EQL query) | Not used — the centre queries the persistent table directly after `gen()` |
| Where the data comes from | An EQL query composed on the fly | Rows persisted by `IGenerator.gen(...)` during the centre's `run` phase |
| `@CritOnly` status | Normal rules apply (declarative stems etc.) | Legitimate as generation parameters |
| `WithCreatedByUser` | Not required | Required (enables per-user row scoping) |

Choose the synthetic pattern when the computation can be expressed purely as a query over existing tables.
Choose the generative pattern when the computation is too complex or iterative to express as EQL, or when it involves side effects that must be materialised before the centre queries.

## Audited Entities

Add `@Audited` to a persistent entity to enable automatic audit-record creation on save.
Use `@DisableAuditing` on individual fields to exclude them from auditing.

For the full auditing facility — generated types, versioning, runtime plumbing (`IAuditTypeFinder`, `ICompanionGenerator`), auditing modes, source generation (`GenAudit`), and Web UI integration — see `auditing/reference.md`.
