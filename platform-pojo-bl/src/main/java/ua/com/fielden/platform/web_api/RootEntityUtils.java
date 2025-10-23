package ua.com.fielden.platform.web_api;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.execution.ValuesResolver;
import graphql.language.*;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchema;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicPropertyAnalyser;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static graphql.execution.CoercedVariables.of;
import static graphql.execution.ValuesResolver.getArgumentValues;
import static java.lang.Byte.valueOf;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.ASCENDING;
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.DESCENDING;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity_centre.review.DynamicParamBuilder.getPropertyValues;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty.createEmptyQueryProperty;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createConditionProperty;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createQuery;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.streaming.ValueCollectors.toLinkedHashMap;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.web_api.FieldSchema.*;

/**
 * Contains querying utility methods for root fields in GraphQL query / mutation schemas.
 * 
 * @author TG Team
 *
 */
public class RootEntityUtils {
    /**
     * The name for built-in data introspection field returning the name of actual data object type in runtime.<br>
     * This is a part of GraphQL spec.
     */
    private static final String __TYPENAME = "__typename";
    static final String QUERY_TYPE_NAME = "Query";

    public static final String ERR_EQ_AND_LIKE_ARE_MUTUALLY_EXCLUSIVE = "Conditions `eq` and `like` are mutually exclusive. Please remove one or both conditions.";
    public static final String ERR_EQ_DOES_NOT_PERMIT_WILDCARDS = "Value for `eq` should not contain wildcard symbols (`*`).";
    public static final String WARN_ORDER_PRIORITIES_ARE_NOT_DISTINCT = "Order priorities are not distinct.";

    private static final Logger LOGGER = getLogger(RootEntityUtils.class);

