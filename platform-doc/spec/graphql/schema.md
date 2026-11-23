# GraphQL Schema

A GraphQL schema in TG is created from a set of _Web API entity types_.

Entity type `E` is queryable if all of the following hold:
* `E` is persistent or synthetic.
* Introspection is allowed for `E`.

For each queryable entity type `E`, the schema contains:
* Root entity type `E` -- an object type that directly corresponds to the entity type.
  Its structure directly reflects that of the entity type.
* Aggregation entity type `E_Agg` -- an object type that enables aggregation over the entity type.
* Condition entity type `E_Cond` -- an input object type that enables fluent expression of conditions for the entity type.
* Root field whose type is `[E]`.
  * Argument `where : E_Cond`.
* Root field whose type is `[E_Agg]`.
  * Argument `where : E_Cond`.

## Entity Type Introspection

TG defines a GraphQL object type named `_EntityType` which represents the domain schema, enabling expressive introspection queries on entity types.

Introspection covers all entity types in the schema.
