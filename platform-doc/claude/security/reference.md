# Security — Detailed Reference

For the token template table and `@Authorise` usage, see `quick-reference.md` in this directory.

## Domain-Centric Authorization

Authorization in TG is domain-centric: permissions are expressed as **security tokens** — classes whose names encode the protected entity and operation.
Tokens are declared once per (entity, operation) pair and applied to DAO methods and producers via the `@Authorise` annotation.
At runtime, an AOP interceptor checks the current user's role-token bindings against the annotated token before the method runs.

## Security Token Templates

| Template | Token Name Pattern | Purpose |
|----------|-------------------|---------|
| `SAVE` | `Entity_CanSave_Token` | Standard save (most common) |
| `SAVE_NEW` | `Entity_CanSaveNew_Token` | Save new only (specific cases) |
| `SAVE_MODIFIED` | `Entity_CanSaveModified_Token` | Save existing only (specific cases) |
| `DELETE` | `Entity_CanDelete_Token` | Deletion |
| `READ` | `Entity_CanRead_Token` | Reading data |
| `READ_MODEL` | `Entity_CanReadModel_Token` | Reading data model |
| `EXECUTE` | `Entity_CanExecute_Token` | Action execution |
| `MODIFY` | `Entity_CanModify_Property_Token` | Property-level modification |
| `MASTER_OPEN` | `Entity_CanOpen_Token` | Opening entity masters |
| `MASTER_MENU_ITEM_ACCESS` | `Entity_CanAccess_Token` | Compound master menu items |

Generally only `SAVE` is used. `SAVE_NEW` / `SAVE_MODIFIED` are for specific cases needing separate create vs update permissions.

## Declarative Authorization

Apply via `@Authorise` annotation on DAO methods and producers:
```java
@Override @SessionRequired @Authorise(Project_CanDelete_Token.class)
public int batchDelete(final Collection<Long> entitiesIds) { ... }
```

**Infrastructure:** `AuthorisationInterceptor` (AOP), `IAuthorisationModel`, thread-local scoping prevents nested redundant checks.

## Authorization Levels

- **DAO:** CRUD operations with appropriate tokens
- **Producer:** Entity creation/opening
- **Property:** Fine-grained access for sensitive properties
- **Action:** Business process execution

## Master-Open Token Enforcement

Token enforcement for master opening differs by master kind: simple-master opens go through a platform convention-based lookup, compound-master opens go through an explicit `@Authorise` annotation on the application's producer.

### Simple master (and action-entity simple master)

Opened via `EntityEditAction` / `EntityNewAction` (`AbstractEntityManipulationAction` subtypes built by `StandardActions.EDIT_ACTION` / `NEW_ACTION`).
The platform producer `EntityManipulationActionProducer.provideDefaultValues` calls `TokenUtils.authoriseOpening(entityTypeSimpleName, authorisation, securityTokenProvider)` and rethrows on failure via `ifFailure(Result::throwRuntime)`.
The lookup probes three simple names in order — first hit authorises:

```java
// TokenUtils.authoriseOpening — pseudocode
findToken(entityTypeSimpleName + "Master",                 MASTER_OPEN, provider);  // <Entity>Master_CanOpen_Token
findToken("Open" + entityTypeSimpleName + "MasterAction",  MASTER_OPEN, provider);  // Open<Entity>MasterAction_CanOpen_Token
findToken(entityTypeSimpleName,                            EXECUTE,     provider);  // <Entity>_CanExecute_Token
```

`MASTER_OPEN` formats as `%s_CanOpen_Token` (per `Template.MASTER_OPEN`).
`EXECUTE` formats as `%s_CanExecute_Token`.
Names are resolved against `ISecurityTokenProvider`; if none of the three matches a loaded class, the open fails at runtime with `ERR_TOKEN_NOT_FOUND.formatted(MASTER_OPEN, entityTypeSimpleName)` — silent at compile time, easy to miss in PR review.

The probe is permissive: any of the three name forms is accepted, so the lookup matches regardless of whether the application has declared a simple-master token, an open-compound-master token, or a `_CanExecute_Token` for the entity type.

### Compound master

