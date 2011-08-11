package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator3;


class FunctionComparisonOperator3<T> extends AbstractComparisonOperator<IFunctionCompoundCondition3<T>> implements IFunctionComparisonOperator3<T> {
    T parent;
    FunctionComparisonOperator3(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IFunctionCompoundCondition3<T> getParent1() {
	return new FunctionCompoundCondition3<T>(getTokens(), parent);
    }
}
