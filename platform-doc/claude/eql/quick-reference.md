# EQL — Quick Reference

```java
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static metamodels.MetaModels.*;
```

## Query Construction

```
select(Entity.class)                         // FROM entity type
select(select(...).model())                  // FROM subquery
  .join(E.class).as("e").on()...             // JOIN
  .leftJoin(E.class).on()...
  .where().prop(E_.x()).eq().val(v)          // WHERE
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
**String:** `.like()`, `.iLike()`, `.notLike()`, `.notILike()`
**Sets:** `.in().values(...)`, `.in().model(subquery)`, `.notIn().values(...)`
**Existence:** `.exists(subquery)`, `.notExists(subquery)`
**Quantified:** `.eq().any(subquery)`, `.eq().all(subquery)`
**Multi-prop:** `.anyOfProps("p1", "p2").isNotNull()`, `.allOfProps("p1", "p2").eq().val(...)`

## Operands

| Operand | Behaviour |
|---|---|
| `.val(v)` | Literal (null → condition fails) |
| `.iVal(v)` | Ignore-if-null (skips condition when null) |
| `.prop("name")` / `.extProp("name")` | Property / outer query property |
| `.param("name")` / `.iParam("name")` | Named parameter (iParam ignores null) |
| `.now()` | Current timestamp |
| `.model(subquery)` | Subquery result |
| `.expr(expressionModel)` | Pre-built expression |

## Functions

**Date:** `yearOf()`, `monthOf()`, `dayOf()`, `hourOf()`, `minuteOf()`, `secondOf()`, `dateOf()`, `dayOfWeekOf()`
**String:** `upperCase()`, `lowerCase()`, `concat().prop("a").with().val(" ").with().prop("b").end()`
**Numeric:** `round().to(2)`, `absOf()`
**Null:** `ifNull().prop("x").then().val(default)`
**Date arithmetic:** `addTimeIntervalOf().val(30).days().to().prop("startDate")`
**Date diff:** `prop("end").count().days().between().prop("start")`
**Aggregates (in yield):** `countAll()`, `countOf()`, `sumOf()`, `avgOf()`, `maxOf()`, `minOf()`
**CASE WHEN:** `caseWhen().prop("x").eq().val(1).then().val("A").otherwise().val("B").end()`
Typed endings: `.endAsInt()`, `.endAsStr(50)`, `.endAsDecimal(10, 2)`, `.endAsBool()`
**Arithmetic:** `.add()`, `.sub()`, `.mult()`, `.div()`, `.mod()`.
**Parentheses:** `.beginExpr()...endExpr()`

## Fetch Models

| Factory | Category | Instrumented |
|---|---|---|
| `fetch(T.class)` | DEFAULT | No |
| `fetchAll(T.class)` | ALL | No |
| `fetchAllInclCalc(T.class)` | ALL_INCL_CALC | No |
| `fetchOnly(T.class)` | ID_AND_VERSION | No |
| `fetchKeyAndDescOnly(T.class)` | KEY_AND_DESC | No |
| `fetch/fetchAll/fetchOnlyAndInstrument(T.class)` | varies | Yes |

**FetchCategory hierarchy:** `ALL_INCL_CALC` → `ALL` → `DEFAULT` → `KEY_AND_DESC` → `ID_AND_VERSION` → `ID_ONLY` → `NONE`

**Preferred:** `IFetchProvider` with metamodel paths in companion interfaces:
```java
static final IFetchProvider<E> FETCH_PROVIDER = EntityUtils.fetch(E.class)
    .with(E_.propA().nested(), E_.propB());
```

Raw `fetch<T>` does **not** support dot-notation — use nested fetch models or `IFetchProvider` instead.

## Topic-Specific Gotchas

- **Contiguous entity IDs**: All entities share a single ID sequence — IDs are globally unique across tables.
  Union entity `.id()` compiles to `CASE WHEN member1 IS NOT NULL THEN member1 WHEN member2 IS NOT NULL THEN member2 ... END`.
  Usable in `groupBy`, `yield`, and JOIN conditions.
- **`@Calculated` properties expand in SQL**: `sumOf().prop(cost)` where `cost = hours * rate` becomes `SUM(hours * rate)`.
  Database covering indexes must target the operand columns.
