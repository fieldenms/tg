# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository as well as for TG-based applications.

## Overview

Trident Genesis (TG) is a comprehensive enterprise application platform that implements the RESTful Objects architectural pattern.
It provides a Domain-Driven Design framework with a rich domain model, sophisticated query language (EQL), and complete application stack from data persistence to web UI.

## Common Development Commands

### Building the Project
```bash
# Clean build
mvn clean install -Dmaven.javadoc.skip=true -DdatabaseUri.prefix=//localhost:5432/ci_ -Dfork.count=4

# Build without tests
mvn clean install -DskipTests -DdatabaseUri.prefix=//localhost:5432/ci_ -Dfork.count=4

# Build specific module
mvn clean install -pl platform-pojo-bl -am

# Deploy to repository
mvn clean deploy
```

### Running Tests
```bash
# Run all tests against local PostgreSQL instance
mvn clean test -Dmaven.javadoc.skip=true -Dfork.count=4 -DdatabaseUri.prefix=//localhost:5432/ci_

# Run all tests against local SQL Server instance
mvn test -Dmaven.javadoc.skip=true -Dfork.count=4 -DdatabaseUri.prefix=//localhost:1433;encrypt=true;trustServerCertificate=true;sendStringParametersAsUnicode=false;databaseName=ci_
```

### Version Management

TG provides a convenient script `tg-update-version.sh` for updating the platform version across all modules:

```bash
# Update version using the script (recommended)
./tg-update-version.sh 2.1.0-SNAPSHOT

# Or manually using Maven commands
mvn versions:set -DnewVersion=2.1.0-SNAPSHOT -DprocessAllModules=true -DgenerateBackupPoms=false
mvn versions:commit -DgenerateBackupPoms=false
```

The script:
- Validates that exactly one argument (new version) is provided
- Updates all module versions consistently using Maven Versions plugin
- Provides colored output for better visibility (errors, success, warnings, info)
- Automatically commits the version changes without creating backup POMs
- Exits with appropriate error messages if the update fails

### Releasing project

