package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere2;

final class Where1 extends AbstractWhere<IComparisonOperator1, ICompoundCondition1, IWhere2> implements IWhere1 {

    Where1(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    protected IWhere2 getParent3() {
	return new Where2(getTokens());
    }

    @Override
    ICompoundCondition1 getParent2() {
	return new CompoundCondition1(getTokens());
    }

    @Override
    IComparisonOperator1 getParent() {
	return new ComparisonOperator1(getTokens());
    }
}
