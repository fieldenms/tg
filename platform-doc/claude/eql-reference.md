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