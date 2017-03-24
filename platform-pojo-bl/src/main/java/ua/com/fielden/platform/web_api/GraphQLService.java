package ua.com.fielden.platform.web_api;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLObjectType.Builder;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.menu.AbstractView;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Represents GraphQL implementation of TG Web API using graphql-java library.
 * 
 * @author TG Team
 *
 */
public class GraphQLService implements IGraphQLService {
    private final Logger logger = Logger.getLogger(getClass());
    public final GraphQL graphQL;
    
    /**
     * Creates GraphQLService instance based on <code>applicationDomainProvider</code> which contains all entity types.
     * 
     * @param applicationDomainProvider
     * @param coFinder
     */
    @Inject
    public GraphQLService(final IApplicationDomainProvider applicationDomainProvider, final ICompanionObjectFinder coFinder) {
        this(applicationDomainProvider.entityTypes(), coFinder);
    }
    
    /**
     * Creates GraphQLService instance based on passed entity types.
     * 
     * @param entityTypes
     * @param coFinder
     */
    private GraphQLService(final List<Class<? extends AbstractEntity<?>>> entityTypes, final ICompanionObjectFinder coFinder) {
        logger.error("GraphQL Web API...");
        logger.error("\tBuilding root query...");
        logger.error("\tBuilding schema...");
        final GraphQLObjectType queryType = createQueryType(entityTypes, coFinder);
        final GraphQLObjectType mutationType = createMutationType(entityTypes, coFinder);
        final GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(queryType)
                .mutation(mutationType)
                .build(createDictionary(entityTypes));
        graphQL = new GraphQL(schema);
        logger.error("GraphQL Web API...done");
    }
    
    /**
     * Creates GraphQL dictionary reflecting existing entity types.
     * 
     * @param entityTypes
     * @return
     */
    private static Set<GraphQLType> createDictionary(final List<Class<? extends AbstractEntity<?>>> entityTypes) {
        final Set<GraphQLType> types = new LinkedHashSet<>();
        for (final Class<? extends AbstractEntity<?>> entityType: entityTypes) {
            types.add(createType(entityType));
        }
        return types;
    }
    
    /**
     * Creates query type for GraphQL quering of TG <code>entityTypes</code> entities.
     * <p>
     * All query field names are represented as uncapitalised entity type simple names. All sub-field names are represented as entity property names.
     * 
     * @param entityTypes
     * @param coFinder
     * @return
     */
    private static GraphQLObjectType createQueryType(final List<Class<? extends AbstractEntity<?>>> entityTypes, final ICompanionObjectFinder coFinder) {
        final Builder queryTypeBuilder = newObject().name("BasicQuery");
        for (final Class<? extends AbstractEntity<?>> entityType: entityTypes) {
            final String typeName = entityType.getSimpleName();
            final String rootQueryFieldDescription = String.format("Query [%s] entity.", TitlesDescsGetter.getEntityTitleAndDesc(entityType).getValue());
            queryTypeBuilder.field(newFieldDefinition()
                .name(StringUtils.uncapitalize(typeName))
                .description(rootQueryFieldDescription)
                .type(new GraphQLList(new GraphQLTypeReference(typeName)))
                .dataFetcher(new RootEntityFetcher(entityType, coFinder))
            );
        }
        return queryTypeBuilder.build();
    }
    
    /**
     * Creates mutation type for GraphQL mutating of TG <code>entityTypes</code> entities.
     * <p>
     * All query field names are represented as uncapitalised entity type simple names. All sub-field names are represented as entity property names.
     * <p>
     * There are two arguments for entity field: <code>input</code> (required) and <code>keys</code> (optional).
     * Update query requires <code>keys</code> in order to define which entity needs mutation.
     * Create query does not require <code>keys</code>, only <code>input</code> that contains mutated properties.
     * 
     * @param entityTypes
     * @param coFinder
     * @return
     */
    private static GraphQLObjectType createMutationType(final List<Class<? extends AbstractEntity<?>>> entityTypes, final ICompanionObjectFinder coFinder) {
        final Builder mutationTypeBuilder = newObject().name("BasicMutation");
        for (final Class<? extends AbstractEntity<?>> entityType: entityTypes) {
            final String typeName = entityType.getSimpleName();
            final String inputArgumentDescription = String.format("Input values for mutating / creating [%s] entity", TitlesDescsGetter.getEntityTitleAndDesc(entityType).getValue());
            final String rootMutationFieldDescription = String.format("Mutate [%s] entity.", TitlesDescsGetter.getEntityTitleAndDesc(entityType).getValue());
            mutationTypeBuilder.field(newFieldDefinition()
                .name(StringUtils.uncapitalize(typeName))
                .description(rootMutationFieldDescription)
                .type(new GraphQLTypeReference(typeName))
                .dataFetcher(new RootEntityMutator(entityType, coFinder))
                .argument(new GraphQLArgument("input", inputArgumentDescription, new GraphQLNonNull(createMutationInputArgumentType(entityType, inputArgumentDescription)), null /*default value of argument */))
                // TODO .argument(new GraphQLArgument("keys", String.format("Key criteria for mutating some concrete [%s] entity", typeName), createKeysType(entityType), null /*default value of argument */))
            );
        }
        return mutationTypeBuilder.build();
    }
    
