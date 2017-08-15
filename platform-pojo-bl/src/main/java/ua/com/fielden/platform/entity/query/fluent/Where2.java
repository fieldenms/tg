package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere3;

final class Where2<ET extends AbstractEntity<?>> extends AbstractWhere<IComparisonOperator2<ET>, ICompoundCondition2<ET>, IWhere3<ET>, ET> implements IWhere2<ET> {

    @Override
    protected IWhere3<ET> nextForAbstractWhere() {
        return new Where3<ET>();
    }

    @Override
    ICompoundCondition2<ET> nextForAbstractConditionalOperand() {
        return new CompoundCondition2<ET>();
    }

    @Override
    IComparisonOperator2<ET> nextForAbstractSingleOperand() {
        return new ComparisonOperator2<ET>();
    }
}