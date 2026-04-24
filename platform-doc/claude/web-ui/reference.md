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