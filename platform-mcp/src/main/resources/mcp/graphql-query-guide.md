# TG GraphQL Query Guide

This document describes how to query data from a TG system using GraphQL.
It is intended to be read by a language model as part of the natural language to GraphQL translation process.

The domain model itself is not described here.
It is obtained from the system, as described in [Domain Discovery](#domain-discovery).

## Query Structure

A query has this shape:

```graphql
{
  entityName(rootArguments) {
    property1
    property2(propertyArguments)
    entityProperty(propertyArguments) {
      key
      desc
      nestedProperty
    }
  }
}
```

- `entityName` is the uncapitalised entity type name (e.g., `workOrder` for entity type `WorkOrder`).
- Root arguments filter and paginate the result set.
- Property names match the names declared in the entity model (e.g., `desc`, `costCentre`, `createdDate`).
- Entity-typed properties are objects with their own sub-fields; request `key` and `desc` for them where those fields are available, which [Domain Discovery](#domain-discovery) establishes.
- Scalar properties (strings, numbers, booleans) are leaf nodes.

## Domain Discovery

Do not guess entity types, property names, or the arguments a property accepts.
Root field `_entityType` describes the domain model: which entity types exist, which of them are queryable, the shape of each key, and every property with its type and arguments.
It is an ordinary root field, so it may be selected alongside data fields and batched with other root fields in a single query.

```graphql
{
  _entityType {
    name rootField title desc kind keyType keyMembers keySeparator hasDesc
    properties { name title desc type typeKind collectional arguments required }
  }
}
```

`_entityType` describes only what the current user is authorised to read.
Do not query a type or a property that it does not describe.

### Selecting Types to Describe

Without arguments, `_entityType` describes every type available to the current user, which is a large result.
Narrow it to the types a request actually concerns.

| Argument | Type | Description |
|----------|------|-------------|
| `eq` | `String` | Include the type whose `name` is exactly the specified value. Does not support comma-separated values. Does not permit wildcard `*`. Mutually exclusive with `like`. |
| `like` | `String` | Include types whose `name` matches the specified comma-separated values. Supports wildcard `*`, without which a value is matched exactly. Mutually exclusive with `eq`. |

Both match against `name`, the entity type's simple name, and not against `rootField`.
Matching here differs from matching in a data query, in three ways:
- It is case-sensitive.
- `*` is the only character with a special meaning; `%` and `_` are matched literally.
- Values are not trimmed, so write `"A,B"` rather than `"A, B"`.

Example — describe the types that a work order query will use:
```graphql
{
  _entityType(like: "WorkOrder,WorkOrderStatus,Priority,CostCentre") {
    name rootField keyType keyMembers keySeparator hasDesc
    properties { name type typeKind collectional arguments }
  }
}
```

### `_EntityType`

| Field | Description |
|-------|-------------|
| `name` | GraphQL type name, equal to the entity type's simple name (e.g., `WorkOrder`). This is the name a property's `type` refers to. |
| `rootField` | Root field used to query this type (e.g., `workOrder`), or `null` if it is not queryable. A type that is described but has no root field, such as a union, is reachable only as a property type. |
| `title` | Human-readable title of the entity type. |
| `desc` | Description of the entity type. |
| `kind` | Nature of the type: `PERSISTENT`, `SYNTHETIC` or `UNION`. |
| `keyType` | Shape of the key: `SIMPLE`, `COMPOSITE` or `NO_KEY`. It decides what `key` yields — see [Keys](#keys). |
| `keyMembers` | Names of the key members: the members themselves if the key is composite, `["key"]` if it is simple, `null` otherwise. |
| `keySeparator` | Separator with which composite key members are concatenated, both in the value of `key` and in what a condition matches against; `null` for other key shapes. |
| `hasDesc` | Whether this type declares a description, and therefore whether `desc` may be selected on it. |
| `properties` | Properties of this type. For a union, its members. |

### `_Property`

| Field | Description |
|-------|-------------|
| `name` | Property name, as used in a query. |
| `title` | Human-readable title of the property. |
| `desc` | Description of the property. |
| `type` | Name of the property's type: a value type, or an entity type name matching `_EntityType.name`. For a collectional property, the type of its elements. |
| `typeKind` | Kind of the property's type: `VALUE`, `ENTITY` or `UNION`. |
| `collectional` | Whether the property holds a collection of values. |
| `arguments` | Names of the arguments this property accepts, e.g., `["eq", "like", "order"]` or `["from", "to", "order"]`. A collectional property accepts none. |
| `required` | Whether the property is always assigned. |

Three of these fields answer questions that nothing else in a query can answer.

`typeKind` cannot be derived from `type`.
The set of value types is closed and described in [Property Arguments](#property-arguments), so a value type is recognisable by name, but an entity type and a union type are not distinguishable that way.
The distinction decides how the property is used: a union exposes neither `key`, `desc` nor `id`, accepts no arguments, and must be traversed through one of its members, as shown in [Union Entity Properties](#union-entity-properties).

`collectional` identifies a property that can be read but never filtered on.
A condition placed inside a collectional property is silently discarded, so recognise a collection before attempting to filter through one — see [Limitations](#limitations).

`required` supports reasoning about missing values.
Every condition implicitly excludes entities where the property is unassigned, which cannot occur for a required property.

## Keys

The shape of an entity type's key decides how it may be selected, and `_EntityType.keyType` reports that shape.

| `keyType` | Property `key` | `keyMembers` |
|-----------|----------------|--------------|
| `SIMPLE` | The key itself, carrying its own type: commonly a string, but it can be a number or an entity reference. | `["key"]` |
| `COMPOSITE` | A string: the key members concatenated with `keySeparator`. | Names of the members. |
| `NO_KEY` | Not available. Selecting it is a validation error. | `null` |

A composite key is therefore reachable in two ways, and they are not interchangeable.
`key` yields the whole key as a single string and accepts `eq`, `like` and `order`, as any string field does.
Each member is a property in its own right, with its own type and arguments, so a numeric member accepts `from` and `to`, and an entity-typed member is expanded with sub-fields.

Select `key` where one human-readable value is wanted, and a member where that member's own type matters.
Where a member is of a type the Web API does not support, it is absent from `properties` altogether, and `key` is then the only way to reach the key at all.

Example — `Inventory` has a composite key whose members are `store` and `partNumber`, concatenated with a space:
```graphql
{
  inventory(pageCapacity: 10) {
    key
    store { key desc }
    partNumber
  }
}
```
This yields `key` values such as `"MAIN 1234-A"`, alongside the members they are composed of.

A condition on `key` matches the whole concatenation; a condition on a member matches that member alone.
The same concatenation is what a condition on an entity-typed property matches against when the referenced type has a composite key.
How `eq` and `like` behave on a composite `key` is not quite how they behave on an ordinary string, so see [Matching Semantics](#matching-semantics-for-eq-and-like).

## Root Arguments

These arguments are placed on the entity field itself and apply to the entity as a whole.

| Argument | Type | Description |
|----------|------|-------------|
| `eq` | `String` | Include entities whose key matches the specified value exactly. Does not support comma-separated values. Does not permit wildcard `*`. Mutually exclusive with `like`. |
| `like` | `String` | Include entities whose key matches the specified comma-separated values. Supports wildcard `*`. Mutually exclusive with `eq`. |
| `order` | `Order` | Order results by entity key. Value is one of `ASC_1`..`ASC_9` or `DESC_1`..`DESC_9`. |
| `pageNumber` | `Int` | Zero-based page index. Default: `0`. |
| `pageCapacity` | `Int` | Maximum number of entities per page. Default: `25`. |

Arguments `eq` and `like` match against the entity's key whatever its shape, so for a composite key they match the concatenation of its members — see [Keys](#keys).

Example — fetch the first 10 work orders whose key starts with "WO":
```graphql
{
  workOrder(like: "WO*", pageCapacity: 10) {
    key
    desc
  }
}
```

## Property Arguments

These arguments are placed on individual property fields within the selection set.
They filter the parent entity based on the property value.
Available arguments depend on the property type.

A property can only be filtered on if it is selected: the same field carries both the condition and the returned value.

### String Properties

| Argument | Description |
|----------|-------------|
| `eq` | Exact match. No wildcards. Mutually exclusive with `like`. |
| `like` | Pattern match. Supports wildcard `*`. If no `*` is used, matches anywhere in the string (i.e., contains). Supports comma-separated values. Mutually exclusive with `eq`. |
| `order` | Ordering. |

Property `key` of a type whose `keyType` is `COMPOSITE` is a string field accepting these same arguments, but `like` does not behave on it as it does on an ordinary string — see [Keys](#keys).

Example — work orders whose description contains "urgent":
```graphql
{
  workOrder {
    key
    desc(like: "urgent")
  }
}
```

### Boolean Properties

| Argument | Description |
|----------|-------------|
| `value` | `true` to include only entities where the property is true; `false` for only false. Omit to include both. |
| `order` | Ordering. |

Example — only active vehicles:
```graphql
{
  vehicle {
    key
    desc
    active(value: true)
  }
}
```

### Numeric Properties (Integer, Long, BigDecimal, Money)

| Argument | Description |
|----------|-------------|
| `from` | Include entities where the property value is greater than or equal to this value. |
| `to` | Include entities where the property value is less than or equal to this value. |
| `order` | Ordering. |

Example — work orders with estimated cost between 1000 and 5000:
```graphql
{
  workOrder {
    key
    estimatedCost(from: 1000, to: 5000)
  }
}
```

### Date Properties

| Argument | Description |
|----------|-------------|
| `from` | Include entities where the date is on or after this value. |
| `to` | Include entities where the date is on or before this value. |
| `order` | Ordering. |

Date input values should be specified as strings in ISO format.
Supported formats (from least to most precise):
- `"2025"` — year only
- `"2025-03"` — year and month
- `"2025-03-15"` — full date
- `"2025-03-15 14"` — date and hour
- `"2025-03-15 14:30"` — date, hour, and minute
- `"2025-03-15 14:30:00"` — date and time with seconds
- `"2025-03-15 14:30:00.000"` — date and time with milliseconds

A less precise value denotes an instant, not a period: `"2025-03"` means `2025-03-01 00:00:00.000`, not the whole of March.
Both `from` and `to` are inclusive, so a period must be specified through its exact boundaries.
Input and output values are interpreted in the time zone of the current request.

Date output values are returned as objects: `{ "value": "2025-03-15 14:30:00", "millis": 1742045400000 }`.

Example — work orders created in March 2025:
```graphql
{
  workOrder {
    key
    createdDate(from: "2025-03-01", to: "2025-03-31 23:59:59.999")
  }
}
```

### Entity-Typed Properties

| Argument | Description |
|----------|-------------|
| `eq` | Exact match on the referenced entity's key. No wildcards. Mutually exclusive with `like`. |
| `like` | Match on the referenced entity's key. Supports comma-separated values for matching multiple keys. When no `*` wildcard is used, performs exact match (unlike string properties where it matches anywhere). Mutually exclusive with `eq`. |
| `order` | Ordering. |

Entity-typed properties are objects: expand them with sub-fields like `key` and `desc`.

Example — work orders with status key "IP":
```graphql
{
  workOrder {
    key
    status(eq: "IP") {
      key
      desc
    }
  }
}
```

Example — work orders for cost centres "A" or "B":
```graphql
{
  workOrder {
    key
    costCentre(like: "A,B") {
      key
      desc
    }
  }
}
```

### Hyperlink and Colour Properties

Only `order` is available. No filtering arguments.

### Union Entity Properties

A union-typed property exposes only its members, each of which is an entity-typed property with its own arguments.
The union-typed property itself accepts no arguments.
Both reading and filtering go through a specific member.

Example — work orders whose asset is equipment "EQ1":
```graphql
{
  workOrder {
    key
    asset {
      equipment(eq: "EQ1") { key desc }
    }
  }
}
```

### Collectional Properties

No arguments at field level. Returns a list of nested entities.
Do not place conditions on properties inside a collectional property — see [Limitations](#limitations).

## Matching Semantics for `eq` and `like`

Common rules for `eq` and `like`, wherever they appear:
- `*` is the wildcard, matching any sequence of characters, including none.
  Only `like` permits it.
- A comma always separates values, so a value that contains a comma cannot be matched.
- Leading and trailing whitespace is discarded from each value, and a value consisting of `*` alone imposes no condition.

For string properties:
- Matching always ignores case.
  `eq` is an exact match that ignores case, not a strict equality.
- Characters `%` and `_` are not escaped.
  They reach the underlying SQL `LIKE` and act as wildcards there: `%` matches any sequence of characters and `_` matches any single character.
  A value that contains either of them cannot be matched literally.

For entity-typed properties and root arguments, which match against the referenced entity's key:
- A value without `*` is matched strictly, and whether such matching ignores case is determined by the database collation.
- A value with `*` is matched ignoring case, with `%` and `_` behaving as described above.

For property `key` of a type whose key is composite, which is a string holding the concatenation of the key members:
- `eq` follows the string rules: an exact match that ignores case.
- `like` follows the entity-typed rules, comma-separated values included.
  It therefore does not match anywhere: `like: "SMITH"` does not match `JOHN SMITH`, whereas `like: "*SMITH"` does.

## Ordering

Use the `order` argument to sort results.
The value is an enum combining direction and priority: `ASC_1`, `DESC_1`, `ASC_2`, `DESC_2`, ..., up to `ASC_9`, `DESC_9`.

- `ASC` = ascending, `DESC` = descending.
- The number (1–9) defines the sort priority: `1` is sorted first, `2` is sorted second, and so on.

Multiple properties can be ordered simultaneously by assigning different priorities.

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

Results are paginated by default.

| Argument | Default | Description |
|----------|---------|-------------|
| `pageNumber` | `0` | Zero-based page index. |
| `pageCapacity` | `25` | Maximum entities per page. |

To retrieve more results, increase `pageCapacity` or request subsequent pages.

Example — second page of 100 results:
```graphql
{
  workOrder(pageNumber: 1, pageCapacity: 100) {
    key
    desc
  }
}
```

## Combining Filters

All filter arguments are combined with AND logic.
If a query has a root-level `like` and a property-level `eq`, both conditions must be satisfied.

Example — work orders whose key starts with "WO" AND status is "IP" AND cost centre is "A":
```graphql
{
  workOrder(like: "WO*") {
    key
    desc
    status(eq: "IP") { key desc }
    costCentre(eq: "A") { key desc }
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

## Complete Example

**Goal:** Find all work orders that are in progress, ordered by priority, for cost centre A.

**Step 1 — Resolve reference values** (if the keys for "in progress" and cost centre "A" are not known):
```graphql
{
  workOrderStatus { key desc }
}
```
Suppose this returns `{ "key": "IP", "desc": "In Progress" }` among others.

**Step 2 — Execute the main query:**
```graphql
{
  workOrder(pageCapacity: 50) {
    key
    desc
    status(eq: "IP") { key desc }
    costCentre(eq: "A") { key desc }
    priority(order: ASC_1) { key desc }
    createdDate(order: DESC_2)
  }
}
```

This returns up to 50 work orders where:
- Status key is "IP" (In Progress).
- Cost centre key is "A".
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
        "createdDate": {"value": "2025-03-15 14:30:00", "millis": 1742045400000}
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

1. Combining conditions with `OR` across different properties is not supported.
   In most cases, conditions are implicitly combined with `AND`.
   Conditions are combined with `OR` in the following cases:
   * Argument `like` on entity-typed properties when its value is a comma-separated list.
     E.g., `costCentre(like: "A,B")`.
   * Argument `like` on string properties when its value is a comma-separated list.
     E.g., `key(like: "WO1,WO2")`.
   * Argument `like` on property `key` of a type whose key is composite, when its value is a comma-separated list.

2. Ad-hoc aggregation cannot be expressed.
   By design, the shape of an output follows the shape of the selected graph.
   TG does not currently provide GraphQL types that could shape ad-hoc aggregations.

3. Conditions on properties inside a collectional property are silently ignored.
   Such a condition is discarded while the query is composed: no error is reported, and the result contains entities that do not match it.
   For example, this query returns *all* users, not only those with role "ADMIN":
   ```graphql
   {
     user {
       key
       activeRoles {
         userRole(eq: "ADMIN") { key }
       }
     }
   }
   ```
   A collectional property can only be read, never filtered on.
   To filter by the contents of a collection, query the type of its elements as a root field instead:
   * Take the type of the collectional property from `_entityType`, where it is reported as the `type` of that property.
     For `activeRoles` above that type is `SynUserAndRoleAssociationActive`.
   * If `_entityType` reports a `rootField` for that type, query it as a root field and place the condition on its own properties.
   * Select the property that references the owning entity, and take the owning entities from the result.
   ```graphql
   {
     synUserAndRoleAssociationActive {
       user { key desc }
       userRole(eq: "ADMIN") { key }
     }
   }
   ```
   Such a result has an entry per collection element rather than per owning entity: the same owning entity is repeated for each of its matching elements and must be de-duplicated, and `pageCapacity` limits the number of elements instead of the number of owning entities.
   If the type of the elements is not queryable, the condition cannot be expressed at all.
   Report this rather than presenting unfiltered results as if they matched.

4. Conditions cannot be negated.
   There is no way to express "not equal to", "not like" or "not among these values".

5. Missing values cannot be queried, and every condition excludes them.
   There is no way to express "property is not assigned" (e.g. work orders with no cost centre).
   Furthermore, a property with a condition is implicitly required to be assigned, so "cost centre is A or is not assigned" is not expressible either.

6. Range conditions are inclusive, and accept only absolute values.
   Arguments `from` and `to` always include the boundary of the specified value; "strictly greater than" and "strictly less than" cannot be expressed.
   Relative periods (e.g. "previous month", "last 7 days") are not supported: compute the absolute period and specify it explicitly.

7. Properties `key` and `desc` are not available on every entity type.
   Property `key` is unavailable where `_EntityType.keyType` is `NO_KEY`, and property `desc` is unavailable where `_EntityType.hasDesc` is `false`.
   Selecting either where it is absent is a validation error, whereas `id` is always available as a stable identifier.
   Select a property only if `_entityType` describes it.

8. A union-typed property, identified by `_Property.typeKind` of `UNION`, exposes only its members.
   Properties `key`, `desc` and `id`, as well as properties common to all members, are not available on a union-typed property.
   Read and filter through a specific member, as shown in [Union Entity Properties](#union-entity-properties).
