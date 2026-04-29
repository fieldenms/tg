# Web UI — Detailed Reference

For standard actions, criterion/editor types, and centre/master options, see `quick-reference.md` in this directory.

Web UI is built around three component types configured via fluent DSL builders.
All components must be registered with `IWebUiBuilder` (via `configApp()` in `IWebUiConfig`).

## Application Configuration Entry Point

```java
public class AppWebUiConfig extends AbstractWebUiConfig {
    @Override
    public void initConfiguration() {
        final IWebUiBuilder builder = configApp();
        VehicleWebUiConfig.register(injector, builder);
        configDesktopMainMenu()
            .addModule("Fleet").description("Fleet Management").icon("menu:fleet")
                .bgColor("#00D4AA").captionBgColor("#00AA88")
                .menu()
                    .addMenuItem("Vehicles").centre(vehicleCentre).done()
                .done()
            .done()
            .setLayoutFor(Device.DESKTOP, null, "[[], [], []]");
    }
}
```

## Entity Centre

Grid/listing component via `EntityCentreBuilder`:

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

**Criterion types:**
- `.asMulti().autocompleter(EntityType.class)` / `.text()` / `.bool()` — multi-value
- `.asSingle().autocompleter(EntityType.class)` / `.text()` / `.integer()` / `.decimal()` / `.date()` / `.dateTime()` — single value (requires `@CritOnly(SINGLE)`)
- `.asRange().integer()` / `.decimal()` / `.date()` / `.dateTime()` / `.time()` — range

**Result property options:** `.order(n).asc()/.desc()`, `.width(px)/.minWidth(px)`, `.withSummary(alias, eqlExpr, titleAndDesc)`, `.withAction(config)`, `.withWordWrap()`

**Centre options:** `.runAutomatically()`, `.hideCheckboxes()`, `.hideToolbar()`, `.setPageCapacity(n)`, `.retrieveAll()`, `.enforcePostSaveRefresh()`, `.hasEventSource(EventSourceClass.class)`

## Entity Master

Form-based editing via `SimpleMasterBuilder`:

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

**Property editor types:** `.asSinglelineText()`, `.asMultilineText()`, `.asRichText()`, `.asAutocompleter()`, `.asDateTimePicker()/.asDatePicker()/.asTimePicker()`, `.asDecimal()/.asSpinner()/.asMoney()`, `.asCheckbox()`, `.asColour()`, `.asHyperlink()`, `.asCollectionalRepresentor()/.asCollectionalEditor()`, `.asFile()`

**Standard actions:** `MasterActions.SAVE` (ctrl+s), `MasterActions.REFRESH` (ctrl+x), `MasterActions.VALIDATE`, `MasterActions.EDIT`, `MasterActions.VIEW`, `MasterActions.DELETE`, `MasterActions.NEW`

### Layout Subheaders

For masters with many properties, use `LayoutBuilder` with `.subheader()` to group properties semantically:

```java
final String layout = cell(
    cell(cell(CELL_LAYOUT).repeat(2).withGapBetweenCells(MARGIN), ROW_LAYOUT).repeat(4)
    .subheader("Common", SUBHEADER_LAYOUT)
        .cell(cell(CELL_LAYOUT).skip(CELL_LAYOUT).withGapBetweenCells(MARGIN), ROW_LAYOUT)
    .subheader("Time-based", SUBHEADER_LAYOUT)
        .cell(cell(CELL_LAYOUT).repeat(2).withGapBetweenCells(MARGIN), ROW_LAYOUT).repeat(2)
    .subheader("Meter-based", SUBHEADER_LAYOUT)
        .cell(cell(CELL_LAYOUT).repeat(2).withGapBetweenCells(MARGIN), ROW_LAYOUT).repeat(2),
    PADDING_LAYOUT).toString();
```

## Master With Centre

`MasterWithCentreBuilder` embeds an Entity Centre inside a master.
Typically used for insertion points and special views where a centre needs to appear as a master's content.

```java
final IMaster<OpenAction> masterConfig = new MasterWithCentreBuilder<OpenAction>()
    .forEntityWithSaveOnActivate(OpenAction.class)  // Auto-saves on activation to trigger data load
    .withCentre(embeddedCentre)
    .done();
return new EntityMaster<>(OpenAction.class, OpenActionProducer.class, masterConfig, injector);
```

