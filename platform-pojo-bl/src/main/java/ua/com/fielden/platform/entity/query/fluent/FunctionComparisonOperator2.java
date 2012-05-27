package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;


class FunctionComparisonOperator2<T, ET extends AbstractEntity<?>> extends AbstractComparisonOperator<IFunctionCompoundCondition2<T, ET>, ET> implements IFunctionComparisonOperator2<T, ET> {
    T parent;
    FunctionComparisonOperator2(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IFunctionCompoundCondition2<T, ET> getParent1() {
	return new FunctionCompoundCondition2<T, ET>(getTokens(), parent);
    }
}
