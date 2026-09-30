# GraphQL Web API: Entity Condition Types

Status: draft specification.
Scope: the TG GraphQL Web API (`platform-pojo-bl`/`platform-dao`, package `ua.com.fielden.platform.web_api`) and its consumers, including the MCP server (`platform-mcp`).

## 1. Purpose

Conditions in the Web API are currently expressed with field arguments (`eq`, `like`, `value`, `from`, `to`) placed on selected fields, and on root fields (`eq`, `like`).
That mechanism has structural limits:

- a property can be filtered on only if it is selected;
- all conditions are combined with AND; there is no OR across properties, no negation and no grouping;
- there is no test for a missing value, and every condition implicitly excludes missing values;
- ranges are inclusive only;
- comma-separated values in `like` act as OR, so a value containing a comma cannot be matched;
- aggregation root fields (`*_agg`) have no way to express a condition at all.

This specification replaces that mechanism with **entity condition types**: GraphQL input object types, one per entity type, accepted through a single argument `where` on every root field, for both data and aggregation queries.

## 2. Terminology

| Term | Meaning |
|------|---------|
| Entity condition type | Input object type `${E}_Cond` generated for entity type `E`: a combination of conditions, a null test, or a property condition. |
| Property condition type | Input object type `${E}_Cond_cond` generated for entity type `E`: conditions on the properties of `E`. |
| Value condition type | Input object type `${S}_Cond` generated for a value type (GraphQL scalar) `S` used by entity properties. |
| Operator | A field of a value condition type, such as `eq` or `ge`. |
| Atomic condition | One operator applied to one property, e.g. `desc: { iLike: "*pump*" }` contributes one atomic condition. |
| Assigned / unassigned | Whether a property has a value (is not `NULL`). |

## 3. Naming

Every condition type is named `${T}_Cond`, where `T` is the name of the GraphQL type it constrains: `WorkOrder_Cond`, `MaintenanceCapable_Cond`, `String_Cond`, `BigDecimal_Cond`.
Every property condition type is named `${T}_Cond_cond`, where `T` is an entity or union type: `WorkOrder_Cond_cond`, `MaintenanceCapable_Cond_cond`.

Rationale.
GraphQL type names are unique within a schema, and an entity type already cannot share a name with a scalar (both would be GraphQL types).
Deriving every condition type name from its constrained type name by a fixed suffix is therefore collision-free, provided no entity type name itself contains `_Cond`.
This mirrors the existing `${E}_Agg` convention.
Schema construction must fail fast if a generated name collides with an existing type name.

## 4. Schema Additions

### 4.1. Root fields

Every data root field and every aggregation root field gains an optional argument `where`:

```graphql
type Query {
  workOrder(where: WorkOrder_Cond, order: Order, pageNumber: Int, pageCapacity: Int): [WorkOrder]
  workOrder_agg(where: WorkOrder_Cond): [WorkOrder_Agg]
}
```

The same entity condition type serves both root fields of an entity type.
Omitting `where`, or passing `null`, imposes no condition.
Because `${E}_Cond` is a `@oneOf` type (§6.1), `where: {}` is a validation error.

`where` is an ordinary argument, so it may be supplied as a variable (`query($w: WorkOrder_Cond) { workOrder(where: $w) { key } }`).

### 4.2. Arguments that are retired

After migration (see §13), the following arguments are removed:

- `eq`, `like` on root fields;
- `eq`, `like`, `value`, `from`, `to` on property fields.

Arguments `order`, `pageNumber` and `pageCapacity` are outside this specification and remain unchanged.

## 5. Value Condition Types

A value condition type is generated for each value type that occurs as the type of a filterable property.
All operators given in one object are combined with AND, so `{ ge: 7, lt: 8 }` expresses a half-open range.

Input values use the existing scalars and their existing input coercion (`GraphQLLong`, `GraphQLBigDecimal`, `GraphQLMoney`, `GraphQLDate`); this specification introduces no new scalars.

