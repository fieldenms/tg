package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;

class ComparisonOperator2<ET extends AbstractEntity<?>> extends AbstractComparisonOperator<ICompoundCondition2<ET>, ET> implements IComparisonOperator2<ET> {

    ComparisonOperator2(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    ICompoundCondition2<ET> getParent1() {
	return new CompoundCondition2<ET>(getTokens());
    }
}