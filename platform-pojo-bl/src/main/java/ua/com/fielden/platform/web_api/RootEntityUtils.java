package ua.com.fielden.platform.web_api;

import static java.lang.Byte.valueOf;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.ASCENDING;
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.DESCENDING;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity_centre.review.DynamicOrderingBuilder.createOrderingModel;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createQuery;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty.createEmptyQueryProperty;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.streaming.ValueCollectors.toLinkedHashMap;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.EntityUtils.fetchNotInstrumented;
import static ua.com.fielden.platform.utils.EntityUtils.isBoolean;
import static ua.com.fielden.platform.utils.EntityUtils.isString;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.web_api.FieldSchema.FROM;
import static ua.com.fielden.platform.web_api.FieldSchema.LIKE;
import static ua.com.fielden.platform.web_api.FieldSchema.ORDER;
import static ua.com.fielden.platform.web_api.FieldSchema.ORDER_ARGUMENT;
import static ua.com.fielden.platform.web_api.FieldSchema.TO;
import static ua.com.fielden.platform.web_api.FieldSchema.VALUE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import graphql.execution.ValuesResolver;
import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.Pair;

/**
 * Contains querying utility methods for root fields in GraphQL query / mutation schemas.
 * 
 * @author TG Team
 *
 */
public class RootEntityUtils {
    static final String QUERY_TYPE_NAME = "Query";
    private static final Logger LOGGER = Logger.getLogger(RootEntityUtils.class);
    
    /**
     * Returns function for generation of EQL query execution model for retrieving <code>rootField</code> and its selection set in GraphQL query or mutation.
     * The argument of function is {@link IDates} instance from which 'now' moment can properly be retrieved and used for date property filtering.
     * 
     * @param rootField -- root field for GraphQL query or mutation
     * @param variables -- existing variable values by names in the query; they can be used in <code>rootField.selectionSet</code>
     * @param fragmentDefinitions -- fragment definitions by names in the query; <code>rootField.selectionSet</code> can contain fragment spreads based on that definitions
     * @param entityType
     * @param schema -- GraphQL schema to assist with resolving of argument values
     * @return
     */
    public static <T extends AbstractEntity<?>> Function<IDates, QueryExecutionModel<T, EntityResultQueryModel<T>>> generateQueryModelFrom(
        final Field rootField,
        final Map<String, Object> variables,
        final Map<String, FragmentDefinition> fragmentDefinitions,
        final Class<T> entityType,
        final GraphQLSchema schema
    ) {
        final SelectionSet selectionSet = rootField.getSelectionSet();
        // convert selectionSet to concrete properties (their dot-notated names) with their arguments
        final Map<String, T2<List<GraphQLArgument>, List<Argument>>> propertiesAndArguments = concat(
            Stream.of(rootPropAndArguments(schema, rootField)), // "entity-itself" property (this can have some arguments, e.g. 'order')
            properties(entityType, null, toFields(selectionSet, fragmentDefinitions), fragmentDefinitions, schema)
        ).collect(toLinkedHashMap(t3 -> t3._1, t3 -> t2(t3._2, t3._3)));
        final List<QueryProperty> queryProperties = propertiesAndArguments.entrySet().stream()
            .filter(propertyAndArguments -> propertyAndArguments.getValue()._1.stream().anyMatch(FieldSchema::isQueryArgument)) // if GraphQL argument definitions contain at least one query argument definition ...
            .map(propertyAndArguments -> createQueryProperty( // ... create query properties based on them
                entityType,
                propertyAndArguments.getKey(),
                propertyAndArguments.getValue(),
                variables,
                schema.getCodeRegistry()
            ))
            .collect(toList());
        final List<Pair<String, Ordering>> orderingProperties = propertiesAndArguments.entrySet().stream()
            .filter(propertyAndArguments -> propertyAndArguments.getValue()._1.contains(ORDER_ARGUMENT)) // if GraphQL argument definitions contain ORDER_ARGUMENT ...
            .map(propertyAndArguments -> createOrderingProperty( // ... create ordering properties based on them
                propertyAndArguments.getKey(),
                propertyAndArguments.getValue(),
                variables,
                schema.getCodeRegistry()
            ))
            .flatMap(orderingProperty -> orderingProperty.isPresent() ? Stream.of(orderingProperty.get()) : Stream.empty()) // exclude empty values
            .sorted((p1, p2) -> p1._3.compareTo(p2._3)) // sort by ordering priority
            .map(prop -> pair(prop._1, prop._2)) // get (name; Ordering) only -- without priority 
            .collect(toList()); // make list -- order is important
        return dates -> from(createQuery(entityType, queryProperties, dates).model())
            .with(fetchNotInstrumented(entityType).with(propertiesAndArguments.keySet()).fetchModel())
            .with(createOrderingModel(entityType, orderingProperties))
            .model();
    }
    
