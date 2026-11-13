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

How the system stores and manages the added properties is described in [Model of user-defined calculated properties](#model-of-user-defined-calculated-properties).
The Web API, in the form of GraphQL, is changed as described in [GraphQL](#graphql).

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
The exception is a server-side failure, such as a database error: its message names the entity type and the properties of the call, but not the reason, which may reveal server-side information.

## GraphQL

The GraphQL schema is updated for the user:
* If the enhanced entity type does not exist, an object type is created for it, with the name `name` and all fields of `baseType`.
  The same is done for the corresponding condition and aggregation types.
* The enhanced entity type, the condition and aggregation types are redefined by removing fields specified in `removeProperties` and adding fields specified in `addProperties`.
* Root fields are created for the enhanced entity type and the aggregation type, if they do not exist.
* The domain meta-schema (`_entityType`) describes the enhanced entity type as it describes a domain entity type, including the added properties.
  Field `_EntityType.baseType` is the name of the base type of an enhanced entity type, and `null` for a domain entity type.

The schema is rebuilt in the same atomic operation as the persistence of records.

TODO: How does the schema stay in sync when another node adds new properties?

### Recovery from failures to register enhanced entity types in the schema

If registering an enhanced entity type in the schema causes an error, that type is excluded from the schema.
If the schema building process cannot determine which specific entity type caused the error, all enhanced entity types are excluded.

## Model of user-defined calculated properties

Added calculated properties and enhanced entity types that contain them are modelled by entities `UserDefinedCalculatedProperty` and `UserDefinedEnhancedEntityType`.
Both entity types are persistent, which enables user-defined properties to survive server restarts and to be used across different nodes.

`UserDefinedEnhancedEntityType` has the following properties:
* `name : String @MapTo @CompositeKeyMember(1)` -- name of this entity type.
  Note that this name is not related to the system name (the name of the Java class), but is rather an identifier for MCP clients to use.
* `user : User @MapTo @CompositeKeyMember(2)` -- the user that defined this entity type.
* `baseType : String @MapTo @Required` -- fully-qualified name of the domain entity type that this entity type is based on.

Validation rules for `UserDefinedEnhancedEntityType`:
1. `name` must not conflict with existing domain types (matched by simple names).
   This should be checked both when persisting and retrieving.
2. `baseType` must exist.
   This should be checked both when persisting and retrieving.
   A retrieved instance may be invalid if the entity type it was originally created for is renamed or removed.

`UserDefinedCalculatedProperty` has the following properties:
* `entityType : UserDefinedEnhancedEntityType @MapTo @CompositeKeyMember(1)` -- the enhanced entity type that contains this property.
* `name : String @MapTo @CompositeKeyMember(2)` -- the name of this property.
* `expression : String @MapTo @Required` -- the expression associated with this property.

Validation rules for `UserDefinedCalculatedProperty`:
1. `name` must not conflict with an existing property in `entityType.baseType`.
   This should be checked both when persisting and retrieving.
   A retrieved instance may be invalid if the entity type it was originally created for attains a property with the same name.
2. `expression` must be valid.
   This should be checked both when persisting and retrieving.
   The original expression may become invalid for several reasons:
   * A property referenced in the expression no longer exists, or its type has changed in such a way that invalidates its use in the expression (e.g., its new type is not supported by the operator used).
   * The Expression Language underwent changes that break the expression.

Separately, the user that defined the property may lose read access, and this should be checked.

### Ideas

Validation rules whose outcome cannot change while the application runs can be checked once, at launch.
E.g., the set of registered domain types is immutable, hence it is sufficient to perform validation of `UserDefinedEnhancedEntityType.name` once.

### Storage

When `enhance_entity_type` is called, and all validation checks pass:
* A `UserDefinedEnhancedEntityType` record is persisted if it does not exist yet.
  `UserDefinedEnhancedEntityType.baseType` is assigned from the `baseType` parameter.
* For each property in `addProperties`, a corresponding `UserDefinedCalculatedProperty` record is persisted.
* For each property in `removeProperties`, a corresponding `UserDefinedCalculatedProperty` record is deleted.

This operation is atomic.

### Instantiation as enhanced entity types

`UserDefinedEnhancedEntityType` records can be instantiated as Java classes.
That is, there is an operation that maps an instance of `UserDefinedEnhancedEntityType` to a Java class that represents an enhanced entity type with the associated properties.

Interface `IEntityTypeEnhancement` provides the following operation:

```
EnhancedEntityType javaTypeFor(UserDefinedEnhancedEntityType instance)
```

All associated properties are validated first, and if any one is invalid, the whole operation fails.

## Lifetime of user-defined calculated properties

Since user-defined calculated properties are persisted, there are 2 kinds of lifetime:
* Persistence level: Stored in the database as long as necessary.
* Application level: Instantiated as Java classes that reside in the server's memory until evicted.

TODO: Define lifetime policies. For now, all records are indefinitely stored at both levels.

## Limits

TODO: Define limit policies. For now, any number of user-defined calculated properties may be created.

Options to consider later:
* Max number of enhanced entity types per user (persistence level).
* Max number of properties per user (persistence level).
* Max number of properties per enhanced entity type (persistence level).
