package ua.com.fielden.platform.web_api;

import graphql.schema.*;
import org.apache.commons.lang3.Strings;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.ImmutableListUtils;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.constructKeysAndProperties;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.utils.EntityUtils.*;

public class GraphQLCommon {

    /// Name of the built-in data introspection field present on every object type in GraphQL.
    public static final String __TYPENAME = "__typename";
    /// Name of the query type in every GraphQL schema in TG.
    public static final String QUERY_TYPE_NAME = "Query";

    /// Name of the GraphQL root field for the specified entity type.
    ///
    public static String rootFieldName(final Class<?> entityType) {
        return uncapitalize(entityType.getSimpleName());
    }

    /// Name of the GraphQL root field for an aggregation entity type.
    ///
    public static String rootFieldNameForAggEntity(final Class<? extends AbstractEntity<?>> entityType) {
        return "%s_agg".formatted(rootFieldName(entityType));
    }

    /// Name of the GraphQL type for the specified entity type.
    ///
    public static String graphQlTypeNameForEntity(final Class<? extends AbstractEntity<?>> entityType) {
        return entityType.getSimpleName();
    }

    /// Name of the GraphQL type for an aggregation entity type.
    ///
    public static String graphQlTypeNameForAggEntity(final Class<? extends AbstractEntity<?>> entityType) {
        return "%s_Agg".formatted(graphQlTypeNameForEntity(entityType));
    }

    /// Name of the GraphQL type for a condition entity type.
    ///
    public static String graphQlTypeNameForCondEntity(final Class<? extends AbstractEntity<?>> entityType) {
        return "%s_Cond".formatted(graphQlTypeNameForEntity(entityType));
    }

    /// True if the specified GraphQL type name represents the type of a condition entity type.
    ///
    public static boolean isCondEntityType(final CharSequence graphQlTypeName) {
        return Strings.CS.endsWith(graphQlTypeName, "_Cond");
    }

    /// Infers the simple name of an entity type associated with the specified GraphQL type name representing a condition entity type.
    ///
    public static Optional<String> entityTypeNameFromGraphQlCondEntityType(final CharSequence graphQlTypeName) {
        final var i = Strings.CS.lastIndexOf(graphQlTypeName, "_Cond");
        return i == -1 ? Optional.empty() : Optional.of(graphQlTypeName.subSequence(0, i).toString());
    }

    /// Name of the GraphQL type for a condition entity type's `cond` field.
    ///
    public static String graphQlTypeNameForCondEntityCond(final Class<? extends AbstractEntity<?>> entityType) {
        return "%s_Cond_cond".formatted(graphQlTypeNameForEntity(entityType));
    }

    /// True if the specified GraphQL type name represents the type of a condition entity type's `cond` field.
    ///
    public static boolean isCondEntityCondType(final CharSequence graphQlTypeName) {
        return Strings.CS.endsWith(graphQlTypeName, "_Cond_cond");
    }

    /// True if the specified GraphQL type represents the type of a condition entity type's `cond` field.
    ///
    public static boolean isCondEntityCondType(final GraphQLType graphQlType) {
        return graphQlType instanceof GraphQLNamedType it && isCondEntityCondType(it.getName());
    }

    /// Infers the simple name of an entity type associated with the specified GraphQL type name representing the type
    /// of a condition entity type's `cond` field.
    ///
    public static Optional<String> entityTypeNameFromGraphQlCondEntityCondType(final CharSequence graphQlTypeName) {
        final var i = Strings.CS.lastIndexOf(graphQlTypeName, "_Cond_cond");
        return i == -1 ? Optional.empty() : Optional.of(graphQlTypeName.subSequence(0, i).toString());
    }

    /// Domain types exposed as GraphQL root fields.
    ///
    /// These are synthetic and persistent types without [ua.com.fielden.platform.entity.annotation.DenyIntrospection].
    /// Union, functional and other entity types are not included.
    ///
    public static Stream<Class<? extends AbstractEntity<?>>> streamQueryableTypes(final IApplicationDomainProvider appDomainProvider) {
        return streamQueryableTypes_(appDomainProvider, EntityUtils::isIntrospectionAllowed);
    }