    /**
     * Returns non-empty {@link QueryProperty} instance representing criterion for <code>property</code> in <code>entityType</code>.
     * Whether to disregard returned criterion is the responsibility of {@link DynamicQueryBuilder} logic.
     * 
     * @param entityType
     * @param property
     * @param arguments -- pair of {@link GraphQLArgument} definitions and corresponding resolved {@link Argument} instances (which contain actual values)
     * @param variables -- existing variable values by names in the query
     * @param codeRegistry -- code registry that is used only to take care of field visibility during {@link ValuesResolver#getArgumentValues(List, List, Map)} conversion
     * 
     * @return
     */
    private static <T extends AbstractEntity<?>> QueryProperty createQueryProperty(
        final Class<T> entityType,
        final String property,
        final T2<List<GraphQLArgument>, List<Argument>> arguments,
        final Map<String, Object> variables,
        final GraphQLCodeRegistry codeRegistry
    ) {
        final QueryProperty queryProperty = createEmptyQueryProperty(entityType, property);
        final Class<?> type = queryProperty.getType();
        
        // The following @Internal API (ValuesResolver) is used for argument value resolving.
        // It is not really clear why this API is @Internal though.
        // Surely ValuesResolver is used when validating argument values and returning the result of validation to the user.
        // But graphql-java has not exposed this as a public API for client implementations.
        // We argue that values resolving logic is error-prone and must follow standard guidelines from ValuesResolver.
        // These guidelines include a) resolving from argument literals b) resolving from raw variable values c) scalar values coercion etc.
        // Please follow these guidelines even if ValuesResolver will be made even more private, however this is unlikely scenario.
        final Map<String, Object> argumentValues = new ValuesResolver().getArgumentValues(codeRegistry, arguments._1, arguments._2, variables);
        
        if (isString(type)) {
            ofNullable(argumentValues.get(LIKE)).ifPresent(value -> {
                queryProperty.setValue(value);
            });
        } else if (isBoolean(type)) {
            ofNullable(argumentValues.get(VALUE)).ifPresent(value -> {
                if ((boolean) value) { // default empty values are 'true' for both 'value' and 'value2'
                    queryProperty.setValue2(false);
                } else {
                    queryProperty.setValue(false);
                }
            });
        } else if (Integer.class.isAssignableFrom(type)
            || Long.class.isAssignableFrom(type)
            || BigDecimal.class.isAssignableFrom(type)
            || Money.class.isAssignableFrom(type)
            || Date.class.isAssignableFrom(type)
        ) {
            ofNullable(argumentValues.get(FROM)).ifPresent(value -> {
                queryProperty.setValue(value);
            });
            ofNullable(argumentValues.get(TO)).ifPresent(value -> {
                queryProperty.setValue2(value);
            });
        }
        return queryProperty;
    }
    
