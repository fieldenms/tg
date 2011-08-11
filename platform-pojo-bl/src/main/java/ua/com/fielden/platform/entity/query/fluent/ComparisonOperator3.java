package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator3;


class ComparisonOperator3 extends AbstractComparisonOperator<ICompoundCondition3> implements IComparisonOperator3 {

    ComparisonOperator3(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    ICompoundCondition3 getParent1() {
	return new CompoundCondition3(getTokens());
    }
}
