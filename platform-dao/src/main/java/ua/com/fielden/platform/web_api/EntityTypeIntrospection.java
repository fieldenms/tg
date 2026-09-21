package ua.com.fielden.platform.web_api;

import graphql.Scalars;
import graphql.schema.*;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLTypeUtil.simplePrint;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.REQUIRED;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determineClass;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.Reflector.getKeyMemberSeparator;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.web_api.FieldSchema.*;
import static ua.com.fielden.platform.web_api.GraphQLCommon.*;
import static ua.com.fielden.platform.web_api.RootEntityUtils.*;

/// GraphQL definitions that describe the domain model.
///
/// The domain is described through the schema itself rather than through standard GraphQL introspection, for two reasons.
///
/// A meta-schema is used in preference to standard GraphQL introspection for two reasons.
///
/// 1. Introspection cannot express what a model needs.
/// The introspection type system is fixed by the GraphQL specification: `__Field` carries only `name`, `description`, `args`, `type`, `isDeprecated` and `deprecationReason`.
/// It has nowhere to record structurally which properties are key members, which arguments a property accepts, whether a property is collectional, or what a union's members are.
///
/// 2. Introspection cannot be batched.
/// The `GoodFaithIntrospection` instrumentation in `graphql-java` rejects a query containing more than one `__type` field, so schema discovery costs one request per type.
/// An ordinary root field is not subject to that restriction.
///
/// The leading underscore in `_entityType`, `_EntityType` and `_Property` avoids collision with an application entity named `EntityType` or `Property`.
/// Only the double underscore prefix is reserved by the GraphQL specification.
///
/// ## Excluded metadata
///
/// Property-level annotations, such as [BeforeChange], are deliberately not exposed.
/// They describe integrity constraints and business rules, all of which are of no concern to this API.
/// They are also costly: on a type such as `WorkOrder` they account for more than half of all description bytes,
/// and they disclose internal class names.
///
@Singleton
public class EntityTypeIntrospection {

    public static final String
            ENTITY_TYPE_GRAPHQL_TYPE_NAME = "_EntityType",
            ENTITY_TYPE_ROOT_FIELD_NAME = "_entityType",
            PROPERTY_GRAPHQL_TYPE_NAME = "_Property",
            ENTITY_KIND_GRAPHQL_TYPE_NAME = "_EntityKind",
            KEY_TYPE_GRAPHQL_TYPE_NAME = "_KeyType",
            TYPE_KIND_GRAPHQL_TYPE_NAME = "_TypeKind";

    /// The only wildcard recognised by argument `like`, standing for any sequence of characters.
    ///
    private static final String WILDCARD = "*";

    /// Shape of an entity type's key.
    ///
    public enum KeyKind { SIMPLE, COMPOSITE, NO_KEY }

    /// Nature of an entity type, as described by the domain meta-schema.
    ///
    public enum EntityKind { PERSISTENT, SYNTHETIC, UNION }

    /// Kind of a property's type, as described by the domain meta-schema.
    ///
    /// The set of value types is closed and documented, so one can be recognised from its type name alone.
    /// An entity type cannot be distinguished from a union type that way, which is why this kind is recorded explicitly:
    /// a union exposes neither `key`, `desc` nor `id` and accepts no arguments, so it must be traversed through one of its members.
    ///
    public enum TypeKind { VALUE, ENTITY, UNION }

    /// Meta-information for an entity type.
    ///
    /// Properties are resolved independently by the [properties fetcher][#propertiesFetcher()].
    ///
    /// @param name          GraphQL type name.
    /// @param rootField     GraphQL root field name, or `null` if this type is not queryable, which is the case for a union.
    /// @param title         Human-readable entity title
    /// @param desc          Entity description
    /// @param keyType       Shape of the key, or `null` where not applicable.
    /// @param keyMembers    Names of the key members if the key is composite; `["key"]` if the key is simple; `null` otherwise.
    /// @param keySeparator  Separator used to concatenate composite key members, or `null` for other key shapes.
    /// @param hasDesc       Whether `desc` may be selected on this type.
    /// @param type          Invisible to GraphQL, exists to provide information to properties fetcher.
    ///
    public record EntityType (
            String name,
            @Nullable String rootField,
            String title,
            String desc,
            @Nullable EntityKind kind,
            KeyKind keyType,
            @Nullable List<String> keyMembers,
            @Nullable String keySeparator,
            boolean hasDesc,
            Class<? extends AbstractEntity<?>> type
    ) {}

