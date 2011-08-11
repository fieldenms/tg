package ua.com.fielden.platform.treemodel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.equery.HqlDateFunctions;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

/**
 * {@link ITreeParameterManager} for distribution trees. The distribution trees are mostly used in analysis reports.
 *
 * @author oleh
 *
 */
class DistributionTreeParameterManager implements ITreeParameterManager<HqlDateFunctions> {

    private final Map<String, HqlDateFunctions> dataModel = new HashMap<String, HqlDateFunctions>();
    private final Class<?> parentType;

    /**
     * Initiates this {@link DistributionTreeParameterManager} with parentType. This parentType - determines the class where properties are located.
     *
     * @param parentType
     */
    public DistributionTreeParameterManager(final Class<?> parentType) {
	this.parentType = parentType;
    }

    @Override
    public HqlDateFunctions getParameterFor(final String propertyName) {
	final HqlDateFunctions dateFunction = dataModel.get(propertyName);
	if (dateFunction == null) {
	    try {
		setParameterFor(propertyName, HqlDateFunctions.MONTH);
		return HqlDateFunctions.MONTH;
	    } catch (final IllegalArgumentException e) {
		return null;
	    }
	}
	return dateFunction;
    }

    @Override
    public void setParameterFor(final String propertyName, final HqlDateFunctions parameterValue) throws IllegalArgumentException {
	final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(parentType, propertyName);
	if (Date.class.isAssignableFrom(propertyType)) {
	    dataModel.put(propertyName, parameterValue);
	} else {
	    throw new IllegalArgumentException("The type of the property must be Date");
	}
    }
}
