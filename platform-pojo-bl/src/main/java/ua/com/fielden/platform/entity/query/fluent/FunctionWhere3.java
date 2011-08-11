package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;


final class FunctionWhere3<T> extends AbstractConditionalOperand<IFunctionComparisonOperator3<T>, IFunctionCompoundCondition3<T>> implements IFunctionWhere3<T> {
    T parent;
    FunctionWhere3(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IFunctionCompoundCondition3<T> getParent2() {
	return new FunctionCompoundCondition3<T>(getTokens(), parent);
    }

    @Override
    IFunctionComparisonOperator3<T> getParent() {
	return new FunctionComparisonOperator3<T>(getTokens(), parent);
    }
}
