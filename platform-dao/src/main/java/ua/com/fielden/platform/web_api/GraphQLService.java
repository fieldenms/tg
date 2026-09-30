package ua.com.fielden.platform.web_api;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.schema.*;
import graphql.schema.GraphQLObjectType.Builder;
import graphql.validation.QueryComplexityLimits;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.utils.IDates;
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

/**
 * Represents GraphQL-based implementation of TG Web API using library <a href="https://github.com/graphql-java/graphql-java">graphql-java</a>.
 * <p>
 * At this stage only GraphQL {@code query} is supported.
 *
 * @author TG Team
 *
 */
@Singleton
public class GraphQLService implements IWebApi {
    private static final Logger LOGGER = getLogger(GraphQLService.class);
    private static final String ERR_EXECUTING_QUERY = "Query [%s] execution completed with errors [%s].";
    private static final String ERR_EXECUTING_QUERY_WITH_EX = "Query [%s] execution completed with exception.";
    public static final Integer DEFAULT_MAX_QUERY_DEPTH = 15; // this is the lowest value needed to load schema in GraphiQL editor (for version >= 3.2.3)
    public static final String WARN_INSUFFICIENT_MAX_QUERY_DEPTH = "Web API maximum query depth [%s] is insufficient for GraphiQL editor. Minimum value [" + DEFAULT_MAX_QUERY_DEPTH + "] was used.";

    private final GraphQLSchema schema;
    private final Integer maxQueryDepth;

    /**
     * Creates GraphQLService instance based on {@code applicationDomainProvider} which contains all entity types.
     * <p>
     * We start by building dictionary of all our custom GraphQL types from existing domain entity types.
     * Then we create GraphQL type for querying (aka GraphQL {@code query}) and assign it to the schema.
     *
     * @param maxQueryDepth -- the maximum depth of GraphQL query that are permitted to be executed.
     * @param applicationDomainProvider
     * @param coFinder
     * @param dates
     * @param authorisationModel -- Guice {@link Provider} for {@link IAuthorisationModel}; would create auth model to authorise running of Web API queries and their {@link FieldVisibility}
     * @param securityTokenProvider
     */
    @Inject
    public GraphQLService(
        final @Named("web.api.maxQueryDepth") Integer maxQueryDepth,
        final IApplicationDomainProvider applicationDomainProvider,
        final ICompanionObjectFinder coFinder,
        final IDates dates,
        final IAuthorisationModel authorisationModel,
        final ISecurityTokenProvider securityTokenProvider,
        final EntityTypeIntrospection entityTypeIntrospection,
        final EntityAggregation entityAggregation,
        final FluentConditions fluentConditions
    ) {
        try {
            LOGGER.info("GraphQL Web API...");
            if (maxQueryDepth == null || maxQueryDepth.compareTo(0) < 0) {
                throw new WebApiException("GraphQL max query depth must be specified and cannot be negative.");
            }
            this.maxQueryDepth = maxQueryDepth;

            LOGGER.info("\tmaxQueryDepth = {}", maxQueryDepth);
            final GraphQLCodeRegistry.Builder codeRegistryBuilder = newCodeRegistry();

            LOGGER.info("\tBuilding dictionary...");
            final var queryableTypes = streamQueryableTypes(applicationDomainProvider).collect(toCollection(LinkedHashSet::new));
            // The dictionary must list all referenced types.
            final var dictionary = createDictionary(streamVisibleTypes(applicationDomainProvider).collect(toCollection(LinkedHashSet::new)));

            LOGGER.info("\tBuilding query type...");
            final GraphQLObjectType queryType = createQueryType(queryableTypes, coFinder, dates, codeRegistryBuilder, authorisationModel, securityTokenProvider);

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

    /**
     * Executes Web API query by using internal {@link GraphQL} service with a predefined schema.
     * <p>
     * {@inheritDoc}
     */
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

    /**
     * Creates a GraphQL dictionary (aka GraphQL "additional types") for entity types.
     * <p>
     * The set of resultant types can be smaller than those derived upon. See {@link #createGraphQLTypeFor(Class)} for more details.
     * 
     * @param entityTypes
     * @return
     */
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
    /// @param entityTypes          entity types to register as root fields
    /// @param codeRegistryBuilder  a place to register root data fetchers
    ///
    private static GraphQLObjectType createQueryType(
            final Set<Class<? extends AbstractEntity<?>>> entityTypes,
            final ICompanionObjectFinder coFinder,
            final IDates dates,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider)
    {
        final Builder queryTypeBuilder = newObject().name(QUERY_TYPE_NAME).description("Query following **entities** represented as GraphQL root fields:");
        entityTypes.forEach(entityType -> {
            final String simpleTypeName = entityType.getSimpleName();
            final String fieldName = rootFieldName(entityType);
            queryTypeBuilder.field(newFieldDefinition()
                .name(fieldName)
                .description(format("Query %s.", bold(getEntityTitleAndDesc(entityType).getKey())))
                .argument(EQ_ARGUMENT)
                .argument(LIKE_ARGUMENT)
                .argument(ORDER_ARGUMENT)
                .argument(PAGE_NUMBER_ARGUMENT)
                .argument(PAGE_CAPACITY_ARGUMENT)
                .type(new GraphQLList(new GraphQLTypeReference(simpleTypeName)))
            );
            codeRegistryBuilder.dataFetcher(coordinates(QUERY_TYPE_NAME, fieldName), new RootEntityFetcher<>(entityType, coFinder, dates, authorisationModel, securityTokenProvider));
        });
        return queryTypeBuilder.build();
    }

    /**
     * Creates {@link Optional} GraphQL object type for querying data of type {@code entityType}.
     * <p>
     * {@code entityType} would not have a corresponding {@link GraphQLObjectType} only if there are no suitable fields for querying.
     * 
     * @param entityType
     * @return
     */
    private static Optional<Pair<Class<? extends AbstractEntity<?>>, GraphQLObjectType>> createGraphQLTypeFor(final Class<? extends AbstractEntity<?>> entityType) {
        if (isExcluded(entityType, "")) { // generic type exclusion logic for root types (exclude abstract entity types, exclude types without KeyType annotation etc. -- see AbstractDomainTreeRepresentation.isExcluded)
            return empty();
        }
        final var graphQLFieldDefinitions = propertiesForGraphQlFields(entityType).stream()
            .filter(field -> !isExcluded(entityType, reflectionProperty(field.getName())))
            .map(field -> createGraphQLFieldDefinition(entityType, field.getName()))
            .flatMap(optField -> optField.map(Stream::of).orElseGet(Stream::empty))
            .collect(toList());
        if (!graphQLFieldDefinitions.isEmpty()) { // ignore types that have no GraphQL field equivalents; we can not use such types for any purpose including querying
            // GraphQL type names are used to reference the types in other ones when the type is not created yet.
            // We argue that simple names of domain types are unique across application domain.
            // So these will be used for GraphQL type naming.
            return of(pair(entityType, newObject()
                .name(entityType.getSimpleName())
                .description(titleAndDescRepresentation(getEntityTitleAndDesc(entityType)))
                .fields(graphQLFieldDefinitions).build()
            ));
        }
        return empty();
    }

}
