package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;

class FunctionWhere0<T> extends AbstractWhere<IFunctionComparisonOperator0<T>, IFunctionCompoundCondition0<T>, IFunctionWhere1<T>> implements IFunctionWhere0<T> {
    T parent;
    FunctionWhere0(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    protected IFunctionWhere1<T> getParent3() {
	return new FunctionWhere1<T>(getTokens(), parent);
    }

    @Override
    IFunctionCompoundCondition0<T> getParent2() {
	return new FunctionCompoundCondition0<T>(getTokens(), parent);
    }

    @Override
    IFunctionComparisonOperator0<T> getParent() {
	return new FunctionComparisonOperator0<T>(getTokens(), parent);
    }
}