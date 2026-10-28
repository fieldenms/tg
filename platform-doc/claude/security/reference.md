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

## CSRF Protection

Web UI requests are authenticated by the `authenticator` cookie, which is `Secure`, `HttpOnly` and `SameSite=Lax` (`AbstractWebResourceGuard.mkAuthenticationCookie`).
Restlet's `CookieSetting` has no `SameSite` support, so the attribute is appended to the cookie path, which Restlet writes verbatim for version-0 cookies; `AbstractWebResourceGuardCookieTest` verifies the rendered `Set-Cookie` header.
`CsrfProtectionFilter` (`platform-web-resources`, package `web.filters`) rejects requests with unsafe methods (`POST`, `PUT`, `DELETE`, `PATCH`) with `403` when `Sec-Fetch-Site` is `same-site`/`cross-site`, or, without `Sec-Fetch-Site`, when `Origin` does not match the request host (`X-Forwarded-Host` takes precedence over the host of the request).
Unsafe requests without either header are rejected; non-browser clients authenticating with the session cookie must send `Origin` matching the application host.
A reverse proxy in front of the application must either preserve `Host` (e.g., nginx with `proxy_set_header Host $host`) or send the original host in `X-Forwarded-Host`; the standard `Forwarded` header is not read.
Otherwise, legitimate requests from browsers without `Sec-Fetch-Site` are rejected, which the filter logs as "Rejected a potentially forged cross-site request".
Web API resources authenticated by headers (e.g. API keys) and attached to the `Component` default host are not affected.

`AbstractWebUiResources` wires the filter in front of the resource guard, so every resource registered in `registerDomainWebResources` is covered.
`CsrfProtectionWiringTest` verifies this wiring end to end: with a valid session cookie, a cross-site `POST` to a guarded resource is rejected before authentication, while a same-origin `POST` and a cross-site `GET` reach it.
Resources that an application attaches elsewhere — e.g. to the `Component` default host, as with the login, logout and password reset resources — are outside this filter.
The factories of the login and password reset resources (`LoginResourceFactory`, `LoginInitiateResetResourceFactory`, `LoginCompleteResetResourceFactory`) apply the same check with `CsrfProtectionFilter.rejectIfForged`, which protects against login CSRF, where another site signs the victim in to the account of an attacker.
`LogoutResourceFactory` does not apply the check: `LogoutResource` handles `GET` only, as identity providers call it cross-site for front-channel logout.

## Login Lockout and Password Reset Cooldown

Both are held in memory by `@Singleton` services in `platform-web-resources` (package `web.resources.webui`): they apply per application server and start afresh when it restarts.
Both measure time with a Guava `Ticker`, which tests replace with a manual one (see `testing/quick-reference.md`).

**Login attempts** — `LoginResource` passes each attempt through `LoginAttemptTracker`, keyed by username (case-insensitive), which determines the response:
- the first `FAILED_ATTEMPTS_BEFORE_BLOCK` (2) failed attempts are denied (`400`);
- each further failed attempt blocks the username for `BLOCK_TIME_SECONDS` (15), and an attempt within that time is blocked (`412`) without authentication, is not counted, and restarts the block time;
- the `LOCKOUT_THRESHOLD`-th (6th) counted failed attempt is denied (`400`), and counting starts afresh; a successful attempt resets the count.

Attempts with every username are tracked alike, in one cache bounded by `MAX_TRACKED_USERNAMES`, so that neither the responses nor eviction from the cache disclose which usernames identify accounts.
Lockout is decided separately and is not reflected in the response: for an account that can sign in with a password, the `LOCKOUT_THRESHOLD`-th failed authentication within `LOCKOUT_WINDOW_HOURS` (24) locks the account out.
These counts are kept only for such accounts and are not evicted, so that a flood of attempts with other usernames cannot reset them.

**Lockout** — `IUser.lockoutUser` deactivates the account and removes its secret, but only for an account that can sign in with a password (`IUser.canSignInWithPassword`); accounts restricted to SSO in the SSO authentication mode are not affected.
`LoginResource` requests the lockout from `AccountLockout`, which performs it asynchronously on a thread of its own, so that the response time of the attempt does not disclose that the username identifies an account.
Deactivation saves the user, which requires a current user: `AccountLockout` sets `SU` as the current user for the lockout and clears it afterwards.
Lockouts are queued in memory and performed one at a time; a lockout requested for a username while one is queued or in progress for it is not queued again, so at most one lockout per account is queued or in progress.
Until a lockout is performed, typically within milliseconds, further attempts with the username are tracked as usual, and are authenticated unless the username is blocked.
The secret is removed in a separate transaction, so that the account can no longer sign in with a password even if its deactivation fails.
A locked-out user regains access when an administrator activates the account again, which starts the password reset procedure.

**Password reset** — `LoginInitiateResetResource` initiates a reset at most once per user within `PasswordResetCooldown.COOLDOWN_IN_MINUTES` (5), keyed by user ID, so the cooldown applies whether the user is identified by username or email.
A reset link remains valid for `UserSecretCo.RESET_UUID_EXPIRATION_IN_MINUTES` (15).
A link that remains valid for at least `LoginInitiateResetResource.MIN_REUSED_LINK_VALIDITY_IN_MINUTES` (5), and no longer than a new link would be, is sent again instead of being replaced, so that repeated requests do not invalidate a link the user has received; its expiry is not extended.
A link with a longer validity, such as the 24-hour activation link for a new user, is replaced with a new reset link, rather than sent again in reply to a reset request.
Requests for a user in cooldown and for an unknown user receive the same response and the same response-time adjustment, so that the response does not disclose whether an account exists.
Expiry of reset links and user sessions is computed with `serverNow()`, not `now()` (see gotcha 11 in `CLAUDE.md`).

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
