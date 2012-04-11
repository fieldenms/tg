package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

final class FunctionCompoundCondition3<T> extends AbstractCompoundCondition<IFunctionWhere3<T>, IFunctionCompoundCondition2<T>> implements IFunctionCompoundCondition3<T> {
    T parent;
    FunctionCompoundCondition3(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IFunctionWhere3<T> getParent() {
	return new FunctionWhere3<T>(getTokens(), parent);
    }

    @Override
    IFunctionCompoundCondition2<T> getParent2() {
	return new FunctionCompoundCondition2<T>(getTokens(), parent);
    }
}