| Type | Operators |
|------|-----------|
| `String_Cond` | `eq`, `ne`, `in`, `notIn`, `like`, `notLike`, `iLike`, `notILike`, `isNull` |
| `Boolean_Cond` | `eq` |
| `Int_Cond`, `Long_Cond`, `BigDecimal_Cond`, `Money_Cond` | `eq`, `ne`, `lt`, `le`, `gt`, `ge`, `in`, `notIn`, `isNull` |
| `Date_Cond` | `eq`, `ne`, `lt`, `le`, `gt`, `ge`, `isNull` |
| `Hyperlink_Cond`, `Colour_Cond` | `isNull` |

Operator types:

- `eq`, `ne`, `lt`, `le`, `gt`, `ge`, `like`, `notLike`, `iLike`, `notILike`: the constrained scalar (`String` for the pattern operators).
- `in`, `notIn`: a list of non-null elements of the constrained scalar, e.g. `[String!]`.
- `isNull`: `Boolean`; `true` means the property is unassigned, `false` means it is assigned.

Notes on the table:

- `Boolean_Cond` has no `isNull` because boolean properties in TG are primitive and always assigned.
  It has no `ne`, which would duplicate `eq` with the opposite value.
- `Date_Cond` has no `in`/`notIn`: equality on date-time instants is rarely meaningful, and ranges cover the practical cases.
- `Hyperlink` and `Colour` scalars have no input coercion (`TgCoercingNoArguments`), so only `isNull` is offered.
  See §15.
- `Money_Cond` compares amounts; the currency is not part of the comparison, as with the existing `from`/`to`.
- The elements of `in` and `notIn` are non-null because, under the semantics of §7.2, a `null` element cannot mean what its author intends.
  `x IN (a, NULL)` is `x = a OR x = NULL`, and `x = NULL` is never true, so the element does not include entities with `x` unassigned.
  `x NOT IN (a, NULL)` is `x <> a AND x <> NULL`, which is never true, so the condition matches no entities at all.
  Unassigned values can be included explicitly with `isNull`, e.g. `or: [{ cond: { status: { in: ["A"] } } }, { cond: { status: { isNull: true } } }]`.
  A `null` element is rejected by GraphQL validation before execution, rather than silently producing such results.

## 6. Entity Condition Types

For every entity type `E`, the schema contains two input types:

```graphql
input E_Cond @oneOf {
  and: [E_Cond!]
  or: [E_Cond!]
  not: E_Cond
  isNull: Boolean
  cond: E_Cond_cond
}

input E_Cond_cond {
  # one field per property of E, see §6.2
  id: Long_Cond
  key: <see §6.3>
  desc: String_Cond        # only if E declares desc
  someString: String_Cond
  someReference: Other_Cond
  someUnion: SomeUnion_Cond
  # ...
}
```

The recursive references are nullable or lists, so the types are valid under the GraphQL input-object circular-reference rule.

### 6.1. Entity condition type

`${E}_Cond` is a `@oneOf` input type: each object specifies exactly one of its fields, with a non-null value.
A condition is therefore exactly one of:

| Field | Meaning |
|-------|---------|
| `and` | All listed conditions hold. |
| `or` | At least one listed condition holds. |
| `not` | The given condition does not hold. |
| `isNull` | Meaningful only where the condition applies to an entity-typed property: `true` means the property is unassigned, `false` means it is assigned. |
| `cond` | All property conditions given in the `${E}_Cond_cond` object hold. |

Keeping connectives and properties in separate types means that no property name can collide with a connective.
Because each object holds exactly one field, there is no question of how `isNull`, `cond` and the connectives combine within one object; that is expressed with `and`.

Graphql-java supports `@oneOf` (`Directives.ONE_OF_DIRECTIVE_DEFINITION`, validated by `ValuesResolverOneOfValidation`); it is marked `@ExperimentalApi` in version 26.0.

### 6.2. Which properties are filterable