## Compound Master

Multi-menu-item master via `CompoundMasterBuilder`:

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
        .andDefaultItemNumber(0)                        // Default menu item (0-based)
        .done();
builder.register(compoundMaster);
```

Requires:
- Action entity extending `AbstractFunctionalEntityWithCentreContext<T>` (the Java class keeps its historical name; the conceptual term is "action entity")
- Menu item classes extending `AbstractFunctionalEntityForCompoundMenuItem<T>`
- A producer class for the action entity

**Key fetch provider note:** Menu item entities receive their key from the compound master's root entity.
If a menu item DAO needs a key property, the fetch provider on the **root entity's companion** must include it — adding one to the menu item's own companion has no effect.

## Action Configuration

**Standard helpers** (preferred):
```java
StandardActions.NEW_ACTION.mkAction(Entity.class)
StandardActions.DELETE_ACTION.mkAction(Entity.class)
StandardActions.EXPORT_ACTION.mkAction(Entity.class)
StandardActions.EDIT_ACTION.mkAction(Entity.class)
CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction()
Compound.openEdit(OpenAction.class, ENTITY_TITLE, "Edit Entity", dims)
Compound.openNew(OpenAction.class, "add-circle-outline", ENTITY_TITLE, "Add new Entity", dims)
```

**Custom actions:**
```java
action(CopyWorkOrderAction.class)
    .withContext(context().withSelectedEntities().build())
    .icon("ports-menu-actions:wa-copy")
    .shortDesc("Copy Work Order")
    .prefDimForView(mkDim(960, 600, Unit.PX))
    .withNoParentCentreRefresh()
    .build();
```

**Context options:** `.withCurrentEntity()`, `.withSelectedEntities()`, `.withSelectionCrit()`, `.withMasterEntity()`, `.withComputation((entity, context) -> value)`

**Post-actions** — execute JS after action completes:
```java
action(MyAction.class)
    .withContext(context().withCurrentEntity().build())
    .postActionSuccess(myPostAction())    // IPostAction — runs on success
    .postActionError(myErrorAction())     // IPostAction — runs on error
    .build();
