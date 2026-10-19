package ua.com.fielden.platform.web_api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import graphql.ExecutionInput;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.schema.*;
import graphql.schema.GraphQLObjectType.Builder;
import graphql.validation.QueryComplexityLimits;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.util.*;
import java.util.stream.Stream;

import static graphql.GraphQL.newGraphQL;
import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLSchema.newSchema;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.reflectionProperty;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isExcluded;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.streaming.ValueCollectors.toLinkedHashMap;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.web_api.FieldSchema.*;
import static ua.com.fielden.platform.web_api.GraphQLCommon.*;
import static ua.com.fielden.platform.web_api.GraphQLPropertyDataFetcher.fetching;
import static ua.com.fielden.platform.web_api.RootEntityUtils.QUERY_TYPE_NAME;
import static ua.com.fielden.platform.web_api.WebApiUtils.*;

/// GraphQL-based implementation of the TG Web API using [graphql-java](https://github.com/graphql-java/graphql-java).
///
/// At this stage only the `query` operation type is supported.
///
/// ### Entity type enhancements
///
/// This implementation uses an [IDomainTreeEnhancer] singleton (bound to [IWebApi#WEB_API_DOMAIN_TREE_ENHANCER])
/// to access enhanced entity types.
///
/// Other parts of the system may use this [IDomainTreeEnhancer] instance to enhance entity types with extra properties,
/// which can then be queried through GraphQL.
///
/// The scope of such enhancements is limited to GraphQL (i.e., the enhanced entity types can be queried only via GraphQL).
///
/// TODO: Ability to access the extra properties in queries.
/// The GraphQL schema is built once, so any extra properties can only be accessed dynamically, in the manner of [AbstractEntity#get].
///
@Singleton
public class GraphQLService implements IWebApi {

    private static final Logger LOGGER = getLogger(GraphQLService.class);

    public static final int DEFAULT_MAX_QUERY_DEPTH = 15; // this is the lowest value needed to load schema in GraphiQL editor (for version >= 3.2.3)
    public static final int DEFAULT_MAX_PAGE_CAPACITY = 100;

    private static final String
            ERR_EXECUTING_QUERY = "Query [%s] execution completed with errors [%s].",
            ERR_EXECUTING_QUERY_WITH_EX = "Query [%s] execution completed with exception.";
    public static final String
            WARN_INSUFFICIENT_MAX_QUERY_DEPTH = "Web API maximum query depth [%s] is insufficient for GraphiQL editor. Minimum value [" + DEFAULT_MAX_QUERY_DEPTH + "] was used.";

    private final GraphQLSchema schema;
    private final Integer maxQueryDepth;

    /// Creates a GraphQLService instance based on `applicationDomainProvider` which contains all entity types.
    ///
    /// @param maxQueryDepth  the maximum depth of GraphQL query that are permitted to be executed.
    /// @param authorisationModel  an auth model to authorise running of Web API queries and their [FieldVisibility]
    ///
    @Inject
    public GraphQLService(
            final @Named("web.api.maxQueryDepth") Integer maxQueryDepth,
            final @Named("web.api.maxPageCapacity") Integer maxPageCapacity,
            final IApplicationDomainProvider applicationDomainProvider,
            final ICompanionObjectFinder coFinder,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider,
            final EntityTypeIntrospection entityTypeIntrospection,
            final EntityAggregation entityAggregation,
            final FluentConditions fluentConditions,
            final @Named(WEB_API_DOMAIN_TREE_ENHANCER) IDomainTreeEnhancer domainTreeEnhancer)
    {
        try {
            LOGGER.info("GraphQL Web API...");
            if (maxQueryDepth == null || maxQueryDepth.compareTo(0) < 0) {
                throw new WebApiException("GraphQL max query depth must be specified and cannot be negative.");
            }
            if (maxPageCapacity == null || maxPageCapacity.compareTo(0) < 0) {
                throw new WebApiException("GraphQL max page capacity must be specified and cannot be negative.");
            }
            this.maxQueryDepth = maxQueryDepth;

            LOGGER.info("\tmaxQueryDepth = {}", maxQueryDepth);
            final GraphQLCodeRegistry.Builder codeRegistryBuilder = newCodeRegistry();

            LOGGER.info("\tBuilding dictionary...");
            final var queryableTypes = streamQueryableTypes(applicationDomainProvider).collect(toCollection(LinkedHashSet::new));
            // The dictionary must list all referenced types.
            final var dictionary = createDictionary(streamVisibleTypes(applicationDomainProvider).collect(toCollection(LinkedHashSet::new)));

            LOGGER.info("\tBuilding query type...");
            final var queryType = createQueryType(queryableTypes,
                                                  coFinder,
                                                  codeRegistryBuilder,
                                                  authorisationModel,
                                                  securityTokenProvider,
                                                  maxPageCapacity,
                                                  domainTreeEnhancer);

            LOGGER.info("\tBuilding field visibility...");
            codeRegistryBuilder.fieldVisibility(new FieldVisibility(authorisationModel, queryableTypes, securityTokenProvider));

            LOGGER.info("\tBuilding default data fetcher...");
            codeRegistryBuilder.defaultDataFetcher(env -> fetching(env.getFieldDefinition().getName()));

            LOGGER.info("\tBuilding schema...");
            schema = fluentConditions.enhanceSchema(entityAggregation.enhanceSchema(entityTypeIntrospection.enhanceSchema(
                    newSchema()
                    .codeRegistry(codeRegistryBuilder.build())
                    .query(queryType)
                    .additionalTypes(new LinkedHashSet<>(dictionary.values()))
                    .build())));

            LOGGER.info("GraphQL Web API...done");
        } catch (final Throwable t) {
            LOGGER.error("GraphQL Web API error.", t);
            throw t;
        }
    }