    /**
     * Creates GraphQL object type for <code>entityType</code>d entities quering.
     * 
     * @param entityType
     * @return
     */
    private static GraphQLObjectType createType(final Class<? extends AbstractEntity<?>> entityType) {
        final Builder builder = newObject();
        
        // the name of object should correspond to simple entity type name
        // TODO naming conflicts?
        builder.name(entityType.getSimpleName());
        
        builder.description(TitlesDescsGetter.getEntityTitleAndDesc(entityType).getValue());
        
        final List<Field> keysAndProperties = AbstractDomainTreeRepresentation.constructKeysAndProperties(entityType);
        for (final Field propertyField : keysAndProperties) {
            createField(entityType, propertyField).map(b -> builder.field(b));
        }
        
        return builder.build();
    }
    
    /**
     * Creates the type for required <code>input</code> argument of mutation query for <code>entityType</code>d entities.
     * 
     * @param entityType
     * @param inputArgumentDescription
     * 
     * @return
     */
    private static GraphQLInputObjectType createMutationInputArgumentType(final Class<? extends AbstractEntity<?>> entityType, final String inputArgumentDescription) {
        final String typeName = entityType.getSimpleName();
        final graphql.schema.GraphQLInputObjectType.Builder builder = newInputObject().name(typeName + "Input").description(inputArgumentDescription);
        
        final List<Field> keysAndProperties = AbstractDomainTreeRepresentation.constructKeysAndProperties(entityType);
        for (final Field propertyField : keysAndProperties) {
            createMutationInputArgumentField(entityType, propertyField).map(b -> builder.field(b));
        }
        
        return builder.build();
    }
    
    /**
     * Creates GraphQL field definition on entity type's object from entity property defined by <code>propertyField</code>.
     * 
     * @param entityType
     * @param propertyField
     * @return
     */
    private static Optional<graphql.schema.GraphQLFieldDefinition.Builder> createField(final Class<? extends AbstractEntity<?>> entityType, final Field propertyField) {
        final String name = propertyField.getName();
        final Optional<GraphQLOutputType> fieldType = determineFieldType(entityType, name);
        return fieldType.map(type -> {
            final graphql.schema.GraphQLFieldDefinition.Builder builder = newFieldDefinition();
            if (Scalars.GraphQLBoolean.equals(type)) {
                builder.argument(new GraphQLArgument("value", null /* description of argument */, Scalars.GraphQLBoolean, null /*default value of argument */));
            }
            builder.name(name);
            builder.description(TitlesDescsGetter.getTitleAndDesc(name, entityType).getValue());
            return builder.type(type);
        });
    }
    
    /**
     * Creates GraphQL field definition on entity type's <b>input</b> object from entity property defined by <code>propertyField</code>.
     * 
     * @param entityType
     * @param propertyField
     * @return
     */
    private static Optional<graphql.schema.GraphQLInputObjectField.Builder> createMutationInputArgumentField(final Class<? extends AbstractEntity<?>> entityType, final Field propertyField) {
        final String name = propertyField.getName();
        final Optional<GraphQLInputType> fieldType = determineMutationInputArgumentFieldType(entityType, name);
        return fieldType.map(type -> {
            final graphql.schema.GraphQLInputObjectField.Builder builder = newInputObjectField();
            // if (Scalars.GraphQLBoolean.equals(type)) {
            //     builder.argument(new GraphQLArgument("value", null /* description of argument */, Scalars.GraphQLBoolean, null /*default value of argument */));
            // }
            builder.name(name);
            builder.description(TitlesDescsGetter.getTitleAndDesc(name, entityType).getValue());
            return builder.type(type);
        });
    }

    private static Optional<GraphQLOutputType> determineFieldType(final Class<? extends AbstractEntity<?>> entityType, final String name) {
        final Class<?> realType = PropertyTypeDeterminator.determineClass(entityType, name, true, false);
        final Class<?> parameterType = PropertyTypeDeterminator.determineClass(entityType, name, true, true);
        if (EntityUtils.isCollectional(realType)) {
            if (Object.class == parameterType) { // TODO descendants of AbstractFunctionalEntityForCollectionModification<ID_TYPE> (chosenIds etc.) || 
                return Optional.empty();
                // return new GraphQLList(Scalars.GraphQLString);
            }
            return determineFieldTypeNonCollectional(parameterType, entityType, name).map(t -> new GraphQLList(t));
        } else {
            return determineFieldTypeNonCollectional(parameterType, entityType, name);
        }
    }
    
