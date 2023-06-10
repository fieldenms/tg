package ua.com.fielden.platform.web_api;

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
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.reflectionProperty;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.constructKeysAndProperties;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isExcluded;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.streaming.ValueCollectors.toLinkedHashMap;
import static ua.com.fielden.platform.utils.EntityUtils.isIntrospectionDenied;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.web_api.FieldSchema.LIKE_ARGUMENT;
import static ua.com.fielden.platform.web_api.FieldSchema.ORDER_ARGUMENT;
import static ua.com.fielden.platform.web_api.FieldSchema.PAGE_CAPACITY_ARGUMENT;
import static ua.com.fielden.platform.web_api.FieldSchema.PAGE_NUMBER_ARGUMENT;
import static ua.com.fielden.platform.web_api.FieldSchema.bold;
import static ua.com.fielden.platform.web_api.FieldSchema.createGraphQLFieldDefinition;
import static ua.com.fielden.platform.web_api.FieldSchema.titleAndDescRepresentation;
import static ua.com.fielden.platform.web_api.RootEntityUtils.QUERY_TYPE_NAME;
import static ua.com.fielden.platform.web_api.WebApiUtils.operationName;
import static ua.com.fielden.platform.web_api.WebApiUtils.query;
import static ua.com.fielden.platform.web_api.WebApiUtils.variables;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import graphql.GraphQL;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLObjectType.Builder;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
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
    private final Logger logger = LogManager.getLogger(getClass());
    private final GraphQLSchema schema;
    private final Integer maxQueryDepth;

    /**
     * Creates GraphQLService instance based on {@code applicationDomainProvider} which contains all entity types.
     * <p>
     * We start by building dictionary of all our custom GraphQL types from existing domain entity types.
     * Then we create GraphQL type for quering (aka GraphQL {@code query}) and assign it to the schema.
     *
     * @param maxQueryDepth -- the maximum depth of GraphQL query that are permitted to be executed.
     * @param applicationDomainProvider
     * @param coFinder
     * @param dates
     * @param authorisationModelProvider -- Guice {@link Provider} for {@link IAuthorisationModel}; would create auth model to authorise running of Web API queries and their {@link FieldVisibility}
     * @param securityTokensPackageName
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
            logger.info("GraphQL Web API...");
            if (maxQueryDepth == null || maxQueryDepth.compareTo(0) < 0) {
                throw new WebApiException("GraphQL max query depth must be specified and cannot be negative.");
            }
            this.maxQueryDepth = maxQueryDepth;

            logger.info("\tmaxQueryDepth = " + maxQueryDepth);
            final GraphQLCodeRegistry.Builder codeRegistryBuilder = newCodeRegistry();

            logger.info("\tBuilding dictionary...");
            final Set<Class<? extends AbstractEntity<?>>> domainTypes = domainTypesOf(applicationDomainProvider, EntityUtils::isIntrospectionAllowed).stream() // synthetic / persistent without @DenyIntrospection; this includes persistent with activatable nature, synthetic based on persistent; this does not include union, functional and any other entities
                .sorted((type1, type2) -> type1.getSimpleName().compareTo(type2.getSimpleName()))
                .collect(toCollection(LinkedHashSet::new));
            final Set<Class<? extends AbstractEntity<?>>> allTypes = new LinkedHashSet<>(domainTypes);
            allTypes.addAll(domainTypesOf(applicationDomainProvider, EntityUtils::isUnionEntityType));
            // dictionary must have all the types that are referenced by all types that should support querying
            final Map<Class<? extends AbstractEntity<?>>, GraphQLType> dictionary = createDictionary(allTypes);

            logger.info("\tBuilding query type...");
            final GraphQLObjectType queryType = createQueryType(domainTypes, coFinder, dates, codeRegistryBuilder, authorisationModel, securityTokenProvider);

            logger.info("\tBuilding field visibility...");
            codeRegistryBuilder.fieldVisibility(new FieldVisibility(authorisationModel, domainTypes, securityTokenProvider));

            logger.info("\tBuilding schema...");
            schema = newSchema()
                .codeRegistry(codeRegistryBuilder.build())
                .query(queryType)
                .additionalTypes(new LinkedHashSet<>(dictionary.values()))
                .build();

            logger.info("GraphQL Web API...done");
        } catch (final Throwable t) {
            logger.error("GraphQL Web API error.", t);
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
     * Executes Web API query by using internal {@link GraphQL} service with predefined schema.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> execute(final Map<String, Object> input) {
        return newGraphQL(schema)
               .instrumentation(new MaxQueryDepthInstrumentation(maxQueryDepth)).build()
               .execute(
                       newExecutionInput()
                       .query(query(input))
                       .operationName(operationName(input).orElse(null))
                       .variables(variables(input)))
               .toSpecification();
    }

    /**
     * Creates map of GraphQL dictionary (aka GraphQL "additional types") and corresponding entity types.
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
     * @param securityTokensPackageName
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
     * Creates {@link Optional} GraphQL object type for {@code entityType}d entities querying.
     * <p>
     * The {@code entityType} will not have corresponding {@link GraphQLObjectType} only if there are no suitable fields for querying.
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