    @Override
    public Map<String, Object> execute(final Map<String, Object> input) {
        try {
            final var graphQL = newGraphQL(schema)
                    .queryExecutionStrategy(new GraphQLAsyncExecutionStrategy(new GraphQLSimpleDataFetcherExceptionHandler()))
                    .instrumentation(new MaxQueryDepthInstrumentation(maxQueryDepth))
                    .build();
            // Some errors are thrown rather than captured in a result object, hence the try/catch.
            // E.g., OneOfTooManyKeysException.
            Map<String, Object> result;
            try {
                result = graphQL.execute(
                                ExecutionInput.newExecutionInput()
                                        .query(WebApiUtils.query(input))
                                        .operationName(WebApiUtils.operationName(input).orElse(null))
                                        .variables(WebApiUtils.variables(input))
                                        // Align graphql-java's default query-complexity limits (introduced in 26.0) with the configured maximum query depth,
                                        // so that raising `web.api.maxQueryDepth` above graphql-java's default of 100 is not silently capped at 100.
                                        // The field-count guard is retained at its default, as a safeguard against pathologically large queries.
                                        .graphQLContext(ctx -> ctx.put(
                                                QueryComplexityLimits.KEY,
                                                QueryComplexityLimits.newLimits()
                                                        .maxDepth(maxQueryDepth)
                                                        .maxFieldsCount(QueryComplexityLimits.DEFAULT_MAX_FIELDS_COUNT)
                                                        .build())))
                        .toSpecification();
            } catch (final Throwable e) {
                final var msg = e.getMessage() != null ? e.getMessage() : e.toString();
                result = mkResultWithErrors(List.of(mkError(msg)));
            }
            final var errors = errors(result);
            if (!errors.isEmpty()) {
                LOGGER.error(() -> ERR_EXECUTING_QUERY.formatted(input, errors));
            }
            return result;
        } catch (final Throwable throwable) {
            LOGGER.error(() -> ERR_EXECUTING_QUERY_WITH_EX.formatted(input), throwable);
            throw throwable;
        }
    }

    /// Creates a GraphQL dictionary (aka GraphQL "additional types") for the specified entity types.
    ///
    /// The set of resultant types can be smaller than the specified one.
    /// See [#createGraphQLTypeFor(Class)] for more details.
    ///
    private static Map<Class<? extends AbstractEntity<?>>, GraphQLNamedType> createDictionary(final Set<Class<? extends AbstractEntity<?>>> entityTypes) {
        return entityTypes.stream()
            .map(GraphQLService::createGraphQLTypeFor)
            .flatMap(optType -> optType.map(Stream::of).orElseGet(Stream::empty))
            .sorted((pair1, pair2) -> pair1.getKey().getSimpleName().compareTo(pair2.getKey().getSimpleName()))
            .collect(toLinkedHashMap(Pair::getKey, Pair::getValue));
    }

    /// Creates the "query" type that will contain root fields.
    ///
    /// Root fields are named using [GraphQLCommon#rootFieldName].
    ///
    /// @param entityTypes         entity types to register as root fields
    /// @param codeRegistryBuilder a place to register root data fetchers
    ///
    private static GraphQLObjectType createQueryType(
            final Set<Class<? extends AbstractEntity<?>>> entityTypes,
            final ICompanionObjectFinder coFinder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider,
            final int maxPageCapacity,
            final IDomainTreeEnhancer domainTreeEnhancer)
    {
        final Builder queryTypeBuilder = newObject().name(QUERY_TYPE_NAME).description("Query following **entities** represented as GraphQL root fields:");
        entityTypes.forEach(entityType -> {
            final String simpleTypeName = entityType.getSimpleName();
            final String fieldName = rootFieldName(entityType);
            queryTypeBuilder.field(newFieldDefinition()
                .name(fieldName)
                .description(format("Query %s.", bold(getEntityTitleAndDesc(entityType).getKey())))
                .argument(ORDER_ARGUMENT)
                .argument(PAGE_NUMBER_ARGUMENT)
                .argument(PAGE_CAPACITY_ARGUMENT)
                .type(new GraphQLList(new GraphQLTypeReference(simpleTypeName))));
            codeRegistryBuilder.dataFetcher(coordinates(QUERY_TYPE_NAME, fieldName),
                                            new RootEntityFetcher<>(entityType,
                                                                    coFinder,
                                                                    authorisationModel,
                                                                    securityTokenProvider,
                                                                    maxPageCapacity,
                                                                    domainTreeEnhancer));
        });
        return queryTypeBuilder.build();
    }

    /// Creates a GraphQL object type for querying `entityType`.
    /// The result is an empty optional if `entityType` has no suitable fields for querying.
    ///
    private static Optional<Pair<Class<? extends AbstractEntity<?>>, GraphQLObjectType>> createGraphQLTypeFor(final Class<? extends AbstractEntity<?>> entityType) {
         // Generic type exclusion logic for root types (exclude abstract entity types, types without KeyType annotation, etc.).
        if (isExcluded(entityType, "")) {
            return empty();
        }
        final var graphQLFieldDefinitions = propertiesForGraphQlFields(entityType).stream()
            .filter(field -> !isExcluded(entityType, reflectionProperty(field.getName())))
            .map(field -> createGraphQLFieldDefinition(entityType, field.getName()))
            .flatMap(optField -> optField.map(Stream::of).orElseGet(Stream::empty))
            .collect(toList());
        if (!graphQLFieldDefinitions.isEmpty()) {
            return of(pair(entityType, newObject()
                .name(entityType.getSimpleName())
                .description(titleAndDescRepresentation(getEntityTitleAndDesc(entityType)))
                .fields(graphQLFieldDefinitions).build()
            ));
        }
        return empty();
    }

}
