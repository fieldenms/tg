package ua.com.fielden.platform.web_api;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.execution.ValuesResolver;
import graphql.language.*;
import graphql.schema.*;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicPropertyAnalyser;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.util.*;
import java.util.stream.Stream;

import static graphql.execution.CoercedVariables.of;
import static graphql.execution.ValuesResolver.getArgumentValues;
import static java.lang.Byte.valueOf;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNullElse;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.concat;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.ASCENDING;
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.DESCENDING;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createConditionProperty;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createJoinCondition;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.streaming.ValueCollectors.toLinkedHashMap;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.EntityUtils.fetchNotInstrumentedWithKeyAndDesc;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.web_api.FieldSchema.ORDER;
import static ua.com.fielden.platform.web_api.FieldSchema.ORDER_ARGUMENT;

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

    public static final String WARN_ORDER_PRIORITIES_ARE_NOT_DISTINCT = "Order priorities are not distinct.";

    private static final Logger LOGGER = getLogger(RootEntityUtils.class);

    /// Generates an EQL query execution model for retrieving the root field of `environment` and its selection set, together with an optional warning about ordering.
    ///
    public static <T extends AbstractEntity<?>> T2<Optional<String>, QueryExecutionModel<T, EntityResultQueryModel<T>>> generateQueryModelFrom(
            final DataFetchingEnvironment environment,
            final Class<T> entityType)
    {
        final var rootField           = environment.getField();
        final var variables           = environment.getVariables();
        final var fragmentDefinitions = environment.getFragmentsByName();
        final var schema              = environment.getGraphQLSchema();
        final var context             = environment.getGraphQlContext();
        final var locale              = environment.getLocale();

        validateDuplicateFields(rootField.getSelectionSet(), fragmentDefinitions);

        // convert selectionSet to concrete properties (their dot-notated names) with their arguments
        final Map<String, T2<List<GraphQLArgument>, List<Argument>>> propertiesAndArguments = concat(
            Stream.of(rootPropAndArguments(schema, rootField)), // Root entity field can have arguments, e.g. `order`.
            properties(entityType, null, toFields(rootField.getSelectionSet(), fragmentDefinitions), fragmentDefinitions, schema))
            // Discard duplicate fields under different aliases -- first one wins.
            .collect(toLinkedHashMap(t3 -> t3._1, t3 -> t2(t3._2, t3._3)));

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
        return t2(optionalWarning, from(createJoinCondition(entityType).where().condition(EntityCondToEqlCompiler.compile(environment))
                                                 .model()
                                                 .setFilterable(true)) // must be filterable to support IFilter part of the model
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
            .lightweight() // must be lightweight to avoid fetching instrumented entities
            .model());
    }

    /// Validates selected fields to ensure that there are no identical fields selected under different aliases.
    /// Processing of the selection set expands any fragments into their contents.
    ///
    /// @throws WebApiException  if validation is unsuccessful
    ///
    public static void validateDuplicateFields(final @Nullable SelectionSet selectionSet, final Map<String, FragmentDefinition> fragmentDefinitions) {
        final var fields = toFields(selectionSet, fragmentDefinitions);
        fields.stream()
                .sorted(comparing(Field::getName))
                .collect(groupingBy(Field::getName, LinkedHashMap::new, toList()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .findFirst()
                .ifPresent(entry -> {
                    throw new WebApiException("Selected non-root fields must be unique. Duplicate field [%s] under aliases: %s.".formatted(
                            entry.getKey(),
                            entry.getValue().stream().map(field -> requireNonNullElse(field.getAlias(), "<none>")).collect(joining(", "))));
                });
        fields.forEach(f -> validateDuplicateFields(f.getSelectionSet(), fragmentDefinitions));
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
