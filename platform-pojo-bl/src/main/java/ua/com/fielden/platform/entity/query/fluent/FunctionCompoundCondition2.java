package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;

final class FunctionCompoundCondition2<T> extends AbstractCompoundCondition<IFunctionWhere2<T>, IFunctionCompoundCondition1<T>> implements IFunctionCompoundCondition2<T> {
    T parent;
    FunctionCompoundCondition2(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IFunctionWhere2<T> getParent() {
	return new FunctionWhere2<T>(getTokens(), parent);
    }

    @Override
    IFunctionCompoundCondition1<T> getParent2() {
	return new FunctionCompoundCondition1<T>(getTokens(), parent);
    }
}
