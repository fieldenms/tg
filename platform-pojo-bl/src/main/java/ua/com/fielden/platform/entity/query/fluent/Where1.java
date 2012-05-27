package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere2;

final class Where1<ET extends AbstractEntity<?>> extends AbstractWhere<IComparisonOperator1<ET>, ICompoundCondition1<ET>, IWhere2<ET>, ET> implements IWhere1<ET> {

    Where1(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    protected IWhere2<ET> getParent3() {
	return new Where2<ET>(getTokens());
    }

    @Override
    ICompoundCondition1<ET> getParent2() {
	return new CompoundCondition1<ET>(getTokens());
    }

    @Override
    IComparisonOperator1<ET> getParent() {
	return new ComparisonOperator1<ET>(getTokens());
    }
}
