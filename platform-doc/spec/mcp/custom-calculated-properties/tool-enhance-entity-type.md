# Tool `enhance_entity_type`

MCP clients add custom calculated properties by defining _enhanced entity types_: named entity types that extend domain entity types with calculated properties.

Tool `enhance_entity_type` -- creates a new enhanced entity type, or enhances an existing one.
If there is no enhanced type named `name`, the system creates a new entity type that extends `baseType` with the properties specified in `addProperties`.
Else, if it exists, the system removes from it the properties specified in `removeProperties`, and then adds to it the properties specified in `addProperties`.

The enhanced entity type is known by `name` in the Web API only.
Its Java type is a subclass of `baseType`, generated and named by the system, which keeps track of the Java type for each `name`.
However, if the enhanced entity type has no properties, `baseType` itself is used.
`baseType` itself never changes.

A call either takes full effect or none.
If a call fails, the Web API remains unchanged.

**Scope is per user**: The enhanced entity type exists only for the user who created it.
Therefore, two enhanced entity types, each created by a different user, may share the same `name`.

**Authorisation**: A user may query an enhanced entity type, and see it in the domain meta-schema, if and only if the user may do so for its base type.

The GraphQL schema is updated for the user: 
* If the enhanced entity type does not exist, an object type is created for it, with the name `name` and all fields of `baseType`.
  The same is done for the corresponding condition and aggregation types.
* The enhanced entity type, the condition and aggregation types are redefined by removing fields specified in `removeProperties` and adding fields specified in `addProperties`.
* Root fields are created for the enhanced entity type and the aggregation type, if they do not exist.
* The domain meta-schema (`_entityType`) describes the enhanced entity type as it describes a domain entity type, including the added properties.
  Field `_EntityType.baseType` is the name of the base type of an enhanced entity type, and `null` for a domain entity type.

Input schema:
* `name : String`, required -- name of the enhanced entity type.
  * It must be a [valid name](#valid-names).
  * When a new enhanced entity type is created, it is an error if the user's GraphQL schema has a type or a root field named after `name`.
* `baseType : String` -- name of the base type to extend.
  * Required if the enhanced entity type does not exist.
  * It is an error if this parameter is specified while the enhanced entity type exists.
  * It is an error if this is not the name of a queryable domain entity type whose model the user may read.
    E.g., an enhanced entity type is not a domain type, hence cannot be used as a base type.
* `addProperties : [Property]` -- list of properties to add to the enhanced type, empty if omitted.
  * It is an error if this list contains a property from `baseType` (matched by `name`).
  * It is an error if the enhanced entity type already exists and this list contains one of its properties (matched by `name`).
    In such a case, that property has to be removed before it can be added.
    Therefore, to change the expression of a property, the caller removes the property in one call and adds it in another.
  * It is an error if this list contains two properties with the same `name`.
* `removeProperties : [String]` -- list of properties to remove from the enhanced type, empty if omitted.
  * Properties of the base type cannot be removed.
    It is an error if this list contains a property from `baseType`.
  * It is an error if this list contains a property from `addProperties`.
  * It is an error if this list contains a property that remains referenced in expressions of other properties.
  * A property in this list that the enhanced entity type does not have is ignored, and the output includes a warning naming it.

`Property` schema:
* `name : String`, required -- a [valid name](#valid-names) for the property.
* `expression : String`, required -- an expression in TG's Expression Language.
  The user associated with the MCP client MUST have read access to all properties referenced by the expression.

## Valid names

A valid name, of an enhanced entity type or of a property, is both a Java identifier and a GraphQL name.
It consists of Latin letters, digits and underscores, does not start with a digit or with two underscores, and is neither a Java keyword nor one of `true`, `false` and `null`.

## Output

The tool's output has no structure, returning a text message on success.
The message names the enhanced entity type, its base type, its properties and its root fields, and is followed by a line for each warning.

```
Changed entity type [MyVehicle], which extends [Vehicle] with properties [myNumber, otherNumber]. Query it with root fields [myVehicle] and [myVehicle_agg].
Warning: Entity type [MyVehicle] has no properties [noSuchProp], so they were not removed.
```

A call that fails returns a tool error (`isError`) with a message that states the reason.

## Lifetime of enhanced entity types

Enhanced entity types last until the server restarts.

Options to consider later:
* Until the client removes it.
* After a certain amount of time.
* After a limit is hit.

## Limits

No limits are enforced.

Options to consider later:
* Max number of enhanced entity types per user.
* Max number of added properties per enhanced type.
* Max value of `types * properties`.
