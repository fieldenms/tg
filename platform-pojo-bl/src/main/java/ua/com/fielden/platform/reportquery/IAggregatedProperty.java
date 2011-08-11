package ua.com.fielden.platform.reportquery;

import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;

public interface IAggregatedProperty extends IDistributedProperty {

    AnalysisPropertyAggregationFunction getAggregationFunction();

}