```

## Rendering Customiser

`IRenderingCustomiser` allows backend-driven CSS styling of Entity Centre rows based on entity state.
Returns a nested map structure: `property path → CSS property → CSS value`.

```java
public class WorkOrderRenderingCustomiser implements IRenderingCustomiser<Map<String, Map<String, Map<String, String>>>> {
    @Override
    public Optional<Map<String, Map<String, Map<String, String>>>> getCustomRenderingFor(final AbstractEntity<?> entity) {
        final var res = new HashMap<String, Map<String, Map<String, String>>>();
        final WorkOrder wo = (WorkOrder) entity;
        final Colour statusColour = wo.get(WorkOrder_.status().colour());
        if (statusColour != null) {
            setBackgroundColour(res, WorkOrder_.status().toPath(), statusColour.getColourValue());
        }
        return of(res);
    }
}
```

Register in centre config: `.setRenderingCustomiser(WorkOrderRenderingCustomiser.class)`

**Important:** The fetch provider for the centre must include any properties accessed by the customiser (e.g., colour properties).

## Dynamic Columns

Dynamic columns add extra result-set columns to an entity centre at run-time — one column per "group" produced by a collectional property.
The wiring is uniform — a one-to-many association, an `addProps` registration, a fetch provider, an `IDynamicColumnBuilder` implementation — but **how the column set is decided** splits cleanly into two patterns.
Both are equally important and routinely deployed.

| | Pattern A — criteria-driven | Pattern B — data-driven |
|---|---|---|
| "One" entity | Synthetic (`model_` over an existing table) | Generative (persistent + `WithCreatedByUser`, populated by `IGenerator`) |
| Column source | Computed deterministically from selection criteria (e.g. iterate dates between `fromDate` and `toDate`) | Discovered post-generation by aggregate-querying the "many" table |
| Group key shape | Always present, deterministic format (`yyyyMMdd`, `yyyyMM`, `yyyy`, …) | May be null — handled with a sentinel UUID and a calculated `dynColumnKey` helper |
| Pre-processor work | Often substantial — composes a display string from each collectional element | Usually a no-op |
| Typical use | Calendars, time-series grids, period-over-period | Distribution / breakdown reports with user-driven facets |

### Common prerequisites

A **one-to-many association** between the centre's entity (the "one" side) and a collectional element entity (the "many" side).
The "many" entity carries three roles:

- **group key** — determines which column a value belongs to; each distinct key becomes one column.
- **display value** — what the cell renders.
- **link property** — references the "one" side via `@IsProperty(linkProperty = "...")` on the collection.

```java
// "One" side — the entity shown in the centre.
@IsProperty(value = MyGroup.class, linkProperty = "parent")
@Title("Groups")
private final Set<MyGroup> groups = new TreeSet<>();
```

**The matching contract.** The string passed to `builder.addColumn(id)` and the row's group-key value are compared as strings; if they don't match exactly, the cell stays blank.
This is the most common cause of "all my dynamic cells are empty" — the format used to yield the row's group key (in EQL or in a calculated property) and the column-builder's id format must agree.

### Pattern A — criteria-driven (synthetic)

The centre's entity is synthetic; its `model_` joins the source table and emits one collectional row per `(parent, groupKey)` pair, with `groupKey` formatted in EQL according to whichever bucketing the criteria selected (year, month, week, …).
Every row has a deterministic bucket, so nulls don't arise.

**The "many" entity** is a synthetic entity whose `model_` projects the bucketed rows; its `groupKey` is a plain `String` formatted to match the column ids the builder will produce, and its display string is computed by the pre-processor:

```java
@IsProperty @CompositeKeyMember(1) private MyParent parent;
@IsProperty @CompositeKeyMember(2) private String groupKey;        // e.g. "20260302" (week), "202603" (month), "2026" (year)
@IsProperty                        private String valueToDisplay;  // computed by the pre-processor
```

**The pre-processor** is where most of the per-row work lives — combine the collectional element's data with selection criteria into the display string:

```java
private static void prepCollectionalElements(final MyParent entity, final Optional<CentreContext<MyParent, ?>> maybeContext) {
    final var ctx = decompose(maybeContext);
    if (ctx.contextNotEmpty() && ctx.selectionCritNotEmpty()) {
        final var params = ctx.selectionCrit();
        final boolean showDuplicates = params.get(critName(MyParent.class, "showDuplicates"));
        entity.getGroups().forEach(g -> g.setValueToDisplay(/* derived from g + criteria */));
    }
}
```

**The column builder** asks a typed companion for the actual data window (so columns aren't rendered for empty stretches), then iterates buckets between the criteria-supplied bounds, calling `addColumn(...)` with an id that matches the EQL-produced group key:

```java
private static class DynColumnsByPeriod implements IDynamicColumnBuilder<MyParent> {

    private final IMyParent co;

    @Inject
    public DynColumnsByPeriod(final IMyParent co) { this.co = co; }

    @Override
    public Optional<IDynamicColumnConfig> getColumnsConfig(final Optional<CentreContext<MyParent, ?>> context) {
        final var ctx = decompose(context);
        if (ctx.contextEmpty() || ctx.selectionCritEmpty()) return empty();

        final var params = ctx.selectionCrit();
        final IDynamicColumnBuilderAddPropWithDone propBuilder =
                DynamicColumnBuilder.forProperty(MyParent.class, MyParent_.groups())
                        .withGroupProp(MyGroup_.groupKey())
                        .withDisplayProp(MyGroup_.valueToDisplay());

        // Clamp the criteria range to the data window.
        final Date fromDate = params.get(critName(MyParent.class, "fromDate"));
        final Date toDate   = params.get(critName(MyParent.class, "toDate"));
        final Date startDate = co.getFirstGroupDate().filter(d -> fromDate == null || d.after(fromDate)).orElse(fromDate);
        final Date endDate   = co.getLastGroupDate() .filter(d -> toDate   == null || d.before(toDate)).orElse(toDate);

        final var grouping = params.<MyGroupingProperty>get(critName(MyParent.class, MyParent_.groupingProperty()));
        return switch (MyGroupingProperty.GroupingProperty.fromValue(grouping)) {
            case YEAR  -> Optional.of(buildYearColumns (startDate, endDate, propBuilder));
            case MONTH -> Optional.of(buildMonthColumns(startDate, endDate, propBuilder));
            case WEEK  -> Optional.of(buildWeekColumns (startDate, endDate, propBuilder));
            default    -> throw new MyModuleException("Grouping property must be specified.");
        };
    }