    /// Meta-information for an entity property.
    ///
    /// @param type          Name of the property's type; for a collectional property, the type of its elements.
    /// @param collectional  Whether the property holds a collection of values.
    /// @param arguments     Names of the GraphQL arguments accepted by this property.
    /// @param required      Whether the property is always assigned.
    ///
    public record Property (
            String name,
            String title,
            String desc,
            String type,
            TypeKind typeKind,
            boolean collectional,
            List<String> arguments,
            boolean required
    ) {}

    private final IApplicationDomainProvider appDomainProvider;
    private final IDomainMetadata domainMetadata;

    @Inject
    protected EntityTypeIntrospection(
            final IApplicationDomainProvider appDomainProvider,
            final IDomainMetadata domainMetadata)
    {
        this.appDomainProvider = appDomainProvider;
        this.domainMetadata = domainMetadata;
    }

    public GraphQLSchema enhanceSchema(final GraphQLSchema schema) {
        final var queryType = schema.getQueryType();
        final var codeRegistry = schema.getCodeRegistry();
        return schema.transform(builder -> builder
                .additionalTypes(Set.of(graphQlTypeEntityType(),
                                        graphQlTypeProperty(),
                                        graphQlTypeEntityKindType(),
                                        graphQlTypeKeyType(),
                                        graphQlTypeTypeKind()))
                .codeRegistry(enhanceCodeRegistry(codeRegistry))
                .query(enhanceQueryType(queryType)));
    }

    private GraphQLCodeRegistry enhanceCodeRegistry(final GraphQLCodeRegistry codeReg) {
        return codeReg.transform(builder -> builder
                .dataFetcher(coordinates(QUERY_TYPE_NAME, ENTITY_TYPE_ROOT_FIELD_NAME), entityTypeFetcher())
                .dataFetcher(coordinates(ENTITY_TYPE_GRAPHQL_TYPE_NAME, "properties"), propertiesFetcher()));
    }

    private GraphQLObjectType enhanceQueryType(final GraphQLObjectType queryType) {
        return queryType.transform(builder -> builder
                .field(newFieldDefinition()
                               .name(ENTITY_TYPE_ROOT_FIELD_NAME)
                               .description("Query %s -- meta-information describing the domain model.".formatted(bold(ENTITY_TYPE_GRAPHQL_TYPE_NAME)))
                               .type(new GraphQLList(new GraphQLTypeReference(ENTITY_TYPE_GRAPHQL_TYPE_NAME)))
                               .argument(newArgument()
                                         .name(EQ)
                                         .description(
                                          """
                                          Include entity types whose simple name (`name`) is exactly equal to the specified value.
                                          Matching is case-sensitive.
                                          Does not support comma separated values.
                                          Does not permit wildcard `*`.
                                          Mutually exclusive with `like`.""")
                                         .type(Scalars.GraphQLString)
                                         .build())
                               .argument(newArgument()
                                         .name(LIKE)
                                         .description(
                                          """
                                          Include entity types whose simple name matches the specified value.
                                          Supports wildcard `*`, without which the match is exact.
                                          Supports multiple selection through comma-separated values.
                                          Matching is case-sensitive, and `*` is the only character with a special meaning.
                                          Mutually exclusive with `eq`.""")
                                         .type(Scalars.GraphQLString)
                                         .build())
                               .build()));
    }

