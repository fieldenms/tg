# EQL Internal Design Guide

How to extend EQL with new functions, operators, or language features.
Based on the implementation of `concatOf` with intra-aggregate `ORDER BY` support.

## Multi-Stage Compilation Pipeline

EQL compiles through 4 stages. Each stage has its own operand/function classes:

| Stage | Location | Purpose |
|-------|----------|---------|
| Stage 0 | `platform-dao/.../eql/stage0/` | Token stream from fluent API → ANTLR parse tree → Stage 1 |
| Stage 1 | `platform-dao/.../eql/stage1/` | Unresolved operands. Parameter values substituted. |
| Stage 2 | `platform-dao/.../eql/stage2/` | Property types resolved. Entity sources enhanced. |
| Stage 3 | `platform-dao/.../eql/stage3/` | SQL generation. Database-specific output. |

Each function must have a class at stages 1, 2, and 3 (e.g., `ConcatOf1`, `ConcatOf2`, `ConcatOf3`).
Transformation flows: `Stage1.transform(context) → Stage2`, `Stage2.transform(context) → Stage3`.

## Adding a New EQL Function — Checklist

### 1. Grammar

The grammar has a two-step generation pipeline:

1. **Canonical grammar** (`CanonicalEqlGrammar.java`) → **`EQL.g4`** via `GrammarActions generate antlr4`
2. **`EQL.g4`** → **parser/lexer/visitor Java files** via ANTLR Maven plugin (`mvn generate-sources`)

**Canonical grammar is the source of truth** (`platform-eql-grammar/src/main/java/fielden/platform/eql/CanonicalEqlGrammar.java`):
- Add new terminals to `EqlTerminal` enum (alphabetical order).
- Add new variables to `EqlVariable` enum if needed.
- Add `derive()` rule for the new production.
- Add `annotate()` call if the variable should be inlined.

**`EQL.g4`** (`platform-eql-grammar/src/main/antlr4/EQL.g4`) is auto-generated.
Do not edit it manually — run `GrammarActions generate antlr4` to regenerate from the canonical grammar.

**Regenerate all generated files:**
```bash
# Step 1: Regenerate EQL.g4 from canonical grammar (run GrammarActions)
# Step 2: Regenerate parser/lexer/visitor from EQL.g4
cd platform-eql-grammar && mvn generate-sources
```
This regenerates `EQLParser.java`, `EQLLexer.java`, `EQLBaseVisitor.java`, `EQLVisitor.java`, `StrictEQLBaseVisitor.java` into `platform-pojo-bl/src/main/java/ua/com/fielden/platform/eql/antlr/`.

### 2. Fluent API

**Progressive interfaces** (`EntityQueryProgressiveInterfaces.java`):
- Define interfaces for each step in the fluent chain.
- Use `ISingleOperand<NextStep, ET>` for steps that accept an arbitrary single operand.
- Use intersection interfaces (extending multiple interfaces) for steps where multiple continuations are valid (e.g., `IYieldOperandConcatOfNext` extends both `IYieldOperandConcatOfOrderBy` and `IYieldOperandConcatOfSeparator`).

**Implementation classes** (one per interface, package-private, extend `AbstractQueryLink` or `SingleOperand`):
- Each class has a `protected abstract T nextFor...(EqlSentenceBuilder builder)` method for wiring.
- Override interface methods to create the next step, passing the builder with the appropriate token appended.
- Use anonymous inner classes in the `nextFor...` methods to close over the outer class's continuation.

**`EqlSentenceBuilder`** — add token-emitting methods if new tokens are needed (e.g., `concatOf()`, `separator()`).
Existing methods (`orderBy()`, `asc()`, `desc()`, `val()`, `param()`, `prop()`, `order()`) can be reused.

**Support `OrderingModel`** — when a function accepts ordering, support both inline (`prop().asc()`) and pre-built `OrderingModel` via an `order(OrderingModel)` method.
This allows reuse of ordering definitions across queries.

**`YieldedItem`** — wire the top-level entry point (e.g., `concatOf()`) by creating the initial `SingleOperand` that transitions into the function's chain.

### 3. Stage Classes

**Stage 1** (e.g., `ConcatOf1`):
- Typically extends `SingleOperandFunction1` or `TwoOperandsFunction1`.
- Holds additional state as immutable fields (e.g., `List<OrderBy1> orderItems`).
- `transform()` converts each field to its Stage 2 equivalent.
- Override `collectEntityTypes()` to include entity types from ALL operands (including auxiliary ones like ORDER BY items).

**Stage 2** (e.g., `ConcatOf2`):
- Extends corresponding `*Function2` base class.
- **Critical:** Override `collectProps()` and `collectEntityTypes()` to include ALL operands.
  This is how the property resolution engine discovers which properties need to be resolved.
  Missing properties here causes `leafProp is null` errors at Stage 3.
- `transform()` threads `TransformationContextFromStage2To3` through each operand sequentially.

**Stage 3** (e.g., `ConcatOf3`):
- `sql()` method generates the final SQL.
- Use `switch (dbVersion)` for database-specific syntax (see `MinuteOf3`, `DayOf3`, `Concat3` for examples).
- Access `operand.sql(metadata, dbVersion)` for each operand's SQL fragment.

### 4. ANTLR Visitor

