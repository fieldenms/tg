package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;


class ComparisonOperator0<ET extends AbstractEntity<?>> extends AbstractComparisonOperator<ICompoundCondition0<ET>, ET> implements IComparisonOperator0<ET> {

    ComparisonOperator0(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    ICompoundCondition0<ET> getParent1() {
	return new CompoundCondition0<ET>(getTokens());
    }
}