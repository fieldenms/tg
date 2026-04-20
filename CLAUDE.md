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
2. **`isInitialising()` in definers**: Definers execute during DB retrieval AND user mutations.
   Check `entity.isInitialising()` to distinguish.
3. **Definer mutations are not silent**: Setting a property from a definer triggers the full validation chain via `ObservableMutatorInterceptor`.
4. **`isDirty()` before side effects**: In DAO `save()`, check property dirtiness before cascading updates.
5. **`try-with-resources` with `stream()`**: Entity streams hold database resources that must be closed.
6. **Fetch model instrumentation precedence**: If a fetch model is instrumented, entities *are* instrumented even if `QueryExecutionModel` is lightweight.
7. **GraphQL API**: Read-only queries only.
   Fields are uncapitalized entity names.
   Token: `GraphiQL_CanExecute_Token`.

## Conventions

**Naming:** Entities = singular nouns (`Vehicle`), Companions = `{Entity}Co`, DAOs = `{Entity}Dao`

**Always use metamodel references** (`Entity_.property()`) instead of string literals in EQL, fetch models, and UI configurations.

**Property declaration:** `@IsProperty` + `@Title` + `@MapTo` (for persistent) + `@Observable` on setter.
Validators chain in declaration order.

**Code documentation:**
- Each sentence on its own line (better diffs)
- End sentences with a full stop
- Use Markdown for Javadoc (not HTML tags)

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
