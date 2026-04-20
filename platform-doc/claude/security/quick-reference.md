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
| `MASTER_OPEN` | `Entity_CanOpen_Token` | Opening entity masters |
| `MASTER_MENU_ITEM_ACCESS` | `Entity_CanAccess_Token` | Compound master menu items |

Generally only `SAVE` is used. `SAVE_NEW` / `SAVE_MODIFIED` are for the rare cases where create and update need separate permissions.

## Applying a Token

```java
@Override @SessionRequired @Authorise(Project_CanDelete_Token.class)
public int batchDelete(final Collection<Long> entitiesIds) { ... }
```

Apply to DAO methods and producers. Combined with `@SessionRequired` for transactional methods.

## Topic-Specific Gotcha

**Audit security tokens are runtime-generated — do not hand-write them.**
`Re{E}_a3t_CanRead_Token` and `Re{E}_a3t_CanReadModel_Token` are generated at application startup by `ISecurityTokenGenerator` and served through `ISecurityTokenProvider`.
Persistent audit-entity types (`E_a3t_{n}`) and audit-prop types receive no tokens — users never query those directly.
See `auditing/reference.md` for the full generation design.