    /// All domain types visible in the GraphQL schema.
    ///
    /// This is [#streamQueryableTypes] together with union types, which are reachable as property types but are not root fields.
    ///
    public static Stream<Class<? extends AbstractEntity<?>>> streamVisibleTypes(final IApplicationDomainProvider appDomainProvider) {
        return concat(streamQueryableTypes(appDomainProvider),
                      streamQueryableTypes_(appDomainProvider, EntityUtils::isUnionEntityType));
    }

    /// Whether the specified type is exposed as a GraphQL root field.
    /// Types that are visible but not queryable, such as unions, can only be reached as property types.
    ///
    public static boolean isQueryableType(final Class<? extends AbstractEntity<?>> entityType) {
        return isIntrospectionAllowed(entityType);
    }

    private static Stream<Class<? extends AbstractEntity<?>>> streamQueryableTypes_(
            final IApplicationDomainProvider applicationDomainProvider,
            final Predicate<Class<? extends AbstractEntity<?>>> pred)
    {
        return applicationDomainProvider.entityTypes()
                .stream()
                .filter(type -> !isIntrospectionDenied(type) && pred.test(type))
                .sorted(comparing(Class::getSimpleName));
    }

    public static boolean containsRootFieldForType(final GraphQLSchema schema, final Class<?> type) {
        return schema.getQueryType().getFieldDefinition(rootFieldName(type)) != null;
    }

    /// Returns the fields of `entityType` that are candidates for becoming GraphQL fields.
    ///
    /// [AbstractDomainTreeRepresentation#constructKeysAndProperties] yields `key` itself for a simple key, but key members for a composite one.
    /// A composite key is selectable as a single `String`-typed field, so `key` is added for such types only.
    /// It goes first to preserve the ordering that [AbstractDomainTreeRepresentation#constructKeysAndProperties] establishes, where a key precedes everything else.
    ///
    @SuppressWarnings("unchecked")
    public static List<java.lang.reflect.Field> propertiesForGraphQlFields(final Class<? extends AbstractEntity<?>> entityType) {
        if (isUnionEntityType(entityType)) {
            return unionProperties((Class<? extends AbstractUnionEntity>) entityType);
        }
        else {
            final var keysAndProperties = constructKeysAndProperties(entityType, true);
            return isCompositeEntity(entityType)
                    ? ImmutableListUtils.prepend(findFieldByName(entityType, KEY), keysAndProperties)
                    : keysAndProperties;
        }
    }

    /// Returns all selected fields for the root field in `env`, resolving any fragments that may have been present in the query.
    ///
    /// ```
    /// {
    ///   workOrder {
    ///     key
    ///     status { key }
    ///     d1: repairStart
    ///     d2: repairStart
    ///   }
    /// }
    /// =>
    /// [key, status, status.key, repairStart(alias: "d1"), repairStart(alias: "d2")]
    /// ```
    ///
    /// ### Duplicate fields
    ///
    /// Duplicate fields are processed by the GraphQL SDK according to the GraphQL specification.
    /// Therefore, after [DataFetchingEnvironment] is constructed, the SDK must have already validated the query.
    /// For posterity, the rule is documented below.
    ///
    /// Two fields with the same response key (the alias, or the field name if there is no alias) must have the same
    /// name and the same arguments.
    /// Otherwise there would be one key in the response with two possible values, which the spec forbids.
    /// For example, `key(order: DESC_2)` and `key(order: DESC_3)` will be rejected.
    ///
    public static Collection<SelectedField> allSelectedFields(final DataFetchingEnvironment env) {
        // NOTE This simple implementation suffices as long as interface and union types are not used.
        return env.getSelectionSet().getFields();
    }

