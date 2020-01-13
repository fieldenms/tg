package ua.com.fielden.platform.web_api;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createQuery;
import static ua.com.fielden.platform.utils.EntityUtils.fetchNotInstrumented;

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
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.Value;
import graphql.language.VariableReference;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;

public class RootEntityMixin {
    private static final Logger LOGGER = Logger.getLogger(RootEntityMixin.class);
    
    public static QueryExecutionModel generateQueryModelFrom(final SelectionSet selectionSet, final Map<String, Object> variables, final Map<String, FragmentDefinition> fragmentDefinitions, final Class<? extends AbstractEntity<?>> entityType, final QueryProperty... additionalQueryProperties) {
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
                final QueryProperty queryProperty = new QueryProperty(entityType, propertyName);
                
                final Argument firstArgument = propertyArguments.get(0);
                // TODO handle other arguments
                
                // TODO check also firstArgument.getName();
                final Value valueOrVariable = firstArgument.getValue();
                LOGGER.error(String.format("\tArg value / variable reference [%s]", valueOrVariable));
                
                final Optional<Object> value = RootEntityMixin.resolveValue(valueOrVariable, variables);
                
                // TODO provide more type safety here 
                if (value.isPresent()) {
                    if (value.get() instanceof Boolean) {
                        if ((Boolean) value.get()) {
                            queryProperty.setValue(true);
                            queryProperty.setValue2(false);
                        } else {
                            queryProperty.setValue(false);
                            queryProperty.setValue2(true);
                        }
                        queryProperties.put(propertyName, queryProperty); // only add query property if some criteria has been applied
                    }
                }
            }
        }
        
        final EntityResultQueryModel eqlQuery = createQuery(entityType, new ArrayList<>(queryProperties.values())).model();
        return from(eqlQuery).with(fetchNotInstrumented(entityType).with(propertiesAndArguments.keySet()).fetchModel()).model();
    }

    private static Optional<Object> resolveValue(final Value valueOrVariable, final Map<String, Object> variables) {
        if (valueOrVariable instanceof VariableReference) {
            final VariableReference variableReference = (VariableReference) valueOrVariable;
            final String variableName = variableReference.getName();
            if (variables.containsKey(variableName)) {
                final Object variableValue = variables.get(variableName);
                return Optional.of(variableValue); // TODO how about 'null' value? 
            } else {
                // no criterion exists for this property argument!
                return Optional.empty();
            }
        } else if (valueOrVariable instanceof BooleanValue) {
            final BooleanValue booleanValue = (BooleanValue) valueOrVariable;
            return Optional.of(booleanValue.isValue());
        } else {
            // TODO implement other cases
            return Optional.of(valueOrVariable);
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