package ua.com.fielden.platform.treemodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;
import ua.com.fielden.platform.swing.review.DynamicCriteriaPropertyAnalyser;
import ua.com.fielden.platform.types.Money;

class AnalysisReportTreeParameterManager implements ITreeParameterManager<List<AnalysisPropertyAggregationFunction>> {

    private final Map<String, List<AnalysisPropertyAggregationFunction>> dataModel = new HashMap<String, List<AnalysisPropertyAggregationFunction>>();
    private final Map<Class<?>, Vector<AnalysisPropertyAggregationFunction>> availableItems = new HashMap<Class<?>, Vector<AnalysisPropertyAggregationFunction>>();
    /**
     * This determines type where properties are allocated.
     */
    private final Class<?> parentType;

    /**
     * Creates new {@link PropertyTreeParameterManager} instance and initiates {@link #parentType} property.
     * 
     * @param parentType
     */
    public AnalysisReportTreeParameterManager(final Class<?> parentType) {
	this.parentType = parentType;
	addFunctionsForType(AbstractEntity.class, AnalysisPropertyAggregationFunction.COUNT, AnalysisPropertyAggregationFunction.DISTINCT_COUNT);
	addFunctionsForType(Number.class, AnalysisPropertyAggregationFunction.MIN, AnalysisPropertyAggregationFunction.MAX, AnalysisPropertyAggregationFunction.SUM, AnalysisPropertyAggregationFunction.AVG, AnalysisPropertyAggregationFunction.DISTINCT_COUNT);
	addFunctionsForType(String.class, AnalysisPropertyAggregationFunction.DISTINCT_COUNT, AnalysisPropertyAggregationFunction.MIN, AnalysisPropertyAggregationFunction.MAX);
	addFunctionsForType(Money.class, AnalysisPropertyAggregationFunction.MIN, AnalysisPropertyAggregationFunction.MAX, AnalysisPropertyAggregationFunction.SUM, AnalysisPropertyAggregationFunction.AVG, AnalysisPropertyAggregationFunction.DISTINCT_COUNT);
    }

    private void addFunctionsForType(final Class<?> type, final AnalysisPropertyAggregationFunction... functions) {
	availableItems.put(type, new Vector<AnalysisPropertyAggregationFunction>(Arrays.asList(functions)));
    }

    @Override
    public List<AnalysisPropertyAggregationFunction> getParameterFor(final String propertyName) {
	List<AnalysisPropertyAggregationFunction> propertyFunctions = dataModel.get(propertyName);
	if (propertyFunctions == null) {
	    propertyFunctions = new ArrayList<AnalysisPropertyAggregationFunction>();
	}
	return propertyFunctions;
    }

    @Override
    public void setParameterFor(final String propertyName, final List<AnalysisPropertyAggregationFunction> parameterValue) throws IllegalArgumentException {
	if (parameterValue == null || parameterValue.size() == 0) {
	    throw new IllegalArgumentException("The list of parameters to set can not be null or empty");
	}
	final Vector<AnalysisPropertyAggregationFunction> availableFunctions = getAvailableParametersFor(propertyName);
	for (final AnalysisPropertyAggregationFunction function : parameterValue) {
	    if (!availableFunctions.contains(function)) {
		throw new IllegalArgumentException("The " + propertyName + " property can not be aggregated with " + function + " function");
	    }
	}
	dataModel.put(propertyName, parameterValue);
    }

    public Vector<AnalysisPropertyAggregationFunction> getAvailableParametersFor(final String propertyName) {
	final DynamicCriteriaPropertyAnalyser analyser = new DynamicCriteriaPropertyAnalyser(parentType, propertyName, null);
	final Class<?> propertyType = analyser.getPropertyType();
	Vector<AnalysisPropertyAggregationFunction> functions = null;
	if (propertyType != null && AbstractEntity.class.isAssignableFrom(propertyType)) {
	    final Vector<AnalysisPropertyAggregationFunction> abstractEntityFunctions = availableItems.get(AbstractEntity.class);
	    functions = abstractEntityFunctions != null ? new Vector<AnalysisPropertyAggregationFunction>(abstractEntityFunctions)
		    : new Vector<AnalysisPropertyAggregationFunction>();
	    if (StringUtils.isEmpty(propertyName)) {
		functions.remove(AnalysisPropertyAggregationFunction.DISTINCT_COUNT);
	    } else {
		functions.remove(AnalysisPropertyAggregationFunction.COUNT);
	    }
	} else if (propertyType != null && Number.class.isAssignableFrom(propertyType)) {
	    functions = availableItems.get(Number.class);
	} else {
	    functions = availableItems.get(propertyType);
	}
	return functions == null ? new Vector<AnalysisPropertyAggregationFunction>() : functions;
    }
}
