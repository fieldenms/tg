package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere3;

final class Where2 extends AbstractWhere<IComparisonOperator2, ICompoundCondition2, IWhere3> implements IWhere2 {

    Where2(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    protected IWhere3 getParent3() {
	return new Where3(getTokens());
    }

    @Override
    ICompoundCondition2 getParent2() {
	return new CompoundCondition2(getTokens());
    }

    @Override
    IComparisonOperator2 getParent() {
	return new ComparisonOperator2(getTokens());
    }
}