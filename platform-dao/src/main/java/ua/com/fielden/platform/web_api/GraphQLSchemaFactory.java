package ua.com.fielden.platform.web_api;

import graphql.schema.*;
import graphql.schema.GraphQLObjectType.Builder;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLSchema.newSchema;
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.reflectionProperty;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isExcluded;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;
import static ua.com.fielden.platform.web_api.FieldSchema.*;
import static ua.com.fielden.platform.web_api.GraphQLCommon.QUERY_TYPE_NAME;
import static ua.com.fielden.platform.web_api.GraphQLPropertyDataFetcher.fetching;

/// Builds GraphQL schemas of the Web API.
///
public class GraphQLSchemaFactory {

    private static final Logger LOGGER = getLogger(GraphQLSchemaFactory.class);

    private final ICompanionObjectFinder coFinder;
    private final IAuthorisationModel authorisationModel;
    private final ISecurityTokenProvider securityTokenProvider;
    private final EntityTypeIntrospection entityTypeIntrospection;
    private final EntityAggregation entityAggregation;
    private final FluentConditions fluentConditions;
    private final GraphQLCommon common;

    @Inject
    protected GraphQLSchemaFactory(
            final ICompanionObjectFinder coFinder,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider,
            final EntityTypeIntrospection entityTypeIntrospection,
            final EntityAggregation entityAggregation,
            final FluentConditions fluentConditions,
            final GraphQLCommon common)
    {
        this.coFinder = coFinder;
        this.authorisationModel = authorisationModel;
        this.securityTokenProvider = securityTokenProvider;
        this.entityTypeIntrospection = entityTypeIntrospection;
        this.entityAggregation = entityAggregation;
        this.fluentConditions = fluentConditions;
        this.common = common;
    }

    /// Builds a schema that includes `entityTypes`.
    ///
    /// Each entity type gets an object type, and those that are not unions also get root fields.
    /// Entity types without properties suitable for querying are omitted entirely.
    ///
    public GraphQLSchema create(final List<WebApiEntityType> entityTypes) {
        final GraphQLCodeRegistry.Builder codeRegistryBuilder = newCodeRegistry();

        final var queryableTypes = entityTypes.stream().filter(ty -> !isUnionEntityType(ty.type())).toList();

        LOGGER.debug("\tBuilding dictionary...");
        // The dictionary must list all referenced types.
        final var dictionary = createDictionary(entityTypes);

        LOGGER.debug("\tBuilding query type...");
        final var queryType = createQueryType(queryableTypes, codeRegistryBuilder);

        LOGGER.debug("\tBuilding field visibility...");
        codeRegistryBuilder.fieldVisibility(new FieldVisibility(authorisationModel, queryableTypes, securityTokenProvider));

        LOGGER.debug("\tBuilding default data fetcher...");
        codeRegistryBuilder.defaultDataFetcher(env -> fetching(env.getFieldDefinition().getName()));

        LOGGER.debug("\tBuilding schema...");
        final var schema = newSchema()
                .codeRegistry(codeRegistryBuilder.build())
                .query(queryType)
                .additionalTypes(new LinkedHashSet<>(dictionary))
                .build();
        return fluentConditions.enhanceSchema(
                entityAggregation.enhanceSchema(
                        entityTypeIntrospection.enhanceSchema(schema, entityTypes),
                        entityTypes),
                entityTypes);
    }

    /// Creates a GraphQL dictionary (aka GraphQL "additional types") for the specified entity types.
    ///
    /// The set of resultant types can be smaller than the specified one.
    /// See [#createGraphQLTypeFor(WebApiEntityType)] for more details.
    ///
    private List<GraphQLObjectType> createDictionary(final List<WebApiEntityType> entityTypes) {
        return entityTypes.stream()
            .map(this::createGraphQLTypeFor)
            .flatMap(Optional::stream)
            .sorted(comparing(GraphQLObjectType::getName))
            .toList();
    }

    /// Creates the "query" type that will contain root fields.
    ///
    /// Root fields are named using [GraphQLCommon#rootFieldName].
    ///
    /// @param entityTypes         entity types to register as root fields
    /// @param codeRegistryBuilder a place to register root data fetchers
    ///
    private GraphQLObjectType createQueryType(
            final List<WebApiEntityType> entityTypes,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder)
    {
        final Builder queryTypeBuilder = newObject().name(QUERY_TYPE_NAME).description("Query following **entities** represented as GraphQL root fields:");
        entityTypes.forEach(entityType -> {
            final String fieldName = common.rootFieldName(entityType);
            queryTypeBuilder.field(newFieldDefinition()
                .name(fieldName)
                .description("Query %s.".formatted(bold(getEntityTitleAndDesc(entityType.type()).getKey())))
                .argument(ORDER_ARGUMENT)
                .argument(PAGE_NUMBER_ARGUMENT)
                .argument(PAGE_CAPACITY_ARGUMENT)
                .type(new GraphQLList(new GraphQLTypeReference(entityType.name()))));
            codeRegistryBuilder.dataFetcher(coordinates(QUERY_TYPE_NAME, fieldName),
                                            new RootEntityFetcher<>(entityType.type(),
                                                                    coFinder,
                                                                    authorisationModel,
                                                                    securityTokenProvider,
                                                                    common));
        });
        return queryTypeBuilder.build();
    }

    /// Creates a GraphQL object type for querying `entityType`.
    /// The result is an empty optional if `entityType` has no suitable fields for querying.
    ///
    private Optional<GraphQLObjectType> createGraphQLTypeFor(final WebApiEntityType entityType) {
        final var type = entityType.type();
         // Generic type exclusion logic for root types (exclude abstract entity types, types without KeyType annotation, etc.).
        if (isExcluded(type, "")) {
            return empty();
        }
        final var graphQLFieldDefinitions = common.propertiesForGraphQlFields(type).stream()
            .filter(field -> !isExcluded(type, reflectionProperty(field.getName())))
            .map(field -> createGraphQLFieldDefinition(type, field.getName()))
            .flatMap(Optional::stream)
            .toList();
        if (!graphQLFieldDefinitions.isEmpty()) {
            return of(newObject()
                .name(entityType.name())
                .description(titleAndDescRepresentation(getEntityTitleAndDesc(type)))
                .fields(graphQLFieldDefinitions).build());
        }
        return empty();
    }

}