    private GraphQLObjectType graphQlTypeEntityType() {
        final var fields = List.of(
                newFieldDefinition()
                        .name("name")
                        .description("""
                                     GraphQL type name, equal to the entity type's simple name.
                                     This is the name that a property's `type` refers to.""")
                        .type(Scalars.GraphQLString)
                        .build(),
                newFieldDefinition()
                        .name("rootField")
                        .description("""
                                     GraphQL root field name used to query this type, or `null` if it is not queryable.
                                     A type that is visible but has no root field, a union for instance, is reachable only as a property type.""")
                        .type(Scalars.GraphQLString)
                        .build(),
                newFieldDefinition()
                        .name("title")
                        .description("Human-readable title of this entity type.")
                        .type(Scalars.GraphQLString)
                        .build(),
                newFieldDefinition()
                        .name("desc")
                        .description("Description of this entity type.")
                        .type(Scalars.GraphQLString)
                        .build(),
                newFieldDefinition()
                        .name("kind")
                        .description("Nature of this entity type.")
                        .type(new GraphQLTypeReference(ENTITY_KIND_GRAPHQL_TYPE_NAME))
                        .build(),
                newFieldDefinition()
                        .name("keyType")
                        .description("Shape of this type's key. There is no rule to generalise from, so this must be established per type: selecting `key` on a type with a composite key is an error.")
                        .type(new GraphQLTypeReference(KEY_TYPE_GRAPHQL_TYPE_NAME))
                        .build(),
                newFieldDefinition()
                        .name("keyMembers")
                        .description("Names of the key members. Where the key is composite, these are selected in place of `key`.")
                        .type(new GraphQLList(Scalars.GraphQLString))
                        .build(),
                newFieldDefinition()
                        .name("keySeparator")
                        .description("Separator used to concatenate composite key members. A condition on a property that references this type matches against its key members concatenated with this separator.")
                        .type(Scalars.GraphQLString)
                        .build(),
                newFieldDefinition()
                        .name("hasDesc")
                        .description("Whether this type declares a description, and therefore whether `desc` may be selected on it.")
                        .type(Scalars.GraphQLBoolean)
                        .build(),
                newFieldDefinition()
                        .name("properties")
                        .description("Properties of this type. For a union, these are its members.")
                        .type(new GraphQLList(new GraphQLTypeReference(PROPERTY_GRAPHQL_TYPE_NAME)))
                        .build()
        );
        return newObject()
                .name(ENTITY_TYPE_GRAPHQL_TYPE_NAME)
                .description("Meta-information for an entity type: how to query it, the shape of its key, and its properties.")
                .fields(fields)
                .build();
    }

    private GraphQLObjectType graphQlTypeProperty() {
        final var fields = List.of(
                newFieldDefinition()
                        .name("name")
                        .description("The name of this property, as used in GraphQL.")
                        .type(Scalars.GraphQLString)
                        .build(),
                newFieldDefinition()
                        .name("title")
                        .description("Human-readable title of this property.")
                        .type(Scalars.GraphQLString)
                        .build(),
                newFieldDefinition()
                        .name("desc")
                        .description("Description of this property.")
                        .type(Scalars.GraphQLString)
                        .build(),
                newFieldDefinition()
                        .name("type")
                        .description("Name of this property's type: a scalar name, or an entity type name matching `%s.name`. For a collectional property, this is the type of its elements.".formatted(ENTITY_TYPE_GRAPHQL_TYPE_NAME))
                        .type(Scalars.GraphQLString)
                        .build(),
                newFieldDefinition()
                        .name("typeKind")
                        .description("Kind of this property's type. The set of scalars is closed, but an entity type cannot be distinguished from a union by name alone.")
                        .type(new GraphQLTypeReference(TYPE_KIND_GRAPHQL_TYPE_NAME))
                        .build(),
                newFieldDefinition()
                        .name("collectional")
                        .description("Whether this property holds a collection of values. A condition placed on a property inside a collectional property is silently discarded, so a collection can only be read. To filter by its contents, query the element type as a root field instead.")
                        .type(Scalars.GraphQLBoolean)
                        .build(),
                newFieldDefinition()
                        .name("arguments")
                        .description("Names of the GraphQL arguments accepted by this property. A collectional property accepts none.")
                        .type(new GraphQLList(Scalars.GraphQLString))
                        .build(),
                newFieldDefinition()
                        .name("required")
                        .description("Whether this property is always assigned. Every condition implicitly excludes entities where the property is unassigned, which cannot occur for a required property.")
                        .type(Scalars.GraphQLBoolean)
                        .build());
        return newObject()
                .name(PROPERTY_GRAPHQL_TYPE_NAME)
                .description("Meta-information for an entity property.")
                .fields(fields)
                .build();
    }

