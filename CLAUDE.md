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
Includes automatic rollback on failure. Requires Maven deploy credentials and Git push privileges.

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

## Detailed Reference Guides

- Entity hierarchy, annotations, validators/definers, union entities, producers, companions, MetaProperty, calculated properties, metamodels: @platform-doc/claude/entity-model.md
- EQL query construction, operators, functions, fetch models, QueryExecutionModel: @platform-doc/claude/eql-reference.md
- Entity Centre, Entity Master, Compound Master, actions, query enhancers, SSE: @platform-doc/claude/web-ui.md
- Testing patterns, assertions, indirect testing, security tokens, authorization: @platform-doc/claude/testing-and-security.md

## Key Non-Obvious Design Decisions

These are things that cannot be easily derived from reading the code:

1. **`co()` vs `co$()`**: `co()` returns uninstrumented (read-only) entities; `co$()` returns instrumented entities with change tracking and validation. Using the wrong one causes subtle bugs.

2. **`isInitialising()` in definers**: Definers execute both during DB retrieval and user mutations. Always check `entity.isInitialising()` to distinguish phases.

3. **Definer mutations are not silent**: When a definer sets a property via its setter, it goes through `ObservableMutatorInterceptor` and triggers the full validation chain.

4. **`isDirty()` before side effects**: In DAO `save()` methods, check property dirtiness before triggering cascading updates to avoid unnecessary work.

5. **`try-with-resources` with `stream()`**: Entity streams hold database resources that must be closed.

6. **Indirect testing pattern**: Business logic in `pojo-bl` is tested through DAO integration tests, not unit tests. This is intentional — don't expect unit tests in `pojo-bl`.

7. **Fetch model instrumentation precedence**: If a fetch model is instrumented, entities *are* instrumented even if `QueryExecutionModel` is lightweight.

8. **Compound master fetch providers**: Menu item entities receive their key from the root entity's companion fetch provider, not their own.

9. **DeleteOperations patterns**:
   - Pessimistic locking with `UPGRADE` lock mode for activatable entities
   - Deliberately catches only `PersistenceException` for referential integrity violations
   - `case null, default -> null` in switch expressions is conventional TG shorthand

10. **GraphQL API**: Read-only queries only. Fields are uncapitalized entity names. Token: `GraphiQL_CanExecute_Token`.

## Conventions

**Naming:** Entities = singular nouns (`Vehicle`), Companions = `{Entity}Co`, DAOs = `{Entity}Dao`

**Always use metamodel references** (`Entity_.property()`) instead of string literals in EQL, fetch models, and UI configurations.

**Property declaration:** `@IsProperty` + `@Title` + `@MapTo` (for persistent) + `@Observable` on setter. Validators chain in declaration order.

**Code documentation:**
- Each sentence on its own line (better diffs)
- End sentences with a full stop
- Use Markdown for Javadoc (not HTML tags)

**Use `StandardActions` and `Compound` helpers** for common centre/master actions instead of building custom actions.