`E_Cond_cond` has a field for property `p` if the GraphQL object type `E` (e.g. `WorkOrder`) has a field `p`.

Which fields a given user can see is decided at runtime, not by the structure of the type (see §9).

The type of the field for `p` is:

- `${S}_Cond` if `p` is of value type `S`;
- `${R}_Cond` if `p` is of entity type `R`;
- `${U}_Cond` if `p` is of union type `U`.

For a collectional property, the type is that of its elements.

The following kinds of property have limited support, which is to be extended (see §15):

- Collectional properties cannot be filtered on.

- `@CritOnly` properties cannot be specified in conditions.

### 6.3. Keys

| `keyType` of `E` | Field `key` |
|------------------|-------------|
| `SIMPLE` | Present, typed by the key type: `String_Cond`, `Int_Cond`, … or `${K}_Cond` for an entity-typed key `K`. |
| `COMPOSITE` | Present as `String_Cond`, matching the concatenation of key members with the key separator, exactly as selecting `key` yields it. Each key member is additionally present as its own property field, with its own type. |
| `NO_KEY` | Absent. |

### 6.4. Union condition types

For every union type `U` with members `m1 … mn` of types `M1 … Mn`, the schema contains:

```graphql
input U_Cond @oneOf {
  and: [U_Cond!]
  or: [U_Cond!]
  not: U_Cond
  isNull: Boolean
  cond: U_Cond_cond
}

input U_Cond_cond {
  m1: M1_Cond
  # ...
  mn: Mn_Cond
}
```

`${U}_Cond` has the same structure and meaning as an entity condition type.

## 7. Semantics

### 7.1. Combination

- The property conditions given in one `${E}_Cond_cond` object are combined with AND, as are the operators given in one value condition object.
- Absent fields and fields given as `null` in a property or value condition impose no condition.
  In particular, `eq: null` does not test for a missing value; `isNull: true` does.
- `cond: {}` imposes no condition.
- `and: []` and `or: []` are ignored, as EQL ignores condition groups whose conditions are all empty.
  Ignoring propagates: a condition that consists only of ignored conditions is itself ignored.
  So `not: { or: [] }`, `cond: { status: { and: [] } }` and `where: { and: [] }` impose no condition; in particular, the second does not mean "`status` is not null".
- `in: []` and `notIn: []` are validation errors, as in EQL.

A *root-level condition* is the value of `where` or any condition reached from it through `and`, `or` and `not` alone.
It constrains the root entity itself rather than a property, so `isNull` in it is a validation error.

```graphql
workOrder(where: { or: [
  { not: { isNull: true } },                                 # error: root-level, a work order is never "unassigned"
  { cond: { technician: { isNull: true } } },                # valid: tests property technician
  { cond: { technician: { or: [
      { isNull: true },                                      # valid: still a condition on technician
      { cond: { person: { cond: { active: { eq: false } } } } }
  ] } } }
] })
```

The first alternative is root-level, being reached from `where` through `or` and `not` alone.
The other `isNull` conditions are not: each is reached through `cond` and applies to property `technician`.

### 7.2. Missing values

Conditions have the semantics of EQL, and hence of SQL, including its treatment of `NULL`.
No condition is extended with tests for missing values: the author of a condition decides how missing values are treated, and expresses that with `isNull`.

- An atomic condition other than `isNull` on an unassigned property is neither true nor false (SQL `UNKNOWN`), and an entity matches only if `where` is true for it.
  So neither `desc: { eq: "X" }` nor `desc: { ne: "X" }` matches an entity without a description, and neither does `not: { cond: { desc: { eq: "X" } } }`.
  Entities without a description are included by saying so: `or: [{ cond: { desc: { ne: "X" } } }, { cond: { desc: { isNull: true } } }]`.
- A property reached through an unassigned entity-typed property is itself unassigned, as with EQL's implicit joins.
  So `model: { cond: { make: { isNull: true } } }` matches entities without a model, and `model: { cond: { key: { eq: "316" } } }` does not.
