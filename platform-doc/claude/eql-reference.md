# EQL Reference

Type-safe fluent query language. Grammar: `platform-eql-grammar/src/main/antlr4/EQL.g4`.
Utilities: `EntityQueryUtils` (static import). Multi-stage compilation (EqlStage0-3).

```java
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static metamodels.MetaModels.*;
```

## Query Construction

```
select(Entity.class)                         // FROM entity type
select(select(...).model())                  // FROM subquery
  .join(E.class).as("e").on()...             // JOIN (optional)
  .leftJoin(E.class).on()...
  .where()                                   // WHERE
    .prop(E_.x()).eq().val(v)
    .and().prop(E_.y()).gt().val(n)
    .or().begin()...end()                    // Grouping (up to 3 levels)
  .groupBy().prop(E_.x())                    // GROUP BY
  .yield().prop(E_.x()).as("x")              // YIELD
  .yield().sumOf().prop(E_.y()).as("total")
  .orderBy().prop(E_.x()).asc()              // ORDER BY
  .model()                                   // EntityResultQueryModel<T>
  .modelAsAggregate()                        // AggregatedResultQueryModel
  .modelAsEntity(Other.class)                // Cast to different entity
```

## Operators

**Comparison:** `.eq()`, `.ne()`, `.gt()`, `.lt()`, `.ge()`, `.le()`
**Nullability:** `.isNull()`, `.isNotNull()`
**String:** `.like()`, `.notLike()`, `.iLike()`, `.notILike()`
**Sets:** `.in().values(...)`, `.in().model(subquery)`, `.notIn().values(...)`
**Existence:** `.exists(subquery)`, `.notExists(subquery)`
**Quantified:** `.eq().any(subquery)`, `.eq().all(subquery)`
**Multi-prop:** `.anyOfProps("p1", "p2").isNotNull()`, `.allOfProps("p1", "p2").eq().val(...)`

## Operand Types

| Operand | Behaviour |
|---------|-----------|
| `.val(value)` | Literal value (null → condition fails) |
| `.iVal(value)` | Ignore-if-null (skips condition when null) |
| `.param("name")` / `.iParam("name")` | Named parameter (iParam ignores null) |
| `.prop("name")` | Property reference |
| `.extProp("name")` | Property from outer/master query |
| `.now()` | Current timestamp |
| `.model(subquery)` | Subquery result |
| `.expr(expressionModel)` | Pre-built expression |

## Functions

**Date:** `.yearOf()`, `.monthOf()`, `.dayOf()`, `.hourOf()`, `.minuteOf()`, `.secondOf()`, `.dateOf()`, `.dayOfWeekOf()`
**String:** `.upperCase()`, `.lowerCase()`, `.concat().prop("a").with().val(" ").with().prop("b").end()`
**Numeric:** `.round().to(2)`, `.absOf()`
**Null:** `.ifNull().prop("x").then().val(default)`
**Date arithmetic:** `.addTimeIntervalOf().val(30).days().to().prop("startDate")`
**Date diff:** `.prop("end").count().days().between().prop("start")`

## Aggregates (in yield)

`.countAll()`, `.countOf()`, `.sumOf()`, `.avgOf()`, `.maxOf()`, `.minOf()`

## CASE WHEN

```java
.caseWhen().prop("status").eq().val("ACTIVE").then().val(1)
    .when().prop("status").eq().val("PENDING").then().val(2)
    .otherwise().val(0)
    .end()       // Auto-detect type
    // Or: .endAsInt(), .endAsStr(50), .endAsDecimal(10, 2), .endAsBool()
```

## Arithmetic

`.add()`, `.sub()`, `.mult()`, `.div()`, `.mod()`
Parentheses: `.beginExpr()...endExpr()`

## Fetch Models

Control which properties are loaded. Core class: `fetch<T>` (immutable).

**FetchCategory hierarchy** (most → least comprehensive):
`ALL_INCL_CALC` → `ALL` → `DEFAULT` → `KEY_AND_DESC` → `ID_AND_VERSION` → `ID_ONLY` → `NONE`

**Factory methods:** `fetch()` (DEFAULT), `fetchAll()`, `fetchAllInclCalc()`, `fetchOnly()` (ID_AND_VERSION), `fetchKeyAndDescOnly()`, `fetchIdOnly()`, `fetchNone()`
**Instrumented variants:** `fetchAndInstrument()`, `fetchAllAndInstrument()`, `fetchOnlyAndInstrument()`, `fetchKeyAndDescOnlyAndInstrument()`

**Preferred pattern — `IFetchProvider`** in companion interfaces with metamodel paths:
```java
static final IFetchProvider<PmXref> FETCH_PROVIDER = EntityUtils.fetch(PmXref.class).with(
        PmXref_.pmRoutine().relatedPriority(),
        PmXref_.asset().equipment().avgReading());
static final fetch<PmXref> FETCH_MODEL = FETCH_PROVIDER.fetchModel();
```

