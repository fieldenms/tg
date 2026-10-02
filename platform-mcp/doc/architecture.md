# TG MCP Server Architecture

## Purpose

The TG MCP server exposes a TG system to 3rd-party AI systems via the [Model Context Protocol](https://modelcontextprotocol.io/).
It enables a language model to query TG data by translating natural language requests into GraphQL queries.

The expected interaction flow:

1. User asks an LLM a question in natural language (e.g., "Show me all work orders in progress ordered by priority for cost centre A").
2. The LLM reads MCP resources to understand the query syntax and to identify which entity types a request refers to.
3. The LLM queries the meta-schema for the shape of those types, and may execute preliminary GraphQL queries to resolve reference values (e.g., mapping "in progress" to a status key).
4. The LLM constructs the final GraphQL query and executes it via the MCP tool.
5. The LLM presents the results to the user.

## MCP Resources

Resources provide the domain context that a language model needs to translate natural language into GraphQL.

### `tg://query-guide`

A reference document describing the GraphQL query syntax supported by TG.
Covers query structure, conditions, ordering, pagination, date handling, and efficiency tips.

This is a static resource.
Its content is defined in [graphql-query-guide.md](../src/main/resources/mcp/graphql-query-guide.md).

### `tg://entities`

A catalogue of all queryable entity types.
Each entry includes:

| Field | Description |
|-------|-------------|
| `name` | Entity type simple name (e.g., `WorkOrder`) |
| `rootField` | GraphQL root field name used in queries (uncapitalised type name, e.g., `workOrder`) |
| `title` | Human-readable entity title |
| `desc` | Entity description |

The catalogue enables a language model to identify which entity type corresponds to a user's natural language reference.
For example, "work orders" maps to the `workOrder` root field.

Both names are present because they serve different purposes.
`rootField` is what a query selects.
`name` is what a property's `type` refers to, so it is the value to match when resolving the type of property back to a catalogue entry.

The contents of this resource are unchanging within the scope of a running application.
It is the shallow projection of the `_entityType` root field described in [Domain Meta-Schema](#domain-meta-schema), and must be generated from the same code path so that the two cannot drift.
Field names are identical in both.

## MCP Tool

### `execute_query`

Executes a GraphQL query against the TG system and returns the result.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `query` | `string` | yes | The GraphQL query string |
| `variables` | `object` | no | GraphQL variables |

**Returns:** The GraphQL response as JSON, containing `data` and optionally `errors`.

**Usage notes:**

- This tool is used both for preliminary queries (e.g., resolving reference values) and for the final data query.
- Batch multiple lookups into a single query by requesting multiple root fields.
  For example, to resolve both status and priority values:
  ```graphql
  {
    workOrderStatus { key desc }
    priority { key desc }
  }
  ```
- The tool delegates to `IWebApi.execute()`, which is implemented by `GraphQLService`.
  Authorisation is enforced per entity type via `Entity_CanRead_Token`.

## Domain Meta-Schema

The domain model is described to a language model through the GraphQL schema itself.
`EntityTypeIntrospection` adds a `_entityType` root field to the GraphQL schema.

### Schema and data

The name "meta-schema" says what `_entityType` conveys, not what it is.
The GraphQL schema is a schema: `GraphQLService` builds it once, at startup, and every request is validated and executed against that one structure.
`_entityType` is an ordinary root field returning ordinary data, computed by its fetchers on each request.

The two therefore live at different stages, and that is the governing distinction between them.
What the schema expresses, it expresses through its shape, fixed before the first request arrives.
What the meta-schema expresses, it expresses through the values its fetchers return, and so it may differ from one request to the next.

Were structure all that mattered, this would be a distinction without a difference, because the structure of entity types does not change over the life of an application.
But what a client may see of that structure does change, and it changes per user.
Authorisation is the case in hand.
The schema handles it through a facility GraphQL provides for exactly this purpose, `GraphqlFieldVisibility`, which lets a statically built schema present a different subset of itself to each request.
No such facility is available to the meta-schema, because it is data, so its fetchers apply the rules themselves.

This is what settles where each rule belongs.
A rule that the schema has already applied is read back off the schema, which the meta-schema can do because the schema is built before any request arrives.
A rule that no schema can hold, because its answer depends on the request, is stated once in `FieldVisibility` and called by both.
Neither kind is restated in the fetchers; see [Visibility Rules and Where They Are Encoded](#visibility-rules-and-where-they-are-encoded).

Where the two legitimately diverge, it is because a schema and a catalogue are under different obligations; see [Authorisation](#authorisation), which records each such divergence.

### `_EntityType`

| Field | Description |
|-------|-------------|
| `name` | GraphQL type name (e.g., `WorkOrder`) |
| `rootField` | GraphQL root field name (e.g., `workOrder`), or `null` if not root-queryable. A type with no root field is reachable only as a property type |
| `title` | Human-readable entity title |
| `desc` | Entity description |
| `kind` | `PERSISTENT`, `SYNTHETIC` or `UNION` |
| `keyType` | `SIMPLE`, `COMPOSITE` or `NO_KEY` |
| `keyMembers` | Names of the key members if the key is composite; `["key"]` if the key is simple; `null` otherwise |
| `keySeparator` | Separator used to concatenate composite key members, or `null` for other key shapes |
| `hasDesc` | Whether this type declares a description -- whether property `desc` exists |
| `properties` | List of properties, see [`_Property`](#_property) |

`keyType` and `keyMembers` exist because there is no rule to generalise from.
`key` is selectable on every type whose key has a shape at all, but what it yields differs: for a simple key it is the key itself, typed accordingly, whereas for a composite key it is a string -- the members concatenated with `keySeparator`.
For example, if `WorkOrder` has a composite key whose single member is `number`, then both `key` and `number` may be selected, and only `number` carries the type and arguments of a number.
If `Buyer` has a key member `person`, itself an entity reference, then only through `keyMembers` is that reachable as an entity.
A type that declares no key does not declare `key` either, and selecting it is a validation error.

`keySeparator` is required by the matching rules for entity-typed properties.
A condition on a property that references a composite-key type matches against its key members concatenated with that type's separator, so the separator must be known before such a condition can be constructed.
The same concatenation is what `key` yields when selected on such a type directly.

Union types are included, with `rootField` set to `null`.
They are reachable as property types but are not root fields, so a model needs to distinguish "query this as a root field" from "this type exists but cannot be queried directly", which the presence of a root field answers.
For a union, `properties` are its members.

### `_Property`

| Field | Description |
|-------|-------------|
| `name` | Property name as used in GraphQL |
| `title` | Human-readable property title |
| `desc` | Property description |
| `type` | Name of the property's type: a value type, or an entity type name matching `_EntityType.name`. For a collectional property, the type of its elements |
| `typeKind` | `VALUE`, `ENTITY` or `UNION` |
| `collectional` | Whether the property is collectional |
| `arguments` | GraphQL arguments accepted by this property field, e.g., `["order"]` |
| `required` | Whether the property is always assigned |

`typeKind` records one fact that cannot be derived from `type` alone.
The set of value types is closed and documented in the query guide, so a value can be recognised from its type name, but an entity type cannot be distinguished from a union type that way.
The distinction matters at the point of use: a union exposes neither `key`, `desc` nor `id`, and accepts no arguments, so it must be traversed through a member.
Without `typeKind`, every entity-typed property that is selected but not filtered on is a guess.

`collectional` exists because a condition placed inside a collectional property is silently discarded while the query is composed.
No error is reported and the result contains entities that do not match the condition, so a model must be able to recognise a collection before it attempts to filter through one.

For a collectional property, `type` reports the type of its elements, because the type of such a property is determined from its element type when the corresponding GraphQL field is built.
That is also the type to query as a root field when filtering by the contents of a collection.

`arguments` is already determined by `FieldSchema` when building field definitions and is surfaced rather than recomputed.

`required` supports reasoning about missing values: every condition implicitly excludes entities where the property is unassigned, and `required` identifies the properties for which that cannot occur.

### Excluded metadata

Property-level annotations are deliberately **not** exposed.
This covers `@AfterChange`, `@BeforeChange`, `@Dependent`, `@Subtitles`, `@SkipActivatableTracking` and `@SkipEntityExistsValidation`.
They describe mutation-time behaviour and this API is read-only, so they cannot inform a query.
They are also costly: on `WorkOrder` they account for 6,026 of 11,047 description bytes, and they disclose internal handler class names.

The same reasoning applies to the `description` strings that `FieldSchema.metaInformationFor` appends to field definitions.
Once the structured fields above are available, a description should carry title and description only.

This decision is recorded on `EntityTypeIntrospection` so that it stays next to the code that would otherwise reintroduce it.

### Deferred

- Authorisation performance.
  Every authorisation check is a database query: `authoriseReading` reaches `SecurityTokenController.canAccess`, which counts active security role associations, and nothing on that path is cached.
  `_entityType` therefore costs one query per visible type, plus one per `@Authorise`d property of every type it describes, so an unfiltered query over the whole domain costs a query per entity type.
  Memoising the decision for the duration of a request is the obvious remedy.
- Consider modelling `_Property.type` as a GraphQL type rather than a name.
  Resolving it to `_EntityType` would let a single query retrieve a type's properties together with the key shape of every type they reference, which is otherwise one request per referenced type.
  Points to settle: value types have no `_EntityType`, so a nullable entity-typed field alongside the existing name is likely simpler than a union; the domain graph is cyclic, so this interacts with the maximum query depth instrumentation; and `typeKind` becomes derivable from the resolved type's `kind`, so the two decisions should be taken together.

## Interaction Example

**User request:** "Show me all work orders in progress ordered by priority for cost centre A"

**Step 1 — Entity identification.**
The LLM reads `tg://entities` and identifies `workOrder` (title: "Work Order").

**Step 2 — Schema discovery.**
The LLM queries the meta-schema for the types it expects to use.
```graphql
{
  _entityType(like: "WorkOrder,WorkOrderStatus,Priority,CostCentre") {
    name rootField keyType keyMembers hasDesc
    properties { name type typeKind collectional arguments }
  }
}
```
This establishes that:
- `WorkOrder` has a composite key whose single member is `number`, so `number` may be selected as a number in its own right, and `key` as its string representation.
- `status` is of type `WorkOrderStatus` and accepts `order`; `priority` and `costCentre` are analogous.
- `WorkOrderStatus`, `Priority` and `CostCentre` each have a simple key and declare a description, so `key` and `desc` may be selected on them.

**Step 3 — Value resolution.**
The LLM does not know the key for "in progress".
It calls `execute_query` with:
```graphql
{
  workOrderStatus { key desc }
}
```
The result includes `{ "key": "IP", "desc": "In Progress" }` among other statuses.

**Step 4 — Final query.**
The LLM constructs and executes:
```graphql
{
  workOrder(pageCapacity: 50, where: { cond: {
      status: { cond: { key: { eq: "IP" } } },
      costCentre: { cond: { key: { eq: "A" } } } } }) {
    number
    desc
    status { key desc }
    costCentre { key desc }
    priority(order: ASC_1) { key desc }
    createdDate
  }
}
```

**Step 5 — Presentation.**
The LLM formats and presents the results to the user.

## Implementation Notes

### Module Dependencies

`platform-mcp` depends on `platform-pojo-bl`, which provides:
- `IWebApi` — the interface for executing GraphQL queries
- Entity model classes and annotations — for generating resource content
- `IApplicationDomainProvider` — for enumerating domain entity types

The runtime implementation of `IWebApi` (`GraphQLService`) resides in `platform-dao`.

### Authorisation

The MCP server inherits the TG authorisation model:
- `GraphiQL_CanExecute_Token` — controls access to the MCP server as a whole.
- `Entity_CanRead_Token` — controls per-entity query access.
- `Entity_CanReadModel_Token` — controls schema visibility (which entities and properties appear in MCP resources).

`FieldVisibility` enforces `Entity_CanReadModel_Token` for the domain schema.
The meta-schema does the same, at both type and property level, so that `_entityType` cannot describe what the current user is not authorised to read.
It does so in its fetchers rather than through field visibility, for the reason given under [Schema and data](#schema-and-data).

- A type is described only where `FieldVisibility.isModelReadable` holds for it.
  A type with no token of its own, a union for instance, answers to the default `_CanReadModel_Token`.
- The properties of a described type are the fields its GraphQL type exposes to this request, which the meta-schema obtains by asking the schema's own field visibility.
  It is therefore the instance the schema already carries, applied to the field definitions the executor would resolve, that answers for both.
  Withdrawing `<Type>_CanRead_<property>_Token` drops that property alone.

Two differences from the schema follow.
The first is intended; the second is inherited from the schema and only partly so.

1. Where reading a type's model is not authorised, the meta-schema omits the type; the schema keeps it, with `id` as its only field.
   The schema has no choice, as the GraphQL specification does not permit a fieldless type, which is why `FieldVisibility` retains `id`.
   A catalogue is under no such constraint, and a type stripped of everything but `id` tells a client nothing it can act on.
   A consequence worth knowing when reading the code: the type-level check runs first, so the `id`-only branch of `visibilityPredicate` is unreachable from the meta-schema.

2. Union types escape field visibility in the schema: `FieldVisibility.getFieldDefinitions` filters only the containers it recognises as queryable types and returns every field of anything else untouched.
   A union's members are therefore always shown, whatever tokens the user holds.
   The meta-schema takes a type's properties from that same method, so a union's members are shown there too, for the same reason.

   The type-level check is separate and knows nothing of this, applying to every type described.
   Withdrawing the default `_CanReadModel_Token` therefore removes a union from the meta-schema while leaving it untouched in the schema.

   That a union escapes property authorisation at all looks like an oversight in `FieldVisibility` rather than a decision: a union member carrying `@Authorise` is unauthorised in the schema today.
   Whatever is settled there will hold for the meta-schema without further work, which is the point of taking the properties from the same method.

### Visibility Rules and Where They Are Encoded

The GraphQL schema, the `_entityType` meta-schema and the `tg://entities` resource must all describe the same domain.
Set out below is every rule that decides what the schema exposes, what the meta-schema does with each one today, and where each is to be stated from now on.

#### The rules

Static rules are settled when `GraphQLService` builds the schema and hold for the life of the application.

| # | Rule | Encoded in |
|---|------|------------|
| S1 | A type is a domain type of a visible kind: persistent or synthetic, and not `@DenyIntrospection`. Unions are visible as property types but get no root field. | `GraphQLCommon.streamQueryableTypes` and `streamVisibleTypes` |
| S2 | A type is not excluded in its own right — abstract, no `@KeyType`, an enum, and the other generic exclusions. | `createGraphQLTypeFor`, through `isExcluded(type, "")` |
| S3 | The candidate properties of a type are its key members and properties, or its members if it is a union. | `GraphQLService.propertiesForGraphQlFields` |
| S4 | A property is not excluded — `@Invisible`, `@Ignore`, `key` without `@KeyTitle`, `desc` without `@DescTitle`, a property whose type is itself excluded, and the rest. | `createGraphQLTypeFor`, through `isExcluded(type, property)` |
| S5 | A property's type is one the Web API supports, which also fixes the field's GraphQL type and its arguments. | `FieldSchema.determineFieldType` |
| S6 | A type left with no field is not a type at all and is dropped. | `createGraphQLTypeFor` |
| S7 | A root field is named by uncapitalising the type's simple name. | `GraphQLCommon.rootFieldName` |

Dynamic rules are evaluated per request, because the answer depends on who is asking.

| # | Rule | Encoded in |
|---|------|------------|
| D1 | The current user may read a type's model, failing which only `id` remains of it. | `FieldVisibility.visibilityPredicate`, through `authoriseReading` under `READ_MODEL` |
| D2 | The current user may read a given property. | `FieldVisibility.visibilityPredicate`, through the property's `@Authorise` token |

Both reach the schema through `GraphqlFieldVisibility`, which graphql-java consults for the fields of a type.
It is not consulted for the fields of `Query`, so a root field survives even where its type has been reduced to `id`.


### Duplicate Fields in Sub-Selections

Aliases are meaningful on root fields, where each aliased root field is a query of its own, with its own `where`, ordering and pagination.
Below root fields, in both data and aggregation queries, a field may be selected at most once per selection set, with or without an alias.
Fields selected through fragments belong to the selection set that contains the fragment.
A duplicate field is rejected with an error for the root field that contains it, which fails that root field only.

In data queries, this rule prevents confusion.
A field for an entity property always returns the same value, and its only argument, `order`, does not affect that value but orders the result set.
A second selection of the same property can therefore differ from the first only in `order`, as in `{ workOrder { a: key(order: ASC_1) b: key(order: DESC_1) } }`.
The result set has a single order, so such a query cannot be honoured as written, and silently applying one of the orderings could return something other than what was asked for.
Rejecting the query makes the conflict explicit.

In aggregation queries, the rule is applied for consistency, so that one rule governs every sub-selection.
A duplicate there is equally without use: each property is grouped by or aggregated once, so `a: sum { intProp } b: sum { longProp }` is the same as `sum { intProp longProp }`.
