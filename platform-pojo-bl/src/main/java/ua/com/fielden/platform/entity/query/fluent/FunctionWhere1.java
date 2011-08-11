package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;

class FunctionWhere1<T> extends AbstractWhere<IFunctionComparisonOperator1<T>, IFunctionCompoundCondition1<T>, IFunctionWhere2<T>> implements IFunctionWhere1<T> {
    T parent;
    FunctionWhere1(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    protected IFunctionWhere2<T> getParent3() {
	return new FunctionWhere2<T>(getTokens(), parent);
    }

    @Override
    IFunctionCompoundCondition1<T> getParent2() {
	return new FunctionCompoundCondition1<T>(getTokens(), parent);
    }

    @Override
    IFunctionComparisonOperator1<T> getParent() {
	return new FunctionComparisonOperator1<T>(getTokens(), parent);
    }
}
