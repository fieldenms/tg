package ua.com.fielden.platform.equery.tokens.conditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.equery.LogicalOperator;
import ua.com.fielden.platform.equery.QueryParameter;
import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.tokens.properties.SearchProperty;

public final class ImplicitOrGroupCondition extends ConditionOnProperty<ImplicitOrGroupCondition> {
    private final ComparisonOperation operation; // LIKE or EQ or NE
    private boolean negated = false;
    private final ArrayList<QueryParameter> params = new ArrayList<QueryParameter>();

    /**
     * Mainly used for serialisation.
     */
    protected ImplicitOrGroupCondition() {
	operation = null;
    }

    public ImplicitOrGroupCondition(final LogicalOperator logicalOperator, final SearchProperty property, final ComparisonOperation operation, final Object... values) {
	setLogicalOperator(logicalOperator);
	setProperty(property);
	this.operation = operation;
	for (final Object value : values) {
	    this.params.add(new QueryParameter(null, value));
	}
    }

    public ImplicitOrGroupCondition(final LogicalOperator logicalOperator, final SearchProperty property, final boolean negated, final ComparisonOperation operation, final Object... values) {
	this(logicalOperator, property, operation, values);
	this.negated = negated;
    }

    private ImplicitOrGroupCondition(final LogicalOperator logicalOperator, final SearchProperty property, final ComparisonOperation operation, final List<QueryParameter> params) {
	setLogicalOperator(logicalOperator);
	setProperty(property);
	this.operation = operation;
	this.params.addAll(params);
    }

    public List<QueryParameter> getParams() {
	return params;
    }

    public String getSql(final RootEntityMapper alias) {
	final StringBuffer sb = new StringBuffer();
	sb.append(params.size() > 1 ? "(" : "");
	for (final Iterator<QueryParameter> iterator = params.iterator(); iterator.hasNext();) {
	    final QueryParameter param = iterator.next();
	    sb.append(getProperty().getSql(alias));
	    sb.append(" ");
	    sb.append(negated ? "NOT " : "");
	    sb.append(operation.getValue());
	    sb.append(" ");
	    sb.append(param.getValue());
	    if (iterator.hasNext()) {
		sb.append(" OR ");
	    }
	}

	sb.append(params.size() > 1 ? ")" : "");

	return sb.toString();
    }

    @Override
    public ImplicitOrGroupCondition clon() {
	final List<QueryParameter> clonedParams = new ArrayList<QueryParameter>();
	for (final QueryParameter queryParameter : params) {
	    clonedParams.add(queryParameter.clon());
	}

	final ImplicitOrGroupCondition clon = new ImplicitOrGroupCondition(getLogicalOperator(), getProperty().clon(), operation, clonedParams);
	return clon;
    }
}
