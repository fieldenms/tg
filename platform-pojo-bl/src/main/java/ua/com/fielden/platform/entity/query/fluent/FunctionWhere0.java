package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;

class FunctionWhere0<T, ET extends AbstractEntity<?>> extends AbstractWhere<IFunctionComparisonOperator0<T, ET>, IFunctionCompoundCondition0<T, ET>, IFunctionWhere1<T, ET>, ET> implements IFunctionWhere0<T, ET> {
    T parent;
    FunctionWhere0(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    protected IFunctionWhere1<T, ET> getParent3() {
	return new FunctionWhere1<T, ET>(getTokens(), parent);
    }

    @Override
    IFunctionCompoundCondition0<T, ET> getParent2() {
	return new FunctionCompoundCondition0<T, ET>(getTokens(), parent);
    }

    @Override
    IFunctionComparisonOperator0<T, ET> getParent() {
	return new FunctionComparisonOperator0<T, ET>(getTokens(), parent);
    }
}