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

## Runtime-generated tokens for audit types

Security tokens for audit types (`Re{E}_a3t_CanRead_Token`, `Re{E}_a3t_CanReadModel_Token`) are generated at application startup by `ISecurityTokenGenerator` and served through `ISecurityTokenProvider`.
Only the synthetic audit-entity side receives tokens — persistent audit-entity types (`E_a3t_{n}`) and audit-prop types do not, because users never query those directly.

Consequences:
- Do not commit hand-written `Re{E}_a3t_*_Token` classes. If you see them in an application module under `security/tokens/`, they are remnants of a pre-generic-auditing design and should be deleted.
- Dynamic token retrieval now goes through `ISecurityTokenProvider` rather than `Class.forName` — requests for tokens that are not in the provider are stricter than before, and an application using tokens outside the provider may break.
- To customise audit-token generation or inject extra tokens, extend `SecurityTokenProvider` and override the appropriate methods.

For the full token generation design, see `auditing/reference.md`.