    private IDynamicColumnConfig buildMonthColumns(final Date from, final Date to, final IDynamicColumnBuilderAddPropWithDone propBuilder) {
        // Iterate months between from and to; the addColumn id MUST match the EQL groupKey format.
        // propBuilder.addColumn("202603").title("Mar 2026").desc("March 2026").minWidth(100);
        return propBuilder.done();
    }
    // buildYearColumns / buildWeekColumns — analogous.
}
```

The intermediate type returned by `DynamicColumnBuilder.forProperty(...).withGroupProp(...).withDisplayProp(...)` is `IDynamicColumnBuilderAddPropWithDone` — name it explicitly when factoring out helper methods like `buildMonthColumns`.

### Pattern B — data-driven (generative)

The centre's entity is generative; `IGenerator.gen()` writes both the "one" rows and the "many" rows into persistent tables.
Group keys originate in user data and may be null, so the "many" entity needs a sentinel-based workaround.

**Sentinel + calculated `dynColumnKey`.**
The Web UI silently skips rows whose group property is null, so a calculated property substitutes a UUID literal for `null`; the literal cannot collide with any real key:

```java
public static final String NULL_GROUP_KEY = "68ef4daf-4bf7-4b56-8797-4d7c50750afa";

@IsProperty @CompositeKeyMember(1) @MapTo
private MyParent parent;

@IsProperty @CompositeKeyMember(2) @MapTo @Optional
private String groupKey;        // user-supplied, may be null

@IsProperty @Calculated
@Title(value = "Dynamic Column Key", desc = "Helper property based on Group Key but with a default value instead of null.")
private String dynColumnKey;
protected static final ExpressionModel dynColumnKey_ = expr().ifNull().prop(MyGroup_.groupKey()).then().val(NULL_GROUP_KEY).model();

@IsProperty @MapTo
private BigDecimal compliancePercent;   // shown in the cell
```

Use `dynColumnKey` (not `groupKey`) as the dynamic-columns group property and map the sentinel back to a readable title in the column builder.
The pre-processor for this pattern is typically a no-op — display values are already in `@MapTo` columns written by `gen()`.

**The column builder** discovers distinct groups by aggregate-querying the just-generated table and maps the sentinel back to a readable title:

```java
public static final String GROUP_KEY_NULL_TITLE = "Unassigned";
public static final String GROUP_KEY_NULL_DESC = "A group for entries with unassigned %s";