    /**
     * Returns {@link Optional} tuple representing ordering <code>property</code> in <code>entityType</code>: dot-notation name, {@link Ordering} and number (priority).
     * Returns {@link Optional#empty()} if there is no ordering.
     * 
     * @param property
     * @param arguments -- pair of {@link GraphQLArgument} definitions and corresponding resolved {@link Argument} instances (which contain actual values)
     * @param variables -- existing variable values by names in the query
     * @param codeRegistry -- code registry that is used only to take care of field visibility during {@link ValuesResolver#getArgumentValues(List, List, Map)} conversion
     * 
     * @return
     */
    private static <T extends AbstractEntity<?>> Optional<T3<String, Ordering, Byte>> createOrderingProperty(
        final String property,
        final T2<List<GraphQLArgument>, List<Argument>> arguments,
        final Map<String, Object> variables,
        final GraphQLCodeRegistry codeRegistry
    ) {
        // The following @Internal API (ValuesResolver) is used for argument value resolving.
        // It is not really clear why this API is @Internal though.
        // Surely ValuesResolver is used when validating argument values and returning the result of validation to the user.
        // But graphql-java has not exposed this as a public API for client implementations.
        // We argue that values resolving logic is error-prone and must follow standard guidelines from ValuesResolver.
        // These guidelines include a) resolving from argument literals b) resolving from raw variable values c) scalar values coercion etc.
        // Please follow these guidelines even if ValuesResolver will be made even more private, however this is unlikely scenario.
        final Map<String, Object> argumentValues = new ValuesResolver().getArgumentValues(codeRegistry, arguments._1, arguments._2, variables);
        
        return ofNullable(argumentValues.get(ORDER))
            .map(val -> {
                final String str = (String) val; // "ASC_1", "DESC_1", "ASC_2", "DESC_2" and so on
                final byte priority = valueOf(str.substring(str.length() - 1));
                return t3(property, "ASC".equals(str.substring(0, str.length() - 2)) ? ASCENDING : DESCENDING, priority);
            });
    }
    
    /**
     * Returns {@link Optional} integer representing custom value for <code>what</code>.
     * Returns {@link Optional#empty()} if there is no custom value.
     * 
     * @param what
     * @param arguments -- pair of {@link GraphQLArgument} definitions and corresponding resolved {@link Argument} instances (which contain actual values)
     * @param variables -- existing variable values by names in the query
     * @param codeRegistry -- code registry that is used only to take care of field visibility during {@link ValuesResolver#getArgumentValues(List, List, Map)} conversion
     * @param significantLimit
     * 
     * @return
     */
    static <T extends AbstractEntity<?>> Optional<Integer> extractValue(
        final String what,
        final T2<List<GraphQLArgument>, List<Argument>> arguments,
        final Map<String, Object> variables,
        final GraphQLCodeRegistry codeRegistry,
        final int significantLimit
    ) {
        // The following @Internal API (ValuesResolver) is used for argument value resolving.
        // It is not really clear why this API is @Internal though.
        // Surely ValuesResolver is used when validating argument values and returning the result of validation to the user.
        // But graphql-java has not exposed this as a public API for client implementations.
        // We argue that values resolving logic is error-prone and must follow standard guidelines from ValuesResolver.
        // These guidelines include a) resolving from argument literals b) resolving from raw variable values c) scalar values coercion etc.
        // Please follow these guidelines even if ValuesResolver will be made even more private, however this is unlikely scenario.
        final Map<String, Object> argumentValues = new ValuesResolver().getArgumentValues(codeRegistry, arguments._1, arguments._2, variables);
        
        return ofNullable(argumentValues.get(what)).map(val -> (int) val).filter(val -> val >= significantLimit); // value less than significantLimit will be ignored
    }
    
