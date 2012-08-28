package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere;

abstract class AbstractWhere<T1 extends IComparisonOperator<T2, ET>, T2 extends ILogicalOperator<? extends IWhere<T1, T2, T3, ET>>, T3, ET extends AbstractEntity<?>> extends AbstractConditionalOperand<T1, T2, ET> implements IWhere<T1, T2, T3, ET> {

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

    public T2 condition(final Object condition) {
	return getParent2();
    }
}