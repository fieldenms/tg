package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

class FunctionWhere2<T, ET extends AbstractEntity<?>> extends AbstractWhere<IFunctionComparisonOperator2<T, ET>, IFunctionCompoundCondition2<T, ET>, IFunctionWhere3<T, ET>, ET> implements IFunctionWhere2<T, ET> {
    T parent;
    FunctionWhere2(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    protected IFunctionWhere3<T, ET> getParent3() {
	return new FunctionWhere3<T, ET>(getTokens(), parent);
    }

    @Override
    IFunctionCompoundCondition2<T, ET> getParent2() {
	return new FunctionCompoundCondition2<T, ET>(getTokens(), parent);
    }

    @Override
    IFunctionComparisonOperator2<T, ET> getParent() {
	return new FunctionComparisonOperator2<T, ET>(getTokens(), parent);
    }
}
