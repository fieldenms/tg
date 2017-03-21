package ua.com.fielden.platform.web_api;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLList;
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
public class GraphQLService {
    public final GraphQL graphQL;
    
    /**
     * Creates GraphQLService instance based on <code>applicationDomainProvider</code> which contains all entity types.
     * 
     * @param applicationDomainProvider
     * @param coFinder
     */
    public GraphQLService(final IApplicationDomainProvider applicationDomainProvider, final ICompanionObjectFinder coFinder) {
        this(applicationDomainProvider.entityTypes(), coFinder);
    }
    
    /**
     * Creates GraphQLService instance based on passed entity types.
     * 
     * @param entityTypes
     * @param coFinder
     */
    public GraphQLService(final List<Class<? extends AbstractEntity<?>>> entityTypes, final ICompanionObjectFinder coFinder) {
        final Builder queryTypeBuilder = newObject().name("BasicQuery");
        for (final Class<? extends AbstractEntity<?>> entityType: entityTypes) {
            final String typeName = entityType.getSimpleName();
            queryTypeBuilder.field(newFieldDefinition()
                    .name(StringUtils.uncapitalize(typeName))
                    .type(new GraphQLList(new GraphQLTypeReference(typeName)))
                    .dataFetcher(new RootEntityDataFetcher(entityType, coFinder)));
        }
        
        final GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(queryTypeBuilder.build())
                .build(createDictionary(entityTypes));
                // .build();

        graphQL = new GraphQL(schema); // GraphQL.newGraphQL(schema).build();
    }
    
    private static Set<GraphQLType> createDictionary(final List<Class<? extends AbstractEntity<?>>> entityTypes) {
        final Set<GraphQLType> types = new LinkedHashSet<>();
        for (final Class<? extends AbstractEntity<?>> entityType: entityTypes) {
            types.add(createGraphQLTypeFrom(entityType));
        }
        return types;
    }

    private static GraphQLType createGraphQLTypeFrom(final Class<? extends AbstractEntity<?>> entityType) {
        final Builder builder = newObject();
        
        // the name of object should correspond to simple entity type name
        // TODO naming conflicts?
        builder.name(entityType.getSimpleName());
        
        builder.description(TitlesDescsGetter.getEntityTitleAndDesc(entityType).getValue());
        
        final List<Field> keysAndProperties = AbstractDomainTreeRepresentation.constructKeysAndProperties(entityType);
        for (final Field propertyField : keysAndProperties) {
            createGraphQLFieldFrom(entityType, propertyField).map(b -> builder.field(b));
        }
        
        return builder.build();
    }

    private static Optional<graphql.schema.GraphQLFieldDefinition.Builder> createGraphQLFieldFrom(final Class<? extends AbstractEntity<?>> entityType, final Field propertyField) {
        final String name = propertyField.getName();
        final Optional<GraphQLOutputType> graphQLType = determineGraphQLTypeFrom(entityType, name);
        return graphQLType.map(type -> {
            final graphql.schema.GraphQLFieldDefinition.Builder builder = newFieldDefinition();
            if (Scalars.GraphQLBoolean.equals(type)) {
                builder.argument(new GraphQLArgument("value", null /* description of argument */, Scalars.GraphQLBoolean, null /*default value of argument */));
            }
            builder.name(name);
            builder.description(TitlesDescsGetter.getTitleAndDesc(name, entityType).getValue());
            return builder.type(type);
        });
    }

    private static Optional<GraphQLOutputType> determineGraphQLTypeFrom(final Class<? extends AbstractEntity<?>> entityType, final String name) {
        final Class<?> realType = PropertyTypeDeterminator.determineClass(entityType, name, true, false);
        final Class<?> parameterType = PropertyTypeDeterminator.determineClass(entityType, name, true, true);
        if (EntityUtils.isCollectional(realType)) {
            if (Object.class == parameterType) { // TODO descendants of AbstractFunctionalEntityForCollectionModification<ID_TYPE> (chosenIds etc.) || 
                return Optional.empty();
                // return new GraphQLList(Scalars.GraphQLString);
            }
            return determineGraphQLTypeFrom(parameterType, entityType, name).map(t -> new GraphQLList(t));
        } else {
            return determineGraphQLTypeFrom(parameterType, entityType, name);
        }
    }
    
    private static Optional<GraphQLOutputType> determineGraphQLTypeFrom(final Class<?> type, final Class<? extends AbstractEntity<?>> entityType, final String name) {
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
            throw new UnsupportedOperationException(String.format("Field type [%s] is unsupported yet (type = %s, name = %s).", type.getSimpleName(), entityType.getSimpleName(), name));
        }
    }
}
