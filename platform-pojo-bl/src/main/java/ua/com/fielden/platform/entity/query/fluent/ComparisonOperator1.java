package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator1;


class ComparisonOperator1 extends AbstractComparisonOperator<ICompoundCondition1> implements IComparisonOperator1 {

    ComparisonOperator1(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    ICompoundCondition1 getParent1() {
	return new CompoundCondition1(getTokens());
    }
}