**`YieldOperandVisitor`** (for yield-context functions) or **`SingleOperandVisitor`** (for scalar functions):
- Add a `visit*` method matching the grammar rule's label.
- Extract operands by visiting child contexts with the appropriate visitor.
- Validate operand types (e.g., separator must be `CharSequence`).
- Use `requireParamValue()` for parameters that must be present.
- Delegate to existing visitors where possible — e.g., `concatOf`'s ORDER BY parsing delegates to `OrderByOperandVisitor` rather than duplicating order-by logic.
- Construct the Stage 1 object.

### 5. Tests

**Execution tests** (`platform-dao/src/test/java/.../eql/execution/functions/`):
- Extend `AbstractEqlExecutionTestCase`.
- Use `retrieveResult(qry)` to execute and get the result.
- Use `retrieveResult(qry, Map.of(...))` for parameterised queries.
- Override `populateDomain()` to insert test data.
- Synthetic UNION subqueries (`select().yield().val(...).as(...).modelAsAggregate()`) are useful for creating test data without persisting entities.

**Fluent API tests** (`FluencyApiTest.java`) — update aggregate/function name arrays if the new function is an aggregate.

## Key Design Patterns

### Reuse Existing Infrastructure

Prefer reusing existing types over creating new ones.
For example, `concatOf`'s ORDER BY reuses the existing `OrderBy1/2/3` records rather than creating dedicated `ConcatOfOrderItem1/2/3` types.
This avoids duplication and leverages existing transformation, SQL generation, and `OrderingModel` support.

### Immutable Collections

Use Guava's `ImmutableList` / `ImmutableSet` for immutable fields in stage classes, not `List.copyOf()` / `List.of()`.
This is the established convention in the EQL codebase:
```java
this.orderItems = ImmutableList.copyOf(orderItems);  // field initialisation
orderItems.stream().map(...).collect(toImmutableList());  // transformation
```

### Property Resolution (collectProps / collectEntityTypes)

The stage 2→3 transformation resolves properties using the set collected by `collectProps()`.
**Every operand that references a property must contribute to this set.**
If an operand is only in an auxiliary position (e.g., ORDER BY inside an aggregate), failing to include it causes `leafProp is null` at runtime.

The pattern:
```java
// In ConcatOf2:
@Override
public Set<Prop2> collectProps() {
    return Stream.concat(super.collectProps().stream(),
                         orderItems.stream().map(OrderBy2::collectProps).flatMap(Collection::stream))
            .collect(toSet());
}
```

### Database-Specific SQL Generation

`ConcatOf3.sql()` demonstrates the pattern:
```java
return switch (dbVersion) {
    case MSSQL -> format("STRING_AGG(%s, %s) WITHIN GROUP (ORDER BY %s)", exprSql, sepSql, orderBySql);
    default -> format("STRING_AGG(%s, %s ORDER BY %s)", exprSql, sepSql, orderBySql);
};
```

PostgreSQL and H2 use the `default` branch. SQL Server (`MSSQL`) uses `WITHIN GROUP (ORDER BY ...)`.
This `switch` pattern is used throughout Stage 3 functions — see `MinuteOf3`, `DayOf3`, `CountDateInterval3` for more examples.

### Context Threading in Stage 2→3

When transforming multiple operands in Stage 2→3, thread the `TransformationContextFromStage2To3` through each transformation sequentially:
```java
var ctx = secondTr.updatedContext;
for (final var item : orderItems) {
    final var itemTr = item.transform(ctx, Yields3.EMPTY);
    orderItems3.add(itemTr.item);
    ctx = itemTr.updatedContext;
}
```

Use `Yields3.EMPTY` when the operands don't reference yield aliases (e.g., intra-aggregate ORDER BY items reference source columns, not yield aliases).

### Fluent API Intersection Interfaces

When a step in the fluent chain allows multiple continuations, define an intersection interface:
```java
interface IYieldOperandConcatOfNext<T, ET>
    extends IYieldOperandConcatOfOrderBy<T, ET>,
            IYieldOperandConcatOfSeparator<T, ET>
{}
```

After `asc()`/`desc()`, the user can either add another `orderBy()` operand or proceed to `separator()`:
```java
interface IYieldOperandConcatOfOrderByOperandOrSeparator<T, ET>
    extends IYieldOperandConcatOfOrderByOperand<T, ET>,
            IYieldOperandConcatOfSeparator<T, ET>
{}
```

### hashCode/equals for Stage Classes

Each stage class must implement `hashCode()` and `equals()` for query caching.
Include the class name hash to differentiate from the parent type, and include all fields:
```java
@Override
public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ConcatOf1.class.getName().hashCode();
    result = prime * result + orderItems.hashCode();
    return result;
}
```

### Install Dependencies Before Running Tests

After changing fluent API classes in `platform-pojo-bl`, install the jar before running DAO tests:
```bash
mvn install -pl platform-pojo-bl,platform-eql-grammar -DskipTests -DdatabaseUri.prefix=//localhost:5432/ci_ -Dfork.count=4
mvn test -pl platform-dao -Dtest=TestClassName -DdatabaseUri.prefix=//localhost:5432/ci_ -Dfork.count=1
```
Otherwise, the `SecurityTokenClassLoader` may load stale classes and fail with `IllegalAccessError`.