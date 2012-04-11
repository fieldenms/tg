package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere;

abstract class AbstractWhere<T1 extends IComparisonOperator<T2>, T2 extends ILogicalOperator<? extends IWhere>, T3> extends AbstractConditionalOperand<T1, T2> implements IWhere<T1, T2, T3> {

    AbstractWhere(final Tokens queryTokens) {
	super(queryTokens);
    }

    protected abstract T3 getParent3();

    private AbstractBeginCondition<T3> getBeginCondition() {
	return new AbstractBeginCondition<T3>(getTokens()) {
	    @Override
	    T3 getParent() {
		return getParent3();
	    }
	};
    }

    @Override
    public T3 begin() {
	return getBeginCondition().begin();
    }

    @Override
    public T3 notBegin() {
	return getBeginCondition().notBegin();
    }
}
