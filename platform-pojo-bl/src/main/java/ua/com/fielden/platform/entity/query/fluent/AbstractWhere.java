package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere;

abstract class AbstractWhere<T1 extends IComparisonOperator<T2, ET>, T2 extends ILogicalOperator<? extends IWhere<T1, T2, T3, ET>>, T3, ET extends AbstractEntity<?>> //
extends AbstractWhereWithoutNesting<T1, T2, ET> //
implements IWhere<T1, T2, T3, ET> {

    protected abstract T3 getParent3();

    @Override
    public T3 begin() {
    	return copy(getParent3(), getTokens().beginCondition(false));
    }

    @Override
    public T3 notBegin() {
    	return copy(getParent3(), getTokens().beginCondition(true));
    }
}
