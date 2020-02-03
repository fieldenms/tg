package ua.com.fielden.platform.web_api;

import static graphql.Scalars.GraphQLBigDecimal;
import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determineClass;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.isBoolean;
import static ua.com.fielden.platform.utils.EntityUtils.isDate;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isString;
import static ua.com.fielden.platform.web_api.TgScalars.GraphQLColour;
import static ua.com.fielden.platform.web_api.TgScalars.GraphQLDate;
import static ua.com.fielden.platform.web_api.TgScalars.GraphQLHyperlink;
import static ua.com.fielden.platform.web_api.TgScalars.GraphQLMoney;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition.Builder;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.menu.AbstractView;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Contains utilities to convert TG entity properties to GraphQL query fields that reside under <code>Query.exampleEntityType</code> fields.
 * 
 * @author TG Team
 *
 */
public class FieldSchema {
    
    /**
     * Creates GraphQL field definition on entity type's object from entity property defined by <code>propertyField</code>.
     * 
     * @param entityType
     * @param name
     * @return
     */
    public static Optional<graphql.schema.GraphQLFieldDefinition> createField(final Class<? extends AbstractEntity<?>> entityType, final String name) {
        return determineFieldType(entityType, name).map(typeAndArguments -> {
            final Builder builder = newFieldDefinition();
            typeAndArguments._2.stream().forEach(argument -> builder.argument(argument));
            return builder
                .name(name)
                .description(getTitleAndDesc(name, entityType).getValue())
                .type(typeAndArguments._1)
                .build();
        });
    }
    
    private static Optional<T2<GraphQLOutputType, List<GraphQLArgument>>> determineFieldType(final Class<? extends AbstractEntity<?>> entityType, final String name) {
        final Class<?> realType = determineClass(entityType, name, true, false);
        final Class<?> parameterType = determineClass(entityType, name, true, true);
        if (EntityUtils.isCollectional(realType)) {
            if (Object.class == parameterType) { // TODO descendants of AbstractFunctionalEntityForCollectionModification<ID_TYPE> (chosenIds etc.) || 
                return Optional.empty();
                // return new GraphQLList(Scalars.GraphQLString);
            }
            return determineFieldTypeNonCollectional(parameterType, entityType, name).map(typeAndArguments -> t2(new GraphQLList(typeAndArguments._1), new ArrayList<>()));
        } else {
            return determineFieldTypeNonCollectional(parameterType, entityType, name);
        }
    }
    
    private static Optional<T2<GraphQLOutputType, List<GraphQLArgument>>> determineFieldTypeNonCollectional(final Class<?> type, final Class<? extends AbstractEntity<?>> entityType, final String name) {
        if (
            IContinuationData.class.isAssignableFrom(type) ||
            Map.class.isAssignableFrom(type) ||
            byte[].class.isAssignableFrom(type) ||
            Class.class.isAssignableFrom(type) || 
            CentreContext.class.isAssignableFrom(type) || 
            Optional.class.isAssignableFrom(type) || 
            Boolean.class == type || // only boolean.class
            int.class == type || // only Integer.class
            AbstractEntity.class == type || // CentreContextHolder.selectedEntities => List<AbstractEntity>, masterEntity => AbstractEntity
            AbstractView.class == type || 
            PropertyDescriptor.class == type ||
            NoKey.class.isAssignableFrom(type) ||
            DynamicEntityKey.class.isAssignableFrom(type) || // this is for the weird cases where DynamicEntityKey is used but no @CompositeKeyMember exists
            AbstractUnionEntity.class.isAssignableFrom(type) // not supported yet
            ) {
            return empty();
        } else if (isString(type)) {
            return of(t2(GraphQLString, asList(newArgument()
                .name("like")
                .description("Include entities with specified string value pattern with % as a wildcard.")
                .type(GraphQLString)
                .build()
            )));
        } else if (isBoolean(type)) {
            return of(t2(GraphQLBoolean, asList(newArgument() // null-valued or non-existing argument in GraphQL query means entities with both true and false values in the property
                .name("value")
                .description("Include entities with specified boolean value.")
                .type(GraphQLBoolean)
                .build()
            )));
        } else if (Integer.class.isAssignableFrom(type)) {
            return of(t2(GraphQLInt, createRangeArgumentsFor(GraphQLInt)));
        } else if (Long.class.isAssignableFrom(type)) {
            // Even though we add here the support for Long values [-9,223,372,036,854,775,808; 9,223,372,036,854,775,807] = [-2^63; 2^63 - 1],
            // the actual support would be limited to              [    -9,007,199,254,740,992;     9,007,199,254,740,991] = [-2^53; 2^53 - 1];
            // This is because Javascript numbers, that are used in GraphiQL client, truncates higher numbers with zeros and performs weird rounding.
            return of(t2(GraphQLLong, createRangeArgumentsFor(GraphQLLong)));
        } else if (BigDecimal.class.isAssignableFrom(type)) {
            return of(t2(GraphQLBigDecimal, createRangeArgumentsFor(GraphQLBigDecimal)));
            // TODO remove return TgScalars.GraphQLBigDecimal;
        } else if (Money.class.isAssignableFrom(type)) {
            return of(t2(GraphQLMoney, createRangeArgumentsFor(GraphQLMoney)));
        } else if (Modifier.isAbstract(type.getModifiers())) {
            return empty();
        } else if (isEntityType(type)) {
            return of(t2(new GraphQLTypeReference(type.getSimpleName()), asList()));
        } else if (isDate(type)) {
            return of(t2(GraphQLDate, createRangeArgumentsFor(GraphQLDate)));
        } else if (Hyperlink.class.isAssignableFrom(type)) {
            return of(t2(GraphQLHyperlink, asList()));
        } else if (Colour.class.isAssignableFrom(type)) {
            return of(t2(GraphQLColour, asList()));
        } else {
            throw new UnsupportedOperationException(format("Field: type [%s] is unknown (type = %s, name = %s).", type.getSimpleName(), entityType.getSimpleName(), name));
        }
    }
    
    private static List<GraphQLArgument> createRangeArgumentsFor(final GraphQLInputType inputType) {
        return asList(
            newArgument()
            .name("from")
            .description("Include entities with property greater than (or equal to) specified value.")
            .type(inputType)
            .build(),
            
            newArgument()
            .name("to")
            .description("Include entities with property less than (or equal to) specified value.")
            .type(inputType)
            .build()
        );
    }
    
}