Opened via the application's `Open<Entity>MasterAction` (built by `Compound.openEdit` / `Compound.openNew`).
The producer extends `AbstractProducerForOpenEntityMasterAction`, whose platform-level `provideDefaultValues` does **not** invoke `TokenUtils.authoriseOpening` and does **not** discover a token by convention.

Authorisation is only enforced if the application's concrete producer subclass annotates its `provideDefaultValues` override:

```java
@Override
@Authorise(OpenRosterProfileMasterAction_CanOpen_Token.class)
protected OpenRosterProfileMasterAction provideDefaultValues(final OpenRosterProfileMasterAction openAction) {
    return super.provideDefaultValues(openAction);
}
```

Without that annotation the open is unguarded.
This is a frequent oversight when scaffolding compound masters — the token class can exist on the classpath and even be wired into release SQL, yet `Open<Entity>MasterAction` opens without authorisation because nothing references the token class.

### Compound master menu item access

The compound-master open token only governs whether the master *opens*; activating individual menu items inside the master is governed independently per menu item.
Each menu item is an action entity `<Entity>Master_Open<M>_MenuItem` extending `AbstractFunctionalEntityForCompoundMenuItem`, with its own companion and DAO.
When the user activates a menu item, the platform persists the menu-item entity by calling `save()` on the DAO; that `save()` invocation is the authorisation gate.

The application's menu-item DAO must annotate `save()` with `@Authorise(<Entity>Master_Open<M>_MenuItem_CanAccess_Token.class)` (template `MASTER_MENU_ITEM_ACCESS`, format `%s_CanAccess_Token`).
Tokens conventionally live in `security/tokens/compound_master_menu/`.

```java
@EntityType(RosterProfileMaster_OpenMain_MenuItem.class)
public class RosterProfileMaster_OpenMain_MenuItemDao
        extends CommonEntityDao<RosterProfileMaster_OpenMain_MenuItem>
        implements RosterProfileMaster_OpenMain_MenuItemCo {

    @Override
    @Authorise(RosterProfileMaster_OpenMain_MenuItem_CanAccess_Token.class)
    public RosterProfileMaster_OpenMain_MenuItem save(final RosterProfileMaster_OpenMain_MenuItem entity) {
        return super.save(entity);
    }
}
```

The same silent-failure mode applies as for the compound-master open: the platform does not probe the menu-item token by convention, so a missing `@Authorise` leaves the menu item freely accessible to anyone who can open the master.
PR review for a new menu item should check three artefacts: the menu-item entity / companion / DAO triple, the access-token class file, and the `@Authorise` annotation on `save()`.

### Where to declare each token

Which name to declare for a given entity depends on the master kind and on whether the entity is persistent or an action entity.
See *Master Kinds and Open Tokens* in `web-ui/quick-reference.md` for the matrix and the action-entity nomenclature note.

For compound masters specifically, the application owes the platform two layers of declared security:
1. **Open the master:** `Open<Entity>MasterAction_CanOpen_Token` + `@Authorise` on `Open<Entity>MasterActionProducer.provideDefaultValues`.
2. **Access each menu item `M`:** `<Entity>Master_Open<M>_MenuItem_CanAccess_Token` + `@Authorise` on the menu-item DAO's `save()`.

All declared tokens must also be wired into release SQL with the appropriate role associations.

## Runtime-generated tokens for audit types

Security tokens for audit types (`Re{E}_a3t_CanRead_Token`, `Re{E}_a3t_CanReadModel_Token`) are generated at application startup by `ISecurityTokenGenerator` and served through `ISecurityTokenProvider`.
Only the synthetic audit-entity side receives tokens — persistent audit-entity types (`E_a3t_{n}`) and audit-prop types do not, because users never query those directly.
Only `CanRead` and `CanReadModel` are generated — no `CanSave` or `CanDelete` — because audit records are immutable from the user's perspective.

Consequences:
- Do not commit hand-written `Re{E}_a3t_*_Token` classes.
  If you see them in an application module under `security/tokens/`, they are remnants of a pre-generic-auditing design and should be deleted.
- Dynamic token retrieval now goes through `ISecurityTokenProvider` rather than `Class.forName` — requests for tokens that are not in the provider are stricter than before, and an application using tokens outside the provider may break.
- To customise audit-token generation or inject extra tokens, extend `SecurityTokenProvider` and override the appropriate methods.

For the full token generation design, see `auditing/reference.md`.