    /**
     * Returns function for generation of EQL query execution model for retrieving {@code rootField} and its selection set in GraphQL query or mutation [and optional warning about ordering].
     * The argument of function is {@link IDates} instance from which 'now' moment can properly be retrieved and used for date property filtering.
     * 
     * @param rootField -- root field for GraphQL query or mutation
     * @param variables -- existing coerced variable values by names in the query; they can be used in {@code rootField.selectionSet}
     * @param fragmentDefinitions -- fragment definitions by names in the query; {@code rootField.selectionSet} can contain fragment spreads based on that definitions
     * @param entityType
     * @param schema -- GraphQL schema to assist with resolving of argument values
     * @param context -- context in current data fetching request
     * @param locale -- locale in current data fetching request
     * @return
     */
    public static <T extends AbstractEntity<?>> Function<IDates, T2<Optional<String>, QueryExecutionModel<T, EntityResultQueryModel<T>>>> generateQueryModelFrom(
        final Field rootField,
        final Map<String, Object> variables,
        final Map<String, FragmentDefinition> fragmentDefinitions,
        final Class<T> entityType,
        final GraphQLSchema schema,
        final GraphQLContext context,
        final Locale locale
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
                schema.getCodeRegistry(),
                context,
                locale
            ))
            .collect(toList());
        final List<T3<String, Ordering, Byte>> propOrderingWithPriorities = propertiesAndArguments.entrySet().stream()
            .filter(propertyAndArguments -> propertyAndArguments.getValue()._1.contains(ORDER_ARGUMENT)) // if GraphQL argument definitions contain ORDER_ARGUMENT ...
            .map(propertyAndArguments -> createOrderingProperty( // ... create ordering properties based on them
                propertyAndArguments.getKey(),
                propertyAndArguments.getValue(),
                variables,
                schema.getCodeRegistry(),
                context,
                locale
            ))
            .flatMap(orderingProperty -> orderingProperty.isPresent() ? Stream.of(orderingProperty.get()) : Stream.empty())
            .toList(); // exclude empty values
        final Optional<String> optionalWarning = propOrderingWithPriorities.stream().map(t3 -> t3._3).distinct().count() < propOrderingWithPriorities.size() ? of(WARN_ORDER_PRIORITIES_ARE_NOT_DISTINCT) : empty(); // in case where order priorities are not distinct, return non-intrusive warning (with data still present)
        final List<Pair<String, Ordering>> specifiedOrderingProperties = propOrderingWithPriorities.stream()
            .sorted((p1, p2) -> p1._3.compareTo(p2._3)) // sort by ordering priority
            .map(prop -> pair(prop._1, prop._2)) // get (name; Ordering) only -- without priority 
            .collect(toList()); // make list -- order is important
        final List<Pair<String, Ordering>> orderingProperties = specifiedOrderingProperties.isEmpty() ? asList(pair("", ASCENDING)) : specifiedOrderingProperties; // ordering by default: ascending by keys
        final Iterator<Pair<String, Ordering>> orderingPropertiesIterator = orderingProperties.iterator();
        return dates -> t2(optionalWarning, from(createQuery(entityType, queryProperties, dates).model().setFilterable(true)) // must be filterable to support IFilter part of the model
            .with(fetchNotInstrumentedWithKeyAndDesc(entityType) // KEY_AND_DESC strategy for root entities is required for loading collectional associations linked through key of root entity; explicit fetching of all keys in GraphQL query does not always work
                .with(propertiesAndArguments.keySet().stream()
                    .filter(name -> !name.endsWith(__TYPENAME)) // do not include built-in data introspection __typename field (possibly dot-notated) as it does not exist in TG entities; resolving of this field is governed by graphql-java internal logic
                    .collect(toSet())
                ).fetchModel()
            )
            .with(orderingModelFrom(
                appendPropertyOrdering(orderBy(), orderingPropertiesIterator.next(), entityType), // there is at least one item in the iterator
                orderingPropertiesIterator,
                entityType
            ).model())
            .with(queryProperties.stream() // add params from crit-only property values
                .filter(QueryProperty::isCritOnly)
                .map(qp -> getPropertyValues(qp, mapOf(t2(qp.getPropertyName(), pair(qp.getValue(), qp.getValue2()))).entrySet().iterator().next()))
                .reduce(new HashMap<>(), (accumulator, propValues) -> {
                    accumulator.putAll(propValues);
                    return accumulator;
                })
            )
            .lightweight() // must be lightweight to avoid fetching instrumented entities
            .model());
    }
    
    /**
     * Iterates through {@code iterator} of [property; ordering] pairs and enhances {@code accumulator} (accumulated ordering model) with corresponding ordering.
     * 
     * @param accumulator
     * @param iterator
     * @param entityType -- root entity type
     * @return
     */
    private static <T extends AbstractEntity<?>> IOrderingItemCloseable orderingModelFrom(final IOrderingItemCloseable accumulator, final Iterator<Pair<String, Ordering>> iterator, final Class<T> entityType) {
        return !iterator.hasNext() ? accumulator : orderingModelFrom(appendPropertyOrdering(accumulator, iterator.next(), entityType), iterator, entityType);
    }
    
    /**
     * Appends {@code partialOrderingModel} with corresponding ordering from [property; ordering] pair ({@code propertyOrdering}).
     * 
     * @param partialOrderingModel
     * @param propertyOrdering
     * @param entityType -- root entity type
     * @return
     */
    private static <T extends AbstractEntity<?>> IOrderingItemCloseable appendPropertyOrdering(final IOrderingItem partialOrderingModel, final Pair<String, Ordering> propertyOrdering, final Class<T> entityType) {
        // we use '.prop(' EQL ordering instead of '.yield(' -- ordering by yield is only required for ad-hoc calculated properties that are not supported in Web API yet
        final EntityQueryProgressiveInterfaces.StandaloneOrderBy.ISingleOperandOrderable part = partialOrderingModel.prop(createConditionProperty(new DynamicPropertyAnalyser(entityType, propertyOrdering.getKey()).getCriteriaFullName())); // createConditionProperty is required because of the need to have property prepended with alias
        return ASCENDING.equals(propertyOrdering.getValue()) ? part.asc() : part.desc();
    }
    
    /**
     * Returns non-empty {@link QueryProperty} instance representing criterion for {@code property} in {@code entityType}.
     * Whether to disregard returned criterion is the responsibility of {@link DynamicQueryBuilder} logic.
     * 
     * @param entityType
     * @param property
     * @param arguments -- pair of {@link GraphQLArgument} definitions and corresponding resolved {@link Argument} instances (which contain actual values)
     * @param variables -- existing coerced variable values by names in the query
     * @param codeRegistry -- code registry that is used only to take care of field visibility during {@link ValuesResolver#getArgumentValues(GraphQLCodeRegistry, List, List, CoercedVariables, GraphQLContext, Locale)} conversion
     * @param context -- context in current data fetching request
     * @param locale -- locale in current data fetching request
     * 
     * @return
     */
    private static <T extends AbstractEntity<?>> QueryProperty createQueryProperty(
        final Class<T> entityType,
        final String property,
        final T2<List<GraphQLArgument>, List<Argument>> arguments,
        final Map<String, Object> variables,
        final GraphQLCodeRegistry codeRegistry,
        final GraphQLContext context,
        final Locale locale
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
        final Map<String, Object> argumentValues = getArgumentValues(codeRegistry, arguments._1, arguments._2, of(variables), context, locale);

        if (isString(type) || isEntityType(type)) {
            if (argumentValues.get(EQ) != null && argumentValues.get(LIKE) != null) {
                throw new WebApiException(ERR_EQ_AND_LIKE_ARE_MUTUALLY_EXCLUSIVE);
            }

            if (argumentValues.get(EQ) != null) {
                final var searchValue = (String) argumentValues.get(EQ);
                if (searchValue.contains("*")) {
                    throw new WebApiException(ERR_EQ_DOES_NOT_PERMIT_WILDCARDS);
                }
                queryProperty.setValue(searchValue);
                queryProperty.setSingle(true); // a search value should only be recognised as representing a single value (i.e. not comma separated).
                queryProperty.setMatchAnywhere(false); // match exactly
            }
            else if (argumentValues.get(LIKE) != null) {
                final var searchValue = (String) argumentValues.get(LIKE);
                // The searchValue must be of type String for string-typed criteria even if it represents comma separated values.
                // However, for entity-typed criteria, comma separated values need to be... separated, and represented as a list.
                queryProperty.setValue(queryProperty.isSingle() || isString(type) ? searchValue : asList(searchValue.split(",")));
                queryProperty.setMatchAnywhere(isString(type)); // match anywhere only applies to string-typed criteria
            }
        }
        else if (isBoolean(type)) {
            ofNullable(argumentValues.get(VALUE)).ifPresent(value -> {
                if ((boolean) value) { // default empty values are 'true' for both 'value' and 'value2'
                    queryProperty.setValue2(false);
                } else {
                    queryProperty.setValue(false);
                }
            });
        }
        else if (Integer.class.isAssignableFrom(type)
            || Long.class.isAssignableFrom(type)
            || BigDecimal.class.isAssignableFrom(type)
            || Money.class.isAssignableFrom(type)
            || Date.class.isAssignableFrom(type)
        ) {
            ofNullable(argumentValues.get(FROM)).ifPresent(queryProperty::setValue);
            ofNullable(argumentValues.get(TO)).ifPresent(queryProperty::setValue2);
        }
        return queryProperty;
    }
    
    /**
     * Returns {@link Optional} tuple representing ordering {@code property} in {@code entityType}: dot-notation name, {@link Ordering} and number (priority).
     * Returns {@link Optional#empty()} if there is no ordering.
     * 
     * @param property
     * @param arguments -- pair of {@link GraphQLArgument} definitions and corresponding resolved {@link Argument} instances (which contain actual values)
     * @param variables -- existing coerced variable values by names in the query
     * @param codeRegistry -- code registry that is used only to take care of field visibility during {@link ValuesResolver#getArgumentValues(GraphQLCodeRegistry, List, List, CoercedVariables, GraphQLContext, Locale)} conversion
     * @param context -- context in current data fetching request
     * @param locale -- locale in current data fetching request
     * 
     * @return
     */
    private static <T extends AbstractEntity<?>> Optional<T3<String, Ordering, Byte>> createOrderingProperty(
        final String property,
        final T2<List<GraphQLArgument>, List<Argument>> arguments,
        final Map<String, Object> variables,
        final GraphQLCodeRegistry codeRegistry,
        final GraphQLContext context,
        final Locale locale
    ) {
        // The following @Internal API (ValuesResolver) is used for argument value resolving.
        // It is not really clear why this API is @Internal though.
        // Surely ValuesResolver is used when validating argument values and returning the result of validation to the user.
        // But graphql-java has not exposed this as a public API for client implementations.
        // We argue that values resolving logic is error-prone and must follow standard guidelines from ValuesResolver.
        // These guidelines include a) resolving from argument literals b) resolving from raw variable values c) scalar values coercion etc.
        // Please follow these guidelines even if ValuesResolver will be made even more private, however this is unlikely scenario.
        final Map<String, Object> argumentValues = getArgumentValues(codeRegistry, arguments._1, arguments._2, of(variables), context, locale);
        
        return ofNullable(argumentValues.get(ORDER))
            .map(val -> {
                final String str = (String) val; // "ASC_1", "DESC_1", "ASC_2", "DESC_2" and so on
                final byte priority = valueOf(str.substring(str.length() - 1));
                return t3(property, "ASC".equals(str.substring(0, str.length() - 2)) ? ASCENDING : DESCENDING, priority);
            });
    }
    
    /**
     * Returns {@link Optional} integer representing custom value for {@code what}.
     * Returns {@link Optional#empty()} if there is no custom value.
     * 
     * @param what
     * @param arguments -- pair of {@link GraphQLArgument} definitions and corresponding resolved {@link Argument} instances (which contain actual values)
     * @param variables -- existing coerced variable values by names in the query
     * @param codeRegistry -- code registry that is used only to take care of field visibility during {@link ValuesResolver#getArgumentValues(GraphQLCodeRegistry, List, List, CoercedVariables, GraphQLContext, Locale)} conversion
     * @param context -- context in current data fetching request
     * @param locale -- locale in current data fetching request
     * @param significantLimit
     * 
     * @return
     */
    static <T extends AbstractEntity<?>> Optional<Integer> extractValue(
        final String what,
        final T2<List<GraphQLArgument>, List<Argument>> arguments,
        final Map<String, Object> variables,
        final GraphQLCodeRegistry codeRegistry,
        final GraphQLContext context,
        final Locale locale,
        final int significantLimit
    ) {
        // The following @Internal API (ValuesResolver) is used for argument value resolving.
        // It is not really clear why this API is @Internal though.
        // Surely ValuesResolver is used when validating argument values and returning the result of validation to the user.
        // But graphql-java has not exposed this as a public API for client implementations.
        // We argue that values resolving logic is error-prone and must follow standard guidelines from ValuesResolver.
        // These guidelines include a) resolving from argument literals b) resolving from raw variable values c) scalar values coercion etc.
        // Please follow these guidelines even if ValuesResolver will be made even more private, however this is unlikely scenario.
        final Map<String, Object> argumentValues = getArgumentValues(codeRegistry, arguments._1, arguments._2, of(variables), context, locale);
        
        return ofNullable(argumentValues.get(what)).map(val -> (int) val).filter(val -> val >= significantLimit); // value less than significantLimit will be ignored
    }
    
    /**
     * Creates stream of dot-notated property names with their lists of argumentDefinitions / arguments.
     * 
     * @param entityType -- type in which we process its selected {@code graphQLFields}
     * @param prefix -- path to the {@code entityType} from its root
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
                Stream.of(propAndArgumentsFrom(schema, graphQLField, property, entityTypeName)), // ... first is single-element stream containing 'graphQLField' property itself and ...
                properties( // ... second contains all selected sub-fields of 'graphQLField'
                    __TYPENAME.equals(graphQLField.getName()) ? String.class : determinePropertyType(entityType, graphQLField.getName()),
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
     * Creates tuple of: {@code property}, its argument definitions and actual arguments.
     * 
     * @param schema -- GraphQL schema needed to extract argument definitions
     * @param graphQLField -- field instance with actual arguments
     * @param property
     * @param parentTypeName -- name of parent GraphQL type that contains {@code graphQLField}
     * @return
     */
    private static T3<String, List<GraphQLArgument>, List<Argument>> propAndArgumentsFrom(final GraphQLSchema schema, final Field graphQLField, final String property, final String parentTypeName) {
        return t3(
            property,
            ofNullable(schema.getObjectType(parentTypeName).getFieldDefinition(graphQLField.getName())) // there can be no field definition, e.g. this is the case for built-in data introspection __typename field
                .map(GraphQLFieldDefinition::getArguments) // argument definitions
                .orElseGet(Collections::emptyList),
            graphQLField.getArguments() // arguments with actual values
        );
    }
    
    /**
     * Converts {@link SelectionSet} instance to a list of first-level fields.
     * <p>
     * This method also handles "fragment spreads" and "inline fragments" converting them to list of concrete fields.
     * This requires access to external {@code fragmentDefinitions}.
     * 
     * @param selectionSet
     * @param fragmentDefinitions
     * @return
     */
    private static List<Field> toFields(final SelectionSet selectionSet, final Map<String, FragmentDefinition> fragmentDefinitions) {
        final List<Field> selectionFields = new ArrayList<>();
        if (selectionSet != null) {
            for (final Selection<?> selection: selectionSet.getSelections()) {
                if (selection instanceof Field) {
                    selectionFields.add((Field) selection);
                } else if (selection instanceof final FragmentSpread fragmentSpread) {
                    final FragmentDefinition fragmentDefinition = fragmentDefinitions.get(fragmentSpread.getName());
                    selectionFields.addAll(toFields(fragmentDefinition.getSelectionSet(), fragmentDefinitions));
                } else if (selection instanceof final InlineFragment inlineFragment) {
                    selectionFields.addAll(toFields(inlineFragment.getSelectionSet(), fragmentDefinitions));
                } else {
                    // this is the only three types of possible selections; log warning if something else appeared
                    LOGGER.warn("Unknown Selection [{}] has appeared.", Objects.toString(selection)); // 'null' selection is possible
                }
            }
        }
        return selectionFields;
    }
    
}
