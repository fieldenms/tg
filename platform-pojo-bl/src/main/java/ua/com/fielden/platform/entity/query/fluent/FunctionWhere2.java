package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

class FunctionWhere2<T> extends AbstractWhere<IFunctionComparisonOperator2<T>, IFunctionCompoundCondition2<T>, IFunctionWhere3<T>> implements IFunctionWhere2<T> {
    T parent;
    FunctionWhere2(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    protected IFunctionWhere3<T> getParent3() {
	return new FunctionWhere3<T>(getTokens(), parent);
    }

    @Override
    IFunctionCompoundCondition2<T> getParent2() {
	return new FunctionCompoundCondition2<T>(getTokens(), parent);
    }

    @Override
    IFunctionComparisonOperator2<T> getParent() {
	return new FunctionComparisonOperator2<T>(getTokens(), parent);
    }
}
