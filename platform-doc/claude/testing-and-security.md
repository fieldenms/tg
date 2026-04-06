# Testing and Security Reference

## Testing Approach

JUnit 4 with integration tests against PostgreSQL and SQL Server.
Test data fixtures using `DbDrivenTestCase` pattern with test-specific Guice modules.

### Assertions

Both JUnit and AssertJ are acceptable. Prefer AssertJ for:
- **Exceptions:** `assertThatThrownBy(() -> ...).isInstanceOf(X.class).hasMessageContaining("...")`
- **Collections:** `assertThat(list).hasSize(3).containsExactly("a", "b", "c")`
- **Descriptive messages:** `.as("context")` is cleaner than JUnit's message parameter

### Indirect Testing Pattern

Business logic (validators, definers) resides in `pojo-bl` but is tested **indirectly through DAO tests**:
- DAO tests manipulate entities → property setters automatically trigger validation chains
- Don't expect traditional unit tests in `pojo-bl` — this is intentional
- A single DAO test may validate multiple validators and definers
- Legacy tests may be `@Deprecated` in favor of newer `IDomainData` approach

### Web Testing

Browser-based test suites using Web Component Tester (WCT):
- Main suite: `platform-web-ui/src/main/web/ua/com/fielden/platform/web/tests.html`
- Individual tests in `*/test/*.html` directories

## Security — Domain-Centric Authorization

### Security Token Templates

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

Generally only `SAVE` is used. `SAVE_NEW`/`SAVE_MODIFIED` are for specific cases needing separate create vs update permissions.

### Declarative Authorization

Apply via `@Authorise` annotation on DAO methods and producers:
```java
@Override @SessionRequired @Authorise(Project_CanDelete_Token.class)
public int batchDelete(final Collection<Long> entitiesIds) { ... }
```

**Infrastructure:** `AuthorisationInterceptor` (AOP), `IAuthorisationModel`, thread-local scoping prevents nested redundant checks.

### Authorization Levels

- **DAO:** CRUD operations with appropriate tokens
- **Producer:** Entity creation/opening
- **Property:** Fine-grained access for sensitive properties
- **Action:** Business process execution