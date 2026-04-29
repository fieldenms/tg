# Web UI — Quick Reference

## Standard Actions (preferred)

```java
StandardActions.NEW_ACTION.mkAction(Entity.class)
StandardActions.DELETE_ACTION.mkAction(Entity.class)
StandardActions.EDIT_ACTION.mkAction(Entity.class)
StandardActions.EXPORT_ACTION.mkAction(Entity.class)
CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction()
Compound.openEdit(OpenAction.class, ENTITY_TITLE, "Edit Entity", dims)
Compound.openNew(OpenAction.class, "add-circle-outline", ENTITY_TITLE, "Add new Entity", dims)
```

## Criterion Types

- `.asMulti().autocompleter(EntityType.class)` / `.text()` / `.bool()` — multi-value
- `.asSingle().autocompleter(EntityType.class)` / `.text()` / `.integer()` / `.decimal()` / `.date()` / `.dateTime()` — single value (requires `@CritOnly(SINGLE)`)
- `.asRange().integer()` / `.decimal()` / `.date()` / `.dateTime()` / `.time()` — range

## Property Editor Types

`.asSinglelineText()`, `.asMultilineText()`, `.asRichText()`, `.asAutocompleter()`, `.asDateTimePicker()` / `.asDatePicker()` / `.asTimePicker()`, `.asDecimal()` / `.asSpinner()` / `.asMoney()`, `.asCheckbox()`, `.asColour()`, `.asHyperlink()`, `.asCollectionalRepresentor()` / `.asCollectionalEditor()`, `.asFile()`

## Master Actions

`MasterActions.SAVE` (ctrl+s), `MasterActions.REFRESH` (ctrl+x), `MasterActions.VALIDATE`, `MasterActions.EDIT`, `MasterActions.VIEW`, `MasterActions.DELETE`, `MasterActions.NEW`

## Centre Options

`.runAutomatically()`, `.hideCheckboxes()`, `.hideToolbar()`, `.setPageCapacity(n)`, `.retrieveAll()`, `.enforcePostSaveRefresh()`, `.hasEventSource(EventSourceClass.class)`

## Result Property Options

`.order(n).asc()/.desc()`, `.width(px)/.minWidth(px)`, `.withSummary(alias, eqlExpr, titleAndDesc)`, `.withAction(config)`, `.withWordWrap()`

## Dynamic Columns

`.addProps(Entity_.collection(), DynColumnBuilder.class, prepFn, context)` + `.setFetchProvider(...)` including collectional sub-properties.
Requires a one-to-many collectional property and an `IDynamicColumnBuilder` implementation.
See `web-ui/reference.md` § *Dynamic Columns*.

## Context Options

`.withCurrentEntity()`, `.withSelectedEntities()`, `.withSelectionCrit()`, `.withMasterEntity()`, `.withComputation((entity, context) -> value)`

## Master Kinds and Open Tokens

There are exactly two master kinds — **simple** (`SimpleMasterBuilder`) and **compound** (`CompoundMasterBuilder`):

- A **persistent entity** has exactly one master: simple **or** compound, never both.
- An **action entity** is always backed by a simple master (the form/dialog for the action's parameters).

> Nomenclature: TG's older "functional entity" term is being retired in favour of **action entity** — "functional" collides with the functional-programming meaning, whereas TG's action entities are intentionally side-effectful.
> The Java base classes (`AbstractFunctionalEntityWithCentreContext`, `AbstractFunctionalEntityForCompoundMenuItem`) keep their historical names; prose should use "action entity".

Each master kind / entity kind requires a specific `_CanOpen_Token` (or `_CanExecute_Token`) class on the classpath — registering an `EntityMaster<X>` alone is not enough.
The matrix for which token to declare per scenario:

| Scenario | Required token | Conventional package |
|---|---|---|
| Simple master, persistent entity | `<Entity>Master_CanOpen_Token` | `security/tokens/open_simple_master/` |
| Compound master, persistent entity | `Open<Entity>MasterAction_CanOpen_Token` | `security/tokens/open_compound_master/` |
| Simple master, action entity | `<Entity>_CanExecute_Token` | `security/tokens/functional/` (legacy package name) |

The enforcement mechanism differs by master kind — see *Master-open token enforcement* in `security/quick-reference.md`:

- **Simple master:** `EntityManipulationActionProducer` (the producer for `EntityEditAction` / `EntityNewAction`) probes the three names by convention via `TokenUtils.authoriseOpening`. Missing the token → runtime failure when the user attempts to open.
- **Compound master:** the application's `Open<Entity>MasterActionProducer` must explicitly annotate `provideDefaultValues` with `@Authorise(Open<Entity>MasterAction_CanOpen_Token.class)`. Missing the annotation → open is **silently unguarded**, even if the token class exists.

Each new master also needs a corresponding role-association row in the application's release SQL.

A compound master also requires a separate `<Entity>Master_Open<M>_MenuItem_CanAccess_Token` per compound-master menu item `M`, enforced by `@Authorise` on each menu-item DAO's `save()`.
The open token alone does not gate menu-item activation — see *Compound master menu item access* in `security/quick-reference.md`.

## Topic-Specific Gotcha

**Compound master fetch providers**: Menu item entities receive their key from the root entity's companion fetch provider, not their own.
Adding a fetch provider to the menu item's own companion has no effect.
