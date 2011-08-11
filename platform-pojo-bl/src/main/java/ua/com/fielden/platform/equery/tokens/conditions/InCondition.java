package ua.com.fielden.platform.equery.tokens.conditions;

import ua.com.fielden.platform.equery.LogicalOperator;
import ua.com.fielden.platform.equery.QueryParameter;
import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.tokens.properties.SearchProperty;

public final class InCondition extends ConditionOnProperty<InCondition> {
    private final QueryParameter parameter;
    private final boolean negated;

    /**
     * Mainly used for serialisation.
     */
    protected InCondition() {
	parameter = null;
	negated = false;
    }

    public InCondition(final LogicalOperator logicalOperator, final boolean negated, final SearchProperty property, final QueryParameter parameter) {
	setLogicalOperator(logicalOperator);
	setProperty(property);
	this.negated = negated;
	this.parameter = parameter;
    }

    public QueryParameter getParameter() {
	return parameter;
    }

    @Override
    public String getSql(final RootEntityMapper alias) {
	final StringBuffer sb = new StringBuffer();
	sb.append(getProperty().getSql(alias));
	sb.append(negated ? " NOT" : "");
	sb.append(" IN (");
	sb.append(parameter.getValue());
	sb.append(")");
	return sb.toString();
    }

    public InCondition clon() {
	final InCondition clon = new InCondition(getLogicalOperator(), negated, getProperty().clon(), parameter.clon());
	return clon;
    }
}
