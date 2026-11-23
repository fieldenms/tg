# Limitations: GraphQL vs EQL

This document discusses the limitations of GraphQL as a query interface for TG systems, both inherent to GraphQL and specific to the current TG GraphQL integration.

## 1. TG GraphQL Limitations vs EQL

The following EQL features are not exposed through the current GraphQL integration.
These are limitations of the current implementation, not inherent to GraphQL.

### 2.1. Condition Logic

Conditions are specified with argument `where` on a root field, typed by the entity condition type of the root entity type.
Property conditions within one `cond` are combined with AND, and `and`, `or` and `not` compose conditions to any depth.
This covers EQL's `and`, `or`, `begin`/`end`, `notBegin`/`end` and `negatedCondition`.

### 2.2. Comparison Operators

The GraphQL integration supports `eq`, `ne`, `lt`, `le`, `gt`, `ge` and `isNull`, with the semantics of EQL, including its treatment of `NULL`.

EQL additionally supports:
- comparisons between properties, and between properties and expressions -- in the GraphQL integration, the right operand is always a value;
- `all(subquery)` / `any(subquery)` -- quantified comparison.

### 2.3. LIKE Variants

The GraphQL integration supports `like`, `notLike`, `iLike` and `notILike`, with wildcard `*`.
A literal `%` cannot be matched, as `%` also acts as a wildcard.

### 2.4. Membership and Existence Predicates

The GraphQL integration supports `in` and `notIn` with lists of values.

EQL additionally supports:
- `in().model(subquery)` / `notIn().model(subquery)` -- membership in a subquery result;
- `exists(subquery)` / `notExists(subquery)` -- existence checks;
- `existsAnyOf` / `notExistsAnyOf` / `existsAllOf` / `notExistsAllOf` -- quantified existence;
- `anyOfProps` / `allOfProps` -- multi-property predicates (e.g., "any of these properties is not null").

Conditions on collectional properties and on `@CritOnly` properties are not supported.

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