- If an entity-typed property `p` is required, EQL joins the entity referenced by `p` with an inner join.
  A condition on a property of `p` then excludes entities without `p` from the result altogether, wherever it occurs in `where`, including under `or` and `not`.
- A union member other than the active one is unassigned, so the same rules apply to conditions through union members.

Rationale.
Entity centre criteria, compiled by `DynamicQueryBuilder.buildCondition`, test for missing values implicitly, because a criterion has no other means of expressing how they are to be treated.
`DynamicQueryBuilder.buildCondition` enhances condition `c` on property `p` using the following rules:
* `p isNull or c` if `negate XOR orNull`.
  Nulls are included if the condition should be negated or nulls are explicitly requested, but not when both.
* `p isNotNull and c` if `negate == orNull`.
  Nulls are skipped by default and when both negation and explicit nulls are on.

Conditions in `where` are composed explicitly with `and`, `or`, `not` and `isNull`, which makes them closer to EQL than to entity centre criteria.
Following EQL keeps the meaning of a condition the same as that of the EQL it compiles to.

### 7.3. Strings

Wildcards.
`*` is the wildcard of `like`, `notLike`, `iLike` and `notILike`, matching any sequence of characters, including none.
A pattern without `*` matches the whole value.
There is no implicit match-anywhere: "contains `pump`" is written `iLike: "*pump*"`.

Literal characters.
`_` is matched literally (EQL escapes it).
`%` acts as a wildcard, because EQL leaves it unescaped and `*` is translated into it.
See §15.2 for making `%` literal.
`eq`, `ne`, `in`, `notIn` use no wildcards at all; `*` is an ordinary character there.

Case.
`iLike` and `notILike` always ignore case.
`eq`, `ne`, `in`, `notIn`, `like`, `notLike` compare according to the database collation: case-insensitive on a typical SQL Server installation, case-sensitive on PostgreSQL.
This keeps the index-friendly operators index-friendly; `iLike` is the portable way to ignore case.

No other transformation of input.
Values are neither trimmed nor split on commas.
A value containing a comma is matched as it is.

### 7.4. Numbers and money

Comparison operators have their usual meaning; `lt`/`gt` are strict and `le`/`ge` inclusive.
`Money` values are compared by amount.

### 7.5. Dates

Date input keeps the existing formats of `GraphQLDate`: ISO strings from `"2025"` to `"2025-03-15 14:30:00.000"`, or epoch milliseconds, interpreted in the time zone of the current request.
A less precise value denotes an instant, not a period: `"2025-03"` is `2025-03-01 00:00:00.000`.
With strict operators available, a calendar period is expressed without boundary arithmetic:

```graphql
createdDate: { ge: "2025-03", lt: "2025-04" }
```

Relative periods ("last 7 days") remain out of scope.

### 7.6. Entity-typed properties

A nested condition object applies to the referenced entity, to any depth, through the referenced type's own condition type:

```graphql
workOrder(where: { cond: {
  status:   { cond: { key: { in: ["B", "F"] } } }
  workshop: { cond: { location: { cond: { key: { eq: "BNE" } } } } }
} })
```

The existing idiom "match the referenced entity by key" becomes `p: { cond: { key: { … } } }`; matching by `id` becomes `p: { cond: { id: { eq: 42 } } }`.

## 8. Validation and Limits

The following are reported as errors in `errors`, with no data for the affected root field:

- `isNull` in a root-level condition (§7.1);
- exceeding the maximum length of a property path in a condition (proposed: 10 properties, e.g. `workshop.location.key` has length 3);
- exceeding the maximum nesting of `and`, `or` and `not` (proposed: 10);
- exceeding the maximum number of atomic conditions in one `where` (proposed: 200);
- an empty list in `in` or `notIn`;
- exceeding the maximum total number of values across all `in`/`notIn` lists in one `where` (proposed: 1000), which keeps a query below SQL Server's limit of 2100 parameters per statement.

