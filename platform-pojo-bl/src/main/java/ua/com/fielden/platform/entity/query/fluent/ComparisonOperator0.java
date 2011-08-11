package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator0;


class ComparisonOperator0 extends AbstractComparisonOperator<ICompoundCondition0> implements IComparisonOperator0 {

    ComparisonOperator0(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    ICompoundCondition0 getParent1() {
	return new CompoundCondition0(getTokens());
    }
}
