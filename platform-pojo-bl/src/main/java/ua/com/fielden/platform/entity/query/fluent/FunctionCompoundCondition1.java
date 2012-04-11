package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;

final class FunctionCompoundCondition1<T> extends AbstractCompoundCondition<IFunctionWhere1<T>, IFunctionCompoundCondition0<T>> implements IFunctionCompoundCondition1<T> {
    T parent;
    FunctionCompoundCondition1(final Tokens queryTokens,  final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IFunctionWhere1<T> getParent() {
	return new FunctionWhere1<T>(getTokens(), parent);
    }

    @Override
    IFunctionCompoundCondition0<T> getParent2() {
	return new FunctionCompoundCondition0<T>(getTokens(), parent);
    }
}
