# TG MCP Server Architecture

## Purpose

The TG MCP server exposes a TG system to 3rd-party AI systems via the [Model Context Protocol](https://modelcontextprotocol.io/).
It enables a language model to query TG data by translating natural language requests into GraphQL queries.

The expected interaction flow:

1. User asks an LLM a question in natural language (e.g., "Show me all work orders in progress ordered by priority for cost centre A").
2. The LLM reads MCP resources to understand the domain model and query syntax.
3. The LLM may execute preliminary GraphQL queries to resolve reference values (e.g., mapping "in progress" to a status key).
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
| `name` | GraphQL field name used in queries (uncapitalised entity type name, e.g., `workOrder`) |
| `title` | Human-readable entity title (e.g., "Work Order") |
| `desc` | Entity description |

The catalogue enables a language model to identify which entity type corresponds to a user's natural language reference.
For example, "work orders" maps to the `workOrder` query field.

This is a dynamic resource.
Its content is generated at runtime from the set of domain entity types registered via `IApplicationDomainProvider`, excluding those annotated with `@DenyIntrospection`.
This is the same set of entity types used to build the GraphQL schema in `GraphQLService`.

### `tg://entities/{name}`

A detailed schema for a single entity type, identified by its GraphQL field name.
This is a resource template parameterised by entity name.

Each entity schema includes:

| Field | Description |
|-------|-------------|
| `name` | GraphQL field name |
| `title` | Human-readable entity title |
| `desc` | Entity description |
| `properties` | List of properties (see below) |

Each property includes:

| Field | Description |
|-------|-------------|
| `name` | Property name as used in GraphQL |
| `title` | Human-readable property title |
| `desc` | Property description |
| `type` | Property type: `String`, `Boolean`, `Integer`, `Long`, `BigDecimal`, `Money`, `Date`, `Hyperlink`, `Colour`, or an entity type name |
| `arguments` | List of available GraphQL arguments for this property (e.g., `eq`, `like`, `from`, `to`, `order`, `value`) |
| `annotations` | Applicable annotations: `Required`, `Readonly`, `Unique`, `Calculated`, `CompositeKeyMember(n)`, `UpperCase`, `DateOnly`, `TimeOnly` |

For entity-typed properties, `type` holds the referenced entity's GraphQL type name.
This tells the language model that filtering arguments for this property match against the referenced entity's key, and that the property can be expanded with sub-fields (`key`, `desc`, etc.).

The content is generated at runtime from entity metadata, mirroring the information used by `FieldSchema` when building GraphQL field definitions.

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

## Interaction Example

**User request:** "Show me all work orders in progress ordered by priority for cost centre A"

**Step 1 — Entity identification.**
The LLM reads `tg://entities` and identifies `workOrder` (title: "Work Order").

**Step 2 — Schema discovery.**
The LLM reads `tg://entities/workOrder` and learns about properties:
- `status` — type `WorkOrderStatus`, arguments: `eq`, `like`, `order`
- `priority` — type `Priority`, arguments: `eq`, `like`, `order`
- `costCentre` — type `CostCentre`, arguments: `eq`, `like`, `order`
- plus other properties such as `key`, `desc`, `createdDate`, etc.

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
    key
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

### Resource Generation

The entity catalogue and entity schema resources are generated from the same metadata that `GraphQLService` uses to build its schema.
The `FieldSchema` class contains the logic for determining property types and available arguments.
MCP resource generation should reuse this logic to ensure consistency between the GraphQL schema and the MCP resource descriptions.
