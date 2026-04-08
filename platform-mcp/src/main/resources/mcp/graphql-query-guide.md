# TG GraphQL Query Guide

This document describes how to query data from a TG system using GraphQL.
It is intended to be read by a language model as part of the natural language to GraphQL translation process.

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
- Entity-typed properties are objects with their own sub-fields; request `key` and `desc` for them where those fields are available (see [Limitations](#limitations)).
- Scalar properties (strings, numbers, booleans) are leaf nodes.

## Root Arguments

These arguments are placed on the entity field itself and apply to the entity as a whole.

| Argument | Type | Description |
|----------|------|-------------|
| `eq` | `String` | Include entities whose key matches the specified value exactly. Does not support comma-separated values. Does not permit wildcard `*`. Mutually exclusive with `like`. |
| `like` | `String` | Include entities whose key matches the specified comma-separated values. Supports wildcard `*`. Mutually exclusive with `eq`. |
| `order` | `Order` | Order results by entity key. Value is one of `ASC_1`..`ASC_9` or `DESC_1`..`DESC_9`. |
| `pageNumber` | `Int` | Zero-based page index. Default: `0`. |
| `pageCapacity` | `Int` | Maximum number of entities per page. Default: `25`. |

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

## Limitations

1. Combining conditions with `OR` across different properties is not supported.
   In most cases, conditions are implicitly combined with `AND`.
   Conditions are combined with `OR` in the following cases:
   * Argument `like` on entity-typed properties when its value is a comma-separated list.
     E.g., `costCentre(like: "A,B")`.
   * Argument `like` on string properties when its value is a comma-separated list.
     E.g., `key(like: "WO1,WO2")`.

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
   * Take the type of the collectional property from the schema of its entity type, as provided by resource `tg://entities/{name}`.
     For `activeRoles` above that type is `SynUserAndRoleAssociationActive`.
   * If that type is present in the entity catalogue, as provided by resource `tg://entities`, query it as a root field and place the condition on its own properties.
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
   If the type of the elements is not present in the entity catalogue, the condition cannot be expressed at all.
   Report this rather than presenting unfiltered results as if they matched.

4. Conditions cannot be negated.
   There is no way to express "not equal to", "not like" or "not among these values".

5. Missing values cannot be queried, and every condition excludes them.
   There is no way to express "property is not assigned" (e.g. work orders with no cost centre).
   Furthermore, a property with a condition is implicitly required to be assigned, so "cost centre is A or is not assigned" is not expressible either.

6. Range conditions are inclusive, and accept only absolute values.
   Arguments `from` and `to` always include the boundary of the specified value; "strictly greater than" and "strictly less than" cannot be expressed.
   Relative periods (e.g. "previous month", "last 7 days") are not supported: compute the absolute period and specify it explicitly.

7. Property `key` is not available for entity types with a composite key.
   Such types expose their key members as separate properties instead, while `id` remains available as a stable identifier.
   Property `desc` is only available for entity types that declare a description.
   Select a property only if it is present in the schema of its entity type.
   Filtering is unaffected: `eq` and `like` for a property that references an entity type with a composite key match against its key members concatenated with the separator declared by that type.

8. A union-typed property exposes only its members.
   Properties `key`, `desc` and `id`, as well as properties common to all members, are not available on a union-typed property.
   Read and filter through a specific member, as shown in [Union Entity Properties](#union-entity-properties).
