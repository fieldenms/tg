package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator2;

class ComparisonOperator2 extends AbstractComparisonOperator<ICompoundCondition2> implements IComparisonOperator2 {

    ComparisonOperator2(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    ICompoundCondition2 getParent1() {
	return new CompoundCondition2(getTokens());
    }
}
