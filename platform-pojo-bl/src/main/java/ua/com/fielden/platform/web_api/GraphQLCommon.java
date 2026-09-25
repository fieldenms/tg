package ua.com.fielden.platform.web_api;

import graphql.language.Field;
import graphql.language.SelectionSet;
import graphql.schema.GraphQLSchema;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static ua.com.fielden.platform.utils.EntityUtils.isIntrospectionAllowed;
import static ua.com.fielden.platform.utils.EntityUtils.isIntrospectionDenied;
import static ua.com.fielden.platform.utils.StreamUtils.typeFilter;

public class GraphQLCommon {

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

}
