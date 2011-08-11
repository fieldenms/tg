package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;

class Where0 extends AbstractWhere<IComparisonOperator0, ICompoundCondition0, IWhere1> implements IWhere0 {

    Where0(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    protected IWhere1 getParent3() {
	return new Where1(getTokens());
    }

    @Override
    ICompoundCondition0 getParent2() {
	return new CompoundCondition0(getTokens());
    }

    @Override
    IComparisonOperator0 getParent() {
	return new ComparisonOperator0(getTokens());
    }
}