Type errors (unknown fields, wrong scalar types, `null` elements in lists) and violations of `@oneOf` (no field or more than one field in an entity condition) are rejected by standard GraphQL validation before execution.
These limits are separate from the existing selection-depth limit, which does not count input objects.

## 9. Authorisation

A condition must not reveal more than a selection could.
Otherwise, a user could infer the value of a property they are not authorised to read by filtering on it.

- Field visibility for input types mirrors field visibility for output types: a property is visible in `${E}_Cond_cond` iff it is visible in `E`.
  `FieldVisibility` implements `getFieldDefinitions(GraphQLInputFieldsContainer)` and `getFieldDefinition(GraphQLInputFieldsContainer, String)` with the same `visibilityPredicate(E)` it applies to `E`.
  The fields of `${E}_Cond` (`and`, `or`, `not`, `isNull`, `cond`) are always visible.
- Graphql-java validates the query against the visible schema, so a condition on an invisible property fails validation as an unknown field.
- The existing `READ` check on the root entity type applies to both root fields unchanged.

## 10. Compilation to EQL

A `where` value is compiled directly into an EQL `ConditionModel`, which becomes the `where` of the EQL query for the root entity type, for both data and aggregation queries (§11).
The Web API no longer constructs `QueryProperty` instances for conditions, overcoming limitations of `DynamicQueryBuilder` which cannot express nested AND/OR/NOT.

### 10.1. Paths

A *path* is a dot-notated property path from the root entity, as accepted by EQL `prop(…)`, e.g. `workshop.location.key`.
The empty path `ε` denotes the root entity itself.
For a path `x` and a property `p`, `x.p` is `x` extended with `p`, and `ε.p` is `p`.

Compilation is defined by three functions:

- `compile(C, x)` compiles an entity or union condition `C` that constrains the entity at path `x`;
- `propCond(y, D)` compiles a condition `D` given in `cond` for the property at path `y`;
- `value(V, y)` compiles a value condition `V` on the property at path `y`.

A `where: C` is compiled as `compile(C, ε)`.
The path `x` is the *prefix* of every property named inside `C`: nesting a condition under a property extends the prefix by that property.

### 10.2. Rules

Entity and union conditions:

| `C` | `compile(C, x)` |
|-----|-----------------|
| `and: [C1, …, Cn]` | `(compile(C1, x) AND … AND compile(Cn, x))` |
| `or: [C1, …, Cn]` | `(compile(C1, x) OR … OR compile(Cn, x))` |
| `not: C1` | `NOT (compile(C1, x))` |
| `isNull: true` / `false` | `x IS NULL` / `x IS NOT NULL` (`x` is never `ε`, §7.1) |
| `cond: { p1: D1, …, pn: Dn }` | `(propCond(x.p1, D1) AND … AND propCond(x.pn, Dn))` |

Property conditions:

| Property at `y` | `propCond(y, D)` |
|-----------------|--------------|
| value-typed | `value(D, y)` |
| entity- or union-typed | `compile(D, y)` |

The same rule covers union members, since a union's `cond` names a member like any other property: `asset: { cond: { equipment: D } }` at prefix `ε` becomes `compile(D, asset.equipment)`.

Value conditions: each operator given in `V` contributes one atomic condition, and the atomic conditions are combined with AND.

| Operator in `V` | Atomic condition in `value(V, y)` |
|-----------------|-----------------------------------|
| `eq: v`, `ne: v`, `lt: v`, `le: v`, `gt: v`, `ge: v` | `y op v` |
| `in: [v…]`, `notIn: [v…]` | `y IN (v…)` / `y NOT IN (v…)` |
| `like: s`, `notLike: s`, `iLike: s`, `notILike: s` | `y LIKE s′` (and the other three variants), where `s′` is `s` with `*` replaced by `%` |
| `isNull: true` / `false` | `y IS NULL` / `y IS NOT NULL` |

### 10.3. Notes

