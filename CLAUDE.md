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

The platform consists of several key modules:

1. **platform-annotations** - Core annotations for entity definition
2. **platform-annotation-processors** - Compile-time annotation processing
3. **platform-pojo-bl** - Business logic layer with domain model foundation
4. **platform-dao** - Data access layer with EQL and Hibernate integration
5. **platform-web-resources** - REST API layer with web resources
6. **platform-web-ui** - Web UI framework with Entity Centre and Master patterns
7. **platform-db-evolution** - Database migration and evolution tools
8. **platform-eql-grammar** - ANTLR-based EQL parser and compiler
9. **platform-benchmark** - Performance benchmarking tools

### Core Architectural Patterns

#### Entity Definition Language (EDL)
All domain entities extend `AbstractEntity` and use annotations for configuration:
- `@MapEntityTo` - ORM mapping configuration for persistent entity.
- `@KeyType` - Defines entity business key
- `@IsProperty` - Declares entity properties
- `@MapTo` - ORM mapping configuration for persistent entity properties.
- `@CompanionObject` - Links to companion object for CRUD operations
- `@Calculated` - Computed properties
- `@Observable` - Change tracking

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
  generally specking action entities are not persistent (do not get saved into a database);
  Method `save` for their companion objects "executes" the action, where an instance of an action entity is passed with all the relevant properties populated.

Entity types that are annotated with `@MapEntityTo` represent persistent entities.

#### Union Entities (Polymorphic Associations)

Union entities model situations where a property can reference different entity types (polymorphic association).
They extend `AbstractUnionEntity` and provide type-safe polymorphic references.

**Key Characteristics:**
- Only one union property can have a value at any time (union constraint)
- All properties must be entity types (no primitive/ordinary types allowed)
- Each property must be of a unique entity type (no duplicates)
- The active property determines the union's `id`, `key`, and `desc`
- Union entities have `@KeyType(String.class)` by default

**Implementation Example:**
```java
@CompanionObject(RotableLocationCo.class)
public class RotableLocation extends AbstractUnionEntity {
    
    @IsProperty
    @MapTo
    private Equipment equipment;
    
    @IsProperty
    @MapTo
    private BulkStoreBin bulkStoreBin;
    
    // Standard setters/getters with @Observable
}
```

**Usage Patterns:**
```java
// Setting a union property
RotableLocation location = new RotableLocation();
location.setEquipment(someEquipment); // Only one can be set
// location.setBulkStoreBin(bin); // Would throw exception - union already has value

// Getting the active entity
AbstractEntity<?> activeEntity = location.activeEntity();

// Using setUnionProperty helper
location.setUnionProperty(someEquipment); // Automatically finds matching property
```

**Common Use Cases:**
- **Location references** - Entity can be in different location types (e.g., Equipment slot or Storage bin)
- **Origin tracking** - Tracking where something came from (different source types)
- **Line items** - Purchase order lines of different types (service, repair, freight, etc.)
- **Approval chains** - Different approver types in workflow

**Important Notes:**
- Union properties often use `@SkipEntityExistsValidation` for performance
- The `activeEntity()` method returns the non-null union property value
- `setUnionProperty()` helper method automatically assigns to the correct property by type
- Union entities are useful for avoiding multiple nullable foreign keys

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
Every entity has a companion object (Co class) that provides:
- Type-safe CRUD operations
- Query execution
- Business logic encapsulation
- Transaction management

Example: Entity `Vehicle` has companion interface `VehicleCo` and DAO implementation `VehicleDao`

#### Entity Query Language (EQL)
Type-safe query language with ANTLR grammar:
- Located in `platform-eql-grammar/src/main/antlr4/EQL.g4`
- Multi-stage compilation (EqlStage0-3) for optimization
- Supports complex queries with joins, aggregations, and calculations

#### Entity Centre and Master Patterns
Web UI is built around two main patterns:
- **Entity Centre**: Grid/listing component for data presentation
- **Entity Master**: Form-based entity editing with validation

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

1. When modifying entities, ensure companion objects are updated
2. Use annotation processors for compile-time validation
3. Test with multiple database engines for compatibility
4. Follow the established patterns for consistency
5. Leverage the meta-property system for runtime validation
6. Use Entity Centre configuration DSL for UI setup

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
