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
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.constructKeysAndProperties;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.web_api.FieldSchema.createField;
import static ua.com.fielden.platform.web_api.WebApiUtils.operationName;
import static ua.com.fielden.platform.web_api.WebApiUtils.query;
import static ua.com.fielden.platform.web_api.WebApiUtils.variables;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLObjectType.Builder;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * Represents GraphQL implementation of TG Web API using graphql-java library.
 * 
 * @author TG Team
 *
 */
public class GraphQLService implements IWebApi {
    private final Logger logger = Logger.getLogger(getClass());
    private final GraphQL graphQL;
    
    /**
     * Creates GraphQLService instance based on <code>applicationDomainProvider</code> which contains all entity types.
     * 
     * @param applicationDomainProvider
     * @param coFinder
     */
    @Inject
    public GraphQLService(final IApplicationDomainProvider applicationDomainProvider, final ICompanionObjectFinder coFinder, final EntityFactory entityFactory) {
        this(applicationDomainProvider.entityTypes(), coFinder, entityFactory);
    }
    
    /**
     * Creates GraphQLService instance based on passed entity types.
     * 
     * @param entityTypes
     * @param coFinder
     */
    private GraphQLService(final List<Class<? extends AbstractEntity<?>>> entityTypes, final ICompanionObjectFinder coFinder, final EntityFactory entityFactory) {
        logger.info("GraphQL Web API...");
        final GraphQLCodeRegistry.Builder codeRegistryBuilder = newCodeRegistry();
        logger.info("\tBuilding dictionary...");
        final Map<Class<? extends AbstractEntity<?>>, GraphQLType> dictionary = createDictionary(entityTypes);
        logger.info("\tBuilding query schema...");
        final GraphQLObjectType queryType = createQueryType(dictionary.keySet(), coFinder, codeRegistryBuilder);
        final GraphQLSchema schema = newSchema()
                .codeRegistry(codeRegistryBuilder.build())
                .query(queryType)
                .additionalTypes(new LinkedHashSet<>(dictionary.values()))
                .build();
        graphQL = newGraphQL(schema).build();
        logger.info("GraphQL Web API...done");
    }
    
    @Override
    public Map<String, Object> execute(final Map<String, Object> input) {
        System.err.println("======================================= INPUT =======================================\n" + input + "\n=====================================================================================");
        final ExecutionResult execResult = graphQL.execute(
            newExecutionInput()
            .query(query(input))
            .operationName(operationName(input).orElse(null))
            .variables(variables(input))
        );
        final Map<String, Object> result = execResult.toSpecification();
        System.err.println("======================================= RESULT =======================================\n" + result + "\n=====================================================================================");
        return result;
    }
    
    /**
     * Creates GraphQL dictionary reflecting existing entity types.
     * 
     * @param entityTypes
     * @return
     */
    private static Map<Class<? extends AbstractEntity<?>>, GraphQLType> createDictionary(final List<Class<? extends AbstractEntity<?>>> entityTypes) {
        final Map<Class<? extends AbstractEntity<?>>, GraphQLType> types = new LinkedHashMap<>();
        for (final Class<? extends AbstractEntity<?>> entityType: entityTypes) {
            createType(entityType).map(type -> types.put(entityType, type));
        }
        return types;
    }
    
    /**
     * Creates query type for GraphQL quering of TG <code>entityTypes</code> entities.
     * <p>
     * All query field names are represented as uncapitalised entity type simple names. All sub-field names are represented as entity property names.
     * 
     * @param dictionary
     * @param coFinder
     * @param codeRegistryBuilder
     * @return
     */
    private static GraphQLObjectType createQueryType(final Set<Class<? extends AbstractEntity<?>>> dictionary, final ICompanionObjectFinder coFinder, final GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        final String queryTypeName = "Query";
        final Builder queryTypeBuilder = newObject().name(queryTypeName);
        for (final Class<? extends AbstractEntity<?>> entityType: dictionary) {
            final String simpleTypeName = entityType.getSimpleName();
            final String fieldName = uncapitalize(simpleTypeName);
            queryTypeBuilder.field(newFieldDefinition()
                .name(fieldName)
                .description(format("Query [%s] entity.", getEntityTitleAndDesc(entityType).getValue()))
                .type(new GraphQLList(new GraphQLTypeReference(simpleTypeName)))
            );
            codeRegistryBuilder.dataFetcher(coordinates(queryTypeName, fieldName), new RootEntityFetcher<>((Class<AbstractEntity<?>>) entityType, coFinder));
        }
        return queryTypeBuilder.build();
    }
    
    /**
     * Creates GraphQL object type for <code>entityType</code>d entities quering.
     * 
     * @param entityType
     * @return
     */
    private static Optional<GraphQLObjectType> createType(final Class<? extends AbstractEntity<?>> entityType) {
        final Builder typeBuilder = newObject();
        
        // the name of object should correspond to simple entity type name
        // TODO naming conflicts?
        typeBuilder.name(entityType.getSimpleName());
        
        typeBuilder.description(getEntityTitleAndDesc(entityType).getValue());
        
        constructKeysAndProperties(entityType).stream().forEach(prop ->
            createField(entityType, prop.getName())
            .map(field -> typeBuilder.field(field))
        );
        
        final GraphQLObjectType type = typeBuilder.build();
        if (type.getFieldDefinitions().isEmpty()) { // ignore types that have no GraphQL field equivalents; we can not use such types for any purpose including quering
            return empty();
        }
        return of(type);
    }
    
}