package ua.com.fielden.platform.treemodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;

/**
 * Manages parameters for collectional properties for single-entity tree model.
 *
 * @author TG Team
 *
 */
public class CollectionalParameterManager {
    private final Map<Class<? extends AbstractEntity>, AnalysisReportTreeParameterManager> managers = new HashMap<Class<? extends AbstractEntity>, AnalysisReportTreeParameterManager>();

    public CollectionalParameterManager(final List<Class<? extends AbstractEntity>> collectionalTypes){
	for (final Class<? extends AbstractEntity> collectionalType : collectionalTypes){
	    managers.put(collectionalType, new AnalysisReportTreeParameterManager(collectionalType));
	}
    }

    public List<AnalysisPropertyAggregationFunction> getParameterFor(final Class<? extends AbstractEntity> collectionalType, final String propertyName) {
	return managers.get(collectionalType).getParameterFor(propertyName);
    }

    public void setParameterFor(final Class<? extends AbstractEntity> collectionalType, final String propertyName, final List<AnalysisPropertyAggregationFunction> parameterValue) throws IllegalArgumentException {
	managers.get(collectionalType).setParameterFor(propertyName, parameterValue);
    }

}