    private GraphQLEnumType graphQlTypeEntityKindType() {
        return newEnum()
                .name(ENTITY_KIND_GRAPHQL_TYPE_NAME)
                .description("Nature of an entity type.")
                .value("PERSISTENT", EntityKind.PERSISTENT, "Persistent entity type.")
                .value("SYNTHETIC",
                       EntityKind.SYNTHETIC,
                       "Synthetic entity type. A synthetic type based on a persistent one is reported as synthetic.")
                .value("UNION",
                       EntityKind.UNION,
                       "Union entity type. Reachable as a property type, but not queryable as a root field.")
                .build();
    }

    private GraphQLEnumType graphQlTypeKeyType() {
        return newEnum()
                .name(KEY_TYPE_GRAPHQL_TYPE_NAME)
                .description("Shape of an entity type's key.")
                .value("SIMPLE", KeyKind.SIMPLE, "A single `key` property.")
                .value("COMPOSITE",
                       KeyKind.COMPOSITE,
                       "Two or more key members. Property `key` is not available, and the members are selected instead.")
                .value("NO_KEY", KeyKind.NO_KEY, "The type declares no meaningful key.")
                .build();
    }

    private GraphQLEnumType graphQlTypeTypeKind() {
        return newEnum()
                .name(TYPE_KIND_GRAPHQL_TYPE_NAME)
                .description("Kind of a property's type.")
                .value("VALUE", TypeKind.VALUE, "A value, selected as a leaf.")
                .value("ENTITY",
                       TypeKind.ENTITY,
                       "An entity reference, expanded with sub-fields. Filtering matches against the referenced entity's key.")
                .value("UNION",
                       TypeKind.UNION,
                       "A union reference. It exposes neither `key`, `desc` nor `id`, accepts no arguments, and must be traversed through one of its members.")
                .build();
    }

    /// Describes `entityType` as it appears in the GraphQL schema.
    ///
    /// Key information is reported only for queryable types.
    /// A union is reached through its members and exposes neither `key` nor `desc`, so a key shape would be meaningless for it.
    ///
    @SuppressWarnings("unchecked")
    private EntityType mkEntityType(final Class<? extends AbstractEntity<?>> entityType) {
        final var titleAndDesc = getEntityTitleAndDesc(entityType);
        final var queryable = isQueryableType(entityType);
        final var keyKind = queryable ? keyKindOf(entityType) : null;
        return new EntityType(
                entityType.getSimpleName(),
                queryable ? rootFieldName(entityType) : null,
                titleAndDesc.getKey(),
                titleAndDesc.getValue(),
                entityKindOf(entityType),
                keyKind,
                keyKind == null ? null : Finder.getKeyMembers(entityType).stream().map(Field::getName).toList(),
                keyKind == KeyKind.COMPOSITE ? getKeyMemberSeparator((Class<? extends AbstractEntity<DynamicEntityKey>>) entityType) : null,
                queryable && hasDescProperty(entityType),
                entityType);
    }

    /// Whether the specified type is synthetic, persistent or a union.
    ///
    /// A synthetic type based on a persistent one is reported as synthetic, as that is the more specific classification.
    ///
    public EntityKind entityKindOf(final Class<? extends AbstractEntity<?>> entityType) {
        if (isUnionEntityType(entityType)) {
            return EntityKind.UNION;
        }
        else if (isSyntheticEntityType(entityType)) {
            return EntityKind.SYNTHETIC;
        }
        else {
            return EntityKind.PERSISTENT;
        }
    }

    private KeyKind keyKindOf(final Class<? extends AbstractEntity<?>> entityType) {
        if (isCompositeEntity(entityType)) {
            return KeyKind.COMPOSITE;
        }
        else if (NoKey.class.equals(getKeyType(entityType))) {
            return KeyKind.NO_KEY;
        }
        else {
            return KeyKind.SIMPLE;
        }
    }

