package ua.com.fielden.platform.web_api;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import graphql.GraphQL;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.schema.*;
import graphql.schema.GraphQLObjectType.Builder;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static graphql.ExecutionInput.newExecutionInput;
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
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.reflectionProperty;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.constructKeysAndProperties;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isExcluded;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.streaming.ValueCollectors.toLinkedHashMap;
import static ua.com.fielden.platform.utils.EntityUtils.isIntrospectionDenied;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.web_api.FieldSchema.*;
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
        final ISecurityTokenProvider securityTokenProvider
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
            final Set<Class<? extends AbstractEntity<?>>> domainTypes = domainTypesOf(applicationDomainProvider, EntityUtils::isIntrospectionAllowed).stream() // synthetic / persistent without @DenyIntrospection; this includes persistent with activatable nature, synthetic based on persistent; this does not include union, functional and any other entities
                .sorted((type1, type2) -> type1.getSimpleName().compareTo(type2.getSimpleName()))
                .collect(toCollection(LinkedHashSet::new));
            final Set<Class<? extends AbstractEntity<?>>> allTypes = new LinkedHashSet<>(domainTypes);
            allTypes.addAll(domainTypesOf(applicationDomainProvider, EntityUtils::isUnionEntityType));
            // dictionary must have all the types that are referenced by all types that should support querying
            final Map<Class<? extends AbstractEntity<?>>, GraphQLType> dictionary = createDictionary(allTypes);

            LOGGER.info("\tBuilding query type...");
            final GraphQLObjectType queryType = createQueryType(domainTypes, coFinder, dates, codeRegistryBuilder, authorisationModel, securityTokenProvider);

            LOGGER.info("\tBuilding field visibility...");
            codeRegistryBuilder.fieldVisibility(new FieldVisibility(authorisationModel, domainTypes, securityTokenProvider));

            LOGGER.info("\tBuilding default data fetcher...");
            codeRegistryBuilder.defaultDataFetcher(env -> fetching(env.getFieldDefinition().getName()));

            LOGGER.info("\tBuilding schema...");
            schema = newSchema()
                .codeRegistry(codeRegistryBuilder.build())
                .query(queryType)
                .additionalTypes(new LinkedHashSet<>(dictionary.values()))
                .build();

            LOGGER.info("GraphQL Web API...done");
        } catch (final Throwable t) {
            LOGGER.error("GraphQL Web API error.", t);
            throw t;
        }
    }

    /**
     * Returns all domain types from {@code applicationDomainProvider} that do not have introspection denied and satisfy predicate {@code toInclude}.
     */
    private Set<Class<? extends AbstractEntity<?>>> domainTypesOf(final IApplicationDomainProvider applicationDomainProvider, final Predicate<Class<? extends AbstractEntity<?>>> toInclude) {
        return applicationDomainProvider.entityTypes().stream()
            .filter(type -> 
                    !isIntrospectionDenied(type) // ensure that only entity types that don't have @DenyIntrospection annotation are included
                &&  toInclude.test(type) )
            .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Executes Web API query by using internal {@link GraphQL} service with a predefined schema.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> execute(final Map<String, Object> input) {
        try {
            final var result = newGraphQL(schema)
                    .queryExecutionStrategy(new GraphQLAsyncExecutionStrategy(new GraphQLSimpleDataFetcherExceptionHandler()))
                    .instrumentation(new MaxQueryDepthInstrumentation(maxQueryDepth)).build()
                    .execute(
                            newExecutionInput()
                                    .query(query(input))
                                    .operationName(operationName(input).orElse(null))
                                    .variables(variables(input)))
                    .toSpecification();
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
    private static Map<Class<? extends AbstractEntity<?>>, GraphQLType> createDictionary(final Set<Class<? extends AbstractEntity<?>>> entityTypes) {
        return entityTypes.stream()
            .map(GraphQLService::createGraphQLTypeFor)
            .flatMap(optType -> optType.map(Stream::of).orElseGet(Stream::empty))
            .sorted((pair1, pair2) -> pair1.getKey().getSimpleName().compareTo(pair2.getKey().getSimpleName()))
            .collect(toLinkedHashMap(Pair::getKey, Pair::getValue));
    }

    /**
     * Creates type for GraphQL 'query' operation to query entities from {@code dictionary}.
     * <p>
     * All query field names are represented as uncapitalised entity type simple names. All sub-field names are represented as entity property names.
     * 
     * @param dictionary -- list of supported GraphQL entity types
     * @param coFinder
     * @param dates
     * @param codeRegistryBuilder -- a place to register root data fetchers
     * @param authorisationModel -- authorises running of Web API queries
     * @param securityTokenProvider
     * @return
     */
    private static GraphQLObjectType createQueryType(final Set<Class<? extends AbstractEntity<?>>> dictionary, final ICompanionObjectFinder coFinder, final IDates dates, final GraphQLCodeRegistry.Builder codeRegistryBuilder, final IAuthorisationModel authorisationModel, final ISecurityTokenProvider securityTokenProvider) {
        final Builder queryTypeBuilder = newObject().name(QUERY_TYPE_NAME).description("Query following **entities** represented as GraphQL root fields:");
        dictionary.stream().forEach(entityType -> {
            final String simpleTypeName = entityType.getSimpleName();
            final String fieldName = uncapitalize(simpleTypeName);
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
        final List<GraphQLFieldDefinition> graphQLFieldDefinitions = (isUnionEntityType(entityType) ? unionProperties((Class<? extends AbstractUnionEntity>) entityType) : constructKeysAndProperties(entityType, true)).stream()
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
