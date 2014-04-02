package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition3;

class ComparisonOperator3<ET extends AbstractEntity<?>> extends AbstractComparisonOperator<ICompoundCondition3<ET>, ET> implements IComparisonOperator3<ET> {

    ComparisonOperator3(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    ICompoundCondition3<ET> getParent1() {
        return new CompoundCondition3<ET>(getTokens());
    }
}