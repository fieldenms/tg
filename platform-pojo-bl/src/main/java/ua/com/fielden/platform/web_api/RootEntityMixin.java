package ua.com.fielden.platform.web_api;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createQuery;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determineClass;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.fetchNotInstrumented;
import static ua.com.fielden.platform.utils.EntityUtils.isBoolean;
import static ua.com.fielden.platform.utils.EntityUtils.isString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;

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
    public static <T extends AbstractEntity<?>> QueryExecutionModel<T, EntityResultQueryModel<T>> generateQueryModelFrom(final SelectionSet selectionSet, final Map<String, Object> variables, final Map<String, FragmentDefinition> fragmentDefinitions, final Class<T> entityType, final GraphQLSchema schema, final QueryProperty... additionalQueryProperties) {
        // convert selectionSet to concrete properties (their dot-notated names) with their arguments
        final LinkedHashMap<String, T2<List<GraphQLArgument>, List<Argument>>> propertiesAndArguments = properties(entityType, null, toFields(selectionSet, fragmentDefinitions), fragmentDefinitions, schema);
        
        // add custom QueryProperty instances to resultant ones
        final Map<String, QueryProperty> queryProperties = new LinkedHashMap<>();
        asList(additionalQueryProperties).stream().forEach(queryProperty -> {
            queryProperties.put(queryProperty.getPropertyName(), queryProperty);
        });
        
        // add query properties based on GraphQL arguments if they are not empty
        for (final Map.Entry<String, T2<List<GraphQLArgument>, List<Argument>>> propertyAndArguments: propertiesAndArguments.entrySet()) {
            final String propertyName = propertyAndArguments.getKey();
            final List<Argument> propertyArguments = propertyAndArguments.getValue()._2;
            if (!propertyAndArguments.getValue()._1.isEmpty()) {
                final QueryProperty queryProperty = createQueryProperty(entityType, propertyName, propertyArguments, variables, schema.getCodeRegistry(), propertyAndArguments.getValue()._1);
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
    private static <T extends AbstractEntity<?>> QueryProperty createQueryProperty(final Class<T> entityType, final String property, final List<Argument> args, final Map<String, Object> varsByName, final GraphQLCodeRegistry codeRegistry, final List<GraphQLArgument> argumentTypes) {
        final QueryProperty queryProperty = new QueryProperty(entityType, property).makeEmpty();
        final Class<?> type = queryProperty.getType();
        
        final Map<String, Object> argumentValues = new ValuesResolver().getArgumentValues(codeRegistry, argumentTypes, args, varsByName);
        final StringBuilder strBuilder = new StringBuilder("\nARGUMENTS:\n");
        argumentValues.entrySet().stream().forEach(entry -> strBuilder.append(String.format("%s: [%s] with type [%s]\n", entry.getKey(), entry.getValue(), entry.getValue() == null ? null : entry.getValue().getClass())));
        System.err.println(strBuilder.toString());
        
        if (isString(type)) {
            ofNullable(argumentValues.get("like")).ifPresent(value -> {
                queryProperty.setValue(value);
            });
        } else if (isBoolean(type)) {
            ofNullable(argumentValues.get("value")).ifPresent(value -> {
                final boolean boolValue = (boolean) value;
                if (boolValue) { // default empty values are 'true' for both 'value' and 'value2'
                    queryProperty.setValue2(false);
                } else {
                    queryProperty.setValue(false);
                }
            });
        } else if (
                Integer.class.isAssignableFrom(type)
            || Long.class.isAssignableFrom(type)
            || BigDecimal.class.isAssignableFrom(type)
            || Money.class.isAssignableFrom(type)
            || Date.class.isAssignableFrom(type)) {
            ofNullable(argumentValues.get("from")).ifPresent(value -> {
                queryProperty.setValue(value);
            });
            ofNullable(argumentValues.get("to")).ifPresent(value -> {
                queryProperty.setValue2(value);
            });
        }
        return queryProperty;
    }
    
    private static LinkedHashMap<String, T2<List<GraphQLArgument>, List<Argument>>> properties(final Class<?> entityType, final String prefix, final List<Field> graphQLFields, final Map<String, FragmentDefinition> fragmentDefinitions, final GraphQLSchema schema) {
        final LinkedHashMap<String, T2<List<GraphQLArgument>, List<Argument>>> properties = new LinkedHashMap<>();
        for (final Field graphQLField: graphQLFields) {
            final List<GraphQLArgument> argumentDefinitions = schema.getObjectType(entityType.getSimpleName()).getFieldDefinition(graphQLField.getName()).getArguments();
            final List<Argument> args = graphQLField.getArguments();
            final String property = prefix == null ? graphQLField.getName() : prefix + "." + graphQLField.getName();
            properties.put(property, t2(argumentDefinitions, args));
            
            properties.putAll(properties(determineClass(entityType, graphQLField.getName(), true, true), property, toFields(graphQLField.getSelectionSet(), fragmentDefinitions), fragmentDefinitions, schema));
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