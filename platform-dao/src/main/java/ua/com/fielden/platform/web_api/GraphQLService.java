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
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.reflectionProperty;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.constructKeysAndProperties;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isExcluded;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.streaming.ValueCollectors.toLinkedHashMap;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.web_api.FieldSchema.createGraphQLFieldDefinition;
import static ua.com.fielden.platform.web_api.WebApiUtils.operationName;
import static ua.com.fielden.platform.web_api.WebApiUtils.query;
import static ua.com.fielden.platform.web_api.WebApiUtils.variables;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import graphql.GraphQL;
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
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.utils.Pair;

/**
 * Represents GraphQL implementation of TG Web API using graphql-java library.
 * <p>
 * At this stage only GraphQL 'query' is supported. GraphQL 'mutation' support is on the next stages.
 * 
 * @author TG Team
 *
 */
public class GraphQLService implements IWebApi {
    private final Logger logger = Logger.getLogger(getClass());
    private final GraphQL graphQL;
    
    /**
     * Creates GraphQLService instance based on <code>applicationDomainProvider</code> which contains all entity types.
     * <p>
     * We start by building dictionary of all our custom GraphQL types from existing domain entity types.
     * Next, we create GraphQL type for quering (aka GraphQL 'query') and assign it to the schema.
     * 
     * @param applicationDomainProvider
     * @param coFinder
     */
    @Inject
    public GraphQLService(final IApplicationDomainProvider applicationDomainProvider, final ICompanionObjectFinder coFinder) {
        logger.info("GraphQL Web API...");
        final GraphQLCodeRegistry.Builder codeRegistryBuilder = newCodeRegistry();
        logger.info("\tBuilding dictionary...");
        final Map<Class<? extends AbstractEntity<?>>, GraphQLType> dictionary = createDictionary(applicationDomainProvider.entityTypes());
        logger.info("\tBuilding query type...");
        final GraphQLObjectType queryType = createQueryType(dictionary.keySet(), coFinder, codeRegistryBuilder);
        logger.info("\tBuilding schema...");
        final GraphQLSchema schema = newSchema()
            .codeRegistry(codeRegistryBuilder.build())
            .query(queryType)
            .additionalTypes(new LinkedHashSet<>(dictionary.values()))
            .build();
        logger.info("\tBuilding service...");
        graphQL = newGraphQL(schema).build();
        logger.info("GraphQL Web API...done");
    }
    
    /**
     * Executes Web API query by using internal {@link GraphQL} service with predefined schema.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> execute(final Map<String, Object> input) {
        return graphQL.execute(newExecutionInput()
            .query(query(input))
            .operationName(operationName(input).orElse(null))
            .variables(variables(input))
        ).toSpecification();
    }
    
    /**
     * Creates map of GraphQL dictionary (aka GraphQL "additional types") and corresponding entity types.
     * <p>
     * The set of resultant types can be smaller than those derived upon. See {@link #createGraphQLTypeFor(Class)} for more details.
     * 
     * @param entityTypes
     * @return
     */
    private static Map<Class<? extends AbstractEntity<?>>, GraphQLType> createDictionary(final List<Class<? extends AbstractEntity<?>>> entityTypes) {
        return entityTypes.stream()
            .map(GraphQLService::createGraphQLTypeFor)
            .flatMap(optType -> optType.map(Stream::of).orElseGet(Stream::empty))
            .collect(toLinkedHashMap(Pair::getKey, Pair::getValue));
    }
    
    /**
     * Creates type for GraphQL 'query' operation to query entities from <code>dictionary</code>.
     * <p>
     * All query field names are represented as uncapitalised entity type simple names. All sub-field names are represented as entity property names.
     * 
     * @param dictionary -- list of supported GraphQL entity types
     * @param coFinder
     * @param codeRegistryBuilder -- a place to register root data fetchers
     * @return
     */
    private static GraphQLObjectType createQueryType(final Set<Class<? extends AbstractEntity<?>>> dictionary, final ICompanionObjectFinder coFinder, final GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        final String queryTypeName = "Query";
        final Builder queryTypeBuilder = newObject().name(queryTypeName);
        dictionary.stream().forEach(entityType -> {
            final String simpleTypeName = entityType.getSimpleName();
            final String fieldName = uncapitalize(simpleTypeName);
            queryTypeBuilder.field(newFieldDefinition()
                .name(fieldName)
                .description(format("Query [%s] entity.", getEntityTitleAndDesc(entityType).getValue()))
                .type(new GraphQLList(new GraphQLTypeReference(simpleTypeName)))
            );
            codeRegistryBuilder.dataFetcher(coordinates(queryTypeName, fieldName), new RootEntityFetcher<>((Class<AbstractEntity<?>>) entityType, coFinder));
        });
        return queryTypeBuilder.build();
    }
    
    /**
     * Creates {@link Optional} GraphQL object type for <code>entityType</code>d entities querying.
     * <p>
     * The <code>entityType</code> will not have corresponding {@link GraphQLObjectType} only if there are no suitable fields for querying.
     * 
     * @param entityType
     * @return
     */
    private static Optional<Pair<Class<? extends AbstractEntity<?>>, GraphQLObjectType>> createGraphQLTypeFor(final Class<? extends AbstractEntity<?>> entityType) {
        final List<GraphQLFieldDefinition> graphQLFieldDefinitions = constructKeysAndProperties(entityType).stream()
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
                .description(getEntityTitleAndDesc(entityType).getValue())
                .fields(graphQLFieldDefinitions).build()
            ));
        }
        return empty();
    }
    
}