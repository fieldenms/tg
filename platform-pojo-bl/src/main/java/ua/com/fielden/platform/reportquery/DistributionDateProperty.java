package ua.com.fielden.platform.reportquery;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.equery.HqlDateFunctions;

public class DistributionDateProperty extends DistributionProperty {

    private final HqlDateFunctions dateFunction;

    protected DistributionDateProperty() {
	super();
	this.dateFunction = HqlDateFunctions.MONTH;
    }

    public DistributionDateProperty(final String name, final String desc, final String actProperty, final HqlDateFunctions dateFunction) {
	super(name, desc, actProperty);
	this.dateFunction = dateFunction;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null || obj.getClass() != this.getClass()) {
	    return false;
	}
	final DistributionDateProperty destProp = (DistributionDateProperty) obj;

	if ((getActualProperty() == null && getActualProperty() != destProp.getActualProperty())
		|| (getActualProperty() != null && !getActualProperty().equals(destProp.getActualProperty()))) {
	    return false;
	}
	if ((getDateFunction() == null && getDateFunction() != destProp.getDateFunction()) || (getDateFunction() != null && !getDateFunction().equals(destProp.getDateFunction()))) {
	    return false;
	}
	return true;
    }

    @Override
    public int hashCode() {
	int result = 17;
	result = 31 * result + (getActualProperty() != null ? getActualProperty().hashCode() : 0);
	result = 31 * result + (getDateFunction() != null ? getDateFunction().hashCode() : 0);
	return result;
    }

    @Override
    public String getParsedValue() {
	final String queryValuePrefix = StringUtils.isEmpty(getTableAlias()) ? "" : getTableAlias() + ".";
	return dateFunction.getActualExpression(queryValuePrefix + (StringUtils.isEmpty(getActualProperty()) ? "id" : getActualProperty()));
    }

    public HqlDateFunctions getDateFunction() {
	return dateFunction;
    }

    @Override
    public String toString() {
	return dateFunction.toString() + " of " + super.toString();
    }

    @Override
    public String getTooltip() {
	return dateFunction.toString() + " of " + super.getTooltip();
    }

    @Override
    public boolean isExpression() {
	return true;
    }

}
