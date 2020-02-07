package ua.com.fielden.platform.web_api;

import static graphql.Scalars.GraphQLBigDecimal;
import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determineClass;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.isBoolean;
import static ua.com.fielden.platform.utils.EntityUtils.isCollectional;
import static ua.com.fielden.platform.utils.EntityUtils.isDate;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isString;
import static ua.com.fielden.platform.web_api.TgScalars.GraphQLColour;
import static ua.com.fielden.platform.web_api.TgScalars.GraphQLDate;
import static ua.com.fielden.platform.web_api.TgScalars.GraphQLHyperlink;
import static ua.com.fielden.platform.web_api.TgScalars.GraphQLMoney;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.menu.AbstractView;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;

/**
 * Contains utilities to convert TG entity properties to GraphQL query fields that reside under <code>Query.exampleEntityType</code> fields.
 * 
 * @author TG Team
 *
 */
public class FieldSchema {
    static final String LIKE = "like";
    static final String VALUE = "value";
    static final String FROM = "from";
    static final String TO = "to";

    /**
     * Creates GraphQL field definition for <code>entityType</code> and <code>property</code>.
     * Set of supported property types:
     * <ul>
     * <li>{@link String}</li>
     * <li>boolean</li>
     * <li>{@link Integer}</li>
     * <li>{@link Long}</li>
     * <li>{@link BigDecimal}</li>
     * <li>{@link Money}</li>
     * <li>entity</li>
     * <li>{@link Date}</li>
     * <li>{@link Hyperlink}</li>
     * <li>{@link Colour}</li>
     * </ul>
     * Returns {@link Optional#empty()} if the property type is not supported.
     * 
     * @param entityType
     * @param property
     * @return
     */
    public static Optional<GraphQLFieldDefinition> createGraphQLFieldDefinition(final Class<? extends AbstractEntity<?>> entityType, final String property) {
        return determineFieldType(entityType, property).map(typeAndArguments -> {
            return newFieldDefinition()
                .name(property)
                .description(getTitleAndDesc(property, entityType).getValue())
                .type(typeAndArguments._1)
                .arguments(typeAndArguments._2)
                .build();
        });
    }
    
    /**
     * Determines GraphQL field type for <code>entityType</code> and <code>property</code>.
     * <p>
     * This method takes into account collectional associations and creates {@link GraphQLList} wrapper around determined field type to be able to fetch list of
     * entities into such GraphQL fields.
     * 
     * @param entityType
     * @param property
     * @return
     */
    private static Optional<T2<GraphQLOutputType, List<GraphQLArgument>>> determineFieldType(final Class<? extends AbstractEntity<?>> entityType, final String property) {
        final Optional<T2<GraphQLOutputType, List<GraphQLArgument>>> nonCollectionalFieldType = determineFieldTypeNonCollectional(determinePropertyType(entityType, property));
        return isCollectional(determineClass(entityType, property, true, false))
            ? nonCollectionalFieldType.map(typeAndArguments -> t2(new GraphQLList(typeAndArguments._1), new ArrayList<>()))
            : nonCollectionalFieldType;
    }
    
    /**
     * Determines GraphQL field type for <code>propertyType</code>.
     * <p>
     * See {@link #createGraphQLFieldDefinition(Class, String)} for the list of supported property types.
     * Note that abstract types derived from {@link AbstractEntity} are not supported; {@link PropertyDescriptor} and {@link AbstractUnionEntity} descendants too.
     * 
     * @param propertyType
     * @return
     */
    private static Optional<T2<GraphQLOutputType, List<GraphQLArgument>>> determineFieldTypeNonCollectional(final Class<?> propertyType) {
        if (isString(propertyType)) {
            return of(t2(GraphQLString, asList(newArgument()
                .name(LIKE)
                .description("Include entities with specified string value pattern with % as a wildcard.")
                .type(GraphQLString)
                .build()
            )));
        } else if (isBoolean(propertyType)) {
            return of(t2(GraphQLBoolean, asList(newArgument() // null-valued or non-existing argument in GraphQL query means entities with both true and false values in the property
                .name(VALUE)
                .description("Include entities with specified boolean value.")
                .type(GraphQLBoolean)
                .build()
            )));
        } else if (Integer.class.isAssignableFrom(propertyType)) {
            return of(t2(GraphQLInt, createRangeArgumentsFor(GraphQLInt)));
        } else if (Long.class.isAssignableFrom(propertyType)) {
            // Even though we add here the support for Long values [-9,223,372,036,854,775,808; 9,223,372,036,854,775,807] = [-2^63; 2^63 - 1],
            // the actual support would be limited to              [    -9,007,199,254,740,992;     9,007,199,254,740,991] = [-2^53; 2^53 - 1];
            // This is because Javascript numbers, that are used in GraphiQL client, truncates higher numbers with zeros and performs weird rounding.
            return of(t2(GraphQLLong, createRangeArgumentsFor(GraphQLLong)));
        } else if (BigDecimal.class.isAssignableFrom(propertyType)) {
            return of(t2(GraphQLBigDecimal, createRangeArgumentsFor(GraphQLBigDecimal)));
        } else if (Money.class.isAssignableFrom(propertyType)) {
            return of(t2(GraphQLMoney, createRangeArgumentsFor(GraphQLMoney)));
        } else if (isDate(propertyType)) {
            return of(t2(GraphQLDate, createRangeArgumentsFor(GraphQLDate)));
        } else if (Hyperlink.class.isAssignableFrom(propertyType)) {
            return of(t2(GraphQLHyperlink, asList()));
        } else if (Colour.class.isAssignableFrom(propertyType)) {
            return of(t2(GraphQLColour, asList()));
        } else if (AbstractView.class == propertyType
            || PropertyDescriptor.class == propertyType
            || AbstractUnionEntity.class.isAssignableFrom(propertyType) // not supported yet
            || isAbstract(propertyType.getModifiers())) { // be careful with boolean.class because it has abstract modifier
            return empty();
        } else if (isEntityType(propertyType)) {
            return of(t2(new GraphQLTypeReference(propertyType.getSimpleName()), asList()));
        } else {
            return empty();
        }
    }
    
    /**
     * Creates GraphQL [from; to] argument definitions for range input type.
     * 
     * @param inputType
     * @return
     */
    private static List<GraphQLArgument> createRangeArgumentsFor(final GraphQLInputType inputType) {
        return asList(
            newArgument()
            .name(FROM)
            .description("Include entities with property greater than (or equal to) specified value.")
            .type(inputType)
            .build(),
            
            newArgument()
            .name(TO)
            .description("Include entities with property less than (or equal to) specified value.")
            .type(inputType)
            .build()
        );
    }
    
}