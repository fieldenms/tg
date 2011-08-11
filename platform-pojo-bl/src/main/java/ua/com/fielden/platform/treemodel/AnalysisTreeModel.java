package ua.com.fielden.platform.treemodel;

import java.util.List;
import java.util.Vector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;
import ua.com.fielden.platform.equery.HqlDateFunctions;

/**
 * The entities tree model for AnalysisReportTree.
 * 
 * @author TG Team
 * 
 */
public class AnalysisTreeModel extends EntityTreeModel {
    private static final long serialVersionUID = 8250180246642760567L;

    // first check-box:
    private final DistributionTreeParameterManager distributionParameterManager;

    // second check-box:
    private final AnalysisReportTreeParameterManager aggregationParameterManager;

    public AnalysisTreeModel(final Class<? extends AbstractEntity> mainClass, final IPropertyFilter propertyFilter) {
	super(mainClass, propertyFilter, true);

	distributionParameterManager = new DistributionTreeParameterManager(mainClass);
	aggregationParameterManager = new AnalysisReportTreeParameterManager(mainClass);
    }

    /**
     * Returns parameter for distribution property. It is currently implemented for Date type of property.
     * 
     * @param propertyName
     * @return
     */
    public HqlDateFunctions getDistributionParameterFor(final String propertyName) {
	return distributionParameterManager.getParameterFor(propertyName);
    }

    /**
     * Sets parameter for distribution property. It is currently implemented for Date type of property.
     * 
     * @param propertyName
     * @param parameterValue
     * @throws IllegalArgumentException
     */
    public void setDistributionParameterFor(final String propertyName, final HqlDateFunctions parameterValue) throws IllegalArgumentException {
	distributionParameterManager.setParameterFor(propertyName, parameterValue);
    }

    /**
     * Returns parameters for aggregation property.
     * 
     * @param propertyName
     * @return
     * @return
     */
    public List<AnalysisPropertyAggregationFunction> getAggregationParameterFor(final String propertyName) {
	return aggregationParameterManager.getParameterFor(propertyName);
    }

    /**
     * Sets parameters for aggregation property.
     * 
     * @param propertyName
     * @param parameterValue
     * @throws IllegalArgumentException
     */
    public void setAggregationParameterFor(final String propertyName, final List<AnalysisPropertyAggregationFunction> parameterValue) throws IllegalArgumentException {
	aggregationParameterManager.setParameterFor(propertyName, parameterValue);
    }

    /**
     * Returns available parameters for property name.
     * 
     * @param propertyName
     * @return
     */
    public Vector<AnalysisPropertyAggregationFunction> getAvailableParametersFor(final String propertyName) {
	return aggregationParameterManager.getAvailableParametersFor(propertyName);
    }
}