private record DynColumnsByGroup(IUserProvider userProvider, ICompanionObjectFinder coFinder)
        implements IDynamicColumnBuilder<MyParent> {

    @Inject DynColumnsByGroup {}

    @Override
    public Optional<IDynamicColumnConfig> getColumnsConfig(final Optional<CentreContext<MyParent, ?>> context) {
        final var ctx = decompose(context);
        if (ctx.contextEmpty() || ctx.selectionCritEmpty()) return empty();

        final var params = ctx.selectionCrit();
        return Optional.ofNullable(params.<MyGroupingProperty>get(critName(MyParent.class, MyParent_.groupingProperty())))
                .map(MyGroupingProperty.GroupingProperty::fromValue)
                .flatMap(groupingProp -> {
                    final var builder = DynamicColumnBuilder.forProperty(MyParent.class, MyParent_.groups())
                            .withGroupProp(MyGroup_.dynColumnKey())
                            .withDisplayProp(MyGroup_.compliancePercent());

                    // Aggregate-query the just-generated rows for distinct (dynColumnKey, groupDesc).
                    final var query = select(select(MyGroup.class).where()
                                                .prop(MyGroup_.parent().createdBy()).eq().val(userProvider.getUser())
                                                .yield().prop(MyGroup_.dynColumnKey()).as("dynColumnKey")
                                                .yield().prop(MyGroup_.groupDesc()).as("groupDesc")
                                                .modelAsAggregate())
                            .groupBy().prop("dynColumnKey")
                            .orderBy().caseWhen().prop("dynColumnKey").eq().val(NULL_GROUP_KEY).then().val(1).otherwise().val(0).end().asc()
                                      .prop("dynColumnKey").asc()
                            .yield().prop("dynColumnKey").as("dynColumnKey")
                            .yield().maxOf().prop("groupDesc").as("groupDesc")
                            .modelAsAggregate();

                    final var aggs = coFinder.find(EntityAggregates.class, true).getAllEntities(from(query).model());
                    if (aggs.isEmpty()) return empty();

                    aggs.forEach(agg -> {
                        final String dynColumnKey = agg.get("dynColumnKey");
                        final String groupTitle = NULL_GROUP_KEY.equals(dynColumnKey) ? GROUP_KEY_NULL_TITLE : dynColumnKey;
                        final String groupDesc  = NULL_GROUP_KEY.equals(dynColumnKey)
                                ? GROUP_KEY_NULL_DESC.formatted(groupingProp.key)
                                : agg.<String>get("groupDesc");
                        builder.addColumn(dynColumnKey).title(groupTitle).desc(groupDesc);
                    });
                    return Optional.of(builder.done());
                });
    }
}
```

Note the `.map(...::fromValue)` step: `params.get(...)` returns the `@CritOnly` *entity* (`MyGroupingProperty`), not its inner enum value — convert via the entity's `fromValue` adapter before reading enum metadata fields like `groupingProp.key`.

### Centre wiring — common to both patterns

Two pieces on the centre config, identical in shape regardless of which pattern is used:

```java
.addProps(MyParent_.groups(), DynColumnsByX.class,
          MyWebUiConfig::prepCollectionalElements,
          context().withSelectionCrit().build())

.setFetchProvider(fetchWithKeyAndDesc(MyParent.class)
                  .with(MyParent_.groups(),
                        fetchWithKeyAndDesc(MyGroup.class)
                                .with(MyGroup_.groupKey(), MyGroup_.valueToDisplay())))
```

- `addProps(propName, dynColBuilderType, entityPreProcessor, contextConfig)` — there is also a 5-arg overload taking a rendering-hints provider before `contextConfig`.
- The fetch provider must include the collectional property *and* the sub-properties read for both the group key and the display (e.g. `dynColumnKey`, `groupDesc`, `compliancePercent` for pattern B).
- Style each column via the chained calls on `addColumn(...)` — `.title(...)`, `.desc(...)`, `.minWidth(px)`.

### Runtime flow

1. The user populates selection criteria and clicks *Run*.
2. **Pattern B only:** the centre invokes `IGenerator.gen()`, which clears the previous run for this user and writes fresh "one" / "many" rows.
   **Pattern A:** no generator step — the centre executes the synthetic `model_` to produce the result set.
3. The centre invokes `getColumnsConfig()` on the `IDynamicColumnBuilder`, passing the selection-crit context.
4. The builder returns an `IDynamicColumnConfig`: pattern A enumerates buckets from the criteria; pattern B aggregate-queries the "many" table for distinct group values.
5. For each row, the centre matches the row's group-key value to a column id and renders the display property in that column's cell.

### Key imports

```java
import ua.com.fielden.platform.web.centre.api.IDynamicColumnConfig;
import ua.com.fielden.platform.web.centre.api.impl.DynamicColumnBuilder;
import ua.com.fielden.platform.web.centre.api.resultset.IDynamicColumnBuilder;
import ua.com.fielden.platform.web.centre.api.dynamic_columns.IDynamicColumnBuilderAddPropWithDone;
import static ua.com.fielden.platform.entity.IContextDecomposer.decompose;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.critName;
```

## Insertion Points

Insertion points inject additional views (typically centres) into an Entity Centre, shown as alternative panels.

```java
EntityCentreBuilder.centreFor(MyWorkOrder.class)
    // ... standard centre config ...
    .addInsertionPoint(
        action(MyWorkPotentialsInsertionPoint.class)
            .withContext(context().withSelectionCrit().build())
            .icon("stub")
            .shortDesc("Unassigned Scheduled WOs")
            .longDesc("Scheduled work orders not assigned to anybody.")
            .prefDimForView(mkDim(1024, 300, Unit.PX))
            .withNoParentCentreRefresh()
            .build(),
        InsertionPoints.ALTERNATIVE_VIEW)
    .build();
