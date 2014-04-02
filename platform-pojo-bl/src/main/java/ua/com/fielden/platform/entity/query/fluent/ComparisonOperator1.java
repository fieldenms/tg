package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;

class ComparisonOperator1<ET extends AbstractEntity<?>> extends AbstractComparisonOperator<ICompoundCondition1<ET>, ET> implements IComparisonOperator1<ET> {

    ComparisonOperator1(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    ICompoundCondition1<ET> getParent1() {
        return new CompoundCondition1<ET>(getTokens());
    }
}