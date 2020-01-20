package ua.com.fielden.platform.web_api;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createQuery;
import static ua.com.fielden.platform.utils.EntityUtils.fetchNotInstrumented;
import static ua.com.fielden.platform.utils.EntityUtils.isBoolean;
import static ua.com.fielden.platform.utils.EntityUtils.isString;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.log4j.Logger;

import graphql.language.Argument;
import graphql.language.BooleanValue;
import graphql.language.Field;
import graphql.language.FloatValue;
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.language.VariableReference;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;

/**
 * Contains querying utility methods for root fields in GraphQL query / mutation schemas.
 * 
 * @author TG Team
 *
 */
public class RootEntityMixin {
    private static final Logger LOGGER = Logger.getLogger(RootEntityMixin.class);
    
    /**
     * Generates EQL query execution model for retrieving <code>selectionSet</code> of fields inside root selection of GraphQL query or mutation.
     * 
     * @param selectionSet
     * @param variables -- existing variable values by names in the query; they can be used in <code>selectionSet</code>
     * @param fragmentDefinitions -- fragment definitions by names in the query; <code>selectionSet</code> can contain fragment spreads based on that definitions
     * @param entityType
     * @param additionalQueryProperties -- additional external constraints to be provided into EQL query execution model
     * @return
     */
    public static <T extends AbstractEntity<?>> QueryExecutionModel<T, EntityResultQueryModel<T>> generateQueryModelFrom(final SelectionSet selectionSet, final Map<String, Object> variables, final Map<String, FragmentDefinition> fragmentDefinitions, final Class<T> entityType, final QueryProperty... additionalQueryProperties) {
        // convert selectionSet to concrete properties (their dot-notated names) with their arguments
        final LinkedHashMap<String, List<Argument>> propertiesAndArguments = properties(null, toFields(selectionSet, fragmentDefinitions), fragmentDefinitions);
        
        // add custom QueryProperty instances to resultant ones
        final Map<String, QueryProperty> queryProperties = new LinkedHashMap<>();
        asList(additionalQueryProperties).stream().forEach(queryProperty -> {
            queryProperties.put(queryProperty.getPropertyName(), queryProperty);
        });
        
        // add query properties based on GraphQL arguments if they are not empty
        for (final Map.Entry<String, List<Argument>> propertyAndArguments: propertiesAndArguments.entrySet()) {
            final String propertyName = propertyAndArguments.getKey();
            final List<Argument> propertyArguments = propertyAndArguments.getValue();
            if (!propertyArguments.isEmpty()) {
                final QueryProperty queryProperty = createQueryProperty(entityType, propertyName, propertyArguments, variables);
                queryProperties.put(queryProperty.getPropertyName(), queryProperty);
            }
        }
        
        final EntityResultQueryModel<T> eqlQuery = createQuery(entityType, new ArrayList<>(queryProperties.values())).model();
        return from(eqlQuery).with(fetchNotInstrumented(entityType).with(propertiesAndArguments.keySet()).fetchModel()).model(); // TODO fetch order etc.
    }
    
    /**
     * Returns non-empty {@link QueryProperty} instance representing criterion for <code>property</code> in type <code>entityType</code>.
     * Whether to disregard returned criterion is the responsibility of {@link DynamicQueryBuilder} logic.
     * 
     * @param entityType
     * @param property
     * @param args
     * @param varsByName
     * @return
     */
    private static <T extends AbstractEntity<?>> QueryProperty createQueryProperty(final Class<T> entityType, final String property, final List<Argument> args, final Map<String, Object> varsByName) {
        final QueryProperty queryProperty = new QueryProperty(entityType, property).makeEmpty();
        final Class<?> type = queryProperty.getType();
        final Map<String, Argument> argsByName = args.stream().collect(toMap(val -> val.getName(), identity()));
        if (isString(type)) {
            ofNullable(argsByName.get("like")).ifPresent(like -> resolveValue(like.getValue(), varsByName).ifPresent(value -> {
                queryProperty.setValue(value);
            }));
        } else if (isBoolean(type)) {
            ofNullable(argsByName.get("value")).ifPresent(value -> resolveValue(value.getValue(), varsByName).ifPresent(v -> {
                final boolean boolValue = (boolean) v;
                if (boolValue) { // default empty values are 'true' for both 'value' and 'value2'
                    queryProperty.setValue2(false);
                } else {
                    queryProperty.setValue(false);
                }
            }));
        } else if (Integer.class.isAssignableFrom(type)) {
            ofNullable(argsByName.get("from")).ifPresent(from -> resolveValue(from.getValue(), varsByName).ifPresent(value -> {
                queryProperty.setValue(((BigInteger) value).intValue()); // narrowing conversion (for big numbers), but should be sufficient in most cases; otherwise be silent and don't blow up the thread here
            }));
            ofNullable(argsByName.get("to")).ifPresent(to -> resolveValue(to.getValue(), varsByName).ifPresent(value -> {
                queryProperty.setValue2(((BigInteger) value).intValue()); // narrowing conversion (for big numbers), but should be sufficient in most cases; otherwise be silent and don't blow up the thread here
            }));
        }
        return queryProperty;
    }
    
    private static Optional<Object> resolveValue(final Value valueOrVariable, final Map<String, Object> variables) {
        if (valueOrVariable instanceof VariableReference) {
            final VariableReference variableReference = (VariableReference) valueOrVariable;
            final String variableName = variableReference.getName();
            if (variables.containsKey(variableName)) {
                return ofNullable(variables.get(variableName)); // TODO here the values are not in exact correspondence to types below; need to use exact possible types to avoid conversion errors for variable values (Integer vs BigInteger)
            } else {
                // no criterion exists for this property argument!
                return empty();
            }
        } else if (valueOrVariable instanceof BooleanValue) {
            final BooleanValue booleanValue = (BooleanValue) valueOrVariable;
            return of(booleanValue.isValue());
        } else if (valueOrVariable instanceof StringValue) {
            final StringValue stringValue = (StringValue) valueOrVariable;
            return of(stringValue.getValue());
        } else if (valueOrVariable instanceof FloatValue) {
            final FloatValue floatValue = (FloatValue) valueOrVariable;
            return of(floatValue.getValue());
        } else if (valueOrVariable instanceof IntValue) {
            final IntValue intValue = (IntValue) valueOrVariable;
            return of(intValue.getValue());
        } else if (valueOrVariable instanceof NullValue) {
            return empty();
        } else {
            // return empty value for other unsupported cases (ArrayValue, EnumValue, ObjectValue -- not used)
            return empty();
        }
    }
    
    private static LinkedHashMap<String, List<Argument>> properties(final String prefix, final List<Field> graphQLFields, final Map<String, FragmentDefinition> fragmentDefinitions) {
        final LinkedHashMap<String, List<Argument>> properties = new LinkedHashMap<>();
        for (final Field graphQLField: graphQLFields) {
            final List<Argument> args = graphQLField.getArguments();
            final String property = prefix == null ? graphQLField.getName() : prefix + "." + graphQLField.getName();
            properties.put(property, args);
            
            properties.putAll(properties(property, toFields(graphQLField.getSelectionSet(), fragmentDefinitions), fragmentDefinitions));
        }
        LOGGER.error(String.format("\tFetching props [%s]", properties));
        return properties;
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