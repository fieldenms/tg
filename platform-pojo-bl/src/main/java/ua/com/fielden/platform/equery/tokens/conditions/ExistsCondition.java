package ua.com.fielden.platform.equery.tokens.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.LogicalOperator;
import ua.com.fielden.platform.equery.QueryModel;
import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;

public final class ExistsCondition extends Condition<ExistsCondition> {
    private final boolean negated;
    private final IQueryModel<? extends AbstractEntity> model;

    /**
     * Mainly used for serialisation.
     */
    protected ExistsCondition() {
	negated = false;
	model = null;
    }

    public ExistsCondition(final LogicalOperator logicalOperator, final boolean negated, final IQueryModel<? extends AbstractEntity> model) {
	setLogicalOperator(logicalOperator);
	this.negated = negated;
	this.model = ((QueryModel) model).clon();
    }

    public IQueryModel<? extends AbstractEntity> getModel() {
	return model;
    }

    @Override
    public String getSql(final RootEntityMapper alias) {
	final StringBuffer sb = new StringBuffer();
	sb.append(negated ? "NOT " : "");
	sb.append("EXISTS (");
	sb.append(model.getModelResult(alias.getMappingExtractor()).getSql());
	sb.append(")");
	return sb.toString();
    }

    public ExistsCondition clon() {
	final ExistsCondition clon = new ExistsCondition(getLogicalOperator(), negated, model);
	return clon;
    }
}