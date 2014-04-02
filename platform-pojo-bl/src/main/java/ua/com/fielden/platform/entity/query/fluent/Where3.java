package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere3;

final class Where3<ET extends AbstractEntity<?>> extends AbstractConditionalOperand<IComparisonOperator3<ET>, ICompoundCondition3<ET>, ET> implements IWhere3<ET> {

    Where3(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    ICompoundCondition3<ET> getParent2() {
        return new CompoundCondition3<ET>(getTokens());
    }

    @Override
    IComparisonOperator3<ET> getParent() {
        return new ComparisonOperator3<ET>(getTokens());
    }
}