- Ignored conditions (§7.1) are removed before compilation and produce no EQL.
- No tests for missing values are added to the compiled conditions (§7.2).
- Paths through entity-typed properties rely on EQL's implicit joins.
- Date values are converted with `IDates` in the request's time zone, as today.

### 10.4. Example

```graphql
workOrder(where: { or: [
  { cond: { status: { cond: { key: { in: ["B", "F"] } } } } },
  { not: { cond: { technician: { isNull: true } } } }
] })
```

compiles to:

```java
cond()
    .condition(cond().prop("status.key").in().values("B", "F").model())
    .or()
    .negatedCondition(cond().prop("technician").isNull().model())
    .model()
```

Here the first alternative is `propCond(status, …)` at prefix `ε`, which is `value({ in: … }, status.key)`.

## 11. Aggregation

`where` on `${e}_agg` restricts the rows before grouping (SQL `WHERE`).
Conditions on aggregated values (SQL `HAVING`) are not supported by EQL at present and are therefore outside this specification.

## 12. Introspection (`_entityType`)

- `_Property.arguments` stops listing `eq`, `like`, `value`, `from`, `to` once they are retired; it continues to list `order`.
- `_Property` gains `conditionType: String`: the name of the condition type through which the property can be filtered, or `null` if the property has none.
  This tells a client, in the same response it already uses for discovery, what may appear under `where` and with which operators, without a separate `__type` introspection query.
- `_EntityType` gains `conditionType: String`: the name of the entity type's condition type (`null` for none).

## 13. Reconciliation with the Existing Mechanism

### 13.1. Equivalents

The new forms are shown as the value of `where`; `…` marks where a property's own condition goes.

| Existing | New |
|----------|-----|
| root `eq: "X"` | `{ cond: { key: { eq: "X" } } }` |
| root `like: "A*"` | `{ cond: { key: { like: "A*" } } }` |
| root `like: "A*,B*"` | `{ or: [{ cond: { key: { like: "A*" } } }, { cond: { key: { like: "B*" } } }] }` |
| string `eq: "x"` (case-insensitive exact) | `{ iLike: "x" }` (portable) or `{ eq: "x" }` (collation) |
| string `like: "x"` (contains, case-insensitive) | `{ iLike: "*x*" }` |
| string `like: "x*"` | `{ iLike: "x*" }` |
| string `like: "a,b"` | `or` of two `iLike` conditions, or `in` for exact values |
| entity `eq: "K"` | `p: { cond: { key: { eq: "K" } } }` |
| entity `like: "A,B"` | `p: { cond: { key: { in: ["A", "B"] } } }` |
| entity `like: "A*"` | `p: { cond: { key: { iLike: "A*" } } }` |
| boolean `value: true` | `p: { eq: true }` |
| numeric/date `from: a, to: b` | `p: { ge: a, le: b }` |
| union member `asset { equipment(eq: "E1") { key } }` | `{ cond: { asset: { cond: { equipment: { cond: { key: { eq: "E1" } } } } } } }` |

### 13.2. Behavioural changes

- Filtering no longer requires selection; selection no longer carries filtering.
- No implicit match-anywhere for strings, and no splitting on commas.
- Strict comparisons, negation, OR, grouping and missing-value tests become expressible.
- Missing values are treated as in EQL (§7.2), rather than excluded by an implicit test.
  An atomic condition still does not match an unassigned property, but its negation with `not` does not match it either.
- Conditions on collectional properties remain rejected, and conditions on `@CritOnly` properties remain restricted to what the existing mechanism supports (§6.2).

### 13.3. Migration

1. **Additive phase.**
   `where` is added; the existing arguments stay, marked `@deprecated` (graphql-java supports deprecating arguments).
   If both are used in one root field, the conditions are combined with AND.
2. **Removal phase.**
   The deprecated arguments are removed, together with the `QueryProperty` construction in `RootEntityUtils`.
   `FieldSchema` keeps only `ORDER_ARGUMENT` on property fields.

