package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere3;

final class Where2<ET extends AbstractEntity<?>> extends AbstractWhere<IComparisonOperator2<ET>, ICompoundCondition2<ET>, IWhere3<ET>, ET> implements IWhere2<ET> {

    Where2(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    protected IWhere3<ET> getParent3() {
	return new Where3<ET>(getTokens());
    }

    @Override
    ICompoundCondition2<ET> getParent2() {
	return new CompoundCondition2<ET>(getTokens());
    }

    @Override
    IComparisonOperator2<ET> getParent() {
	return new ComparisonOperator2<ET>(getTokens());
    }
}