    /**
     * Creates stream of dot-notated property names with their lists of argumentDefinitions / arguments.
     * 
     * @param entityType -- type in which we process its selected <code>graphQLFields</code>
     * @param prefix -- path to the <code>entityType</code> from its root
     * @param graphQLFields
     * @param fragmentDefinitions -- definitions of named fragments to extract concrete field selections from fragment spreads
     * @param schema -- GraphQL schema needed to extract argument definitions
     * @return
     */
    private static Stream<T3<String, List<GraphQLArgument>, List<Argument>>> properties(
        final Class<?> entityType,
        final String prefix,
        final List<Field> graphQLFields,
        final Map<String, FragmentDefinition> fragmentDefinitions,
        final GraphQLSchema schema
    ) {
        return graphQLFields.stream().flatMap(graphQLField -> { // flatten resultant stream of prop+arguments derived from 'graphQLField'
            final String property = prefix == null ? graphQLField.getName() : prefix + "." + graphQLField.getName(); // 'property' has dot-notated property name from root entity type to currently selected 'graphQLField'
            final String entityTypeName = entityType.getSimpleName();
            return concat( // concatenate two streams: ...
                of(propAndArgumentsFrom(schema, graphQLField, property, entityTypeName)), // ... first is single-element stream containing 'graphQLField' property itself and ...
                properties( // ... second contains all selected sub-fields of 'graphQLField'
                    determinePropertyType(entityType, graphQLField.getName()),
                    property,
                    toFields(graphQLField.getSelectionSet(), fragmentDefinitions),
                    fragmentDefinitions,
                    schema
                )
            );
        });
    }
    
    /**
     * Creates tuple of Query root property with its argument definitions and actual arguments.
     * 
     * @param schema -- GraphQL schema needed to extract argument definitions
     * @param graphQLField -- field instance with actual arguments
     * @return
     */
    static T3<String, List<GraphQLArgument>, List<Argument>> rootPropAndArguments(final GraphQLSchema schema, final Field graphQLField) {
        return propAndArgumentsFrom(schema, graphQLField, "", QUERY_TYPE_NAME);
    }
    
    /**
     * Creates tuple of: <code>property</code>, its argument definitions and actual arguments.
     * 
     * @param schema -- GraphQL schema needed to extract argument definitions
     * @param graphQLField -- field instance with actual arguments
     * @param property
     * @param parentTypeName -- name of parent GraphQL type that contains <code>graphQLField</code>
     * @return
     */
    private static T3<String, List<GraphQLArgument>, List<Argument>> propAndArgumentsFrom(final GraphQLSchema schema, final Field graphQLField, final String property, final String parentTypeName) {
        return t3(
            property,
            schema.getObjectType(parentTypeName).getFieldDefinition(graphQLField.getName()).getArguments(), // argument definitions
            graphQLField.getArguments() // arguments with actual values
        );
    }
    
    /**
     * Converts {@link SelectionSet} instance to a list of first-level fields.
     * <p>
     * This method also handles "fragment spreads" and "inline fragments" converting them to list of concrete fields.
     * This requires access to external <code>fragmentDefinitions</code>.
     * 
     * @param selectionSet
     * @param fragmentDefinitions
     * @return
     */
    private static List<Field> toFields(final SelectionSet selectionSet, final Map<String, FragmentDefinition> fragmentDefinitions) {
        final List<Field> selectionFields = new ArrayList<>();
        if (selectionSet != null) {
            for (final Selection selection: selectionSet.getSelections()) {
                if (selection instanceof Field) {
                    selectionFields.add((Field) selection);
                } else if (selection instanceof FragmentSpread) {
                    final FragmentSpread fragmentSpread = (FragmentSpread) selection;
                    final FragmentDefinition fragmentDefinition = fragmentDefinitions.get(fragmentSpread.getName());
                    selectionFields.addAll(toFields(fragmentDefinition.getSelectionSet(), fragmentDefinitions));
                } else if (selection instanceof InlineFragment) {
                    final InlineFragment inlineFragment = (InlineFragment) selection;
                    selectionFields.addAll(toFields(inlineFragment.getSelectionSet(), fragmentDefinitions));
                } else {
                    // this is the only three types of possible selections; log warning if something else appeared
                    LOGGER.warn(format("Unknown Selection [%s] has appeared.", Objects.toString(selection))); // 'null' selection is possible
                }
            }
        }
        return selectionFields;
    }
    
}