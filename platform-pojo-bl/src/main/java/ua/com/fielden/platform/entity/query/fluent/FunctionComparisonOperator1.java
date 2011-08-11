package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator1;


class FunctionComparisonOperator1<T> extends AbstractComparisonOperator<IFunctionCompoundCondition1<T>> implements IFunctionComparisonOperator1<T> {
    T parent;
    FunctionComparisonOperator1(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IFunctionCompoundCondition1<T> getParent1() {
	return new FunctionCompoundCondition1<T>(getTokens(), parent);
    }
}
