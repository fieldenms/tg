# Limitations: GraphQL vs EQL

This document discusses the limitations of GraphQL as a query interface for TG systems, both inherent to GraphQL and specific to the current TG GraphQL integration.

## 1. Inherent GraphQL Limitation: Aggregation

GraphQL's type system ties the response shape to the schema.
Every query returns data conforming to pre-defined types.
This makes aggregation queries (GROUP BY, SUM, COUNT, etc.) unnatural.

In EQL, an aggregation query produces a result with a different shape from the source entity:

```java
select(WorkOrder.class)
    .where().prop(WorkOrder_.status()).ne().val(CANCELLED)
    .groupBy().prop(WorkOrder_.priority())
    .yield().prop(WorkOrder_.priority()).as("priority")
    .yield().countAll().as("count")
    .yield().sumOf().prop(WorkOrder_.estimatedCost()).as("totalCost")
    .modelAsAggregate()
```

This returns `{priority, count, totalCost}` — a shape that does not match the `WorkOrder` type.

To support this in GraphQL, one would need to pre-define auxiliary types (e.g., `WorkOrderAgg`) for each aggregation pattern.
This does not scale for ad-hoc analytical questions because users' aggregation needs are open-ended.

TG partially addresses this through synthetic/report entities, which are pre-built aggregation views queryable via GraphQL.
But these only cover anticipated requests.

## 2. Current TG GraphQL Limitations vs EQL

The following EQL features are not exposed through the current GraphQL integration.
These are limitations of the current implementation, not inherent to GraphQL.

### 2.1. Condition Logic

**No OR conditions.**
All GraphQL property filters are combined with AND.
A query cannot express "status is IP OR priority is HIGH".

EQL supports full boolean logic: `and`, `or`, `begin`/`end` for grouping, `notBegin`/`end` for negation.

**No negation.**
There is no way to negate a condition.
A query cannot express "status is NOT IP".

EQL supports: `ne` (not equal), `notLike`, `notILike`, `notIn`, `notExists`, `negatedCondition`, and `notBegin...end`.

**No nested condition grouping.**
Conditions cannot be parenthesised to control precedence.

EQL supports: `begin`/`end` for grouping (up to 3 nesting levels in the fluent API).

### 2.2. Comparison Operators

The GraphQL integration supports:
- `eq` — exact match
- `like` — pattern match
- `from`/`to` — range (inclusive, i.e., `>=` and `<=`)
- `value` — boolean filter

EQL additionally supports:
- `gt` — strictly greater than
- `lt` — strictly less than
- `ne` — not equal
- `isNull` / `isNotNull` — null testing
- `all(subquery)` / `any(subquery)` — quantified comparison

### 2.3. LIKE Variants

The GraphQL integration supports only basic `like` (with wildcard `*`).

EQL additionally supports:
- `iLike` / `notILike` — case-insensitive matching
- `notLike` — negated pattern matching

### 2.4. Membership and Existence Predicates

The GraphQL integration has limited membership support: `like` with comma-separated values approximates `IN` for entity-typed properties.

EQL additionally supports:
- `in().values(...)` / `in().model(subquery)` — set membership
- `notIn().values(...)` — negated set membership
- `exists(subquery)` / `notExists(subquery)` — existence checks
- `existsAnyOf` / `notExistsAnyOf` / `existsAllOf` / `notExistsAllOf` — quantified existence
- `anyOfProps` / `allOfProps` — multi-property predicates (e.g., "any of these properties is not null")

### 2.5. Functions

The GraphQL integration does not expose any EQL functions.

**String functions:** `upperCase`, `lowerCase`, `concat`.

**Date extraction:** `yearOf`, `monthOf`, `dayOf`, `hourOf`, `minuteOf`, `secondOf`, `dayOfWeekOf`, `dateOf`.

**Date arithmetic:** `addTimeIntervalOf` — add an interval (`seconds`, `minutes`, `hours`, `days`, `months`, `years`) to a date.

**Date difference:** `count().days().between()` — compute the difference between two dates in a chosen unit.

**Numeric functions:** `absOf`, `round`.

**Null handling:** `ifNull` — coalesce (return alternative value when null).

**Conditional expressions:** `caseWhen...when...then...otherwise...end` — with typed endings (`endAsInt`, `endAsBool`, `endAsStr`, `endAsDecimal`).

**Current timestamp:** `now` — reference to the current date/time.

**Arithmetic:** `add`, `sub`, `mult`, `div`, `mod` with `beginExpr`/`endExpr` for grouping.

### 2.6. Aggregation and Yields

The GraphQL integration returns entity properties as declared in the schema.
There is no mechanism for custom yields.

EQL additionally supports:
- `groupBy` — grouping
- Aggregate functions in yields: `maxOf`, `minOf`, `sumOf`, `countOf`, `avgOf`, `countAll`
- Distinct aggregates: `sumOfDistinct`, `countOfDistinct`, `avgOfDistinct`
- `concatOf` — string aggregation with ordering and separator
- Custom yield aliases: `yield().prop("x").as("alias")`
- Yield expressions: arithmetic and functions in yields
- `yieldAll` — yield all properties

### 2.7. Joins and Subqueries

The GraphQL integration resolves dot-notation property paths automatically (e.g., `workOrder { costCentre { key } }`), but provides no control over join strategy.

EQL additionally supports:
- Explicit `join` / `leftJoin` with `on` conditions
- Subqueries in conditions: `model(subquery)` as an operand
- Subqueries in yields: `modelAsEntity`, `modelAsPrimitive`
- Correlated subqueries via `extProp` (reference to outer query properties)
- Source subqueries: `select(select(...).model())` — query from a subquery

### 2.8. Pagination

The GraphQL integration uses `pageNumber`/`pageCapacity` (default: page 0, 25 per page).

EQL additionally supports:
- Direct `limit`/`offset` control
