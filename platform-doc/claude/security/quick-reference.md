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

## Standard token set for a new persistent entity

Every new persistent entity needs the canonical CRUD token set, wired in three places: token classes, the application's release SQL, and (where applicable) `@Authorise` annotations.

| Token | Always | Notes |
|---|---|---|
| `Entity_CanSave_Token` | Yes | Save / update |
| `Entity_CanDelete_Token` | If deletable | Skip for activatable entities where deactivation replaces deletion |
| `Entity_CanRead_Token` | Yes | Reading rows |
| `Entity_CanReadModel_Token` | Yes | Reading the data model (centre and GraphQL metadata) |

For action entities, replace this set with `Entity_CanExecute_Token`.
For masters, see *Master-open token enforcement* and *Compound master menu item access* below.

Wire each new token into the release SQL (insert into `SECURITYROLEASSOCIATION_`) so it is granted to the appropriate roles (typically `ADMIN` at minimum).

**PR-review checkpoint — easy to miss:** `CanRead` / `CanReadModel` are *silent* gaps; there is no `@Authorise` annotation whose absence would flag them.
When a PR introduces a new persistent entity, explicitly confirm both tokens exist as classes *and* are granted in the SQL script.

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

## CSRF Protection

Web UI requests are authenticated by the `authenticator` cookie, which is `Secure`, `HttpOnly` and `SameSite=Lax` (`AbstractWebResourceGuard.mkAuthenticationCookie`).
Restlet's `CookieSetting` has no `SameSite` support, so the attribute is appended to the cookie path, which Restlet writes verbatim for version-0 cookies; `AbstractWebResourceGuardCookieTest` verifies the rendered `Set-Cookie` header.
`CsrfProtectionFilter` (`platform-web-resources`, package `web.filters`) rejects requests with unsafe methods (`POST`, `PUT`, `DELETE`, `PATCH`) with `403` when `Sec-Fetch-Site` is `same-site`/`cross-site`, or, without `Sec-Fetch-Site`, when `Origin` does not match the request host (`X-Forwarded-Host` takes precedence over the host of the request).
Unsafe requests without either header are rejected; non-browser clients authenticating with the session cookie must send `Origin` matching the application host.
Web API resources authenticated by headers (e.g. API keys) and attached to the `Component` default host are not affected.

`AbstractWebUiResources` wires the filter in front of the resource guard, so every resource registered in `registerDomainWebResources` is covered.
Resources that an application attaches elsewhere — e.g. to the `Component` default host, as with the login, logout and password reset resources — are outside this filter.

## Login Lockout and Password Reset Cooldown

Both are held in memory by `@Singleton` services in `platform-web-resources` (package `web.resources.webui`): they apply per application server and start afresh when it restarts.
Both measure time with a Guava `Ticker`, which tests replace with a manual one (see `testing/quick-reference.md`).

**Login attempts** — `LoginResource` passes each attempt through `LoginAttemptTracker`, keyed by username (case-insensitive):
- the first `FAILED_ATTEMPTS_BEFORE_BLOCK` (2) failed attempts are denied (`400`);
- each further failed attempt blocks the username for `BLOCK_TIME_SECONDS` (15), and an attempt within that time is blocked (`412`) without authentication and is not counted;
- the `LOCKOUT_THRESHOLD`-th (6th) counted failed attempt locks the account out; a successful attempt resets the count.

Attempts with every username are tracked and blocked alike, so that the outcomes do not disclose which usernames identify accounts.

**Lockout** — `IUser.lockoutUser` deactivates the account and removes its secret, but only for an account that can sign in with a password (`IUser.findUserForPasswordLogin`); accounts restricted to SSO in the SSO authentication mode are not affected.
Deactivation saves the user, which requires a current user: `LoginResource.lockoutUser` sets `SU` as the current user for the lockout and clears it afterwards.
The secret is removed in a separate transaction, so that the account can no longer sign in with a password even if its deactivation fails.
A locked-out user regains access when an administrator activates the account again, which starts the password reset procedure.

**Password reset** — `LoginInitiateResetResource` initiates a reset at most once per user within `PasswordResetCooldown.COOLDOWN_IN_MINUTES` (5), keyed by user ID, so the cooldown applies whether the user is identified by username or email.
A reset link remains valid for `UserSecretCo.RESET_UUID_EXPIRATION_IN_MINUTES` (15).
A link that remains valid for at least `LoginInitiateResetResource.MIN_REUSED_LINK_VALIDITY_IN_MINUTES` (5) is sent again instead of being replaced, so that repeated requests do not invalidate a link the user has received; its expiry is not extended.
Requests for a user in cooldown and for an unknown user receive the same response and the same response-time adjustment, so that the response does not disclose whether an account exists.
Expiry of reset links and user sessions is computed with `serverNow()`, not `now()` (see gotcha 11 in `CLAUDE.md`).

## Topic-Specific Gotcha

**Audit security tokens are runtime-generated — do not hand-write them.**
`Re{E}_a3t_CanRead_Token` and `Re{E}_a3t_CanReadModel_Token` are generated at application startup by `ISecurityTokenGenerator` and served through `ISecurityTokenProvider`.
Persistent audit-entity types (`E_a3t_{n}`) and audit-prop types receive no tokens — users never query those directly.
See `auditing/reference.md` for the full generation design.