Override `createFetchProvider()` in DAOs. Raw `fetch<T>` does **not** support dot-notation — use nested fetch models or `IFetchProvider` instead.

**Instrumentation precedence:** Fetch model instrumentation overrides `QueryExecutionModel` lightweightness.

## QueryExecutionModel

```java
from(query)
    .with(fetchModel)                        // Fetch strategy
    .with(orderBy)                           // Ordering
    .with("paramName", paramValue)           // Named parameters
    .lightweight()                           // No instrumentation (unless fetch overrides)
    .model()
```

Execute via: `co(E.class).getAllEntities(qem)`, `.getPage(qem, page, size)`, `.stream(qem)` (use try-with-resources).

## JOIN to Aggregated Subqueries

`leftJoin` accepts entity classes, `EntityResultQueryModel`, and `AggregatedResultQueryModel`:
```java
select(Entity.class)
    .leftJoin(select(Other.class).where(...)
        .groupBy().prop(Other_.foreignKey())
        .yield().prop(Other_.foreignKey()).as("fk")
        .yield().sumOf().prop(Other_.amount()).as("total")
        .modelAsAggregate()).as("agg")
    .on().prop("agg.fk").eq().prop(AbstractEntity.ID)
    .yield().prop(Entity_.name()).as("name")
    .yield().ifNull().prop("agg.total").then().val(0).as("total")
    ...
```

Joined subquery columns are referenced via the alias: `prop("alias.columnName")`.
`IFNULL` handles the LEFT JOIN no-match case (NULL → default).

## Union Entity `.id()` in EQL

`union.id()` resolves to `CASE WHEN member1 IS NOT NULL THEN member1 WHEN member2 IS NOT NULL THEN member2 ... END` in the generated SQL (not `COALESCE`).
Because TG uses **contiguous entity IDs** (globally unique across all tables), this produces a collision-free scalar key.

This enables using union entity IDs in `groupBy`, `yield`, and JOIN conditions:
```java
// GROUP BY a union entity's ID — one group per unique entity across all member types
.groupBy().prop(SomeEntity_.unionProp().id())
.yield().prop(SomeEntity_.unionProp().id()).as("unionId")

// JOIN on a union entity's ID
.leftJoin(pivot).as("p").on().prop("p.unionId").eq().prop(SynEntity_.unionProp().id())
```

## Conditional Aggregation (Pivot Pattern)

Replace N correlated scalar subqueries with a single `GROUP BY` + `SUM(CASE WHEN)`.
This is the EQL equivalent of SQL's conditional aggregation / pivot pattern.

**Before** — N correlated subqueries, each scanning the source table **per outer row**:
```java
// Each .model() is a correlated subquery with extProp — executed once per row in the outer query.
// With 7 categories × 1000 assets = 7000 subquery executions.
.yield().ifNull().model(costByCategory(DAMAGE)).then().val(0).as("damageCost")
.yield().ifNull().model(costByCategory(REPAIR)).then().val(0).as("repairCost")
// ... 5 more categories
```

**After** — one aggregated subquery, scanned **once**, LEFT JOINed:
```java
private static AggregatedResultQueryModel costPivot() {
    return select(SourceEntity.class).where(...)
        .groupBy().prop(SourceEntity_.foreignKey().id())
        .yield().prop(SourceEntity_.foreignKey().id()).as("fkId")
        .yield().sumOf().caseWhen().prop(category).eq().val(DAMAGE)
                .then().prop(cost).otherwise().val(0).end().as("damageCost")
        .yield().sumOf().caseWhen().prop(category).eq().val(REPAIR)
                .then().prop(cost).otherwise().val(0).end().as("repairCost")
        // ... one yield per category
        .modelAsAggregate();
}
```

Then LEFT JOIN the pivot and use `IFNULL` for the no-match default:
```java
select(mainQuery)
    .leftJoin(costPivot()).as("cp").on().prop("cp.fkId").eq().prop(MainEntity_.id())
    .yield().ifNull().prop("cp.damageCost").then().val(0).as("damageCost")
    ...
```

`sumOf()` accepts `caseWhen()` directly (no need for an intermediate `ExpressionModel`), or use `sumOf().expr(exprModel)` for reusable CASE expressions.

## `critCondition` in Subqueries

`critCondition(entityProp, critProp)` works inside LEFT JOIN subqueries and correlated subqueries, not just the main query.
Criteria parameter injection applies to the entire query tree:
```java
.leftJoin(select(Timesheet.class).where()
        .critCondition(Timesheet_.date(), SynEntity_.dateRangeCrit())  // works here
        .groupBy()...
        .modelAsAggregate()).as("lc").on()...
```