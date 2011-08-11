package ua.com.fielden.platform.reportquery;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;

public class AggregationProperty extends DistributionProperty implements IAggregatedProperty {

    private AnalysisPropertyAggregationFunction aggregationFunction;

    protected AggregationProperty() {
	super();
	this.aggregationFunction = null;
    }

    public AggregationProperty(final String name, final String desc, final String actProperty, final AnalysisPropertyAggregationFunction aggregationFunction) {
	super(name, desc, actProperty);
	this.aggregationFunction = aggregationFunction;
    }

    public AggregationProperty(final String name, final String desc, final String actProperty) {
	this(name, desc, actProperty, null);
    }

    @Override
    public AnalysisPropertyAggregationFunction getAggregationFunction() {
	return aggregationFunction;
    }

    @Override
    public String toString() {
	return aggregationFunction.toString() + " of " + super.toString();
    }

    @Override
    public String getTooltip() {
	return aggregationFunction.toString() + " of " + super.getTooltip();
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null || obj.getClass() != this.getClass()) {
	    return false;
	}
	final AggregationProperty destProp = (AggregationProperty) obj;
	if ((getActualProperty() == null && getActualProperty() != destProp.getActualProperty())
		|| (getActualProperty() != null && !getActualProperty().equals(destProp.getActualProperty()))) {
	    return false;
	}
	if ((getAggregationFunction() == null && getAggregationFunction() != destProp.getAggregationFunction())
		|| (getAggregationFunction() != null && !getAggregationFunction().equals(destProp.getAggregationFunction()))) {
	    return false;
	}
	return true;
    }

    @Override
    public int hashCode() {
	int result = 17;
	result = 31 * result + (getActualProperty() != null ? getActualProperty().hashCode() : 0);
	result = 31 * result + (getAggregationFunction() != null ? getAggregationFunction().hashCode() : 0);
	return result;
    }

    @Override
    public String getParsedValue() {
	final String queryValuePrefix = StringUtils.isEmpty(getTableAlias()) ? "" : getTableAlias() + ".";
	return getAggregationFunction().createQueryString(queryValuePrefix + (StringUtils.isEmpty(getActualProperty()) ? "id" : getActualProperty()));
    }

    @Override
    public boolean isExpression() {
	return true;
    }

}
