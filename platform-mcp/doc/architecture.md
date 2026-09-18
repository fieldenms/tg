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
Covers query structure, filtering arguments, ordering, pagination, date handling, and efficiency tips.

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
`WorkOrder` has no `key` and must be selected through `number`; `PurchaseOrder` does have `key`; `Buyer` has neither, its key member being `person`, itself an entity reference.
Selecting `key` on a type that does not declare it is a validation error, and it is the most frequent one in practice.

`keySeparator` is required by the matching rules for entity-typed properties.
A condition on a property that references a composite-key type matches against its key members concatenated with that type's separator, so the separator must be known before such a condition can be constructed.

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
| `arguments` | GraphQL arguments accepted by this property, e.g., `["eq", "like", "order"]`, `["from", "to", "order"]` |
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

- Root field arguments (`eq`, `like`) for selecting and searching entity types.
  Until these exist, `_entityType` returns all types, and selecting `properties` fans out across the whole domain.
- Authorisation.
  `_entityType` must honour `Entity_CanReadModel_Token` at both type and property level, consistent with `FieldVisibility`.
  Both fetchers currently carry a `TODO`.
- Property filtering.
  `GraphQLService.propertiesForGraphQlFields` does not apply the exclusion rules that `createGraphQLTypeFor` applies.
  Properties of an unsupported type are already dropped, because a property cannot be described without the schema information that determines its type and arguments.
  Properties rejected by `isExcluded` are not, so `_Property` can still list a property that has no GraphQL field.
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
- `WorkOrder` has a composite key whose member is `number`, so `key` is not available on it.
- `status` is of type `WorkOrderStatus` and accepts `eq`, `like` and `order`; `priority` and `costCentre` are analogous.
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
  workOrder(pageCapacity: 50) {
    number
    desc
    status(eq: "IP") { key desc }
    costCentre(eq: "A") { key desc }
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
The meta-schema must do the same, at both type and property level, so that `_entityType` cannot enumerate a type whose model the current user is not authorised to read.
This is outstanding; see [Deferred](#deferred).

### Schema and Resource Generation

The GraphQL schema, the `_entityType` meta-schema and the `tg://entities` resource must all describe the same set of types.
They are separate code paths today and have already drifted, which is why the set of GraphQL-visible types is defined once, as a static method returning a stream of types, and used by every consumer:

- `GraphQLService.createQueryType`, when building the dictionary of root fields.
- `EntityTypeIntrospection.EntityTypeFetcher`, when populating `_entityType`.
- The `tg://entities` resource generator.
- `FieldVisibility`, when building its set of domain types.

This method belongs in `platform-pojo-bl`, not in `GraphQLService`.
`platform-mcp` depends on `platform-pojo-bl` only, so a method on `GraphQLService` in `platform-dao` is out of reach of the resource generator — which is how the drift arose.
The ingredients are already in `platform-pojo-bl`: `constructKeysAndProperties` and `isExcluded` on `AbstractDomainTreeRepresentation`, and `unionProperties` on `AbstractUnionEntity`.
`FieldSchema` is the natural home, as it already owns the logic that determines what becomes a GraphQL field.

`FieldSchema` likewise remains the single source for property types and available arguments, so that `_Property.type` and `_Property.arguments` cannot disagree with the field definitions actually present in the schema.
