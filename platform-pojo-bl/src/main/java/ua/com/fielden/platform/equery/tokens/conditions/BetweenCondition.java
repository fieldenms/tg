package ua.com.fielden.platform.equery.tokens.conditions;

import java.util.List;

import ua.com.fielden.platform.equery.LogicalOperator;
import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.tokens.properties.SearchProperty;

public final class BetweenCondition extends ConditionOnProperty<BetweenCondition> {
    private ComparisonOperation operation;
    private SearchProperty lowerBoundary;
    private SearchProperty upperBoundary;

    /**
     * Mainly used for serialisation.
     */
    protected BetweenCondition() {
    }

    public BetweenCondition(final LogicalOperator logicalOperator, final SearchProperty property, final ComparisonOperation operation, final SearchProperty lowerBoundary, final SearchProperty upperBoundary) {
	setLogicalOperator(logicalOperator);
	setProperty(property);
	this.operation = operation;
	this.lowerBoundary = lowerBoundary;
	this.upperBoundary = upperBoundary;
    }

    public SearchProperty getParameter1() {
	return lowerBoundary;
    }

    public SearchProperty getParameter2() {
	return upperBoundary;
    }

    @Override
    public String getSql(final RootEntityMapper alias) {
	final StringBuffer sb = new StringBuffer();
	sb.append(getProperty().getSql(alias));
	sb.append(" ");
	sb.append(operation.getValue());
	sb.append(" ");
	sb.append(lowerBoundary != null ? lowerBoundary.getSql(alias) : null);
	sb.append(" AND ");
	sb.append(upperBoundary != null ? upperBoundary.getSql(alias) : null);
	return sb.toString();
    }

    public BetweenCondition clon() {
	final BetweenCondition clon = new BetweenCondition(getLogicalOperator(), getProperty().clon(), operation, (lowerBoundary != null ? lowerBoundary.clon() : null), (upperBoundary != null ? upperBoundary.clon()
		: null));
	return clon;
    }

    @Override
    public List<SearchProperty> getProps() {
	final List<SearchProperty> result = super.getProps();
	result.add(lowerBoundary);
	result.add(upperBoundary);
	return result;
    }
}