    private static Optional<GraphQLInputType> determineMutationInputArgumentFieldType(final Class<? extends AbstractEntity<?>> entityType, final String name) {
        final Class<?> realType = PropertyTypeDeterminator.determineClass(entityType, name, true, false);
        final Class<?> parameterType = PropertyTypeDeterminator.determineClass(entityType, name, true, true);
        if (EntityUtils.isCollectional(realType)) {
            return Optional.empty(); // TODO at this stage there will be no support for collectional input object fields
            // TODO return determineFieldTypeNonCollectional(parameterType, entityType, name).map(t -> new GraphQLList(t));
        } else {
            return determineMutationInputArgumentFieldTypeNonCollectional(parameterType, entityType, name);
        }
    }
    
    private static Optional<GraphQLOutputType> determineFieldTypeNonCollectional(final Class<?> type, final Class<? extends AbstractEntity<?>> entityType, final String name) {
        if (EntityUtils.isString(type)) {
            return Optional.of(Scalars.GraphQLString);
        } else if (EntityUtils.isBoolean(type)) {
            return Optional.of(Scalars.GraphQLBoolean);
        } else if (EntityUtils.isDecimal(type)) {
            return Optional.of(Scalars.GraphQLBigDecimal);
            // TODO remove return TgScalars.GraphQLBigDecimal;
        } else if (Long.class.isAssignableFrom(type)) {
            return Optional.of(Scalars.GraphQLLong);
        } else if (Integer.class.isAssignableFrom(type)) {
            return Optional.of(Scalars.GraphQLInt);
        } else if (
                IContinuationData.class.isAssignableFrom(type) ||
                Map.class.isAssignableFrom(type) ||
                byte[].class.isAssignableFrom(type) ||
                Class.class.isAssignableFrom(type) || 
                CentreContext.class.isAssignableFrom(type) || 
                Optional.class.isAssignableFrom(type) || 
                Boolean.class.isAssignableFrom(type) || 
                int.class.isAssignableFrom(type) || 
                AbstractEntity.class == type || // CentreContextHolder.selectedEntities => List<AbstractEntity>, masterEntity => AbstractEntity
                AbstractView.class == type || 
                PropertyDescriptor.class == type
        ) {
            return Optional.empty();
        } else if (EntityUtils.isDate(type)) {
            return Optional.empty();
            // TODO return TgScalars.GraphQLDate;
        } else if (Hyperlink.class.isAssignableFrom(type)) {
            return Optional.empty();
            // TODO return TgScalars.GraphQLHyperlink;
        } else if (Colour.class.isAssignableFrom(type)) {
            return Optional.empty();
            // TODO return TgScalars.GraphQLColour;
        } else if (EntityUtils.isEntityType(type)) {
            return Optional.of(new GraphQLTypeReference(type.getSimpleName()));
        } else {
            throw new UnsupportedOperationException(String.format("Field: type [%s] is unknown (type = %s, name = %s).", type.getSimpleName(), entityType.getSimpleName(), name));
        }
    }
    
    private static Optional<GraphQLInputType> determineMutationInputArgumentFieldTypeNonCollectional(final Class<?> type, final Class<? extends AbstractEntity<?>> entityType, final String name) {
        if (EntityUtils.isString(type)) {
            return Optional.of(Scalars.GraphQLString);
        } else if (EntityUtils.isBoolean(type)) {
            return Optional.of(Scalars.GraphQLBoolean);
        } else if (EntityUtils.isDecimal(type)) {
            return Optional.of(Scalars.GraphQLBigDecimal);
            // TODO remove return TgScalars.GraphQLBigDecimal;
        } else if (Long.class.isAssignableFrom(type)) {
            return Optional.of(Scalars.GraphQLLong);
        } else if (Integer.class.isAssignableFrom(type)) {
            return Optional.of(Scalars.GraphQLInt);
        } else if (
                IContinuationData.class.isAssignableFrom(type) ||
                Map.class.isAssignableFrom(type) ||
                byte[].class.isAssignableFrom(type) ||
                Class.class.isAssignableFrom(type) || 
                CentreContext.class.isAssignableFrom(type) || 
                Optional.class.isAssignableFrom(type) || 
                Boolean.class.isAssignableFrom(type) || 
                int.class.isAssignableFrom(type) || 
                AbstractEntity.class == type || // CentreContextHolder.selectedEntities => List<AbstractEntity>, masterEntity => AbstractEntity
                AbstractView.class == type || 
                PropertyDescriptor.class == type
        ) {
            return Optional.empty();
        } else if (EntityUtils.isDate(type)) {
            return Optional.empty();
            // TODO return TgScalars.GraphQLDate;
        } else if (Hyperlink.class.isAssignableFrom(type)) {
            return Optional.empty();
            // TODO return TgScalars.GraphQLHyperlink;
        } else if (Colour.class.isAssignableFrom(type)) {
            return Optional.empty();
            // TODO return TgScalars.GraphQLColour;
        } else if (EntityUtils.isEntityType(type)) {
            // TODO return Optional.of(new GraphQLTypeReference(type.getSimpleName()));
            // TODO at this stage there will be no support for input object fields of entity type
            return Optional.empty();
        } else {
            throw new UnsupportedOperationException(String.format("Mutation input argument field: type [%s] is unknown (type = %s, name = %s).", type.getSimpleName(), entityType.getSimpleName(), name));
        }
    }
}