    public static String toPropertyPath(final SelectedField field) {
        return propertyPathFromSelectedFieldName(field.getQualifiedName());
    }

    public static String propertyPathFromSelectedFieldName(final CharSequence name) {
        return name.toString().replace('/', '.');
    }

    /// Returns all _terminal_ fields from a selection set.
    /// A field is terminal if none of its sub-fields were selected.
    /// Each returned field's qualified name is fully qualified with respect to `selectionSet`.
    ///
    /// For example, assuming this method is called for the selection set of `workOrder`:
    ///
    /// ```
    /// {
    ///   workOrder {
    ///     key
    ///     status { key }
    ///   }
    /// }
    /// ==>
    /// ["key", "status/key"]
    /// ```
    ///
    public static List<SelectedField> terminalFields(final DataFetchingFieldSelectionSet selectionSet) {
        return selectionSet.getFields().stream().filter(fld -> fld.getSelectionSet().getImmediateFields().isEmpty()).toList();
    }

    /// Returns an object type whose name is `typeName`.
    /// An alternative that does not throw is [#findGraphQlEntityType].
    ///
    /// If `typeName` names an enhanced entity type that does not exist in the schema (and enhanced types never do),
    /// the original entity type is returned instead.
    ///
    /// This method should always be used instead of [GraphQLSchema#getObjectType].
    ///
    /// @throws WebApiException  if there is no such object type
    ///
    public static GraphQLObjectType getGraphQlEntityType(final GraphQLSchema schema, final String typeName) {
        return findGraphQlEntityType(schema, typeName).orElseThrow(() -> new WebApiException("No such GraphQL type: %s".formatted(typeName)));
    }

    /// Returns an object type whose name is `typeName`.
    /// An alternative that throws is [#getGraphQlEntityType].
    ///
    /// If `typeName` names an enhanced entity type that does not exist in the schema (and enhanced types never do),
    /// the original entity type is returned instead.
    ///
    public static Optional<GraphQLObjectType> findGraphQlEntityType(final GraphQLSchema schema, final String typeName) {
        final var type = schema.getObjectType(typeName);
        if (type != null) {
            return Optional.of(type);
        }
        else if (typeName.contains(DynamicTypeNamingService.APPENDIX)) {
            return Optional.ofNullable(schema.getObjectType(DynamicTypeNamingService.decodeOriginalTypeFrom(typeName)));
        }
        else {
            return Optional.empty();
        }
    }

    /// Retrieves from the schema a GraphQL type whose name is `typeName` and that is an instance of `expectedType`.
    /// An alternative that does not throw is [#findCoercedType].
    ///
    /// @throws WebApiException  if the requested type does not exist or is not an instance of `expectedType`
    ///
    @SuppressWarnings("unchecked")
    public static <T extends GraphQLType> T getCoercedType(
            final GraphQLSchema schema,
            final String typeName,
            final Class<T> expectedType)
    {
        final var type = schema.getType(typeName);
        if (type == null) {
            throw new WebApiException("No such GraphQL type: %s".formatted(typeName));
        }
        else if (expectedType.isInstance(type)) {
            return (T) type;
        }
        else {
            throw new WebApiException(format("GraphQL type [%s] is [%s] while [%s] is expected.",
                                             typeName, type.getClass().getTypeName(), expectedType.getTypeName()));
        }
    }

    /// Retrieves from the schema a GraphQL type whose name is `typeName` and that is an instance of `expectedType`.
    /// An empty optional is returned if the requested type does not exist or is not an instance of `expectedType`.
    /// An alternative that throws is [#getCoercedType].
    ///
    @SuppressWarnings("unchecked")
    public static <T extends GraphQLType> Optional<T> findCoercedType(
            final GraphQLSchema schema,
            final String typeName,
            final Class<T> expectedType)
    {
        final var type = schema.getType(typeName);
        if (expectedType.isInstance(type)) {
            return Optional.of((T) type);
        }
        else {
            return Optional.empty();
        }
    }

}