    /// Describes `property` of `entityType` as it appears in the GraphQL schema.
    ///
    /// Returns an empty [Optional] if the property has no GraphQL field, so that the meta-schema cannot advertise a property that is not there.
    ///
    private Optional<Property> mkProperty(final Class<? extends AbstractEntity<?>> entityType, final CharSequence property) {
        return domainMetadata.forPropertyOpt(entityType, property)
                .flatMap(pm -> {
                    final var valueType = determinePropertyType(entityType, property);
                    final var collectional = isCollectional(determineClass(entityType, property.toString(), true, false));
                    return determineFieldTypeNonCollectional(valueType)
                            .map(typeAndArguments -> typeAndArguments.map((graphQLType, arguments) -> {
                                final var titleAndDesc = getTitleAndDesc(property, entityType);
                                return new Property(
                                        property.toString(),
                                        titleAndDesc.getKey(),
                                        titleAndDesc.getValue(),
                                        simplePrint(graphQLType),
                                        typeKindOf(valueType),
                                        collectional,
                                        // A collectional property accepts no arguments; see `determineFieldType`.
                                        collectional ? List.of() : arguments.stream().map(GraphQLArgument::getName).toList(),
                                        pm.is(REQUIRED));
                            }));
                });
    }

    private TypeKind typeKindOf(final Class<?> valueType) {
        if (isUnionEntityType(valueType)) {
            return TypeKind.UNION;
        }
        else if (isEntityType(valueType)) {
            return TypeKind.ENTITY;
        }
        else {
            return TypeKind.VALUE;
        }
    }

    private DataFetcher<List<EntityType>> entityTypeFetcher() {
        return new DataFetcher<>() {
            @Override
            public List<EntityType> get(final DataFetchingEnvironment environment) {
                // TODO Authorisation -- honour Entity_CanReadModel_Token, consistent with FieldVisibility.
                return streamVisibleTypes(appDomainProvider)
                        .filter(namePredicate(environment.getArgument(EQ), environment.getArgument(LIKE)))
                        .map(EntityTypeIntrospection.this::mkEntityType)
                        .toList();
            }
        };
    }

    /// A predicate on an entity type's simple name, as specified by arguments `eq` and `like` of [#ENTITY_TYPE_ROOT_FIELD_NAME].
    /// An argument that was not specified, or was specified as `null`, imposes no restriction.
    ///
    /// These arguments are the counterpart of the equally named arguments of an ordinary root field, which
    /// [RootEntityUtils] compiles into EQL conditions on an entity's key.
    /// The two do not share an implementation, as matching happens here against Java objects rather than in the database,
    /// and they deliberately differ in a few rules: matching here is case-sensitive, values are taken as given without
    /// trimming, and `*` is the only character with any special meaning.
    /// Both sets of rules, and why the difference is admissible, are recorded in the architecture document of `platform-mcp`.
    ///
    private static Predicate<Class<?>> namePredicate(final @Nullable String eq, final @Nullable String like) {
        if (eq != null && like != null) {
            throw new WebApiException(ERR_EQ_AND_LIKE_ARE_MUTUALLY_EXCLUSIVE);
        }
        else if (eq != null) {
            if (eq.contains(WILDCARD)) {
                throw new WebApiException(ERR_EQ_DOES_NOT_PERMIT_WILDCARDS);
            }
            return type -> eq.equals(type.getSimpleName());
        }
        else if (like != null) {
            final var patterns = Arrays.stream(StringUtils.split(like, ','))
                    .map(EntityTypeIntrospection::likePattern)
                    .toList();
            return type -> patterns.stream().anyMatch(pattern -> pattern.matcher(type.getSimpleName()).matches());
        }
        else {
            return _ -> true;
        }
    }

    /// Compiles a single `like` value into a pattern where [#WILDCARD] stands for any sequence of characters and everything else is matched literally.
    /// Quoting is essential: without it, a value containing a regular expression metacharacter would either match too much or fail to compile.
    ///
    private static Pattern likePattern(final String value) {
        return Pattern.compile(Arrays.stream(value.split(quote(WILDCARD), -1)).map(Pattern::quote).collect(joining(".*")));
    }

    private DataFetcher<List<Property>> propertiesFetcher() {
        return new DataFetcher<>() {
            @Override
            public List<Property> get(final DataFetchingEnvironment environment) {
                if (environment.getSource() instanceof EntityType entityType) {
                    // TODO Reuse rules from FieldVisibility to filter properties.
                    // TODO Apply the exclusion rules used by GraphQLService.createGraphQLTypeFor, which also drops excluded properties.
                    final var type = entityType.type();
                    return GraphQLService.propertiesForGraphQlFields(type)
                            .stream()
                            .map(field -> mkProperty(type, field.getName()))
                            .flatMap(Optional::stream)
                            .toList();
                }
                else {
                    return List.of();
                }
            }
        };
    }

}