```

Each insertion point needs:
- An action entity (the insertion point action)
- A master wrapping the embedded centre, built via `MasterWithCentreBuilder`:

```java
final IMaster<OpenInsertionPointAction> masterConfig = new MasterWithCentreBuilder<OpenInsertionPointAction>()
    .forEntityWithSaveOnActivate(OpenInsertionPointAction.class)
    .withCentre(embeddedCentre)
    .done();
```

`.forEntityWithSaveOnActivate()` auto-saves the entity when the insertion point becomes active, triggering data load.

## Custom Code Injection

For advanced scenarios, inject custom JavaScript into masters:

```java
new MasterWithCentreBuilder<OpenAction>()
    .injectCustomCode(new JsCode("self.$.egi.refreshEntities()"))           // Runs during initialisation
    .injectCustomCodeOnAttach(new GuardCentreRegenerationPostAction(...))    // Runs when element attaches to DOM
    .forEntityWithSaveOnActivate(OpenAction.class)
    .withCentre(centre)
    .done();
```

Use sparingly — prefer declarative configuration.
Typical uses: coordinating refresh between insertion points and parent centres, custom DOM manipulation.

## Query Enhancer Pattern

`IQueryEnhancer` lets a centre configuration mutate the final query after `DynamicQueryBuilder` has finished building it.
Its primary legitimate use is **master-context binding in embedded centres** — making an embedded centre filter by the root Entity Master's key or other context values.

**When NOT to use `IQueryEnhancer`.**
For correlated filters over cross-reference tables driven by the user's selection criteria (the classic Entity Centre filtering scenario), use the declarative `@CritOnly(entityUnderCondition, propUnderCondition)` + `{propName}_` stem pattern on a synthetic `Re*` entity instead — see *Declarative correlated filters* in `entity-model/reference.md`.
That style was added later to TG; historically `IQueryEnhancer` was the only option for correlated filters, but placing correlation logic in the Web UI config layer is against TG's model-driven philosophy.
Use `IQueryEnhancer` for master-context propagation into embedded centres (where the filter genuinely belongs to the UI-composition layer), and use the declarative crit-only pattern for everything else.

**Master-context binding in an embedded centre** (the canonical `IQueryEnhancer` use case):
```java
private static class FuelUsageCentre_QueryEnhancer implements IQueryEnhancer<FuelUsage> {
    @Override
    public ICompleted<FuelUsage> enhanceQuery(IWhere0<FuelUsage> where, Optional<CentreContext<FuelUsage, ?>> context) {
        return enhanceEmbededCentreQuery(where,
            createConditionProperty(FuelUsage_.vehicle()),
            context.get().getMasterEntity().getKey());
    }
}
```

For centres that also need named query parameters, override `enhanceQueryParams()` alongside `enhanceQuery()`:
```java
private static class LabourHoursCentre_QueryEnhancer implements IQueryEnhancer<LabourHours> {
    @Override
    public ICompleted<LabourHours> enhanceQuery(IWhere0<LabourHours> where, Optional<CentreContext<LabourHours, ?>> context) {
        return enhanceEmbededCentreQuery(where, LabourHours_.workOrder(), context.get().getMasterEntity().getKey());
    }

    @Override
    public Map<String, Object> enhanceQueryParams(Map<String, Object> queryParams, Optional<CentreContext<LabourHours, ?>> context) {
        queryParams.put("selectedWA", context.get().getMasterEntity().getKey());
        return queryParams;
    }
}
```

## Audit UI

For the full audit Web UI reference — `IAuditWebUiConfigFactory`, `PersistentEntityInfo` compound master, and the "do not hand-wire audit menu items" rule — see `auditing/reference.md`.

## Server-Sent Events (SSE)

- `IEventSource` / `AbstractEventSource<T, OK>` — implement `eventToData(T event)`
- `SseServlet` — separate Jetty server (default port 8092), configurable via `sse.*` properties
- Integration: `.hasEventSource(EventSourceClass.class).withCountdownRefreshPrompt(5)`
