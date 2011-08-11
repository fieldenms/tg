package ua.com.fielden.platform.treemodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.equery.PropertyAggregationFunction;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

/**
 * {@link ITreeParameterManager} for {@link DynamicReportTreeWithTotals}. This type of tree is mostly used in the entity centers.
 *
 * @author oleh
 */
class PropertyTreeParameterManager implements ITreeParameterManager<PropertyAggregationFunction> {

    private final Map<String, PropertyAggregationFunction> dataModel = new HashMap<String, PropertyAggregationFunction>();
    /**
     * This determines type where properties are allocated.
     */
    private final Class<?> parentType;

    /**
     * Creates new {@link PropertyTreeParameterManager} instance and initiates {@link #parentType} property.
     *
     * @param parentType
     */
    public PropertyTreeParameterManager(final Class<?> parentType) {
	this.parentType = parentType;
    }

    @Override
    public PropertyAggregationFunction getParameterFor(final String propertyName) {
	final PropertyAggregationFunction propertyFunction = dataModel.get(propertyName);
	if (propertyFunction == null) {
	    try {
		setParameterFor(propertyName, PropertyAggregationFunction.NONE);
		return PropertyAggregationFunction.NONE;
	    } catch (final IllegalArgumentException e) {
		return null;
	    } catch (final NullPointerException e) {
		// NullPointerException in case of collectional sub-property:
		return null;
	    }
	}
	return propertyFunction;
    }

    @Override
    public void setParameterFor(final String propertyName, final PropertyAggregationFunction parameterValue) throws IllegalArgumentException, NullPointerException {
	final Class<?> propertyType = StringUtils.isEmpty(propertyName) ? parentType : PropertyTypeDeterminator.determinePropertyType(parentType, propertyName);
	if (PropertyAggregationFunction.getFunctionForType(propertyType).contains(parameterValue)) {
	    dataModel.put(propertyName, parameterValue);
	} else {
	    throw new IllegalArgumentException("The property can not be aggregated");
	}
    }

    /**
     * Returns all property names for which {@link PropertyAggregationFunction} was set.
     *
     * @return
     */
    public Set<String> getPropertyNames() {
	return Collections.unmodifiableSet(dataModel.keySet());
    }
}