Known consumers to migrate: the MCP query guide, GraphiQL saved queries in applications, and any external integrations using the Web API.

## 14. Documentation to Update

- `platform-mcp/src/main/resources/mcp/graphql-query-guide.md`: sections *Root Arguments*, *Property Arguments*, *Matching Semantics*, *Combining Filters*, the aggregation *Conditions* example, and *Limitations* 1, 3, 4, 5, 6 are rewritten or removed.
  The claim that `_` acts as a wildcard is incorrect today and must not survive (EQL escapes `_`; `%` is the effective wildcard).
- `platform-mcp/doc/limitations.md` §2.1–§2.4.
- `platform-mcp/doc/architecture.md`, where it describes condition handling.
- Javadoc of `FieldSchema`, `RootEntityUtils`, `EntityAggregation`, `FieldVisibility`, `EntityTypeIntrospection`.

## 15. Open Questions and Deferred Work

### 15.1. Open questions

1. **`@CritOnly` properties.**
   A crit-only property is a parameter of the query rather than a predicate on data, so it fits neither `or`/`not` nor nested conditions.
   One option is a separate root argument, e.g. `params: ${E}_Params`, leaving `${E}_Cond_cond` with predicates only.
2. **Limits.**
   The values in §8 are proposals.

### 15.2. Deferred

1. **Literal `%`.**
   Making `%` literal in patterns requires support from EQL.
   EQL escapes the characters that the databases use to escape `%` (`[` on SQL Server, `\` on PostgreSQL), so an escape added by the Web API before EQL is itself escaped, and the `%` remains a wildcard.
2. **Collections.**
   A natural extension is quantifiers on collectional properties, `p: { _some: C }`, `_every`, `_none`, compiled to `EXISTS`/`NOT EXISTS` subqueries in the manner of `DynamicQueryBuilder.buildCollection`.
   That would change the type of a collectional property's field from the element condition type to a quantifier type, which is a breaking change for clients.
3. **Hyperlink and Colour.**
   Adding input coercion to their scalars would permit `eq`/`in`.
4. **Common properties of unions.**
   EQL supports paths through the common properties of a union (`asset.key`); the output types do not expose them, so the condition types do not either.
   Exposing both would be one change.

## 16. Example: `Person_Cond` (excerpt)

```graphql
input Person_Cond @oneOf {
  and: [Person_Cond!]
  or: [Person_Cond!]
  not: Person_Cond
  isNull: Boolean
  cond: Person_Cond_cond
}

input Person_Cond_cond {
  id: Long_Cond
  key: String_Cond
  desc: String_Cond
  title: String_Cond
  employeeNo: String_Cond
  email: String_Cond

  active: Boolean_Cond
  contractor: Boolean_Cond
  timesheetAllowed: Boolean_Cond

  normalDailyHours: BigDecimal_Cond
  normalWeeklyHours: BigDecimal_Cond
  coreRate: Money_Cond
  refCount: Int_Cond
  createdDate: Date_Cond

  user: User_Cond
  personType: PersonType_Cond
  costCentre: CostCentre_Cond
  misManager: Manager_Cond
  misTeamLeader: TeamLeader_Cond
  delegatedTimesheetMisManager: Person_Cond
  createdBy: User_Cond
}
```

A query using it: active non-contractors in cost centre `A` or `B`, whose email is not on `example.com`, or who have no person type.

```graphql
{
  person(where: {
    and: [
      { cond: {
          active: { eq: true }
          contractor: { eq: false }
          costCentre: { cond: { key: { in: ["A", "B"] } } }
      } }
      { or: [
          { cond: { email: { notILike: "*@example.com" } } }
          { cond: { personType: { isNull: true } } }
      ] }
    ]
  })
  {
    key desc email
    costCentre { key }
  }

  person_agg(where: { cond: { active: { eq: true } } })
  {
    groupBy { costCentre { key } }
    count
    avg { normalWeeklyHours }
  }
}
```
