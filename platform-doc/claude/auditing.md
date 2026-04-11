# Generic Auditing Reference

Generic auditing is a TG platform facility (introduced in 2.3.0) that creates an audit record whenever an audited entity is saved.
An audit record captures a snapshot of the entity's auditable properties together with the set of properties that changed in that save, linked to the user and transaction.
Audit types, their companions, their Web UI, and their security tokens are all source-/runtime-generated — application code only annotates the domain entity and wires a few factory calls.

Wiki companion: [Guide to Auditing](https://github.com/fieldenms/tg/wiki/Guide-to-Auditing).

## Making an entity auditable

Add `@Audited` at the entity class level (`ua.com.fielden.platform.audit.annotations.Audited`).
Presence of the annotation is enough — there are no parameters.

```java
@MapEntityTo
@CompanionObject(VehicleCo.class)
@Audited
public class Vehicle extends ActivatableAbstractEntity<String> { ... }
```

An entity property is *auditable* if:

* it is persistent, **and**
* it is not `id`, `version`, one of the `created*` group, one of the `lastUpdated*` group, `refCount`, and
* it is not annotated with `@DisableAuditing` (`ua.com.fielden.platform.audit.annotations.DisableAuditing`, applied to fields).

To opt a `key` or `desc` property out of auditing, redeclare it in the subclass and place `@DisableAuditing` on the redeclaration.

## Generated audit types

For each audited entity `E`, four types are generated:

| Type | Naming | Base class | Role |
|---|---|---|---|
| Persistent audit-entity | `E_a3t_{n}` | `AbstractAuditEntity<E>` | One row per save event with a full snapshot of auditable property values. Versioned by `{n}`. |
| Persistent audit-prop | `E_a3t_{n}_Prop` | `AbstractAuditProp<E>` | One row per dirty property in each audit event — the "many" side of a 1:N with the persistent audit-entity. |
| Synthetic audit-entity | `Re{E}_a3t` | `AbstractSynAuditEntity<E>` | Read-only UNION over all persistent audit-entity versions of `E`. This is the type the Web UI queries. |
| Synthetic audit-prop | `Re{E}_a3t_Prop` | `AbstractSynAuditProp<E>` | Read-only UNION over all persistent audit-prop versions of `E`. |

Every generated type carries `@AuditFor(value = E.class, version = n)` (persistent) or `@AuditFor(E.class)` (synthetic) so the platform can find audit types at runtime.
Generated types are also annotated with `@CompanionIsGenerated`, `@SkipVerification`, `@SkipEntityRegistration`, `@DenyIntrospection`, and (where appropriate) `@WithoutMetaModel`.

### Naming and column conventions

Auditable property names in an audit type are prefixed with `a3t_` (e.g., `a3t_licence`) so they cannot collide with the base service properties.
Column names of audit-properties are prefixed with `A3T_` (e.g., `A3T_LICENCE_`).

### Service properties common to all persistent audit-entities

Declared on `AbstractAuditEntity<E extends AbstractEntity<?>>`:

| Property | Type | Role |
|---|---|---|
| `auditedEntity` | `E` | Composite key member 1 — reference to the audited entity. Entity-exists validation is disabled so audited entities can be deleted while audit rows remain. |
| `auditedVersion` | `Long` | Composite key member 2 — the `version` of `E` at audit time. |
| `auditDate` | `Date` | Timestamp of the audit event (final, required). |
| `auditUser` | `User` | User that performed the audited save (final, required). |
| `auditedTransactionGuid` | `String` | Ties the record back to the save transaction (final, required). |

Persistent audit-prop types (`AbstractAuditProp<E>`) have a composite key `(auditEntity, property: PropertyDescriptor<AbstractSynAuditEntity<E>>)`.

Synthetic audit-entity types (`AbstractSynAuditEntity<E>`) mirror the service-property layout, plus:
* `changedProps: Set<? extends AbstractSynAuditProp<E>>` — one-to-many with the synthetic audit-prop;
* `changedPropsCrit: PropertyDescriptor<AbstractSynAuditEntity<E>>` — crit-only field for searching by changed property.

## Audit-type versioning

Persistent audit-entity types form an inheritance chain from oldest to newest: for `n` versions of `E`, `E_a3t_n <: … <: E_a3t_1`.
This is how the platform preserves audit history across entity-shape changes.

### Evolving audit types

When the audited entity's structure changes, the developer picks between two strategies per change:

1. **Generate a new version** — produces `E_a3t_{n+1}` (and regenerates the synthetic types).
   Use this when a property is added or removed, so that historical rows remain valid under the old version.
   Removed properties are declared `@InactiveAuditProperty` in the new version, effectively hiding the inherited active one.
2. **Refactor an existing version in place** — modify an existing `E_a3t_{n}` directly.
   Use this for property-type changes, renames, or title changes where declaring a new property would produce a collision.
   Propagate the change through all subsequent versions that might hide the modified property, and mirror it in the synthetic `Re{E}_a3t`.

Only the **latest** persistent audit-entity type is used to create new audit records.
Prior versions hold historical data; the synthetic type unifies them into a rectangular view, filling "holes" with `null` (or `false` for `boolean`).

## Auditing modes and configuration

`AuditingMode` (`ua.com.fielden.platform.audit.AuditingMode`) has three values:

| Mode | Meaning |
|---|---|
| `ENABLED` | Standard runtime mode. Every `@Audited` entity must have its audit types on the audit classpath; audit records are written on save. |
| `GENERATION` | Relaxed bootstrap mode used **only** when running the source generator (`GenAudit`). Permits `@Audited` entities to exist without corresponding audit types yet. |
| `DISABLED` | Auditing is a no-op. No records are written; `IEntityAuditor` is not invoked. Appropriate for unit tests where audit overhead is unwanted — see *Testing with auditing* below. |

Two application properties control auditing, both defined in `AuditingIocModule`:

| Property | Constant | Purpose |
|---|---|---|
| `audit.mode` | `AuditingIocModule.AUDIT_MODE` | The mode name (`ENABLED` / `GENERATION` / `DISABLED`). Default is `ENABLED`. A system property of the same name overrides the application property. |
| `audit.path` | `AuditingIocModule.AUDIT_PATH` | Classpath root where audit `.class` files live — a JAR for deployments, typically `../app-pojo-bl/target/classes` for development. Required whenever the active mode is **not** `DISABLED`; a missing or blank value produces `ApplicationConfigurationException` at startup. Not a system property. Rule of thumb: wherever `tokens.path` is set, `audit.path` should be set too. |

`audit.mode` should be set in `application.properties`, test configurations (`IDomainDrivenTestCaseConfiguration`), and data-population configs.
`audit.path` can be omitted when the chosen mode is `DISABLED`.

## Automatic audit-record creation

`PersistentEntitySaver` hooks into entity save and, after `super.save()` succeeds, checks `AuditUtils.isAudited(entityType)`.
If the entity is audited and `AuditingMode` is `ENABLED`, it resolves the latest persistent audit-entity type via `IAuditTypeFinder.navigate(E.class).auditEntityType()`, obtains its runtime-generated companion, and calls:

```java
void audit(Long auditedEntityId,
           Long auditedEntityVersion,
           String transactionGuid,
           Collection<String> dirtyProperties);
```

on `IEntityAuditor<E>`.
The call **participates in the caller's transaction** — `IEntityAuditor.audit(...)` is deliberately not `@SessionRequired`, so an audit failure rolls back the original save.
Only auditable properties that are dirty at save time produce `_Prop` rows; if nothing auditable is dirty, no audit row is written.

## Runtime plumbing

### `IAuditTypeFinder` — runtime E ↔ audit-type mapping

`IAuditTypeFinder` (`ua.com.fielden.platform.audit.IAuditTypeFinder`) is the single lookup point between audited types and their audit types.
`finder.navigate(E.class)` returns a `Navigator<E>` with methods including:

```java
Class<E> auditedType();

Class<AbstractSynAuditEntity<E>> synAuditEntityType();
Optional<Class<AbstractSynAuditEntity<E>>> findSynAuditEntityType();
Class<AbstractSynAuditProp<E>>  synAuditPropType();
Optional<Class<AbstractSynAuditProp<E>>> findSynAuditPropType();

Class<AbstractAuditEntity<E>>   auditEntityType();                 // latest version
Optional<Class<AbstractAuditEntity<E>>> findAuditEntityType();
Class<AbstractAuditEntity<E>>   auditEntityType(int version);      // specific version
Optional<Class<AbstractAuditEntity<E>>> findAuditEntityType(int version);
Collection<Class<AbstractAuditEntity<E>>> allAuditEntityTypes();

Class<AbstractAuditProp<E>>     auditPropType();                   // latest version
Optional<Class<AbstractAuditProp<E>>> findAuditPropType();
Class<AbstractAuditProp<E>>     auditPropType(int version);
Optional<Class<AbstractAuditProp<E>>> findAuditPropType(int version);
Collection<Class<AbstractAuditProp<E>>>  allAuditPropTypes();

List<Class<? extends AbstractEntity<?>>>       allPersistentAuditTypes();  // versions + props
Collection<Class<? extends AbstractEntity<?>>> allAuditTypes();            // the above + synthetic pair
```

The `find*` variants return `Optional` and are safe to use in `GENERATION` mode (where audit types may not yet exist).
The non-optional variants assume the audit types are present and will fail otherwise.

Reverse navigation is also supported via `IAuditTypeFinder.navigateAudit(Class<AbstractAuditEntity<E>>)`, `.navigateAuditProp(...)`, `.navigateSynAudit(...)`, `.navigateSynAuditProp(...)` — each returning a `Navigator<E>` for the *audited* type.

> [!WARNING]
> `IAuditTypeFinder` is still bound and injectable when auditing is `DISABLED`, but every `navigate*` method throws `AuditingModeException`.
> This is a trap for tooling classes (`GenDdl`, `GenAudit`) that inject `IAuditTypeFinder` — they must run under `ENABLED` or `GENERATION` mode, never `DISABLED`.

### Generated companions — never hand-write

Audit-type companions are generated at application startup by `ICompanionGenerator` / `AuditCompanionGenerator` using ByteBuddy.
Each generated companion subclasses one of:

| Base companion | Used for |
|---|---|
| `CommonAuditEntityDao<E>` | Persistent audit-entity (`E_a3t_{n}`) |
| `CommonAuditPropDao<E>` | Persistent audit-prop (`E_a3t_{n}_Prop`) |
| `CommonSynAuditEntityDao<E>` | Synthetic audit-entity (`Re{E}_a3t`) — implements `ISynAuditEntityDao<E>` and `IEntityAuditor<E>` |
| `CommonEntityDao<…>` | Synthetic audit-prop (`Re{E}_a3t_Prop`) |

Application code should never write a companion for an audit type.
If you see `WorkActivity_a3t_1_Dao.java` (or similar) in an application module, delete it.

### Generated security tokens

Security tokens for audit types are generated at application startup by `ISecurityTokenGenerator` and made available through `ISecurityTokenProvider`.
Only the synthetic audit-entity side receives tokens: `Re{E}_a3t_CanRead_Token` and `Re{E}_a3t_CanReadModel_Token` (no `CanSave`/`CanDelete` — audit records are immutable from the user's perspective).

Customisation is possible by extending `SecurityTokenProvider` and overriding the relevant methods.

### Audit types are *dynamically* registered in the application domain

The generated `ApplicationDomain` class contains only the compile-time entity types.
A freshly instantiated `new ApplicationDomain().entityTypes()` does **not** include audit types, regardless of auditing mode.

`BasicWebServerIocModule.provideApplicationDomain(IAuditTypeFinder, AuditingMode)` provides a **different** `IApplicationDomainProvider` via `@Provides` — a lambda that starts from the generated provider's entity types and, for each `@Audited` entity, appends all of its audit types.
What gets appended depends on the mode:

* `ENABLED` — appends every persistent audit-entity type (`E_a3t_{n}` for all `n`), each paired with its corresponding audit-prop type, plus the synthetic audit-entity and synthetic audit-prop. All are required to exist; missing types cause failure at binding time.
* `GENERATION` — same shape, but uses the `find*` optional variants on `IAuditTypeFinder.Navigator` so missing types are silently skipped.
* `DISABLED` — no audit types are appended; the provider returns only the compile-time types.

The consequence for application code:

* Anywhere that needs the *full* entity-type list (including audit types), **inject `IApplicationDomainProvider` and use the injector-supplied instance**.
  Do not instantiate `new ApplicationDomain()` directly — that bypasses the augmentation.
* Typical offenders in older TG applications:
  * `PersistDomainMetadataModel.persist(...)` — must receive the augmented list so audit tables have domain metadata rows.
  * Any startup code that iterates entity types for registration or reflection.
* `EntityMetadata.build(...)` is the exception: passing the locally-instantiated `ApplicationDomain` is fine because most audit types do not need that metadata.

### `AuditUtils`

Common helpers in `ua.com.fielden.platform.audit.AuditUtils`:

```java
boolean isAudited(Class<? extends AbstractEntity<?>> type);
boolean isAuditEntityType(Class<?> type);
boolean isSynAuditEntityType(Class<?> type);
boolean isAuditPropEntityType(Class<?> type);
boolean isSynAuditPropEntityType(Class<?> type);
boolean isAuditProperty(CharSequence property);

String auditPropertyName(CharSequence auditedPropertyName);        // e.g., "licence" → "a3t_licence"
String auditedPropertyName(CharSequence auditPropertyName);        // e.g., "a3t_licence" → "licence"
String getAuditTypeName(Class<? extends AbstractEntity<?>>, int);  // e.g., (Vehicle.class, 2) → "…Vehicle_a3t_2"
int    getAuditTypeVersion(Class<? extends AbstractEntity<?>> type);
```

### `ISynAuditEntityDao` — reading audits

`ISynAuditEntityDao<E>` is the read-side contract exposed by every synthetic audit-entity companion:

```java
Stream<AbstractSynAuditEntity<E>> streamAudits(Long auditedEntityId, fetch<…> fetchModel);
List<AbstractSynAuditEntity<E>>   getAudits(Long auditedEntityId, fetch<…> fetchModel);
AbstractSynAuditEntity<E>         getAuditOrThrow(Long auditedEntityId, Long version, fetch<…> fetchModel);
Optional<AbstractSynAuditEntity<E>> getAuditOptional(Long auditedEntityId, Long version, fetch<…> fetchModel);
```

(Overloads exist that accept the audited entity `E` in place of `Long auditedEntityId`.)

Application code should prefer these over raw EQL against the generated types — the generated class names change with versioning.

## Source generation tooling

### Project layout

Generated audit sources live in a **separate source root** in the `pojo-bl` module, conventionally `src/audit/java/`.
Register it in the module's `pom.xml`:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.6.0</version>
    <executions>
        <execution>
            <id>audit-sources</id>
            <phase>generate-sources</phase>
            <goals><goal>add-source</goal></goals>
            <configuration>
                <sources><source>src/audit/java/</source></sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

The source root must correspond to the location `audit.path` resolves to at runtime (typically `target/classes/` of the same module).

### `IAuditEntityGenerator`

```java
Collection<Path> generate(
    Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes,
    Path sourceRoot,
    VersionStrategy versionStrategy);

enum VersionStrategy {
    NEW,             // generate a new version (e.g., E_a3t_{n+1})
    OVERWRITE_LAST   // regenerate the latest version in place
}
```

Use `NEW` for property adds/removes that must preserve history; use `OVERWRITE_LAST` for cosmetic regenerations during development before the first release ships.
After generation, run `GenDdl` to produce DDL for the new/updated audit tables and write the corresponding SQL migration script.

### `GenAudit` utility pattern

Every TG application should have a `GenAudit` main class alongside `GenDdl`, driven from `application.properties`, that calls `IAuditEntityGenerator.generate(...)`.
Sketch:

```java
public final class GenAudit {
    public static void main(final String[] args) throws Exception {
        final var props = loadProps(args);
        final var config = new AuditGenerationConfig(props);
        final var generator = config.getInstance(IAuditEntityGenerator.class);
        generator.generate(
            Set.of(Vehicle.class, /* other @Audited entities */),
            Path.of("../app-pojo-bl/src/audit/java"),
            VersionStrategy.NEW);
    }
}
```

`AuditGenerationConfig` is a separate `IDomainDrivenTestCaseConfiguration` that forces `AUDIT_MODE = GENERATION` before building the Guice module — otherwise the generator cannot start because audit types for the entities being generated do not yet exist.

> [!WARNING]
> Do not let the IDE auto-import `ua.com.fielden.platform.audit.AuditGenerationConfig` from the TG platform — that is a test-only class. Create your own `AuditGenerationConfig` in the application's `dev_mod/util` package.

### `GenDdl`

`GenDdl` should generate DDL for both ordinary entities and audit entities.
The audit block obtains the types through `IAuditTypeFinder`:

```java
final var auditTypeFinder = config.getInstance(IAuditTypeFinder.class);
auditedDomainEntities.forEach(auditedType -> {
    final List<Class<? extends AbstractEntity<?>>> auditEntities =
        auditTypeFinder.navigate(auditedType).allPersistentAuditTypes();
    ddlGenerator.generateDatabaseDdl(dialect, auditEntities).forEach(System.out::println);
});
```

`allPersistentAuditTypes()` returns every persistent audit-entity and audit-prop across all versions, so DDL for the entire audit history is produced in one call.

## Web UI integration

Two orthogonal integration points:

1. A **stand-alone audit centre** under a main-menu item — built through `IAuditWebUiConfigFactory`.
2. An **audit tab on every persistent entity's info master** — built dynamically by the platform via `PersistentEntityInfo`; **no per-application code required**.

### Stand-alone audit centre via `IAuditWebUiConfigFactory`

`ua.com.fielden.platform.web.audit.IAuditWebUiConfigFactory`:

```java
AuditWebUiConfig create(Class<? extends AbstractEntity<?>> auditedType, IWebUiBuilder builder);
EntityCentre<?> createEmbeddedCentre(Class<? extends AbstractEntity<?>> auditedType);
Class<MiWithConfigurationSupport<?>> miTypeForEmbeddedCentre(Class<? extends AbstractEntity<?>> auditedType);
```

`AuditWebUiConfig` is a record with accessors `auditType()` and `centre()`.

Usage in the application's `IWebUiConfig.initConfiguration()`:

```java
final var auditWebUiConfigFactory = injector().getInstance(IAuditWebUiConfigFactory.class);
final var vehicleAuditConfig = auditWebUiConfigFactory.create(Vehicle.class, builder);

configDesktopMainMenu()
    .addModule(APP.title)
        .menu()
            .addMenuItem(makeMenuItemTitle(vehicleAuditConfig.auditType()))
                .description(makeMenuItemDesc(vehicleAuditConfig.auditType()))
                .centre(vehicleAuditConfig.centre())
                .done()
            ...
```

Mi-types and producers for stand-alone audit centres are generated at runtime — do not write them by hand.

### `PersistentEntityInfo` — the dynamically-built info master

`PersistentEntityInfo` (`ua.com.fielden.platform.entity.PersistentEntityInfo`) is the functional action entity that backs the standard "info" button on every `AbstractPersistentEntity` master.
At runtime the platform opens `PersistentEntityInfo` in one of two shapes depending on whether the target entity type is `@Audited`:

| Target entity | Master shape | Built by |
|---|---|---|
| Not `@Audited` | **Simple master** with just the version-info fields (`createdBy`, `createdDate`, `lastUpdatedBy`, `lastUpdatedDate`, `entityId`, `entityVersion`). | `StandardMastersWebUiConfig.createPersistentEntityInfoSimpleMaster(injector)` |
| `@Audited` | **Compound master** `OpenPersistentEntityInfoAction` with two menu items: main info + a polymorphic audit centre. | `StandardMastersWebUiConfig.createPersistentEntityInfoCompoundMaster(injector, builder, mainMaster)` |

The compound variant uses `.addMenuItem(AuditCompoundMenuItem.class).withPolymorphicCenter()`.
`AuditCompoundMenuItem` extends `AbstractPolymorphicCentreCompoundMenuItem<PersistentEntityInfo>` — a polymorphic centre whose *actual* entity type is decided at runtime.
`IAuditMenuItemInitialiser.init(auditedType, menuItem)` fills the menu item with centre metadata for the correct synthetic audit-entity type (`Re{E}_a3t`), so the very same `AuditCompoundMenuItem` shell is parameterised differently each time it is opened.

Both masters are registered once by the platform and reused for every persistent entity in the application.

### Do not hand-wire audit tabs in application compound masters

> [!IMPORTANT]
> When an existing `@Audited` entity's info/open-master is opened, the platform's `PersistentEntityInfo` compound master already hosts the audit-review menu. Do not add an "Audit" tab to the entity's own `OpenEMasterAction` compound master — that duplicates the platform's standard UI and will drift out of sync.

Concretely, in `*WebUiConfig` classes for audited entities:

* **Do not** add `.addMenuItem(EMaster_OpenReE_a3t_MenuItem.class).withView(embeddedAuditCentre)` to the compound-master builder.
* **Do not** call `IAuditWebUiConfigFactory.createEmbeddedCentre(E.class)` to embed an audit centre in the entity's own compound master — the factory is provided for the platform's own use through `PersistentEntityInfo`; applications call only `create(...)` (for the stand-alone main-menu centre).
* **Do not** define `MiEMaster_OpenReE_a3t_MenuItem` / producer / companion for an audit tab; the platform owns this.

When migrating an older TG application with a hand-written audit tab (a common shape before 2.3.0), the migration is a straight deletion of the tab and its supporting classes — no replacement factory call is needed.

## Testing with auditing

Unit tests that exercise entity saves pay a real cost with auditing enabled — every audited save writes one persistent audit-entity row plus one persistent audit-prop row per dirty property.
The conventional trade-off in TG-based applications is to **disable auditing in DAO unit tests** via the test configuration:

```java
// In DaoDomainDrivenTestCaseConfiguration
props.setProperty(AuditingIocModule.AUDIT_MODE, AuditingMode.DISABLED.name());
// audit.path is not needed when the mode is DISABLED
```

Data-population configurations (`DataPopulationConfig`) and the live webapp still run with `AUDIT_MODE = ENABLED`, so web-UI tests and manual testing exercise the audit path.
If a specific test genuinely needs auditing on, it should configure a custom `IDomainDrivenTestCaseConfiguration` rather than flip the shared DAO test config.

Do not interpret "tests must pass with auditing enabled" as "every DAO test must run with `AUDIT_MODE = ENABLED`" — the requirement is satisfied at the webapp and data-population level.

## Auditing external entity types

Auditing of external entity types (e.g., TG's own `User`) is currently unsupported.
`@Audited` must be present on the entity's own source, which rules out platform-owned types.