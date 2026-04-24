# Security — Quick Reference

TG uses domain-centric authorization: security tokens are classes whose names encode the entity and operation, applied declaratively to DAO methods and producers via `@Authorise`.

## Security Token Templates

| Template | Token Name Pattern | Purpose |
|---|---|---|
| `SAVE` | `Entity_CanSave_Token` | Standard save (most common) |
| `SAVE_NEW` / `SAVE_MODIFIED` | `Entity_CanSaveNew_Token` / `Entity_CanSaveModified_Token` | Separate create vs update (rare) |
| `DELETE` | `Entity_CanDelete_Token` | Deletion |
| `READ` | `Entity_CanRead_Token` | Reading data |
| `READ_MODEL` | `Entity_CanReadModel_Token` | Reading data model |
| `EXECUTE` | `Entity_CanExecute_Token` | Action execution |
| `MODIFY` | `Entity_CanModify_Property_Token` | Property-level modification |
| `MASTER_OPEN` | `Entity_CanOpen_Token` | Opening entity masters (see *Master-open token enforcement* below) |
| `MASTER_MENU_ITEM_ACCESS` | `Entity_CanAccess_Token` | Compound master menu items |

Generally only `SAVE` is used. `SAVE_NEW` / `SAVE_MODIFIED` are for the rare cases where create and update need separate permissions.

## Master-open token enforcement

There are two parallel enforcement paths depending on which open action fires:

**Simple master** (and action-entity simple master) — opened via `EntityEditAction` / `EntityNewAction` (built by `StandardActions.EDIT_ACTION` / `NEW_ACTION`).
`EntityManipulationActionProducer.provideDefaultValues` calls `TokenUtils.authoriseOpening(entityTypeSimpleName, ...)`, which probes the `ISecurityTokenProvider` for three simple names in order — first hit authorises:

1. `<Entity>Master_CanOpen_Token`
2. `Open<Entity>MasterAction_CanOpen_Token`
3. `<Entity>_CanExecute_Token`

If none resolve, the lookup returns `failure(ERR_TOKEN_NOT_FOUND)` and the producer propagates it via `ifFailure(Result::throwRuntime)` — the master will not open at runtime.

**Compound master** — opened via the application's `Open<Entity>MasterAction` (built by `Compound.openEdit` / `openNew`).
The application's concrete producer (extending `AbstractProducerForOpenEntityMasterAction`) must annotate its `provideDefaultValues` override with `@Authorise(Open<Entity>MasterAction_CanOpen_Token.class)`.
The platform base class does **not** invoke `TokenUtils.authoriseOpening` and does **not** discover the token by convention; missing the annotation leaves the open unguarded.

A compound master is **not** fully secured by the open token alone — each compound-master menu item requires its own access token, see *Compound master menu item access* below.

Which name to declare for a given entity is a Web UI / entity-model decision — see *Master Kinds and Open Tokens* in `web-ui/quick-reference.md`.

## Compound master menu item access

Each compound-master menu item is its own action entity (`<Entity>Master_Open<M>_MenuItem`, extending `AbstractFunctionalEntityForCompoundMenuItem`) with its own DAO.
Activating a menu item triggers the platform to call `save()` on its DAO — that `save()` invocation is the authorisation gate.

The application's menu-item DAO must annotate `save()` with `@Authorise(<Entity>Master_Open<M>_MenuItem_CanAccess_Token.class)` (template `MASTER_MENU_ITEM_ACCESS`):

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

Tokens go in `security/tokens/compound_master_menu/`.
Each menu item requires its own token + annotation pair; like the compound-master open path, the platform does not probe by convention, so a missing `@Authorise` silently allows menu-item access.

**Compound-master security checklist:**
- Compound master open: `Open<Entity>MasterAction_CanOpen_Token` + `@Authorise` on `Open<Entity>MasterActionProducer.provideDefaultValues`.
- For each menu item `M`: `<Entity>Master_Open<M>_MenuItem_CanAccess_Token` + `@Authorise` on `<Entity>Master_Open<M>_MenuItemDao.save`.
- Wire all tokens into the application's release SQL so they get associated with the appropriate roles.

## Applying a Token

```java
@Override @SessionRequired @Authorise(Project_CanDelete_Token.class)
public int batchDelete(final Collection<Long> entitiesIds) { ... }
```

Apply to DAO methods and producers.
Combine with `@SessionRequired` for transactional methods.

## Topic-Specific Gotcha

**Audit security tokens are runtime-generated — do not hand-write them.**
`Re{E}_a3t_CanRead_Token` and `Re{E}_a3t_CanReadModel_Token` are generated at application startup by `ISecurityTokenGenerator` and served through `ISecurityTokenProvider`.
Persistent audit-entity types (`E_a3t_{n}`) and audit-prop types receive no tokens — users never query those directly.
See `auditing/reference.md` for the full generation design.
