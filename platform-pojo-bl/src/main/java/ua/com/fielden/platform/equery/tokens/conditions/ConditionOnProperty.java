package ua.com.fielden.platform.equery.tokens.conditions;

import java.util.List;

import ua.com.fielden.platform.equery.tokens.properties.SearchProperty;

public abstract class ConditionOnProperty<T> extends Condition<T> {
    private SearchProperty property;

    protected ConditionOnProperty() {
    }

    public SearchProperty getProperty() {
	return property;
    }

    public void setProperty(final SearchProperty property) {
	this.property = property;
    }

    @Override
    public String toString() {
        return (getLogicalOperator() != null ? getLogicalOperator() : "") + " " + getProperty();
    }

    @Override
    public List<SearchProperty> getProps() {
	final List<SearchProperty> result = super.getProps();
	result.add(property);
	return result;
    }
}
