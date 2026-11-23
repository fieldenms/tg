package ua.com.fielden.platform.web_api;

import graphql.language.*;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.ImmutableListUtils;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.constructKeysAndProperties;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.utils.StreamUtils.typeFilter;

public class GraphQLCommon {

    private static final Logger LOGGER = LogManager.getLogger();

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

    public static Optional<Field> findField(final @Nullable SelectionSet selectionSet, final CharSequence name) {
        if (selectionSet == null) {
            return Optional.empty();
        }
        return selectionSet.getSelections().stream()
                .mapMulti(typeFilter(Field.class))
                .filter(f -> f.getName().contentEquals(name))
                .findFirst();
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

    /// Returns a stream of first-level fields for a selection set, replacing any fragments by their contents.
    ///
    public static Stream<Field> streamFields(final @Nullable SelectionSet selectionSet, final Map<String, FragmentDefinition> fragments) {
        if (selectionSet == null) {
            return Stream.of();
        }
        return selectionSet.getSelections()
                .stream()
                .flatMap(sel -> switch (sel) {
                    case Field field -> Stream.of(field);
                    case FragmentSpread frag -> {
                        final var def = fragments.get(frag.getName());
                        if (def == null) {
                            throw new WebApiException("Unknown fragment [%s].".formatted(frag.getName()));
                        }
                        yield streamFields(def.getSelectionSet(), fragments);
                    }
                    case InlineFragment frag -> streamFields(frag.getSelectionSet(), fragments);
                    default -> {
                        LOGGER.warn(() -> "Ignoring unknown selection [%s].".formatted(Objects.toString(sel)));
                        yield Stream.of();
                    }
                });
    }

}
