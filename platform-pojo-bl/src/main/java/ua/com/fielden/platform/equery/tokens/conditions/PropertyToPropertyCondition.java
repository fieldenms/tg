package ua.com.fielden.platform.equery.tokens.conditions;

import java.util.List;

import ua.com.fielden.platform.equery.LogicalOperator;
import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.tokens.properties.SearchProperty;

public final class PropertyToPropertyCondition extends ConditionOnProperty<PropertyToPropertyCondition> {

    private final SearchProperty secondProperty;
    private final ComparisonOperation operation;
    private final boolean negated;

    /**
     * Mainly used for serialisation.
     */
    protected PropertyToPropertyCondition() {
	secondProperty = null;
	operation = null;
	negated = false;
    }

    public PropertyToPropertyCondition(final LogicalOperator logicalOperator, final SearchProperty property, final SearchProperty secondProperty, final boolean negated, final ComparisonOperation operation) {
	setLogicalOperator(logicalOperator);
	setProperty(property);
	this.secondProperty = secondProperty;
	this.operation = operation;
	this.negated = negated;
    }

    @Override
    public String getSql(final RootEntityMapper alias) {
	final StringBuffer sb = new StringBuffer();
	sb.append(getProperty().getSql(alias));
	sb.append(" ");
	sb.append(negated ? "NOT " : "");
	sb.append(operation.getValue());
	sb.append(" ");
	sb.append(secondProperty.getSql(alias));
	return sb.toString();
    }

    public PropertyToPropertyCondition clon() {
	final PropertyToPropertyCondition clon = new PropertyToPropertyCondition(getLogicalOperator(), getProperty().clon(), secondProperty.clon(), negated, operation);
	return clon;
    }

    @Override
    public List<SearchProperty> getProps() {
	final List<SearchProperty> result = super.getProps();
	result.add(secondProperty);
	return result;
    }
}
