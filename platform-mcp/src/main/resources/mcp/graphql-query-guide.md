# TG GraphQL Query Guide

This document describes how to query data from a TG system using GraphQL.
It is intended to be read by a language model as part of the natural language to GraphQL translation process.

The domain model itself is not described here.
It is obtained from the system, as described in [Domain Discovery](#domain-discovery).

## Query Structure

A query has this shape:

```graphql
{
  entityName(where: condition, pageCapacity: 10) {
    property1
    property2(order: ASC_1)
    entityProperty {
      key
      desc
      nestedProperty
    }
  }
}
```

- `entityName` is the uncapitalised entity type name (e.g., `workOrder` for entity type `WorkOrder`).
- Argument `where` restricts the result set, as described in [Conditions](#conditions).
  Arguments `pageNumber` and `pageCapacity` paginate the result set.
- Argument `order` on a property field orders the result set, as described in [Ordering](#ordering).
- Property names match the names declared in the entity model (e.g., `desc`, `costCentre`, `createdDate`).
- Entity-typed properties are objects with their own sub-fields; request `key` and `desc` for them where those fields are available, which [Domain Discovery](#domain-discovery) establishes.
- Value-typed properties (strings, numbers, booleans, dates) are leaf nodes, and cannot be expanded with sub-fields.

## Domain Discovery

Do not guess entity types or property names.
Root field `_entityType` describes the domain model: which entity types exist, which of them are queryable, the shape of each key, and every property with its type.
It is an ordinary root field, so it may be selected alongside data fields and batched with other root fields in a single query.

```graphql
{
  _entityType {
    name rootField title desc kind keyType keyMembers keySeparator hasDesc
    properties { name title desc type typeKind collectional required }
  }
}
```

`_entityType` describes only what the current user is authorised to read.
Do not query a type or a property that it does not describe.

### Selecting Types to Describe

Without arguments, `_entityType` describes every type available to the current user, which is a large result.
Even one type with all its properties can be large, so narrow the result to the types a request actually concerns, and select only the fields of `_EntityType` and `_Property` that are needed.

| Argument | Type | Description |
|----------|------|-------------|
| `eq` | `String` | Include the type whose `name` is exactly the specified value. Does not support comma-separated values. Does not permit wildcard `*`. Mutually exclusive with `like`. |
| `like` | `String` | Include types whose `name` matches the specified comma-separated values. Supports wildcard `*`, without which a value is matched exactly. Mutually exclusive with `eq`. |

These are the only arguments of `_entityType`; its `properties` cannot be narrowed.
Both match against `name`, the entity type's simple name, and not against `rootField`.
Matching here differs from matching in a condition, in three ways:
- It is case-sensitive.
- `*` is the only character with a special meaning; `%` and `_` are matched literally.
- A comma separates values, and values are not trimmed, so write `"A,B"` rather than `"A, B"`.

Example — describe the types that a work order query will use:
```graphql
{
  _entityType(like: "WorkOrder,WorkOrderStatus,Priority,CostCentre") {
    name rootField keyType keyMembers keySeparator hasDesc
    properties { name type typeKind collectional required }
  }
}
```

### `_EntityType`

| Field | Description |
|-------|-------------|
| `name` | GraphQL type name, equal to the entity type's simple name (e.g., `WorkOrder`). This is the name a property's `type` refers to. The type of conditions on it is named `${name}_Cond`. |
| `rootField` | Root field used to query this type (e.g., `workOrder`), or `null` if it is not queryable. A type that is described but has no root field, such as a union, is reachable only as a property type. |
| `title` | Human-readable title of the entity type. |
| `desc` | Description of the entity type. |
| `kind` | Nature of the type: `PERSISTENT`, `SYNTHETIC` or `UNION`. |
| `keyType` | Shape of the key: `SIMPLE`, `COMPOSITE` or `NO_KEY`. It decides what `key` yields — see [Keys](#keys). |
| `keyMembers` | Names of the key members: the members themselves if the key is composite, `["key"]` if it is simple, `null` otherwise. |
| `keySeparator` | Separator with which composite key members are concatenated, both in the value of `key` and in what a condition on `key` matches against; `null` for other key shapes. |
| `hasDesc` | Whether this type declares a description, and therefore whether `desc` may be selected on it or used in a condition. |
| `properties` | Properties of this type. For a union, its members. |

### `_Property`

| Field | Description |
|-------|-------------|
| `name` | Property name, as used in a selection and in a condition. |
| `title` | Human-readable title of the property. |
| `desc` | Description of the property. |
| `type` | Name of the property's type: a value type, or an entity type name matching `_EntityType.name`. For a collectional property, the type of its elements. |
| `typeKind` | Kind of the property's type: `VALUE`, `ENTITY` or `UNION`. |
| `collectional` | Whether the property holds a collection of values. |
| `arguments` | Names of the arguments this property field accepts: `order` (see [Ordering](#ordering)), or none for a union-typed or collectional property. |
| `required` | Whether the property is always assigned. |

Four of these fields decide how a property is used in a query.

`type` decides the operators available in a condition on a value-typed property, as listed in [Conditions](#conditions).

`typeKind` cannot be derived from `type`.
The set of value types is closed and listed in [Value Types](#value-types), so a value type is recognisable by name, but an entity type and a union type are not distinguishable that way.
The distinction decides how the property is used: a union exposes neither `key`, `desc` nor `id`, and is both read and filtered through one of its members, as shown in [Union-Typed Properties](#union-typed-properties).

`collectional` identifies a property that can be read but never used in a condition.
To filter by the contents of a collection, see [Limitations](#limitations).

`required` supports reasoning about missing values, which is described in [Missing Values](#missing-values).

## Keys

The shape of an entity type's key decides how it may be selected and used in conditions, and `_EntityType.keyType` reports that shape.

| `keyType` | Property `key` | `keyMembers` |
|-----------|----------------|--------------|
| `SIMPLE` | The key itself, carrying its own type: commonly a string, but it can be a number or an entity reference. | `["key"]` |
| `COMPOSITE` | A string: the key members concatenated with `keySeparator`. | Names of the members. |
| `NO_KEY` | Not available. Selecting it or using it in a condition is a validation error. | `null` |

A composite key is therefore reachable in two ways, and they are not interchangeable.
`key` yields the whole key as a single string, and a condition on it is a `String` condition matching the whole concatenation.
Each member is a property in its own right, with its own type, so a numeric member takes numeric operators, and an entity-typed member takes a nested condition.

Select `key` where one human-readable value is wanted, and a member where that member's own type matters.
Where a member is of a type the Web API does not support, it is absent from `properties` altogether, and `key` is then the only way to reach the key at all.

Example — `Inventory` has a composite key whose members are `store` and `partNumber`, concatenated with a space:
```graphql
{
  inventory(where: { cond: { store: { cond: { key: { eq: "MAIN" } } } } }, pageCapacity: 10) {
    key
    store { key desc }
    partNumber
  }
}
```
This yields `key` values such as `"MAIN 1234-A"`, alongside the members they are composed of.
The same inventory items are matched by `where: { cond: { key: { like: "MAIN *" } } }`.

## Conditions

Every root field, both `e` and `e_agg`, accepts argument `where`, which restricts the entities returned.
For `e_agg`, the condition restricts the entities before they are grouped.
A condition may refer to any non-collectional property that `_entityType` describes, whether or not that property is selected.
`where` is an ordinary argument, so it may also be supplied as a variable of type `E_Cond`, and the same variable can be passed to `e` and `e_agg`.

### Structure

For entity type `E`, `where` is of type `E_Cond`.
Each `E_Cond` input object must specify exactly one field; `{}` and objects with two or more fields are validation errors.

| Field    | Meaning                                                                                                           |
|----------|-------------------------------------------------------------------------------------------------------------------|
| `cond`   | Conditions on properties of `E`, one field per property, named as in `_entityType`. Implicitly combined with AND. |
| `and`    | A list of `E_Cond`, combined with AND. An empty list imposes no condition.                                        |
| `or`     | A list of `E_Cond`, combined with OR. An empty list imposes no condition.                                         |
| `not`    | Negation of an `E_Cond`.                                                                                          |
| `isNull` | Only in a condition on an entity-typed property: `true` if the property is unassigned, `false` if it is assigned. |

To combine `cond` with a connective or with `isNull`, list them in `and`.

`isNull` cannot apply to the root entity itself: it is an error in `where`, and in any condition reached from `where` through `and`, `or` and `not` alone.
It is valid once a condition applies to an entity-typed property, e.g. `where: { cond: { technician: { isNull: true } } }` or `where: { not: { cond: { technician: { isNull: true } } } }`.

The field for a property in `cond` depends on the type of the property:
- an entity-typed property takes the condition type of the referenced type, so conditions nest to any depth: `status: { cond: { key: { eq: "IP" } } }`;
- a union-typed property takes a condition whose `cond` names the union's members, as described in [Union-Typed Properties](#union-typed-properties);
- a value-typed property takes one or more operators, all of which are implicitly combined with AND: `{ ge: 1000, lt: 5000 }`.

An entity-typed property takes no operators directly.
Write `status: { cond: { key: { eq: "C" } } }`, not `status: { eq: "C" }`; the latter is reported as `contains a field not in 'WorkOrderStatus_Cond': 'eq'`.
Match a referenced entity by `id` with `status: { cond: { id: { eq: 42 } } }`.

| Value type                           | Operators                                                                   |
|--------------------------------------|-----------------------------------------------------------------------------|
| `String`                             | `eq`, `ne`, `in`, `notIn`, `like`, `notLike`, `iLike`, `notILike`, `isNull` |
| `Int`, `Long`, `BigDecimal`, `Money` | `eq`, `ne`, `lt`, `le`, `gt`, `ge`, `in`, `notIn`, `isNull`                 |
| `Date`                               | `eq`, `ne`, `lt`, `le`, `gt`, `ge`, `isNull`                                |
| `Boolean`                            | `eq`                                                                        |
| `Hyperlink`, `Colour`                | `isNull`                                                                    |

- `lt` and `gt` are strict; `le` and `ge` are inclusive.
- `in` and `notIn` take a non-empty list without `null` elements.
- `isNull: true` tests that the property is unassigned, `isNull: false` that it is assigned.
- An operator given as `null` imposes no condition: `eq: null` does not test for a missing value, `isNull: true` does.
- `Boolean` has only `eq`: "not active" is `active: { eq: false }`.
- `Money` values are compared by amount.

Key conditions follow the key's shape (see [Keys](#keys)).
A simple key is a property `key` of its own type.
A composite key is a `String` property `key`, matched against the concatenation of the members with `keySeparator`, and each member is also a property in its own right.
A type whose `keyType` is `NO_KEY` has no `key` to put a condition on.

### Strings

- `*` is the wildcard of `like`, `notLike`, `iLike` and `notILike`, matching any sequence of characters, including none.
  A pattern without `*` matches the whole value, so "contains `pump`" is `iLike: "*pump*"`, and "starts with `WO`" is `iLike: "WO*"`.
- `_` is matched literally, while `%` acts as a wildcard, like `*`.
- `eq`, `ne`, `in` and `notIn` use no wildcards.
- `iLike` and `notILike` ignore case.
  Whether the other operators ignore case depends on the database, so use `iLike` wherever case must not matter.
- Values are used as given: they are not trimmed, and commas have no special meaning.
  Several alternative values are expressed with `in`, or with `or` over several patterns.

### Dates

Date values are written in the formats described in [Value Types](#value-types), in the time zone of the current request.
A less precise value denotes an instant, so a calendar period is expressed with a half-open range: `createdDate: { ge: "2025-03", lt: "2025-04" }` is the whole of March 2025.
Relative periods, such as "last 7 days", must be computed and written as absolute values.

### Missing Values

Conditions treat missing values as SQL does, and nothing is added to account for them.
- A condition other than `isNull` on an unassigned property is neither true nor false, so it does not match, and neither does its negation with `not`.
  Neither `desc: { eq: "X" }` nor `not: { cond: { desc: { eq: "X" } } }` matches an entity without a description.
- To include entities where a property is unassigned, use `isNull`: `or: [{ cond: { desc: { ne: "X" } } }, { cond: { desc: { isNull: true } } }]`.
- A property reached through an unassigned entity-typed property is itself unassigned: `model: { cond: { make: { isNull: true } } }` matches entities without a model.
- If an entity-typed property is `required`, a condition on one of its properties excludes entities without it, wherever that condition occurs.
- Every member of a union other than the one that is assigned is unassigned; see [Union-Typed Properties](#union-typed-properties).

Negation is where this matters most.
When a request says "not X", decide whether entities for which X cannot be evaluated belong in the answer, and if they do, add the `isNull` alternative explicitly.

### Union-Typed Properties

A union-typed property holds exactly one of its members, each of which is an entity-typed property.
A condition on it names the members in `cond`:

```graphql
{
  workOrder(where: { cond: { asset: { cond: { equipment: { cond: { key: { eq: "EQ1" } } } } } } }) {
    key
    asset {
      equipment { key desc }
    }
  }
}
```

- Conditions on several members in one `cond` are combined with AND, and since only one member is ever assigned, such a condition never holds.
  Alternatives over members are expressed with `or`: `asset: { or: [{ cond: { equipment: … } }, { cond: { vehicle: … } }] }`.
- A condition that should hold whatever the member, such as "the asset is in workshop BSD", must list every member in `or`.
- `asset: { isNull: true }` matches entities without an asset at all.
- Negating a condition on one member does not match entities whose asset is of another member type, because that member is unassigned for them.
  "Work orders not on equipment EQ1" is therefore not `not: { cond: { asset: { cond: { equipment: { cond: { key: { eq: "EQ1" } } } } } } }`, which omits work orders on vehicles, tools and so on.
  It is:
  ```graphql
  where: { or: [
    { not: { cond: { asset: { cond: { equipment: { cond: { key: { eq: "EQ1" } } } } } } } },
    { cond: { asset: { cond: { equipment: { isNull: true } } } } }
  ] }
  ```
  The second alternative matches work orders whose asset is not equipment, and also those without an asset.

### Errors

Errors are reported in `errors`.
These are validation errors, which fail the whole query document, including every other root field batched with it:
- a field or operator that the condition type does not have, e.g. a misspelt property, a collectional property, or an operator on an entity-typed property;
- a value of the wrong type, or a `null` element in `in` or `notIn`;
- an `E_Cond` object with no field or with more than one field, reported as `Exactly one key must be specified for OneOf type '…_Cond'.`

These are execution errors, which fail only the affected root field:
- an empty list in `in` or `notIn`;
- `isNull` in a condition that applies to the root entity itself.

A validation error message ends with the name of the offending field and the condition type it is not in, e.g. `contains a field not in 'WorkOrder_Cond_cond': 'technicain'`; the text before that repeats the input and can be skipped.
An error in `@oneOf` names only the condition type, so check each object of that type for a missing or a second field.

### Examples

Work orders in progress or on hold, with no technician assigned or with a technician whose person is not active, created in 2025:
```graphql
{
  workOrder(where: { and: [
    { cond: {
        status: { cond: { key: { in: ["IP", "OH"] } } }
        createdDate: { ge: "2025", lt: "2026" }
    } },
    { or: [
        { cond: { technician: { isNull: true } } },
        { cond: { technician: { cond: { person: { cond: { active: { eq: false } } } } } } }
    ] }
  ] }) {
    key
    desc
    technician { key }
  }
}
```

Work orders whose description does not contain "test", including those without a description:
```graphql
{
  workOrder(where: { or: [
    { cond: { desc: { notILike: "*test*" } } },
    { cond: { desc: { isNull: true } } }
  ] }) {
    key
    desc
  }
}
```

Work orders with a status other than "C" or "X", or with an estimated cost of at least 5000 but less than 10000:
```graphql
{
  workOrder(where: { or: [
    { cond: { status: { cond: { key: { notIn: ["C", "X"] } } } } },
    { cond: { estimatedCost: { ge: 5000, lt: 10000 } } }
  ] }) {
    key
    status { key }
    estimatedCost
  }
}
```

Work orders whose workshop is at location "BNE":
```graphql
{
  workOrder(where: { cond: { workshop: { cond: { location: { cond: { key: { eq: "BNE" } } } } } } }) {
    key
    workshop { key }
  }
}
```

## Value Types

| Type | Values |
|------|--------|
| `String` | Strings. |
| `Int`, `Long` | Integers. |
| `BigDecimal` | Decimal numbers. |
| `Money` | Monetary amounts, written as numbers in conditions. |
| `Boolean` | `true` or `false`; always assigned. |
| `Date` | Date-times, described below. |
| `Hyperlink`, `Colour` | Hyperlinks and colours; they can be read and tested for being assigned, but not compared. |

Date input values are strings in one of these formats, from least to most precise:
- `"2025"` — year only
- `"2025-03"` — year and month
- `"2025-03-15"` — full date
- `"2025-03-15 14"` — date and hour
- `"2025-03-15 14:30"` — date, hour, and minute
- `"2025-03-15 14:30:00"` — date and time with seconds
- `"2025-03-15 14:30:00.000"` — date and time with milliseconds

The date and time are separated by a space: the ISO 8601 separator `T` (`"2025-03-15T14:30"`) is rejected, and so is a time zone offset.
An integer is accepted only as a date in the form `yyyyMMdd` (e.g. `20250315`), not as epoch milliseconds.
A less precise value denotes an instant, not a period: `"2025-03"` means `2025-03-01 00:00:00.000`, not the whole of March.
Input values are interpreted in the time zone of the current request.

A `Date` property is a leaf: select it without sub-fields (`createdDate`, not `createdDate { value }`).
Its value is returned as an object with the date-time in the time zone of the current request, including its offset, and epoch milliseconds, e.g. `{ "value": "2025-03-15 14:30:00.000+10:00", "millis": 1742013000000 }`.

## Ordering

Use the `order` argument on a property field to sort results.
The value is an enum combining direction and priority: `ASC_1`, `DESC_1`, `ASC_2`, `DESC_2`, ..., up to `ASC_9`, `DESC_9`.

- `ASC` = ascending, `DESC` = descending.
- The number (1–9) defines the sort priority: `1` is sorted first, `2` is sorted second, and so on.

Multiple properties can be ordered simultaneously by assigning different priorities.
A property is ordered on only if it is selected with `order`.
Argument `order` on a root field orders by the entity key.

Example — order by priority ascending (first), then by created date descending (second):
```graphql
{
  workOrder(pageCapacity: 50) {
    key
    priority(order: ASC_1) { key desc }
    createdDate(order: DESC_2)
  }
}
```

## Pagination

Results of data root fields are paginated by default.

| Argument | Default | Description |
|----------|---------|-------------|
| `pageNumber` | `0` | Zero-based page index. |
| `pageCapacity` | `25` | Maximum entities per page. |

To retrieve more results, increase `pageCapacity` or request subsequent pages.
To learn how many entities satisfy a condition, use `count` of the aggregation root field with the same `where` rather than paging through them.

Example — second page of 100 results:
```graphql
{
  workOrder(pageNumber: 1, pageCapacity: 100) {
    key
    desc
  }
}
```

## Aggregation

The GraphQL schema in TG-based applications contains special types that enable aggregation queries to be expressed.
E.g., answering questions, such as "How many work orders are in progress" or "Average total cost across work orders".

For each queryable entity type `E` with root field `e`, there exists a corresponding aggregation type `E_Agg` with root field `e_agg`.

```
type E_Agg {
  groupBy: E
  count: Int!
  avg: E
  sum: E
  max: E
  min: E
}
```

* `groupBy` -- specifies the shape of a grouping key.
  If `groupBy` is absent, it implicitly places all records into one group (i.e., aggregation runs over the whole set).

* `count` -- selects the number of records within a group.
  This value is never null.
  If a value overflows, the result is 0.

* `avg` -- specifies the properties of `E` for which an average is computed within each group.
  Computation of an average ignores null values.
  It is an error to specify a non-numeric property.
  The type of each resulting value matches the selected property's type.
  This matters for integers -- if a resulting value is not exact, it will be rounded.

* `sum` -- specifies the properties of `E` for which a sum is computed within each group.
  Computation of a sum ignores null values.
  It is an error to specify a non-numeric property.
  The type of each resulting value matches the selected property's type.
  This matters for integers -- if a resulting value is not exact, it will be rounded.

* `max` -- specifies the properties of `E` for which a maximum is computed within each group.
  Computation of a maximum ignores null values.

* `min` -- specifies the properties of `E` for which a minimum is computed within each group.
  Computation of a minimum ignores null values.

Argument `where` restricts the entities before they are grouped, as described in [Conditions](#conditions).
An aggregation root field has no other arguments: its rows are neither paginated nor ordered, so all groups are returned.
Without `groupBy`, the result is a single row, even if no entity satisfies `where` (`count` is then 0); with `groupBy`, it has no rows in that case.

### Example

This example illustrates the use of an aggregation query for entity `WorkOrder`.

```graphql
{
  workOrder_agg(where: { cond: { status: { cond: { key: { eq: "C" } } } } }) {
    groupBy { costCentre { key } }
    count
    avg {
      actualDuration
      lastMeterReading { reading }
    }
  }
}
```

The query above returns the number of completed work orders and their average duration and last meter reading, per cost centre:

```json
{
  "data": {
    "workOrder_agg": [
      {
        "groupBy": {"costCentre": {"key": "A"}},
        "count": 25,
        "avg": {
          "actualDuration": 133.33,
          "lastMeterReading": {"reading": 55.50}
        }
      },
      {
        "groupBy": {"costCentre": {"key": "B"}},
        "count": 9,
        "avg": {
          "actualDuration": 256.25,
          "lastMeterReading": {"reading": 77.3}
        }
      }
    ]
  }
}
```

## Batching Queries

Multiple root fields can be requested in a single query.
Use this to resolve reference values efficiently.

Example — look up all status values and all priority values in one request:
```graphql
{
  workOrderStatus { key desc }
  priority { key desc }
  costCentre { key desc }
}
```

This is more efficient than sending three separate queries.
Use batching whenever you need to resolve multiple reference entity values before constructing the main query.

The same root field can be requested several times with different arguments by giving each an alias, e.g. to count entities under several conditions at once:
```graphql
{
  open: workOrder_agg(where: { cond: { status: { cond: { key: { in: ["IP", "OH"] } } } } }) { count }
  closed: workOrder_agg(where: { cond: { status: { cond: { key: { eq: "C" } } } } }) { count }
}
```

A validation error in any part of a batched query fails the whole query, so none of its root fields return data.
An execution error fails only the root field in which it occurs.

## Complete Example

**Goal:** Find work orders that are in progress, for cost centre A or without a cost centre, ordered by priority.

**Step 1 — Discover the types and resolve reference values** in one request (if the keys for "in progress" and cost centre "A" are not known):
```graphql
{
  _entityType(like: "WorkOrder,WorkOrderStatus,CostCentre") {
    name rootField keyType hasDesc
    properties { name type typeKind collectional required }
  }
  workOrderStatus { key desc }
}
```
Suppose this returns `{ "key": "IP", "desc": "In Progress" }` among the statuses, and shows that `costCentre` of `WorkOrder` is not required.

**Step 2 — Execute the main query:**
```graphql
{
  workOrder(pageCapacity: 50, where: { cond: {
    status: { cond: { key: { eq: "IP" } } }
    costCentre: { or: [{ cond: { key: { eq: "A" } } }, { isNull: true }] }
  } }) {
    key
    desc
    status { key desc }
    costCentre { key desc }
    priority(order: ASC_1) { key desc }
    createdDate(order: DESC_2)
  }
}
```

This returns up to 50 work orders where:
- Status key is "IP" (In Progress).
- Cost centre key is "A", or there is no cost centre.
- Sorted by priority ascending, then by created date descending.

## Response Format

A result is a GraphQL response document with these top-level fields:

- `data` — the requested values, keyed by the response keys of the root selection set (an alias, where used, otherwise a field name).
  Absent if the query failed before execution began.
- `errors` — errors raised while handling the query, each with at least a `message`.
  Present only if there were errors.
- `extensions` — values outside the scope of the GraphQL specification.
  Present only if there are any.

For the query in [Complete Example](#complete-example), a result looks like this:
```json
{
  "data": {
    "workOrder": [
      {
        "key": "WO1",
        "desc": "Replace hydraulic hose",
        "status": {"key": "IP", "desc": "In Progress"},
        "costCentre": {"key": "A", "desc": "Maintenance"},
        "priority": {"key": "1", "desc": "Urgent"},
        "createdDate": {"value": "2025-03-15 14:30:00.000+10:00", "millis": 1742013000000}
      }
    ]
  }
}
```

Fields `data` and `errors` can both be present, because a query can produce partial data.
Always inspect `errors` before reporting a result, even where `data` is populated.

A query that could not be parsed, validated or executed is reported in `errors`.
This is a normal response, not a tool error.
A tool error means only that the request itself was malformed (e.g., no query was supplied) or that the system failed to process it.

## Limitations

1. Collectional properties cannot be used in conditions.
   A collectional property has no field in the condition type, so a condition on it is a validation error, such as `contains a field not in 'User_Cond_cond': 'activeRoles'`.
   To filter by the contents of a collection, query the type of its elements as a root field instead:
   * Take the type of the collectional property from `_entityType`, where it is reported as the `type` of that property.
     For `activeRoles` of `User` that type is `SynUserAndRoleAssociationActive`.
   * If `_entityType` reports a `rootField` for that type, query it as a root field and place the condition on its own properties.
   * Select the property that references the owning entity, and take the owning entities from the result.
   ```graphql
   {
     synUserAndRoleAssociationActive(where: { cond: { userRole: { cond: { key: { eq: "ADMIN" } } } } }) {
       user { key desc }
       userRole { key }
     }
   }
   ```
   Such a result has an entry per collection element rather than per owning entity: the same owning entity is repeated for each of its matching elements and must be de-duplicated, and `pageCapacity` limits the number of elements instead of the number of owning entities.
   If the type of the elements is not queryable, the condition cannot be expressed at all.
   The absence of matching elements ("users without role ADMIN") cannot be expressed either.
   Report this rather than presenting unfiltered results as if they matched.

2. Conditions compare a property with given values only.
   Two properties cannot be compared with each other (e.g. "actual cost exceeds estimated cost"), and relative periods (e.g. "previous month", "last 7 days") must be computed and specified as absolute values.

3. Aggregates cannot be used in conditions.
   `where` restricts the entities before grouping, and there is no way to restrict or order the groups by an aggregated value (e.g. "workshops with more than 10 closed work orders").
   Retrieve all groups and select the matching ones from the result.

4. Properties `key` and `desc` are not available on every entity type.
   Property `key` is unavailable where `_EntityType.keyType` is `NO_KEY`, and property `desc` is unavailable where `_EntityType.hasDesc` is `false`.
   Selecting either, or using it in a condition, where it is absent is a validation error, whereas `id` is always available as a stable identifier.
   Select a property only if `_entityType` describes it.

5. A union-typed property, identified by `_Property.typeKind` of `UNION`, exposes only its members.
   Properties `key`, `desc` and `id`, as well as properties common to all members, are not available on a union-typed property.
   Read and filter through a specific member, as shown in [Union-Typed Properties](#union-typed-properties).
