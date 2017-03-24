package ua.com.fielden.platform.web_api;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;

import graphql.language.Argument;
import graphql.language.BooleanValue;
import graphql.language.Field;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.Value;
import graphql.language.VariableReference;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.utils.EntityUtils;

public class RootEntityMixin {
    private static final Logger LOGGER = Logger.getLogger(RootEntityMixin.class);
    
    public static QueryExecutionModel generateQueryModelFrom(final List<Field> fields, final Map<String, Object> variables, final Class<? extends AbstractEntity<?>> entityType) {
        final List<Field> innerFieldsForEntityQuery = RootEntityMixin.toFields(fields.get(0).getSelectionSet()); // TODO fields could be empty? could contain more than one?
        final LinkedHashMap<String, List<Argument>> properties = RootEntityMixin.properties(null, innerFieldsForEntityQuery);
        
        final Map<String, QueryProperty> queryProperties = new LinkedHashMap<>();
        for (final Map.Entry<String, List<Argument>> propertyAndArguments: properties.entrySet()) {
            final String propertyName = propertyAndArguments.getKey();
            final List<Argument> propertyArguments = propertyAndArguments.getValue();
            if (!propertyArguments.isEmpty()) {
                final QueryProperty queryProperty = new QueryProperty(entityType, propertyName);
                
                final Argument firstArgument = propertyArguments.get(0);
                // TODO handle other arguments
                
                // TODO check also firstArgument.getName();
                final Value valueOrVariable = firstArgument.getValue();
                LOGGER.error(String.format("Arg value / variable reference [%s]", valueOrVariable));
                
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
        
        final ICompleted<? extends AbstractEntity<?>> query = createQuery(entityType, new ArrayList<QueryProperty>(queryProperties.values()));
        final EntityResultQueryModel eqlQuery = query.model();
        
        final fetch<? extends AbstractEntity> fetchModel = EntityUtils.fetchNotInstrumented(entityType).with(properties.keySet()).fetchModel();
        final QueryExecutionModel queryModel = from(eqlQuery).with(fetchModel).lightweight().model();
        return queryModel;
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
    
    private static LinkedHashMap<String, List<Argument>> properties(final String prefix, final List<Field> graphQLFields) {
        final LinkedHashMap<String, List<Argument>> properties = new LinkedHashMap<>();
        for (final Field graphQLField: graphQLFields) {
            final List<Argument> args = graphQLField.getArguments();
            final String property = prefix == null ? graphQLField.getName() : prefix + "." + graphQLField.getName();
            properties.put(property, args);
            
            properties.putAll(properties(property, toFields(graphQLField.getSelectionSet())));
        }
        LOGGER.error(String.format("Fetching props [%s]", properties));
        return properties;
    }

    private static List<Field> toFields(final SelectionSet selectionSet) {
        final List<Field> selectionFields = new ArrayList<>();
        if (selectionSet != null) {
            final List<Selection> selections = selectionSet.getSelections();
            for (final Selection selection: selections) {
                if (selection instanceof Field) {
                    selectionFields.add((Field) selection);
                } else {
                    // TODO investigate what needs to be done here
                }
            }
        }
        return selectionFields;
    }
}