TG platform follows [semantic versioning](https://semver.org/) (MAJOR.MINOR.PATCH) for releases.
The `tg-release.sh` script automates the entire release process following Git Flow workflow:

```bash
# Release a new version
./tg-release.sh 2.1.0 2.1.1-SNAPSHOT '//localhost:5432/ci_' 4 develop

# Parameters:
# 1. release-version: The version being released (e.g., 2.1.0)
# 2. next-development-version: Next SNAPSHOT version (e.g., 2.1.1-SNAPSHOT)
# 3. database-uri-prefix: Database connection for tests during deployment
# 4. fork-count: Number of parallel test forks
# 5. base-branch: Development branch to release from (typically 'develop')
```

**Release Process Steps:**

1. **Pre-release verification**: Confirms parameters with user before proceeding
2. **Create release branch**: Creates `release-{version}` branch from base branch
3. **Update to release version**: Sets all module versions to release version
4. **Merge to master**: Merges release branch into master with no-ff merge
5. **Tag release**: Creates annotated tag for the release version
6. **Build and deploy**: Runs full build with tests and deploys to Maven repository
7. **Merge back**: Merges release branch back to base branch
8. **Update to next version**: Sets versions to next development SNAPSHOT
9. **Cleanup**: Deletes local release branch
10. **Push to remote**: Pushes all changes (base branch, master, tags) to origin

**Important Notes:**

- The script includes interactive pauses at critical steps for verification
- Automatic rollback (`abort_release`) if any step fails, including:
  - Branch cleanup
  - Tag removal
  - Return to original branch
- Requires appropriate Maven repository deployment credentials
- Requires Git push privileges for master branch and tags
- All tests must pass during the deployment step

**Version Conventions:**

- Release versions: `MAJOR.MINOR.PATCH` (e.g., `2.1.0`)
- Development versions: `MAJOR.MINOR.PATCH-SNAPSHOT` (e.g., `2.1.1-SNAPSHOT`)
- Follows semantic versioning:
  - MAJOR: Incompatible API changes
  - MINOR: Backwards-compatible functionality additions
  - PATCH: Backwards-compatible bug fixes

## High-Level Architecture

### Module Structure

The platform targets **Java 25** and consists of several key modules:

1. **platform-annotations** - Core annotations for entity definition
2. **platform-annotation-processors** - Compile-time annotation processing and metamodel generation
3. **platform-annotation-processors-test** - Tests for annotation processors
4. **platform-pojo-bl** - Business logic layer with domain model foundation
5. **platform-dao** - Data access layer with EQL, Hibernate, and GraphQL Web API
6. **platform-web-resources** - REST API layer with web resources
7. **platform-web-ui** - Web UI framework with Entity Centre and Master patterns
8. **platform-db-evolution** - Database migration and evolution tools
9. **platform-eql-grammar** - ANTLR-based EQL parser and compiler
10. **platform-benchmark** - Performance benchmarking tools

Comprehensive LaTeX documentation is available in `platform-doc/` (developer's guide, architecture overview, security model).

### Core Architectural Patterns

#### Entity Definition Language (EDL)
All domain entities extend `AbstractEntity` and use annotations for configuration:
- `@MapEntityTo` - ORM mapping configuration for persistent entity.
- `@KeyType` - Defines entity business key
- `@IsProperty` - Declares entity properties
- `@MapTo` - ORM mapping configuration for persistent entity properties.
- `@CompanionObject` - Links to companion object for CRUD operations
- `@Calculated` - Computed properties (see [Calculated Properties](#calculated-properties) below)
- `@Observable` - Change tracking (required on all property setters)

**Additional Property Annotations (from real-world usage):**
- `@Dependent(prop1, prop2, ...)` - Declares property dependencies for UI refreshing
- `@Final` - Property value cannot be changed after initial setting
- `@Required` - Property must have a value
- `@Readonly` - Property cannot be edited by users
- `@UpperCase` - Automatically converts string values to uppercase
- `@EntityTitle(value, desc)` - Provides entity-level title and description
- `@DisplayDescription` - Shows entity description in UI
- `@DescRequired` - Entity description is mandatory
- `@DescTitle(value, desc)` - Customizes description field title
- `@Subtitles(@PathTitle(path, title))` - Displays related entity properties as subtitles
- `@SkipEntityExistsValidation(skipActiveOnly, skipNew)` - Controls entity existence validation
- `@LeProperty(prop)` / `@GeProperty(prop)` - Less/Greater than or equal property constraints

**Property Validation and Business Logic:**
- `@BeforeChange(@Handler(ValidatorClass.class))` - Validators (integrity constraints)
  - Implements `IBeforeChangeEventHandler<T>` interface
  - Validates property values before they are set
  - Returns `Result.failure()` to reject invalid values
  - Returns `Result.warning()` for acceptable values with warnings
  - Returns `Result.informative()` for acceptable values with informational messages
  - Can access entity companions for complex validations
  - Executed during property mutation phase

- `@AfterChange(DefinerClass.class)` - Definers (automatic value calculation)
  - Implements `IAfterChangeEventHandler<T>` interface
  - Executes after property value is successfully set
  - Used to automatically calculate/update dependent properties
  - Can set domain validation results with `prop.setDomainValidationResult()`
  - **Executes during database retrieval** (unlike validators which only run during user/business logic mutations)
  - Often checks `entity.isInitialising()` to differentiate between:
    - **Initialising phase**: Entity being loaded from database
    - **Mutation phase**: User or business logic setting property values
  - Cannot reject values (runs after successful validation)
  - **Important:** When a definer sets a value on the same or another property via its setter, the setter call goes through `ObservableMutatorInterceptor` and triggers the full validation chain for that property.
    Definer-initiated mutations are therefore **not** silent — they undergo the same validation as any other property mutation.

**Validation Result Types:**
- **Failure**: Rejects the value, property remains unchanged
- **Warning**: Accepts the value but displays a warning message
- **Informative**: Accepts the value with an informational message
- **Success**: Accepts the value without any messages

**Common Patterns:**
```java
// Validator example
@BeforeChange(@Handler(DateValidator.class))
private Date dateProp;

// Definer example
@AfterChange(TgPersistentEntityWithPropertiesEntityPropDefiner.class)
private TgPersistentEntityWithProperties entityProp;

// Combined validation and defining
@BeforeChange(@Handler(EntityValidator.class))
@AfterChange(EntityPropDefiner.class)
private SomeEntity someProp;
```

Other common ancestors for domain entities are:
- `AbstractPersistentEntity` -- adds 6 common properties: `createdBy`, `createdDate`, `createdTransactionGuid`, `lastUpdatedBy`, `lastUpdatedDate`, and `lastUpdatedTransactionGuid`.
- `ActivatableAbstractEntity` -- introduces property `active` for modelling activatable entities;
   activatable entities are used where their values should remain persisted and referenced, but should not be used when creating new data.
- `AbstractFunctionalEntityWithCentreContext` -- a base class for action entities (used to be called functional entities), which represent an action;
  generally speaking action entities are not persistent (do not get saved into a database);
  Method `save` for their companion objects "executes" the action, where an instance of an action entity is passed with all the relevant properties populated.

Entity types that are annotated with `@MapEntityTo` represent persistent entities.

#### Union Entities (Polymorphic Associations)

Union entities model situations where a property can reference different entity types (polymorphic association).
They extend `AbstractUnionEntity` and provide type-safe polymorphic references.

**Key Characteristics:**
- Only one union property can have a value at any time (the union constraint — attempting to set a second property throws an exception).
- All properties must be entity types (no primitive/ordinary types allowed).
- Each property must be of a unique entity type (no duplicates).
- The active property determines the union's `id`, `key`, and `desc`.
- Union entities have `@KeyType(String.class)` by default.
- Union entities are synthetic (not directly persisted) — EQL expands union properties into individual member columns.

**Interface Contract Pattern:**
Union members typically implement a common interface that defines shared behaviour.
The union entity then provides delegation methods that cast `activeEntity()` to that interface.

```java
/// A contract for maintenance-related properties common to all members of MaintenanceCapable.
public interface IMaintainable<M extends IMaintainable<M>> {
    Workshop getWorkshop();
    Ownership getOwnership();
}

@DomainEntity
@CompanionObject(MaintenanceCapableCo.class)
public class MaintenanceCapable extends AbstractUnionEntity {

    @IsProperty @MapTo
    private Equipment equipment;       // implements IMaintainable

    @IsProperty @MapTo
    private Building building;         // implements IMaintainable

    @IsProperty @MapTo
    @SkipActivatableTracking           // allows Tool to be deactivated while WorkOrder remains active
    private Tool tool;                 // implements IMaintainable

    @IsProperty @MapTo
    private Vehicle vehicle;           // implements IMaintainable

    @IsProperty @MapTo
    @SkipActivatableTracking           // allows Rotable to be deactivated while WorkOrder remains active
    private Rotable rotable;           // implements IMaintainable

    // Standard setters/getters with @Observable ...

    /// Delegates to the active member's IMaintainable contract.
    public Workshop workshop() {
        if (activeEntity() instanceof final IMaintainable<?> m) {
            return m.getWorkshop();
        }
        throw new IllegalStateException("All union members must implement IMaintainable.");
    }

    public Ownership ownership() {
        if (activeEntity() instanceof final IMaintainable<?> m) {
            return m.getOwnership();
        }
        throw new IllegalStateException("All union members must implement IMaintainable.");
    }
}
```

**Instantiation — Always Use Companion `new_()`:**
Union entities must be created through the companion object, never via `new`.

```java
// Create a MaintenanceCapable for a specific Equipment
final var asset = co$(MaintenanceCapable.class).new_().setEquipment(equipment);

// setUnionProperty() automatically finds the matching property by type
final var asset = co$(MaintenanceCapable.class).new_().setUnionProperty(equipment);
```

**Key Methods on `AbstractUnionEntity`:**
- `activeEntity()` — returns the non-null union member value.
- `setUnionProperty(entity)` — automatically assigns to the correct property by entity type.
- `isActivePropertyUnionMemberOf(OtherUnion.class)` — checks whether the active member type also exists as a member in another union entity.

**Cross-Union Conversion Pattern:**
When multiple union entities share some member types, companion default methods provide safe conversion.

```java
public interface MeterCapableCo extends IEntityDao<MeterCapable> {

    /// Converts a MaintenanceCapable to MeterCapable if the active member is meter-capable.
    default Optional<MeterCapable> asMeterCapable(final MaintenanceCapable maintenanceCapable) {
        return maintenanceCapable.isActivePropertyUnionMemberOf(MeterCapable.class)
                ? of(new_().setUnionProperty(maintenanceCapable.activeEntity()))
                : empty();
    }
}
```

**Union Entities in Validators and Definers:**
Validators use `switch` on `activeEntity()` for type-specific validation per member.
Definers use `isActivePropertyUnionMemberOf()` for capability testing and cross-union conversion.

```java
// Validator — type-specific validation via switch
switch (asset.activeEntity()) {
    case final Rotable it   -> validateRotable(it);
    case final Equipment it -> validateEquipment(it);
    case final Vehicle it   -> validateVehicle(it);
    case final Tool it      -> validateTool(it);
    case final Building it  -> validateBuilding(it);
}

// Definer — capability testing and cross-union conversion
if (asset.isActivePropertyUnionMemberOf(MeterCapable.class)) {
    coMeterCapable.asMeterCapable(asset).ifPresent(meterCapable -> {
        // use meterCapable for meter-related logic
    });
}
```

**Union Entities in Producers:**
Producers create union entities based on the master entity context, then lock editability.

```java
@Override
protected PmXref provideDefaultValuesForStandardNew(final PmXref entity, final EntityNewAction masterEntity) {
    if (ofMasterEntity().keyOfMasterEntityInstanceOf(Equipment.class)) {
        final var equipment = refetch(ofMasterEntity().keyOfMasterEntity(Equipment.class));
        entity.setAsset(co$(MaintenanceCapable.class).new_().setEquipment(equipment));
        entity.getProperty(PmXref_.asset()).setEditable(false);
    }
    return entity;
}
```

**`@SkipActivatableTracking` on Union Members:**
By default, setting an activatable entity as a union member increments its reference count.
Use `@SkipActivatableTracking` when the union member should be allowed to become inactive while entities referencing the union remain active (e.g., a Tool can be deactivated while its WorkOrder stays active).

**Common Properties:**
Properties that exist on *all* union members are called *common properties*.
The platform detects these automatically via `AbstractUnionEntity.commonProperties(unionType)` (intersection of real properties across all member types).

Common properties are significant because they can be accessed transparently through the union entity using dot-notation — in EQL queries, fetch models, and Entity Centre configurations — without knowing which member is active.
The platform automatically expands such access to each union member behind the scenes.

For example, if all members of `MaintenanceCapable` have an `ownership` property, it can be used as a path through the union:

```java
// In Entity Centre configuration — dot-notation through the union's common property
.addCrit(WorkOrder_.asset().ownership()).asMulti().autocompleter(Ownership.class)
.addProp(WorkOrder_.asset().ownership()).minWidth(96)

// In EQL — querying through common properties
select(WorkOrder.class).where().prop(WorkOrder_.asset().ownership()).eq().val(someOwnership).model()
```

The platform handles common properties specially in several places:
- **Fetch models** (`FetchProvider`): when a common property is included, it is automatically expanded to `member.commonProp` for each union member.
- **Entity retrieval** (`EntityRetrievalModel`): common properties are not proxied on the union entity itself; instead, access is resolved through the active member.
- **Serialisation** (`EntitySerialiser`): common properties are included in type metadata sent to the client for UI rendering.

Note: the interface delegation methods like `workshop()` and `ownership()` on the union entity itself (shown earlier) are a complementary pattern for *programmatic* access in business logic.
Common properties as described here enable *declarative* access through dot-notation paths in EQL and UI configurations.

**Common Use Cases:**
- **Capability grouping** — grouping entity types that share a capability (e.g., all assets that can have maintenance work orders, or all assets that can have meter readings).
- **Polymorphic references** — a single property (e.g., `asset` on WorkOrder) that can reference different entity types without multiple nullable foreign keys.
- **Capability subsetting** — narrower union entities (MeterCapable) as subsets of broader ones (MaintenanceCapable), with conversion methods between them.

#### Entity Producer Pattern (`IEntityProducer`)

Entity producers provide context-aware instantiation of entities with proper initialization and defaults.

**Usage Patterns:**
- **Domain Entity Producers**: Used exclusively in Web UI configurations for Entity Masters/Centres
  - Provide default values based on master entity context
  - Example: `RotableEoProducer` sets defaults when creating from an embedded centre
- **Action Entity Producers**: Used both in Web UI and programmatically in business logic
  - Initialize action entities with complex validation and context
  - Can be instantiated programmatically for workflow operations
  - Example: `WorkActivityAffectedServicesUpdaterProducer` used in DAO methods

**Key Characteristics:**
- Extend `DefaultEntityProducerWithContext<T>` for context awareness
- Access master entity and selection criteria via `ofMasterEntity()` and `selectionCrit()`
- Can set property editability and perform validation during creation
- Support authorization checks with `@Authorise` annotations

**Example Implementation:**
```java
public class RotableEoProducer extends DefaultEntityProducerWithContext<RotableEo> {
    @Override
    protected RotableEo provideDefaultValuesForStandardNew(RotableEo entity, EntityNewAction masterEntity) {
        if (ofMasterEntity().keyOfMasterEntityInstanceOf(Eo.class)) {
            entity.setEo(refetch(ofMasterEntity().keyOfMasterEntity(Eo.class)));
            entity.getProperty("eo").setEditable(false);
        }
        return entity;
    }
}
```

#### Companion Object Pattern

Every entity has a companion object (Co class) that provides type-safe CRUD operations, query execution, business logic encapsulation, and transaction management.

**Naming Convention:** Entity `Vehicle` → interface `VehicleCo` → implementation `VehicleDao`

**Two Companion Access Methods:**

| Method | Returns | Instrumented | Use Case |
|--------|---------|--------------|----------|
| `co(Type.class)` | `IEntityReader<T>` | No | Read-only queries, exports, lookups |
| `co$(Type.class)` | `IEntityDao<T>` | Yes | Full CRUD — create, update, save, delete |

The critical difference is **entity instrumentation**: `co$()` returns entities with full change tracking, validation (@BeforeChange/@AfterChange), and meta-property support.
`co()` returns lightweight, uninstrumented entities suitable for read-only use.

**IEntityReader API** (available via both `co()` and `co$()`):
```java
IEntityReader<Vehicle> reader = co(Vehicle.class);

reader.findById(123L);                                  // Find by ID
reader.findByKey("VEH-001");                            // Find by business key
reader.findByKeyAndFetch(fetch(Vehicle.class), "VEH-001"); // Find with fetch model
reader.entityExists(entity);                            // Check existence
reader.entityWithKeyExists("VEH-001");                  // Check key existence
reader.count(queryModel);                               // Count matching entities

// Query multiple entities
reader.getAllEntities(from(query).with(fetchModel).model());
reader.getPage(from(query).model(), 0, 25);             // Paginated results

// Stream entities (must be used with try-with-resources)
try (Stream<Vehicle> stream = reader.stream(from(query).model())) {
    stream.forEach(v -> process(v));
}
```

**IEntityDao API** (additional methods via `co$()`):
```java
IEntityDao<Vehicle> dao = co$(Vehicle.class);

dao.save(entity);                                       // Save (insert or update)
dao.delete(entity);                                     // Delete single entity
dao.batchDelete(Collection<Long> ids);                  // Delete by IDs
dao.new_();                                             // Create new entity instance
```

**Usage in DAO implementations:**
```java
// Within a DAO class, co() and co$() are instance methods:
@Override
@SessionRequired
@Authorise(Vehicle_CanSave_Token.class)
public Vehicle save(Vehicle entity) {
    // Read-only lookup (uninstrumented, no overhead)
    final Station station = co(Station.class).findByKey("MAIN");

    // Fetch for modification (instrumented, supports save)
    final VehicleType type = co$(VehicleType.class).findById(entity.getType().getId());

    return super.save(entity);
}
```

#### Entity Query Language (EQL)

Type-safe fluent query language with ANTLR grammar:
- Located in `platform-eql-grammar/src/main/antlr4/EQL.g4`
- Multi-stage compilation (EqlStage0-3) for optimization
- Entry points and utilities in `EntityQueryUtils` (static import)

**Static Imports:**
```java
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static metamodels.MetaModels.*;  // Generated metamodel references
```

**Metamodel References:**
Always prefer type-safe metamodel references over string literals for property access.
Metamodel classes are generated at compile time by `MetaModelProcessor` and provide `Entity_.property()` accessors:
```java
// Prefer metamodel references:
.prop(Vehicle_.model())                              // Type-safe, refactor-friendly
.prop(PmXref_.asset().equipment().avgReading())      // Deep property chains

// Avoid string literals:
.prop("model")                                       // Fragile, no compile-time checking
.prop("asset.equipment.avgReading")                  // Error-prone
```

**Query Construction Phases:**

1. **SELECT** — entry point:
   ```java
   select(Vehicle.class)                    // From entity type
   select(select(...).model())              // From subquery
   select()                                 // For standalone aggregates
   ```

2. **JOIN** — optional joins:
   ```java
   select(Vehicle.class)
       .join(Employee.class).as("e").on().prop("owner").eq().prop("e.id")
       .leftJoin(Department.class).on().prop("dept").eq().prop("id")
   ```

3. **WHERE** — filter conditions:
   ```java
   .where()
       .prop("status").eq().val("ACTIVE")
       .and().prop("age").gt().val(18)
       .or().begin()
           .prop("type").eq().val("A")
           .or().prop("type").eq().val("B")
       .end()
   ```

4. **GROUP BY / YIELD** — for aggregations:
   ```java
   select(FuelUsage.class)
       .groupBy().prop("vehicle.station")
       .yield().prop("vehicle.station").as("station")
       .yield().sumOf().prop("qty").as("totalQty")
       .modelAsAggregate()
   ```

5. **ORDER BY** — sorting:
   ```java
   .orderBy().prop("name").asc()
   ```

6. **MODEL** — terminates the query:
   ```java
   .model()                                 // EntityResultQueryModel<T>
   .modelAsAggregate()                      // AggregatedResultQueryModel
   .modelAsEntity(OtherEntity.class)        // Cast to different entity type
   ```

**Condition Operators:**
- Comparison: `.eq()`, `.ne()`, `.gt()`, `.lt()`, `.ge()`, `.le()`
- Nullability: `.isNull()`, `.isNotNull()`
- String: `.like()`, `.notLike()`, `.iLike()`, `.notILike()`
- Sets: `.in().values(...)`, `.in().model(subquery)`, `.notIn().values(...)`
- Existence: `.exists(subquery)`, `.notExists(subquery)`
- Quantified: `.eq().any(subquery)`, `.eq().all(subquery)`
- Multi-prop: `.anyOfProps("p1", "p2").isNotNull()`, `.allOfProps("p1", "p2").eq().val(...)`

**Operand Types:**
- `.val(value)` — literal value (null causes condition to fail)
- `.iVal(value)` — ignore-if-null (skips the condition when value is null)
- `.param("name")` — named parameter (null causes condition to fail)
- `.iParam("name")` — ignore-if-null named parameter
- `.prop("name")` — property reference
- `.extProp("name")` — property from outer/master query
- `.now()` — current timestamp
- `.model(subquery)` — subquery result
- `.expr(expressionModel)` — pre-built expression

**Logical Grouping:**
```java
.where()
    .begin()                                // Opens parenthesis
        .prop("a").eq().val(1)
        .or().prop("b").eq().val(2)
    .end()                                  // Closes parenthesis
    .and().prop("c").eq().val(3)
```
Up to 3 nesting levels are supported (depth-tracked interfaces prevent deeper nesting).

**Functions:**
- Date: `.yearOf()`, `.monthOf()`, `.dayOf()`, `.hourOf()`, `.minuteOf()`, `.secondOf()`, `.dateOf()`, `.dayOfWeekOf()`
- String: `.upperCase()`, `.lowerCase()`, `.concat().prop("a").with().val(" ").with().prop("b").end()`
- Numeric: `.round().to(2)`, `.absOf()`
- Null: `.ifNull().prop("x").then().val(defaultVal)`
- Date arithmetic: `.addTimeIntervalOf().val(30).days().to().prop("startDate")`
- Date diff: `.prop("end").count().days().between().prop("start")`

**Aggregate Functions (in yield):**
```java
.yield().countAll().as("total")
.yield().countOf().prop("id").as("count")
.yield().sumOf().prop("amount").as("sum")
.yield().avgOf().prop("salary").as("avg")
.yield().maxOf().prop("price").as("max")
.yield().minOf().prop("price").as("min")
```

**CASE WHEN:**
```java
.caseWhen()
    .prop("status").eq().val("ACTIVE").then().val(1)
    .when().prop("status").eq().val("PENDING").then().val(2)
    .otherwise().val(0)
    .end()                                  // Auto-detect type
    // Or: .endAsInt(), .endAsStr(50), .endAsDecimal(10, 2), .endAsBool()
```

**Arithmetic in Expressions:**
```java
.prop("price").add().val(10)
.prop("amount").sub().prop("discount")
.prop("qty").mult().prop("unitPrice")
.prop("total").div().val(2)
.prop("value").mod().val(3)
// With parentheses:
.beginExpr().prop("a").add().prop("b").endExpr().mult().prop("c")
```

**Complete Query Example:**
```java
final EntityResultQueryModel<PmXref> query = select(PmXref.class).where()
    .prop(PmXref_.asset()).eq().val(savedPmXref.getAsset()).and()
    .prop(PmXref_.pmRoutine().pmRoutineType().key()).eq().val(TIME_BASED).and()
    .prop(PmXref_.pmRoutine().pmRelationship()).eq().val(savedPmXref.getPmRoutine().getPmRelationship()).and()
    .prop(PmXref_.pmRoutine().relatedPriority()).gt().val(savedPmXref.getPmRoutine().getRelatedPriority())
    .model();

final OrderingModel orderBy = orderBy().prop(PmXref_.pmRoutine().relatedPriority()).desc().model();

// Execute with fetch model and ordering
try (final Stream<PmXref> results = stream(from(query).with(PmXrefCo.FETCH_MODEL).with(orderBy).model())) {
    results.forEach(pmXref -> { /* process */ });
}
```

**Subquery Example:**
```java
select(Vehicle.class)
    .where().exists(
        select(FuelUsage.class)
            .where().prop(FuelUsage_.vehicle()).eq().extProp("id")
            .model()
    )
    .model()
```

#### Fetch Model Patterns

Fetch models control which entity properties are loaded from the database, enabling efficient entity graph retrieval and preventing N+1 query problems.
The core class is `fetch<T>` — all instances are **immutable** (builder methods return new instances).

**FetchCategory Hierarchy (most to least comprehensive):**

| Category | Includes |
|----------|----------|
| `ALL_INCL_CALC` | ALL + all calculated properties |
| `ALL` | DEFAULT + entity-typed properties as DEFAULT |
| `DEFAULT` | KEY_AND_DESC + all retrievable properties + entity-typed properties as ID_ONLY |
| `KEY_AND_DESC` | ID_AND_VERSION + key + desc (composite key members as DEFAULT) |
| `ID_AND_VERSION` | ID_ONLY + version + refCount/active for activatables + "last updated" properties |
| `ID_ONLY` | Just the id property (creates proxy objects for other properties) |
| `NONE` | Nothing |

**Factory Methods** (from `EntityQueryUtils`):
```java
fetch(Vehicle.class)                        // DEFAULT
fetchAll(Vehicle.class)                     // ALL
fetchAllInclCalc(Vehicle.class)             // ALL_INCL_CALC
fetchOnly(Vehicle.class)                    // ID_AND_VERSION
fetchKeyAndDescOnly(Vehicle.class)          // KEY_AND_DESC
fetchIdOnly(Vehicle.class)                  // ID_ONLY
fetchNone(Vehicle.class)                    // NONE

// Instrumented variants (entities support lazy-loading and mutation tracking):
fetchAndInstrument(Vehicle.class)           // DEFAULT + instrumented
fetchAllAndInstrument(Vehicle.class)        // ALL + instrumented
fetchOnlyAndInstrument(Vehicle.class)       // ID_AND_VERSION + instrumented
fetchKeyAndDescOnlyAndInstrument(Vehicle.class) // KEY_AND_DESC + instrumented
```

**`IFetchProvider` — the Preferred Pattern:**

In practice, fetch models are defined as `IFetchProvider<T>` static fields in companion interfaces using metamodel references.
`IFetchProvider` supports dot-notation via metamodel paths (unlike raw `fetch<T>` which requires nested fetch models).
Convert to `fetch<T>` via `.fetchModel()` when needed for query execution.

```java
// In companion interface (e.g., PmXrefCo.java):
static final IFetchProvider<PmXref> FETCH_PROVIDER = EntityUtils.fetch(PmXref.class).with(
        PmXref_.pmRoutine().pmRelationship().tolerance(), // Deep property chains via metamodel
        PmXref_.pmRoutine().relatedPriority(),
        PmXref_.pmRoutine().pmRoutineType(),
        PmXref_.asset().equipment().avgReading(),
        PmXref_.lastActualCompletedWorkOrder().repairFinish(),
        PmXref_.active(),
        PmXref_.lastForecastedDate());

static final fetch<PmXref> FETCH_MODEL = FETCH_PROVIDER.fetchModel();
```

```java
// In DAO — override createFetchProvider():
@Override
protected IFetchProvider<PmXref> createFetchProvider() {
    return FETCH_PROVIDER;
}

// Use in queries:
from(query).with(PmXrefCo.FETCH_MODEL).with(orderBy).model()
```

**Raw `fetch<T>` Customisation** (when needed directly):
```java
// Add specific properties
fetch(Vehicle.class).with("model").with("vin", "registrationNumber")

// Exclude properties
fetchAll(Vehicle.class).without("largeBlob")

// Nested fetch for entity-typed properties
fetch(Vehicle.class)
    .with("model", fetchOnly(VehicleModel.class)
        .with("key")
        .with("make", fetchOnly(VehicleMake.class).with("key")))
```

**Important:** Raw `fetch<T>` does **not** support dot-notation like `"model.make"` — use nested fetch models or `IFetchProvider` with metamodel paths instead.

**Instrumentation Precedence:**
Fetch model instrumentation has higher precedence than `QueryExecutionModel` lightweightness.
If QEM is lightweight but fetch model is instrumented, entities **are** instrumented.

**Integration with QueryExecutionModel:**
```java
from(query)
    .with(PmXrefCo.FETCH_MODEL)            // Fetch strategy (from companion)
    .with(orderBy().prop(PmXref_.pmRoutine().relatedPriority()).desc().model()) // Ordering
    .with("paramName", paramValue)          // Named parameters
    .lightweight()                          // Optional: no instrumentation (unless fetch overrides)
    .model()                                // QueryExecutionModel
```

#### Web UI Configuration

Web UI is built around three main component types configured via fluent DSL builders.
All components must be registered with `IWebUiBuilder` (accessed via `configApp()` in `IWebUiConfig`).

**Application Configuration Entry Point:**
```java
public class AppWebUiConfig extends AbstractWebUiConfig {
    @Override
    public void initConfiguration() {
        final IWebUiBuilder builder = configApp();
        VehicleWebUiConfig.register(injector, builder);  // Register entity UI configs
        // ... more registrations

        configDesktopMainMenu()
            .addModule("Fleet").description("Fleet Management").icon("menu:fleet")
                .bgColor("#00D4AA").captionBgColor("#00AA88")
                .menu()
                    .addMenuItem("Vehicles").centre(vehicleCentre).done()
                    .addMenuItem("Drivers").centre(driverCentre).done()
                .done()
            .done()
            .setLayoutFor(Device.DESKTOP, null, "[[], [], []]");
    }
}
```

##### Entity Centre

Grid/listing component for data presentation, configured via `EntityCentreBuilder`:

```java
final var standardNewAction = StandardActions.NEW_ACTION.mkAction(PmTask.class);
final var standardDeleteAction = StandardActions.DELETE_ACTION.mkAction(PmTask.class);
final var standardEditAction = StandardActions.EDIT_ACTION.mkAction(PmTask.class);

final EntityCentreConfig<PmTask> ecc = EntityCentreBuilder.centreFor(PmTask.class)
    // Front and top toolbar actions
    .addFrontAction(standardNewAction)
    .addTopAction(standardNewAction).also()
    .addTopAction(standardDeleteAction).also()
    .addTopAction(CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction()).also()
    .addTopAction(StandardActions.EXPORT_ACTION.mkAction(PmTask.class))
    // Selection criteria (using metamodel references)
    .addCrit(PmTask_.pmRoutine()).asMulti().autocompleter(PmRoutine.class).also()
    .addCrit(PmTask_.task()).asMulti().autocompleter(Task.class).also()
    .addCrit(PmTask_.active()).asMulti().bool()
    // Criteria layout
    .setLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkVarGridForCentre(2, 1))
    .withScrollingConfig(standardStandaloneScrollingConfig(0))
    // Result set properties (using metamodel references)
    .addProp(PmTask_.pmRoutine()).order(1).asc().minWidth(80)
        .withSummary("total_count_", "COUNT(SELF)",
            format("Count:Total number of matching %ss.", PmTask.ENTITY_TITLE))
        .withAction(standardEditAction).also()
    .addProp(PmTask_.task()).order(2).asc().minWidth(80).also()
    .addProp(PmTask_.task().desc()).minWidth(160).also()
    .addProp(PmTask_.active()).width(50)
    // Primary action (row click)
    .addPrimaryAction(standardEditAction)
    .build();

final EntityCentre<PmTask> centre = new EntityCentre<>(MiPmTask.class, ecc, injector);
builder.register(centre);
```

**Criterion Types:**
- `.asMulti().autocompleter(EntityType.class)` — multi-value entity picker
- `.asMulti().text()` — text wildcard search
- `.asMulti().bool()` — boolean multi-selector
- `.asSingle().autocompleter(EntityType.class)` — single entity picker (requires `@CritOnly(SINGLE)`)
- `.asSingle().text()`, `.integer()`, `.decimal()`, `.date()`, `.dateTime()`
- `.asRange().integer()`, `.decimal()`, `.date()`, `.dateTime()`, `.time()`

**Result Property Options:**
- `.order(n).asc()` / `.desc()` — default sort order
- `.width(px)` / `.minWidth(px)` — fixed or flexible width
- `.withSummary(alias, eqlExpr, titleAndDesc)` — footer aggregate (e.g., `"COUNT(SELF)"`, `"SUM(amount)"`)
- `.withAction(actionConfig)` — action on the property column
- `.withWordWrap()` — enable text wrapping

**Centre Options:**
- `.runAutomatically()` — execute query on load
- `.hideCheckboxes()` — hide row selection
- `.hideToolbar()` — hide toolbar
- `.setPageCapacity(n)` — rows per page (default 30)
- `.retrieveAll()` — load all matching records
- `.enforcePostSaveRefresh()` — refresh after entity save
- `.hasEventSource(EventSourceClass.class)` — SSE-based refresh

##### Entity Master

Form-based entity editing, configured via `SimpleMasterBuilder`:

```java
final String layout = LayoutComposer.mkVarGridForMasterFitWidth(2, 1, 2, 1);
final IMaster<PmTask> masterConfig = new SimpleMasterBuilder<PmTask>()
    .forEntity(PmTask.class)
    // Property editors (using metamodel references)
    .addProp(PmTask_.pmRoutine()).asAutocompleter().also()
    .addProp(PmTask_.task()).asAutocompleter().also()
    .addProp(PmTask_.assetClass()).asAutocompleter()
        .withMatcher(PmTaskAssetClassMatcher.class).also()  // Custom value matcher
    .addProp(PmTask_.assetMake()).asAutocompleter().also()
    .addProp(PmTask_.active()).asCheckbox()
    // Standard actions
    .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
    .addAction(MasterActions.SAVE)
    // Layouts
    .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
    .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
    .setLayoutFor(Device.TABLET, Optional.empty(), layout)
    .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
    .withDimensions(mkDim(SIMPLE_ONE_COLUMN_MASTER_DIM_WIDTH, 540, Unit.PX))
    .done();

final EntityMaster<PmTask> master = new EntityMaster<>(
    PmTask.class, PmTaskProducer.class, masterConfig, injector);
builder.register(master);
```

**Property Editor Types:**
- `.asSinglelineText()` — text input
- `.asMultilineText()` — textarea
- `.asRichText()` — rich text editor
- `.asAutocompleter()` / `.asAutocompleter(EntityType.class)` — entity picker
- `.asDateTimePicker()` / `.asDatePicker()` / `.asTimePicker()` — date/time
- `.asDecimal()` / `.asSpinner()` (or `.asInteger()`) / `.asMoney()` — numeric
- `.asCheckbox()` — boolean
- `.asColour()` — colour picker
- `.asHyperlink()` — hyperlink
- `.asCollectionalRepresentor()` — read-only collection display
- `.asCollectionalEditor()` — editable collection
- `.asFile()` — file upload

**Standard Master Actions:**
- `MasterActions.SAVE` — save entity (shortcut: ctrl+s / meta+s)
- `MasterActions.REFRESH` — cancel/reload (shortcut: ctrl+x / meta+x)
- `MasterActions.VALIDATE`, `MasterActions.EDIT`, `MasterActions.VIEW`, `MasterActions.DELETE`, `MasterActions.NEW`

##### Compound Master

Multi-tab master combining multiple views (masters and centres) in a tabbed layout:

```java
final EntityMaster<OpenVehicleMasterAction> compoundMaster =
    CompoundMasterBuilder.<Vehicle, OpenVehicleMasterAction>create(injector, builder)
        .forEntity(OpenVehicleMasterAction.class)
        .withProducer(OpenVehicleMasterActionProducer.class)
        .addMenuItem(VehicleMaster_OpenMain_MenuItem.class)
            .icon("icons:picture-in-picture")
            .shortDesc("Main")
            .longDesc("Vehicle details")
            .withView(mainMaster)                       // Embed a master
        .also()
        .addMenuItem(VehicleMaster_OpenFuelUsages_MenuItem.class)
            .icon("icons:view-module")
            .shortDesc("Fuel Usages")
            .longDesc("Fuel usage records")
            .withView(fuelUsageCentre)                  // Embed a centre
        .andDefaultItemNumber(0)                        // Default tab (0-based)
        .done();
builder.register(compoundMaster);
```

Compound masters require:
- A functional entity extending `AbstractFunctionalEntityWithCentreContext<T>`
- Menu item classes extending `AbstractFunctionalEntityForCompoundMenuItem<T>`
- A producer class for the functional entity

##### Action Configuration

**Standard Action Helpers** (preferred for common operations):

Use `StandardActions` and `CentreConfigActions` for pre-built action configurations, and `Compound` helpers for compound master open/edit actions:
```java
// StandardActions — pre-built centre actions
StandardActions.NEW_ACTION.mkAction(PmTask.class)
StandardActions.DELETE_ACTION.mkAction(PmTask.class)
StandardActions.EXPORT_ACTION.mkAction(PmTask.class)
StandardActions.EDIT_ACTION.mkAction(PmTask.class)
CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction()

// Compound helpers — for compound master open/edit/new actions
final PrefDim dims = mkDim(1280, 640, Unit.PX);
Compound.openEdit(OpenVehicleMasterAction.class, Vehicle.ENTITY_TITLE, "Edit Vehicle", dims)
Compound.openNew(OpenVehicleMasterAction.class, "add-circle-outline", Vehicle.ENTITY_TITLE, "Add new Vehicle", dims)
```

**Custom Actions** (when standard helpers are insufficient):
```java
final EntityActionConfig customAction = action(CopyWorkOrderAction.class)
    .withContext(context().withSelectedEntities().build())
    .icon("ports-menu-actions:wa-copy")
    .shortDesc("Copy Work Order")
    .longDesc("Create copy of Work Order for selected Vehicles")
    .prefDimForView(mkDim(960, 600, Unit.PX))
    .withNoParentCentreRefresh()                        // Don't refresh parent centre
    .build();
```

**Context Options** (what data the action receives):
- `.withCurrentEntity()` — selected entity
- `.withSelectedEntities()` — multiple selected entities
- `.withSelectionCrit()` — current filter criteria
- `.withMasterEntity()` — master entity (for embedded centres)
- `.withComputation((entity, context) -> value)` — custom computation

##### Query Enhancer Pattern

For embedded centres that need to filter by the master entity:

```java
private static class VehicleMaster_FuelUsageCentre_QueryEnhancer implements IQueryEnhancer<FuelUsage> {
    @Override
    public ICompleted<FuelUsage> enhanceQuery(
            final IWhere0<FuelUsage> where,
            final Optional<CentreContext<FuelUsage, ?>> context) {
        return enhanceEmbededCentreQuery(where,
            createConditionProperty(FuelUsage_.vehicle()),  // Metamodel reference
            context.get().getMasterEntity().getKey());
    }
}
```

#### Calculated Properties

Calculated properties are read-only, database-computed fields defined with `@Calculated`.
They use a companion `protected static final ExpressionModel` field named `propertyName_` that defines the computation using EQL's `expr()` API.

**Simple property dereferencing:**
```java
@IsProperty
@Readonly
@Calculated
@Title(value = "Asset Class", desc = "The class of this asset.")
private AssetClass assetClass;
protected static final ExpressionModel assetClass_ = expr().prop(Vehicle_.vehicleClass().assetClass()).model();
```

**Subquery — find current value from a timeline entity:**
```java
@IsProperty
@Readonly
@Calculated
@Title(value = "Location", desc = "The current Location of the Vehicle.")
private Location location;
protected static final ExpressionModel location_ = expr().model(
    select(LocationTimeline.class).where()
        .prop(LocationTimeline_.capable().vehicle()).eq().extProp(Vehicle_.id()).and()
        .prop(LocationTimeline_.periodStart()).le().now().and()
        .begin()
            .prop(LocationTimeline_.periodFinish()).gt().now().or()
            .prop(LocationTimeline_.periodFinish()).isNull()
        .end()
        .yield().prop(LocationTimeline_.location()).modelAsEntity(Location.class)).model();
```

**CASE WHEN — boolean conditional:**
```java
@IsProperty
@Readonly
@Calculated
@Title(value = "GPS present?", desc = "Indicates whether GPS coordinates are present.")
private boolean gpsPresent;
protected static final ExpressionModel gpsPresent_ = expr()
    .caseWhen().begin()
        .prop(Equipment_.gisInfo().longitude()).isNotNull().and()
        .prop(Equipment_.gisInfo().latitude()).isNotNull()
    .end()
    .then().val(true)
    .otherwise().val(false).end()
    .model();
```

**Financial aggregation:**
```java
@IsProperty
@Readonly
@Calculated
@Title("Total Estimate")
private Money totalEstimate = Money.zero;
protected static final ExpressionModel totalEstimate_ = expr().ifNull().expr(
    expr().
        ifNull().prop(WorkOrderCost_.labourEstimate()).then().val(0).add().
        ifNull().prop(WorkOrderCost_.consumablesEstimate()).then().val(0).add().
        ifNull().prop(WorkOrderCost_.dcConsumablesEstimate()).then().val(0).add().
        ifNull().prop(WorkOrderCost_.dcServiceEstimate()).then().val(0).model()
    ).then().val(0).model();
```

**Inline expression string** (simpler alternative for trivial cases):
```java
@IsProperty
@Calculated("price.amount + purchasePrice.amount")
@Title("Calc0")
private BigDecimal calc0;
```

#### MetaProperty System

Every instrumented entity property has a `MetaProperty<T>` object providing runtime metadata, validation state, and change tracking.
Access via `entity.getProperty(Entity_.propName())` (preferred) or `entity.getProperty("propName")`.

**Validation and Domain Results** (commonly used in definers):
```java
// In a definer (IAfterChangeEventHandler):
final MetaProperty<Date> mpFromDate = entity.getProperty(Delegation_.fromDate());
mpFromDate.setDomainValidationResult(result);          // Set informative/warning/failure
if (!result.isSuccessful()) {
    mpFromDate.setLastInvalidValue(entity.getFromDate());
}
```

**Change Tracking** (commonly used in DAOs before save):
```java
// Check which properties were modified before save
final boolean wasActiveDirty = entity.getProperty(Person_.active()).isDirty();
final boolean wasAuthoriserDirty = entity.getProperty(Person_.authoriser()).isDirty();
// ... use this to conditionally trigger side effects
```

**Original Value Access:**
```java
final WorkOrder originalWa = timesheet.<WorkOrder>getProperty(Timesheet_.workOrder()).getOriginalValue();
```

**Editability Control** (commonly used in producers):
```java
// In a producer (provideDefaultValuesForStandardNew):
entityOut.setPmRoutine(refetch(ofMasterEntity().keyOfMasterEntity(PmRoutine.class)));
entityOut.getProperty(PmXref_.pmRoutine()).setEditable(false);
```

**Validation Checking** (in producers):
```java
entityOut.getProperty("attachedTo").validationResult().ifFailure(Result::throwRuntime);
entityOut.getProperty("attachedTo").setEditable(false);
```

**Initialising vs Mutation Phase** (in definers):
```java
// Definers run both when loading from DB and when user sets values
public void handle(final MetaProperty<Location> property, final Location major) {
    final Location location = property.getEntity();
    if (!location.isInitialising()) {
        // Only recalculate during user/business logic mutation, not DB load
        location.setPath(computePath(major));
        location.setLevel(major.getLevel() + 1);
    }
}
```

**Key MetaProperty Methods:**

| Method | Purpose |
|--------|---------|
| `isDirty()` | Property was modified since last save |
| `isValid()` | Current value passed validation |
| `validationResult()` | Get validation result (Success/Warning/Failure) |
| `setDomainValidationResult(result)` | Set validation result from definer/DAO |
| `isEditable()` / `setEditable(boolean)` | Control property editability |
| `getValue()` / `getOriginalValue()` / `getPrevValue()` | Value tracking |
| `isChangedFromOriginal()` | Changed since entity retrieval |
| `getValueChangeCount()` | Number of times value was set |

#### MetaModel Annotation Processor

The `MetaModelProcessor` generates type-safe metamodel classes at compile time for every domain entity.
These are used throughout the codebase for refactor-safe property references in EQL queries, fetch models, centre/master configurations, and meta-property access.

**Generated output** (in `target/generated-sources/`):
- `metamodels/MetaModels.java` — master class with static fields for all entity metamodels
- `{package}/meta/{Entity}MetaModel.java` — individual entity metamodel
- `{package}/meta/{Entity}MetaModelAliased.java` — aliased variant for use in self-joins

**Usage:**
```java
import static metamodels.MetaModels.*;  // Static import all metamodel fields

// Property references in EQL:
.prop(Vehicle_.model())                              // Simple property
.prop(PmXref_.asset().equipment().avgReading())      // Deep property chain

// In fetch providers:
EntityUtils.fetch(PmXref.class).with(PmXref_.pmRoutine().relatedPriority())

// In centre/master configurations:
.addCrit(PmTask_.pmRoutine()).asMulti().autocompleter(PmRoutine.class)
.addProp(PmTask_.task().desc()).minWidth(160)

// Aliased metamodels for self-joins:
private static final VehicleRegistrationMetaModelAliased VR1 = new VehicleRegistrationMetaModelAliased("VR1");
select(VehicleRegistration.class).as(VR1.alias).where()
    .prop(VR1.vehicle()).eq().extProp(Vehicle_.id())
```

#### GraphQL Web API

The platform includes a GraphQL API (`GraphQLService` in `platform-dao`) that auto-generates schemas from domain entities.

**Key characteristics:**
- Read-only — supports GraphQL `query` operations only (no mutations/subscriptions)
- Query fields are uncapitalized entity type names (e.g., `tgVehicleModel`)
- Supports arguments: `eq` (equals), `like` (pattern matching), `order`, `pageNumber`, `pageCapacity`
- Field-level authorization via security tokens (`FieldVisibility`)
- Max query depth configurable via `web.api.maxQueryDepth` (default: 15)
- Security token: `GraphiQL_CanExecute_Token`

#### Server-Sent Events (SSE)

Real-time event infrastructure for pushing notifications to Entity Centres.

**Key components:**
- `IEventSource` — contract for event sources; implement `connect(emitter)` and `disconnect()`
- `AbstractEventSource<T, OK>` — base class subscribing to RxJava observables; implement `eventToData(T event)`
- `EventSourceDispatchingEmitter` — broadcasts events to multiple registered client emitters
- `SseServlet` — separate Jetty server (default port 8092), configurable via `sse.*` properties

**Integration with Entity Centres:**
```java
EntityCentreBuilder.centreFor(WorkOrder.class)
    .hasEventSource(WorkOrderEventSource.class)     // Register SSE event source
    .withCountdownRefreshPrompt(5)                  // Auto-refresh with 5s countdown
    // ...
```

### Key Classes and Interfaces

#### Domain Layer
- `ua.com.fielden.platform.entity.AbstractEntity` - Base entity class
- `ua.com.fielden.platform.entity.AbstractPersistentEntity` - Adds audit properties (createdBy, createdDate, etc.)
- `ua.com.fielden.platform.entity.ActivatableAbstractEntity` - Entities with active/inactive states and reference counting
- `ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext` - Base for action entities (non-persistent)
- `ua.com.fielden.platform.entity.AbstractUnionEntity` - Union type entities (polymorphic associations)
- `ua.com.fielden.platform.entity.IEntityProducer` - Context-aware entity instantiation pattern
- `ua.com.fielden.platform.entity.factory.ICompanionObjectFinder` - Companion lookup

#### DAO Layer
- `ua.com.fielden.platform.dao.IEntityDao` - Generic DAO interface
- `ua.com.fielden.platform.dao.CommonEntityDao` - Base DAO implementation with standard CRUD operations
- `ua.com.fielden.platform.entity.query.model.EntityResultQueryModel` - Entity query models for data retrieval
- `ua.com.fielden.platform.dao.QueryExecutionModel` - Query execution wrapper with fetch, ordering, and parameters

**Query Execution Pattern:**
TG uses a sophisticated query execution model that separates query definition from execution:

```java
// 1. Build the query model
final EntityResultQueryModel<WaTypeDefault> query = select(WaTypeDefault.class)
    .where().prop(WaTypeDefault_.workActivityType()).eq().val(waType)
    .model();

// 2. Define fetch strategy and ordering
final fetch<WaTypeDefault> fetch = fetchAll(WaTypeDefault.class);
final OrderingModel orderBy = orderBy().prop(WaTypeDefault_.defaultProperty()).desc().model();

// 3. Combine into QueryExecutionModel
final QueryExecutionModel<WaTypeDefault, EntityResultQueryModel<WaTypeDefault>> qem = 
    from(query).with(fetch).with(orderBy).model();

// 4. Execute via companion object
final List<WaTypeDefault> results = co(WaTypeDefault.class).getAllEntities(qem);
```

**Key Benefits:**
- **Separation of Concerns**: Query logic separate from fetch strategy and execution parameters
- **Type Safety**: Generic type parameters ensure compile-time query validation
- **Performance Control**: Explicit fetch models prevent N+1 problems
- **Parameterization**: Support for named parameters with `.with(paramName, paramValue)`
- **Lightweight Mode**: Optional lightweight execution for read-only scenarios
- **Instrumentation Control**: Fine-grained control over entity instrumentation

**Common QueryExecutionModel Usage:**
- **Simple queries**: `from(query).model()` - minimal execution model
- **With fetch**: `from(query).with(fetchModel).model()` - controls property loading
- **With ordering**: `from(query).with(orderBy).model()` - adds sorting
- **With parameters**: `from(query).with("param", value).model()` - parameterized queries
- **Aggregates**: Used with `AggregatedResultQueryModel` for analytical queries

#### Web Layer
- `ua.com.fielden.platform.web.resources.webui.AbstractWebResource` - Base web resource
- `ua.com.fielden.platform.web.centre.EntityCentre` - Data grid component
- `ua.com.fielden.platform.web.view.master.EntityMaster` - Entity editing form

### Testing Approach

The project uses JUnit 4 for testing with extensive test coverage:
- Integration tests with PostgreSQL and SQL Server (primary database targets)
- Test data fixtures using DbDrivenTestCase pattern
- Test-specific Guice modules for dependency injection

#### Testing Best Practices

**Prefer AssertJ over JUnit Assertions:**
- Always use AssertJ's fluent assertions (`assertThat()`) instead of traditional JUnit assertions (`assertTrue()`, `assertEquals()`, etc.)
- AssertJ provides better readability, more descriptive failure messages, and a more intuitive API
- Exception testing: Use AssertJ's `assertThatThrownBy()` for exception testing instead of try-catch blocks or JUnit's `@Test(expected=...)`
- Descriptive test names: Use method names that clearly state the expected behavior

**Examples:**
```java
// Prefer this (AssertJ):
assertThat(result).isTrue();
assertThat(entity.getProperty()).isEqualTo(expectedValue);
assertThat(list).hasSize(3).containsExactly("a", "b", "c");
assertThatThrownBy(() -> method.call())
    .isInstanceOf(InvalidArgumentException.class)
    .hasMessageContaining("must be a simple property name");

// Instead of this (JUnit):
assertTrue(result);
assertEquals(expectedValue, entity.getProperty());
assertEquals(3, list.size());
try {
    method.call();
    fail("Should have thrown InvalidArgumentException");
} catch (InvalidArgumentException ex) {
    assertTrue(ex.getMessage().contains("must be a simple property name"));
}
```

#### Important: Indirect Testing Pattern in TG Applications

TG-based applications use an **indirect testing pattern** where business logic validation is tested through the DAO layer.

**Test Evolution Note**: Legacy test cases may be marked with `@Deprecated` in favor of newer `IDomainData` test approach. New tests should follow the domain-driven testing pattern where possible.

1. **Business Logic Location**: 
   - Validators, definers, and handlers reside in the `pojo-bl` module
   - These are attached to entities via `@BeforeChange`, `@AfterChange`, and other annotations
   - Business rules are declaratively defined on entity properties

2. **How Testing Works**:
   - DAO layer tests manipulate entities (create, update properties, save)
   - Setting entity properties automatically triggers validation chains
   - This means DAO tests comprehensively test business logic validators
   - Example: `personDao.save(person.setActive(false))` tests `PersonActiveValidator`

3. **Why This Pattern**:
   - Tests business logic in context (not isolated)
   - Validates complete validation chains and workflows
   - Ensures database constraints align with business rules
   - Provides integration testing that's more valuable than unit tests

4. **Code Coverage Implications**:
   - Don't expect traditional unit tests in `pojo-bl` module
   - Business logic IS tested, just indirectly through DAO tests
   - A single DAO test may validate multiple validators and definers
   - This is intentional and represents best practice for AOP-based systems

#### Web Testing Pattern

Web modules (`platform-web-resources` and `platform-web-ui`) are tested through browser-based test suites:

1. **Test Suite Location**:
   - Main test suite: `platform-web-ui/src/main/web/ua/com/fielden/platform/web/tests.html`
   - Individual test files in `*/test/*.html` directories throughout the web module
   - Uses Web Component Tester (WCT) for browser-based testing

2. **Test Coverage Includes**:
   - Entity Centre components (grid, selection, filtering, context)
   - Entity Master components (forms, validation, conflict resolution)
   - Editors (datetime pickers, entity editors, collectional editors)
   - EGI (Enterprise Grid Infrastructure) rendering and columns
   - Serialization and reflection utilities
   - Global error handling

3. **Running Web Tests**:
   - Tests execute in actual browsers (not unit test runners)
   - Validates real DOM manipulation and user interactions
   - Tests Polymer components and custom elements
   - Ensures cross-browser compatibility

4. **Why Browser Testing**:
   - Web components need real browser environment
   - Tests actual user interactions (clicks, typing, etc.)
   - Validates CSS and rendering behavior
   - Tests async operations and data binding

### Security and Validation

TG implements a comprehensive security framework with declarative authorization at the domain level:

#### Domain-Centric Authorization

**Security Tokens**: Every operation is protected by security tokens following standardized templates:

**Save Operation Tokens**:
- `@Template.SAVE` - `EntityName_CanSave_Token` - **Standard Save**: Authorizes saving both new and modified entities (most common usage)
- `@Template.SAVE_NEW` - `EntityName_CanSaveNew_Token` - **Creating New Entities**: Authorizes saving non-persisted entities (`!entity.isPersisted()`) - used only in specific domain cases requiring fine-grained control
- `@Template.SAVE_MODIFIED` - `EntityName_CanSaveModified_Token` - **Updating Existing Entities**: Authorizes saving already-persisted entities (`entity.isPersisted()`) - used only in specific domain cases requiring fine-grained control

**Usage Pattern**: Generally, only `SAVE` token is used for standard save operations. `SAVE_NEW` and `SAVE_MODIFIED` are employed only in specific domain cases where business requirements demand separate permissions for creates vs updates. When `SAVE_NEW`/`SAVE_MODIFIED` are used, the general `SAVE` token is not used for that entity.

**Other Operation Tokens**:
- `@Template.DELETE` - `EntityName_CanDelete_Token` - Authorizes deletion operations
- `@Template.READ` - `EntityName_CanRead_Token` - Authorizes reading entity data
- `@Template.READ_MODEL` - `EntityName_CanReadModel_Token` - Authorizes reading data model
- `@Template.EXECUTE` - `EntityName_CanExecute_Token` - Authorizes action execution
- `@Template.MODIFY` - `EntityName_CanModify_PropertyName_Token` - Property-level modification rights
- `@Template.MASTER_OPEN` - `EntityName_CanOpen_Token` - Authorizes opening entity masters
- `@Template.MASTER_MENU_ITEM_ACCESS` - `EntityName_CanAccess_Token` - Authorizes access to compound master menu items

**Declarative Authorization**: Security is applied using `@Authorise` annotation:
```java
@Override
@SessionRequired
@Authorise(Project_CanDelete_Token.class)
public int batchDelete(final Collection<Long> entitiesIds) {
    // Delete implementation with authorization check
}

@Override
@Authorise(OpenWorkActivityMasterAction_CanOpen_Token.class)
protected OpenWorkActivityMasterAction provideDefaultValues(...) {
    // Producer with authorization check
}
```

**Authorization Infrastructure**:
- `AuthorisationInterceptor` - AOP interceptor that processes `@Authorise` annotations
- `IAuthorisationModel` - Contract for authorization implementations (database, LDAP, etc.)
- `AbstractAuthorisationModel` - Base implementation with start/stop scope management
- Thread-local scoping prevents nested authorization checks within the same operation

**Nested Authorization Scopes**: The interceptor prevents redundant checks:
- `isStarted()` method tracks if authorization is already in progress
- First intercepted method performs authorization check
- Subsequent nested calls bypass authorization (already authorized)
- Finally block ensures proper cleanup of authorization state

**Security Token Naming Convention**:
Templates use format strings to generate consistent token names:
- Class-based: `%s_CanSave_Token` → `WorkActivity_CanSave_Token`
- Property-based: `%s_CanModify_%s_Token` → `WorkActivity_CanModify_Type_Token`

**Authorization Patterns**:
- **DAO Level**: CRUD operations protected with appropriate tokens
- **Producer Level**: Entity creation/opening requires authorization
- **Property Level**: Fine-grained access control for sensitive properties
- **Action Level**: Business processes require execution permissions

#### Traditional Security Features
- Role-based access control with fine-grained permissions
- Multi-layered validation framework
- Property-level and entity-level validators
- Transaction support with optimistic locking

### Important Conventions

1. **Naming Conventions**:
   - Entities: Singular nouns (e.g., `Vehicle`, `Person`)
   - Companions: Interface `{Entity}Co` (e.g., `VehicleCo`)
   - DAOs: Implementation `{Entity}Dao` (e.g., `VehicleDao`)

2. **Package Structure**:
   - `ua.com.fielden.platform` - Core platform classes
   - `fielden.test_app` - Test application examples
   - Domain-specific packages under main package

3. **Property Declaration**:
   - Always use `@IsProperty` annotation
   - Define property titles with `@Title`
   - Use `@MapTo` for persistent properties
   - Apply validators as needed
   - Multiple validators can be chained: `@BeforeChange({@Handler(Validator1.class), @Handler(Validator2.class)})`
   - Order matters in validator chains - validators execute in declaration order

4. **Query Construction**:
   - Use EQL for complex queries
   - Leverage fetch providers for optimization
   - Apply appropriate fetch strategies

### Code Documentation Standards

**Comment and Javadoc Formatting:**
- Always place each new sentence on a new line for better readability and version control diffs
- Always end sentences with a full stop (period)
- This applies to both inline comments and Javadoc documentation
- Multi-line comments should have each sentence on its own line
- **Always use Markdown for Javadoc** instead of HTML tags for better readability and modern documentation standards

**Examples:**
```java
// Prefer this format:
// TgPerson extends ActivatableAbstractEntity and is persistent.
// This ensures proper activation functionality.

/// Checks if the entity type represents activatable entities.
/// Only persistent entities can be considered activatable.
/// Synthetic entities are not supported for activation. 

// Instead of this format:
// TgPerson extends ActivatableAbstractEntity and is persistent, this ensures proper activation functionality
```

## Development Tips

1. **Always use metamodel references** (`Entity_.property()`) instead of string literals for property access in EQL, fetch models, and UI configurations
2. **Define `FETCH_PROVIDER` in companion interfaces** using `IFetchProvider` with metamodel paths; override `createFetchProvider()` in DAOs
3. **Use `co()` for read-only and `co$()` for mutations** — choosing the wrong one causes subtle bugs
4. **Check `isInitialising()` in definers** to distinguish between DB load and user mutation phases
5. **Use `isDirty()` before triggering side effects** in DAO `save()` methods to avoid unnecessary cascading updates
6. **Use `try-with-resources` with `stream()`** — entity streams hold database resources that must be closed
7. **Test with PostgreSQL and SQL Server** — both are primary database targets; test with both for compatibility
8. **Use `StandardActions` and `Compound` helpers** for common centre/master actions instead of building custom actions

## Delete Operations Design Patterns

The `DeleteOperations` class in `platform-dao` implements several important patterns:

1. **Transaction Management**: 
   - Transaction boundaries are managed declaratively via `@SessionRequired` annotations on companion methods
   - No explicit transaction management needed within DeleteOperations methods
   - All operations within a delete method execute in the same transaction context

2. **Locking Strategy**:
   - Uses pessimistic locking with `UPGRADE` lock mode for activatable entities
   - Locks are acquired on both the entity being deleted and all referenced activatables
   - Locks are held until transaction completion, preventing race conditions
   - Sequential processing of references is safe due to proper locking

3. **Error Handling**:
   - Deliberately catches only `PersistenceException` to handle referential integrity violations
   - Wraps constraint violations in user-friendly platform-level exceptions
   - Allows other exceptions to bubble up to preserve original context
   - This is intentional design to provide meaningful errors for common cases

4. **Code Style Conventions**:
   - Pattern `case null, default -> null` in switch expressions is conventional TG shorthand
   - Combines null and default cases when they have identical behavior
   - More concise than separating into distinct cases
