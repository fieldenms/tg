package ua.com.fielden.platform.reportquery;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.basic.IPropertyEnum;

public class DistributionProperty implements IDistributedProperty{

    private final String name, desc, actProperty;

    private String tableAlias;

    protected DistributionProperty() {
	this.name = null;
	this.desc = null;
	this.actProperty = null;
    }

    public DistributionProperty(final String name, final String desc, final String actProperty) {
	this.name = name;
	this.desc = desc;
	this.actProperty = actProperty;
    }

    @Override
    public String getActualProperty() {
	return actProperty;
    }

    @Override
    public String getTableAlias() {
	return tableAlias;
    }

    @Override
    public String getTooltip() {
	return desc;
    }

    @Override
    public String toString() {
	return name;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null || obj.getClass() != this.getClass()) {
	    return false;
	}
	final DistributionProperty destProp = (DistributionProperty) obj;

	if ((getActualProperty() == null && getActualProperty() != destProp.getActualProperty())
		|| (getActualProperty() != null && !getActualProperty().equals(destProp.getActualProperty()))) {
	    return false;
	}

	return true;
    }

    @Override
    public int hashCode() {
	int result = 17;
	result = 31 * result + (getActualProperty() != null ? getActualProperty().hashCode() : 0);
	return result;
    }

    @Override
    public void setTableAlias(final String tableAlias) {
	this.tableAlias = tableAlias;
    }

    @Override
    public int compareTo(final IPropertyEnum o) {
	return toString().compareTo(o.toString());
    }

    @Override
    public String getParsedValue() {
	final String queryValuePrefix = StringUtils.isEmpty(getTableAlias()) ? "" : getTableAlias() + ".";
	return queryValuePrefix + (StringUtils.isEmpty(getActualProperty()) ? "id" : getActualProperty());
    }

    @Override
    public boolean isExpression() {
	return false;
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public String getDesc() {
	return desc;
    }
}
