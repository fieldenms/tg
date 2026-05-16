# CLAUDE.md

This file provides guidance to Claude Code when working with code in the TG repository and TG-based applications.

## Overview

Trident Genesis (TG) is an enterprise application platform implementing the RESTful Objects architectural pattern.
It provides a Domain-Driven Design framework with a rich domain model, sophisticated query language (EQL), and complete application stack from data persistence to web UI.

## Common Development Commands

### Building
```bash
mvn clean install -Dmaven.javadoc.skip=true -DdatabaseUri.prefix=//localhost:5432/ci_ -Dfork.count=4   # Clean build
mvn clean install -DskipTests -DdatabaseUri.prefix=//localhost:5432/ci_ -Dfork.count=4                 # Without tests
mvn clean install -pl platform-pojo-bl -am                                                              # Specific module
mvn clean deploy                                                                                        # Deploy
```

### Testing
```bash
mvn clean test -Dmaven.javadoc.skip=true -Dfork.count=4 -DdatabaseUri.prefix=//localhost:5432/ci_                                                                                # PostgreSQL
mvn test -Dmaven.javadoc.skip=true -Dfork.count=4 -DdatabaseUri.prefix=//localhost:1433;encrypt=true;trustServerCertificate=true;sendStringParametersAsUnicode=false;databaseName=ci_  # SQL Server
```

### Version Management
```bash
./tg-update-version.sh 2.1.0-SNAPSHOT          # Update version (recommended)
```

### Releasing
```bash
./tg-release.sh 2.1.0 2.1.1-SNAPSHOT '//localhost:5432/ci_' 4 develop
# Args: release-version, next-snapshot, db-uri-prefix, fork-count, base-branch
```
Release follows Git Flow: create release branch → set version → merge to master → tag → build & deploy → merge back → set next SNAPSHOT → push.
Includes automatic rollback on failure.
Requires Maven deploy credentials and Git push privileges.

## Module Structure

The platform targets **Java 25**:

| Module | Purpose |
|--------|---------|
| `platform-annotations` | Core annotations for entity definition |
| `platform-annotation-processors` | Compile-time annotation processing and metamodel generation |
| `platform-annotation-processors-test` | Tests for annotation processors |
| `platform-pojo-bl` | Business logic layer with domain model foundation |
| `platform-dao` | Data access layer with EQL, Hibernate, and GraphQL Web API |
| `platform-web-resources` | REST API layer |
| `platform-web-ui` | Web UI framework with Entity Centre and Master patterns |
| `platform-db-evolution` | Database migration and evolution tools |
| `platform-eql-grammar` | ANTLR-based EQL parser and compiler |
| `platform-benchmark` | Performance benchmarking tools |

LaTeX documentation in `platform-doc/`.

## Critical Design Gotchas

These apply regardless of topic.
Topic-specific gotchas live in each directory's `quick-reference.md`.

1. **`co()` vs `co$()`**: `co()` returns uninstrumented (read-only) entities; `co$()` returns instrumented with change tracking.
   Using the wrong one causes subtle bugs.
2. **Definers fire on successful property change AND once per property post-load — never during the load itself.**
   On user mutation: the `@AfterChange` handler runs synchronously after the setter, after validators have accepted the new value.
   On DB load: TG sets all property values first via *direct field writes* (bypassing setters/validators/definers), then `DefinersExecutor` walks the object graph and invokes each property's `@AfterChange` handler.
   Consequence: all sibling properties have their loaded values by the time any definer fires — so it is safe to read them and there is no per-declaration-order race.
3. **Validators do NOT fire at DB load.**
   `@BeforeChange` handlers are wired through `ObservableMutatorInterceptor`, which only intercepts setter method calls.
   Load uses direct field writes, so no validator runs.
   To surface a load-time message on a property (warning, info, error), use a definer that calls `metaProp.setDomainValidationResult(...)`.
4. **No-op setters short-circuit both validators and definers.**
   `ObservableMutatorInterceptor` skips validation and the `@AfterChange` handler when `equalsEx(currValue, newValue)` returns true.
   So `setX(null)` on an entity whose current `X` is already null fires neither — relevant when writing tests that try to provoke a validator on the loaded value (set a different value first to clear, then re-set the value you want to provoke).
5. **`isInitialising()` in definers**: Definers execute during DB retrieval AND user mutations.
   Check `entity.isInitialising()` to distinguish.
   It is `true` from `beginInitialising()` until `endInitialising()` (called per-entity at the end of `DefinersExecutor.explore`).
6. **Definer mutations are not silent**: Setting a property from a definer triggers the full validation chain via `ObservableMutatorInterceptor`.
7. **`isDirty()` before side effects**: In DAO `save()`, check property dirtiness before cascading updates.
8. **`try-with-resources` with `stream()`**: Entity streams hold database resources that must be closed.
9. **Fetch model instrumentation precedence**: If a fetch model is instrumented, entities *are* instrumented even if `QueryExecutionModel` is lightweight.
10. **GraphQL API**: Read-only queries only.
    Fields are uncapitalized entity names.
    Token: `GraphiQL_CanExecute_Token`.

## Conventions

**Naming:** Entities = singular nouns (`Vehicle`), Companions = `{Entity}Co`, DAOs = `{Entity}Dao`

**Always use metamodel references** (`Entity_.property()`) instead of string literals in EQL, fetch models, and UI configurations.
Metamodel references implement `CharSequence`, so APIs typed for `CharSequence` (or with a `PropertyMetaModel` overload) accept `Entity_.property()` directly — there is no need to fall back to a `String`.

**Property declaration:** `@IsProperty` + `@Title` + `@MapTo` (for persistent) + `@Observable` on setter.
Validators chain in declaration order.

