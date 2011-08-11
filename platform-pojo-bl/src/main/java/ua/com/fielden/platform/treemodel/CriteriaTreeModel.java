package ua.com.fielden.platform.treemodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.PropertyAggregationFunction;

/**
 * The entities tree model for dynamic criteria tree.
 *
 * @author TG Team
 *
 */
public class CriteriaTreeModel extends EntityTreeModel {
    private static final long serialVersionUID = 6879145992638957259L;
    private final Logger logger = Logger.getLogger(this.getClass());

    // first check-box: currently no parameter manager is used
    // second check-box:
    private final PropertyTreeParameterManager totalsParameterManager;
//    private final CollectionalParameterManager collectionAggregationParameterManager;

    public CriteriaTreeModel(final Class<? extends AbstractEntity> mainClass, final IPropertyFilter propertyFilter, final Map<String, PropertyAggregationFunction> totals) {
	super(mainClass, propertyFilter, false);

	totalsParameterManager = new PropertyTreeParameterManager(mainClass);

	if (totals != null) {
	    for (final Entry<String, PropertyAggregationFunction> totalsEntry : totals.entrySet()) {
		try {
		    totalsParameterManager.setParameterFor(totalsEntry.getKey(), totalsEntry.getValue());
		    logger.debug("The " + totalsEntry.getValue() + " value for " + totalsEntry.getKey() + " property was set correctly");
		} catch (final IllegalArgumentException ex) {
		    logger.debug("Couldn't set " + totalsEntry.getValue() + " value for " + totalsEntry.getKey() + " property");
		}
	    }
	}

//	collectionAggregationParameterManager = new CollectionalParameterManager(getCollectionalPropertyTypes());
    }

    public Map<String, PropertyAggregationFunction> getCorrectTreeTotals(final List<String> selectedTableHeaders) {
	final Map<String, PropertyAggregationFunction> correctedTotals = new HashMap<String, PropertyAggregationFunction>();
	for (final String propertyName : totalsParameterManager.getPropertyNames()) {
	    final PropertyAggregationFunction function=totalsParameterManager.getParameterFor(propertyName);
	    if (selectedTableHeaders.contains(propertyName) && function!=null &&!PropertyAggregationFunction.NONE.equals(function)) {
		correctedTotals.put(propertyName, totalsParameterManager.getParameterFor(propertyName));
	    }
	}
	return correctedTotals;
    }

    /**
     * Returns "totals" parameter for concrete property in main entity.
     *
     * @param propertyName
     * @return
     */
    public PropertyAggregationFunction getTotalsParameterFor(final String propertyName) {
	return totalsParameterManager.getParameterFor(propertyName);
    }

    /**
     * Sets "totals" parameter for concrete property in main entity.
     *
     * @param propertyName
     * @param parameterValue
     * @throws IllegalArgumentException
     */
    public void setTotalsParameterFor(final String propertyName, final PropertyAggregationFunction parameterValue) throws IllegalArgumentException {
	totalsParameterManager.setParameterFor(propertyName, parameterValue);
    }

//    /**
//     * Returns parameters for concrete property (defined by "propertyName") in collectional sub-property hierarchy (defined by "collectionType") in main entity.
//     *
//     * @param collectionType
//     * @param propertyName
//     * @return
//     */
//    public List<AnalysisPropertyAggregationFunction> getCollectionAggregationParameterFor(final Class<? extends AbstractEntity> collectionType, final String propertyName) {
//	return collectionAggregationParameterManager.getParameterFor(collectionType, propertyName);
//    }
//
//    /**
//     * Sets parameters for concrete property (defined by "propertyName") in collectional sub-property hierarchy (defined by "collectionType") in main entity.
//     *
//     * @param collectionType
//     * @param propertyName
//     * @param parameterValue
//     * @throws IllegalArgumentException
//     */
//    public void setCollectionAggregationParameterFor(final Class<? extends AbstractEntity> collectionType, final String propertyName, final List<AnalysisPropertyAggregationFunction> parameterValue) throws IllegalArgumentException {
//	collectionAggregationParameterManager.setParameterFor(collectionType, propertyName, parameterValue);
//    }
}
