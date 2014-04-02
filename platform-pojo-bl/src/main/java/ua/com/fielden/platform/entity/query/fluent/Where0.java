package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;

class Where0<ET extends AbstractEntity<?>> extends AbstractWhere<IComparisonOperator0<ET>, ICompoundCondition0<ET>, IWhere1<ET>, ET> implements IWhere0<ET> {

    Where0(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    protected IWhere1<ET> getParent3() {
        return new Where1<ET>(getTokens());
    }

    @Override
    ICompoundCondition0<ET> getParent2() {
        return new CompoundCondition0<ET>(getTokens());
    }

    @Override
    IComparisonOperator0<ET> getParent() {
        return new ComparisonOperator0<ET>(getTokens());
    }
}