**Title strings — no backticks, no straight quotes.**
The platform serialises `@Title` / `@EntityTitle` / `@KeyTitle` values (`value` and `desc`) into generated JavaScript as string literals; embedded `` ` `` or `"` characters break that JS at parse time.
If you need quotation marks or an apostrophe, use the Unicode equivalents — U+2019 `’` for apostrophe, U+201C `“` and U+201D `”` for double quotes — they survive serialisation cleanly.
This rule covers only `@Title`/`@EntityTitle`/`@KeyTitle` annotation values; surrounding Javadoc is unrestricted (Markdown backticks remain the right tool there).

**Title constants — opt in when a title is referenced elsewhere.**
The dominant style in the codebase is inline string titles (`@Title(value = "Bowser ID", desc = "...")`); ~97% of `@Title` annotations follow it.
Switch to a `public static final String <PROPERTY>_TITLE = "Title Text";` constant **only when the title appears in more than one place** — typically when another property's `desc` references it (`desc = "... resolved from " + X_TITLE + "."`), or when another file needs the title via static import.
The constant pays off by removing the drift risk between the title string and its references; for single-use titles it's pure boilerplate.
Group related title constants under a single `public static final String` line per the *Grouped constants* convention below.
Example: `MeterReading.TOTAL_READING_TITLE` is declared on the entity because it is statically imported into `Equipment` (and other files) that reference it.

**Code documentation:**
- Each sentence on its own line (better diffs)
- End sentences with a full stop
- Use Markdown for Javadoc (not HTML tags)
- Use Markdown backticks (`` ` ``) for inline code, identifiers, and literals; in new code do **not** use the `{@code …}` Javadoc tag (predates Markdown support, is now noise — more characters, breaks Markdown rendering of the surrounding text). Same for paths and shell snippets. Legacy `{@code}` is still widespread; touch it opportunistically rather than mass-rewriting.

**String formatting:** in new code, prefer the `String#formatted` instance method over `String.format(…)`.
Reads as the format string operating on its arguments (`"foo [%s]".formatted(x)`) and avoids the `java.lang.String.format` import.
Legacy `String.format` calls are still common; touch them opportunistically but don't churn unrelated code.

**Lazy logging:** when a log call requires string formatting, pass a `Supplier` lambda so the formatting cost is paid only when the level is enabled:
```java
LOGGER.info(() -> "…".formatted(args));
LOGGER.error(() -> "…".formatted(args), ex);
```
Constants (`LOGGER.info("static text")`) and parameterised SLF4J/Log4j-style calls (`LOGGER.warn("template [{}]", arg)` — already lazy) do not need a lambda.
When the same formatted message is also needed by surrounding code (e.g. as a response body), format eagerly into a local and pass it to the logger.

**`@Singleton` for stateless / immutable injectables:** default to `@Singleton` (from `jakarta.inject.Singleton` — the in-project convention) for any Guice-injected class whose state is set once at construction and never mutated.
Settings holders (e.g. `WebApiSettings`), stateless services, and IoC-bound utilities all qualify.
Without `@Singleton`, Guice creates a fresh instance per injection point — wasteful, and conceptually wrong for "global app config" or shared infrastructure.
Do **not** apply `@Singleton` to classes that hold per-request / per-thread state, or that are explicitly intended to be re-instantiated.

**Grouped constants:** when several `static final` fields of the same type form a logical *set of alternatives* — alternative error messages produced by the same validator, alternative warnings from the same definer, parallel format-string templates — declare them under a single `public static final <Type>` line, separated by commas:
```java
public static final String
    ERR_NO_ROSTERED_DAYS = "Cannot activate: the profile must have at least one rostered day (with Shift Start assigned).",
    ERR_GAPS_IN_DAY_NUMBERS = "Cannot activate: there are gaps in Day numbers. Day count is [%s] but the highest Day number is [%s].";
```
Common cases: validator error messages, named query aliases, related token strings.

**Use `StandardActions` and `Compound` helpers** for common centre/master actions.

**SQL migration scripts** for new persistent entities (in TG-based applications):
- Use `GenDdl` to generate DDL.
  Table names: uppercased entity class + `_`.
  Column names: uppercased property + `_`.
- `boolean` → `char(1) NOT NULL` (`'Y'`/`'N'`).
  Entity references → `bigint` (FK to `_ID`).

## Reference Topics

Each topic directory under `platform-doc/claude/` has a `quick-reference.md` for common lookups and detailed files for full reference.
Read the quick reference first; read the detailed file only when you need depth.
Cross-links from a quick-reference should prefer another quick-reference; from a detailed reference, point to wherever the content lives.

| Directory | Quick reference | Detailed reference |
|---|---|---|
| `entity-model/` | Hierarchy, annotations, property patterns, companions, calculated/synthetic, metamodel | `reference.md` — composite keys, activatable, validators/definers, union entities, producers, ISaveWithFetch, MetaProperty, declarative filters, generative entities |
| `eql/` | Query construction, operators, functions, fetch models | `reference.md` — JOIN patterns, pivot, CASE WHEN, critCondition, QEM. `design.md` — EQL internals, adding new functions |
| `web-ui/` | Standard actions, criterion/editor types, centre/master options | `reference.md` — full builder APIs, action config, query enhancers, insertion points, rendering customisers |
| `testing/` | Fetch patterns, indirect testing, test data caching | `reference.md` — DynamicQueryBuilder testing, test clock, web resource testing |
| `security/` | Token templates, `@Authorise` usage | `reference.md` — `@Authorise` + AOP infrastructure, authorization scopes (DAO/Producer/Property/Action), runtime-generated audit tokens |
| `auditing/` | @Audited basics, generated types, test config | `reference.md` — full type hierarchy, versioning, runtime plumbing, GenAudit, Web UI |
