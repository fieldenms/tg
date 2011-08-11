package ua.com.fielden.platform.equery.tokens.conditions;

import ua.com.fielden.platform.equery.LogicalOperator;
import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.tokens.properties.SearchProperty;

public final class ConditionWithoutArgument extends ConditionOnProperty<ConditionWithoutArgument> {
    private final ComparisonWithoutArgumentOperation operation;

    /**
     * Mainly used for serialisation.
     */
    protected ConditionWithoutArgument() {
	operation = null;
    }

    public ConditionWithoutArgument(final LogicalOperator logicalOperator, final SearchProperty property, final ComparisonWithoutArgumentOperation operation) {
	setLogicalOperator(logicalOperator);
	setProperty(property);
	this.operation = operation;
    }

    @Override
    public String getSql(final RootEntityMapper alias) {
	final StringBuffer sb = new StringBuffer();
	sb.append(getProperty().getSql(alias));
	sb.append(" ");
	sb.append(operation.getValue());
	return sb.toString();
    }

    public ConditionWithoutArgument clon() {
	final ConditionWithoutArgument clon = new ConditionWithoutArgument(getLogicalOperator(), getProperty().clon(), operation);
	return clon;
    }
}
