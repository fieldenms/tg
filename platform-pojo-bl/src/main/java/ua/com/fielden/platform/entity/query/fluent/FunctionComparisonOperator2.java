package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator2;


class FunctionComparisonOperator2<T> extends AbstractComparisonOperator<IFunctionCompoundCondition2<T>> implements IFunctionComparisonOperator2<T> {
    T parent;
    FunctionComparisonOperator2(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IFunctionCompoundCondition2<T> getParent1() {
	return new FunctionCompoundCondition2<T>(getTokens(), parent);
    }
}
