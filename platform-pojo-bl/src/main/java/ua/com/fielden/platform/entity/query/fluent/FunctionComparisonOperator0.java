package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator0;


class FunctionComparisonOperator0<T> extends AbstractComparisonOperator<IFunctionCompoundCondition0<T>> implements IFunctionComparisonOperator0<T> {
    T parent;
    FunctionComparisonOperator0(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0<T> getParent1() {
	return new FunctionCompoundCondition0<T>(getTokens(), parent);
    }
}
