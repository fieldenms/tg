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

**Centre options:** `.runAutomatically()`, `.hideCheckboxes()`, `.hideToolbar()`, `.setPageCapacity(n)`, `.retrieveAll()`, `.enforcePostSaveRefresh()`, `.hasEventSource(EventSourceClass.class)`, `.withMinAutoRefreshInterval(seconds)`

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

**Property editor types:** `.asSinglelineText()`, `.asMultilineText()`, `.asRichText()`, `.asAutocompleter()`, `.asDateTimePicker()/.asDatePicker()/.asTimePicker()`, `.asDecimal()/.asSpinner()/.asMoney()`, `.asCheckbox()`, `.asColour()`, `.asHyperlink()`, `.asCollectionalRepresentor()/.asCollectionalEditor()`, `.asFile()`, `.asComponent(importPath)` (see *Master Components*)

**Standard actions:** `MasterActions.SAVE` (ctrl+s), `MasterActions.REFRESH` (ctrl+x), `MasterActions.VALIDATE`, `MasterActions.EDIT`, `MasterActions.VIEW`, `MasterActions.DELETE`, `MasterActions.NEW`

### Layout — direct builder

The `LayoutComposer.mkGrid*` helpers (`mkGridForMasterFitWidth`, `mkGridForCentre`, `mkGridForMaster`) produce a uniform N×M grid; `mkVarGrid*` lets each row pick its own column count but still constrains every cell to the same shape.
For anything beyond that — **skipped cells, variable-height rows (e.g. for `asMultilineText` / `asRichText`), subheaders, or mixed cell sizes within a row** — reach for `LayoutBuilder.cell(...)` directly.
A practical rule: if any property is multiline (and you want the editor to grow with the master's height), or any row needs a skipped slot, a per-row gap/padding override, or a subheader between groups, the helpers won't fit and you should switch to the builder.

**Variable-height row for a multiline editor.**
Tag the row carrying the multiline editor with `FLEXIBLE_ROW` so it expands to fill remaining vertical space; the surrounding `FLEXIBLE_LAYOUT_WITH_PADDING` makes the master itself stretch to its dimensions.
`skip(CELL_LAYOUT)` leaves a blank slot in an otherwise-3-cell row.

```java
final var layout = cell(
        cell(cell(CELL_LAYOUT).repeat(3).withGapBetweenCells(MARGIN))
        .cell(cell(CELL_LAYOUT).repeat(3).withGapBetweenCells(MARGIN))
        .cell(cell(CELL_LAYOUT).repeat(2).skip(CELL_LAYOUT).withGapBetweenCells(MARGIN))
        .cell(cell(CELL_LAYOUT), FLEXIBLE_ROW)            // multiline editor row
    , FLEXIBLE_LAYOUT_WITH_PADDING).toString();
```

**Subheaders.**
For masters with many properties, use `.subheader(title, SUBHEADER_LAYOUT)` to group them semantically:

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

All constants (`CELL_LAYOUT`, `MARGIN`, `ROW_LAYOUT`, `PADDING_LAYOUT`, `SUBHEADER_LAYOUT`, `FLEXIBLE_ROW`, `FLEXIBLE_LAYOUT_WITH_PADDING`) live on `LayoutComposer`; `cell(...)` is `LayoutBuilder.cell` (`ua.com.fielden.platform.web.layout.api.impl`).

## Master Components

A simple master can contain read-only web components next to its editors, such as a chart, a map or an embedded centre.
A component is bound to the fully-fledged entity of the master; it displays data and invokes actions, but does not change property values.
There are two forms:

- `.addProp(prop).asComponent(importPath)` — a component in place of an editor of the property, which may be dot-notated.
  It receives the title and description of the property, and its property actions are declared as those of editors.
- `.addComponent(importPath)` — a component bound to the entity rather than to a property, placed next to the editors.
  It has no title or description unless declared, and its action is invoked by the component itself.

`importPath` is resolved as `/resources/<importPath>.js`, and the element name defaults to its last segment, such as `tg-map-component` for `gis/tg-map-component`.

```java
final IMaster<Location> masterConfig = new SimpleMasterBuilder<Location>()
    .forEntity(Location.class)
    .addProp(Location_.key()).asSinglelineText().also()
    .addProp(Location_.desc()).asMultilineText().also()
    // a map in place of an editor of the GIS information of the location
    .addProp(Location_.gisInfo()).asComponent("gis/tg-map-component")
        .withAttr("gis-component-uri", "gis/location/tg-location-gis-master-component")
        .withAttr("gis-component-name", "LocationGisMasterComponent")
        .withAttr("labels-toggle", "")
        .skipBlockingWhenUnsaved()          // the map shows a value of the entity itself, which is consistent while it is unsaved
        .withAction(editGisInfoAction)      // a property action, as for editors
        .also()
    // an embedded centre bound to the entity
    .addComponent("centre/tg-centre-component")
        .withTitle("Active Work Orders by Status")
        .withHeight(300)                    // resizable by the user, and kept at least
        .withMinHeight(200)
        .withAttr("mi-type", MiLocationMaster_ActiveWorkOrders.class.getName())
        .also()
    .addAction(MasterActions.REFRESH)
    .addAction(MasterActions.SAVE)
    .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
    .done();
```

### Configuration

The options are declared in this order, and each of them is optional:

| Method | Purpose |
|---|---|
| `withTitle(title)` / `withDesc(desc)` | The caption and its tooltip, in place of the title and description of the property for `asComponent`. `withTitle("")` leaves a component without a title, such as a map whose purpose is evident. |
| `withElementName(name)` | The element name, in place of the last segment of the import path. |
| `withHeight(px)` | Makes the component resizable with a resizer at its bottom right corner; the component keeps this height, or the height set by the user, at least. The height set by the user is remembered in the browser for the user, the entity type of the master and the component; a double tap on the resizer restores the declared height. |
| `withMinHeight(px)` | The least height, below which the component is not shrunk or resized; it cannot be greater than the declared height. By default, it is that of the component itself, such as 200 pixels for a map. |
| `withAttr(name, value)` | An attribute of the element, in dash-case, with a static value or a pass-through binding expression over the master, such as `[[centreUuid]]`. Declaring an attribute again replaces its value. |
| `withPropAttr(name, prop)` | An attribute bound to the value of a property of the master entity, which may be dot-notated or a metamodel reference; the value follows the entity and is passed as is, such as an entity or a collection. |
| `skipBlockingWhenUnsaved()` | Keeps the component interactive while the entity is unsaved (see *Blocking*). |
| `refreshWhen(Condition.class)` | The condition for refreshing the component after a validation of the master entity (see *Refreshing*). |
| `withAction(config)` / `withMultiAction(config)` | A property action of a component in place of an editor, or the action of a component bound to the entity, which the component invokes itself. |

A component has no height of its own, and fills the height of its layout cell.
Each component takes a layout cell in the order of declaration, as an editor does.
In a `FLEXIBLE_ROW` of the master layout, a component grows with the master, and shrinks with the master down to the height that it keeps at least; in a row that takes the height of its content, it has that height.

The element id is `component_4_<property>` for a component in place of an editor, and `component-<n>` for the `n`-th component bound to the entity, counted from `0`.

The configuration is validated as it is declared, and errors are reported with `EntityMasterConfigurationException`, with messages that are public constants of `AbstractComponentWidget` and `WidgetSelector`:

- the import path must be a path relative to `/resources/` without the `.js` extension, and the element name must be a valid custom element name;
- the property of `asComponent` and of `withPropAttr` must exist;
- attribute names must be in lowercase dash-case, and the attributes that the platform renders, such as `id`, `entity`, `prop-title`, `height` and `block-when-unsaved`, cannot be declared;
- binding expressions must be balanced, heights must be positive, and the height cannot be less than the minimum height;
- titles and descriptions cannot contain a straight single quote, a backtick or `${`, which cannot be rendered.

### Blocking

A component is visible as soon as the master is loaded, so, unlike a section of a compound master, nothing would prevent interaction with it while the entity is unsaved.
By default, a component is therefore blocked while the master entity is an instance of a persistent type that is new or has changes that are not yet saved, including edits in progress.
A pane covers the component with a message to save or cancel the changes, and the component is made `inert`, so that neither the component nor its actions can be interacted with, including through the shortcuts of the actions.
`skipBlockingWhenUnsaved()` opts out, for components that display values of the master entity itself, which stay consistent with it while it is unsaved.

### Refreshing

The master asks its components to refresh whenever it retrieves the entity, as when it is opened or its changes are cancelled, and whenever it saves the entity.
Between these, a component in place of an editor is refreshed whenever the value of its property changes, whether the user or a definer changed it, while a component bound to the entity is not refreshed.
`refreshWhen` replaces this with a condition, evaluated on the server after each validation of a change made by the user:

```java
public class VehicleModelChanged implements IComponentRefreshCondition<Vehicle> {
    @Override
    public boolean shouldRefresh(final ComponentRefreshContext<Vehicle> context) {
        return context.isChanged(Vehicle_.model());
    }
}
```

`ComponentRefreshContext` provides the validated entity, with the changes of the user and the values that definers set in turn, and the properties whose values the user changed since the previous validation, which the dirtiness of properties does not tell.
The condition is created with the injector, so its dependencies are injected.
What refreshing does is up to the component, such as running an embedded centre again.
A refresh asked for while the component is blocked takes place once it is unblocked.

### Platform components

**`centre/tg-centre-component`** embeds an entity centre whose query depends on the master entity, usually with `addComponent`.

- Attribute `mi-type` is the class name of the menu item type of the centre, a `MiWithConfigurationSupport` subclass; the centre is registered with `builder.register(centre)` as any other centre.
- The context of the centre includes the master entity with its unsaved changes, so a query enhancer with `context().withMasterEntity()` restricts the query by it.
- The centre runs whenever the component is refreshed, and not while the component is blocked.
- The centre has its own uuid, so its actions do not refresh the master.
- The selection view and the grid fill the component; the grid always fits to height there, whether or not the centre declares `fitToHeight()`, and whatever height or number of visible rows the centre declares or the user configures.
- The centre and the masters of its views do not take the focus from the master when they load or refresh, only when the user acts on them.
- A toolbar without selection criteria suits a centre restricted to the master entity, such as `CentreToolbar.withTopActionsViewSwitchPaginationAndRefresh()`, with `standardEmbeddedScrollingConfig(0)`.

**`gis/tg-map-component`** draws the value of its property, an entity or a collection of entities, with `asComponent`, or the master entity itself with `addComponent`.

- Attribute `gis-component-uri` is the path of the module of the GIS component, relative to `/resources/` and without `.js`, and `gis-component-name` is the name under which the module exports it.
- The GIS component is a subclass of `MasterGisComponent` (`gis/tg-master-gis-component.js`), with the extension points of `GisComponent`, such as `featureType`, `createGeometry`, `createMarkerFactory` and `createBaseLayers`, so the methods of a centre map for the same entity types can be reused; clustering is declared with `createMarkerClusterGroup`.
- Boolean attribute `labels-toggle` adds a toggle of the labels of map features to its toolbar, and `zoom` is the zoom for a single point, `14` by default.
- The property actions are in the toolbar of the map, and reach the map as `action.mapComponent`, and the GIS component as `action.mapComponent.gisComponent`.
- The map zooms with its controls and with the mouse wheel with Option (Alt); the mouse wheel alone scrolls the master.

### Application components

An application component is a Polymer element with the client-side contract of `TgMasterComponentBehavior` (`master/tg-master-component-behavior.js`):

```js
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import { TgMasterComponentBehavior } from '/resources/master/tg-master-component-behavior.js';

const template = html`
    <div class="tg-component-title" hidden$="[[!propTitle]]" tooltip-text$="[[propDesc]]">[[propTitle]]</div>
    <div id="content" tabindex="0">...</div>
    <slot name="property-action"></slot>
`;

class TgFuelUsageChart extends mixinBehaviors([TgMasterComponentBehavior], PolymerElement) {
    static get template() { return template; }
    static get properties() {
        return { mode: { type: String } }; // declared with withAttr("mode", "compact")
    }
    // the element at which the component is focused as a whole, which declares tabindex
    component() { return this.$.content; }
    // asked for by the master, such as when it retrieves or saves the entity
    refresh() { /* load data for this.masterEntityContext() */ }
}
customElements.define('tg-fuel-usage-chart', TgFuelUsageChart);
```

- The behaviour provides `entity`, `propertyName`, `propTitle`, `propDesc` and the index of the property action to show, and adds the resizer, the blocking pane and the common styles to the shadow tree, such as a margin of `8px` above and below, as for editors.
- The property actions are the children slotted into `property-action`, of which only the action for the entity is shown.
- `masterEntityContext()` returns the master entity with its unsaved changes as a context, as for the actions of the master.
- The component is focused as a whole at `component()`, the component itself by default, which needs `tabindex="0"`: Tab moves among the editors to it and on, Enter moves into its content, where Tab cycles, and Esc moves back.
  The element has a focus ring, and the element with class `tg-component-title` looks as the title of a focused editor while the focus is within the component.
  Without `tabindex`, the focusable elements of the component take part in the focus traversal of the master in their place among the editors.
- `isInMasterComponent(element)` tells whether an element is in the content of a master component, as the centres and masters in it are, so that they do not take the focus by themselves.

## Master With Centre

`MasterWithCentreBuilder` embeds an Entity Centre inside a master.
Typically used for insertion points and special views where a centre needs to appear as a master's content.
To show a centre next to the editors of a simple master, use component `centre/tg-centre-component` instead (see *Master Components*).

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
- Integration: `.hasEventSource(EventSourceClass.class).withMinAutoRefreshInterval(300).withCountdownRefreshPrompt(5)`
- A started data load (Run, paging, refresh) supersedes a pending SSE-driven auto-refresh and any SSE events still awaiting their delayed (`minDelay`/`delay`) handling — the auto-refresh is skipped as redundant; only the next SSE event can instigate one again.
  Applies to all SSE-enabled centres, with or without `withMinAutoRefreshInterval`.
  Events arriving while the load is in progress are not superseded (their changes may postdate the load's query) and prompt after it completes, subject to the quiet window.
- `.withMinAutoRefreshInterval(seconds)` (optional, follows `hasEventSource`) — the "refresh governor": a minimum interval between SSE-driven refreshes.
  An event arriving inside the quiet window defers the refresh (trailing edge) instead of dropping it.
  Any completed data load restarts the window, and so does Skip in the refresh prompt.
  The window governs only refreshes instigated by subsequent SSE events — a pending auto-refresh is not deferred to the new window edge, but cancelled outright by a load (previous bullet).
  Combined effect: an actively working user is never auto-refreshed — each load cancels the pending refresh and pushes the window out for the next one.
  Without it, every SSE event may trigger a refresh (subject to payload `minDelay`/`delay` and the prompt); the load-supersedes-pending cancellation (previous bullet) still applies.
