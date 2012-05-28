package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;

final class FunctionCompoundCondition1<T, ET extends AbstractEntity<?>> extends AbstractCompoundCondition<IFunctionWhere1<T, ET>, IFunctionCompoundCondition0<T, ET>> implements IFunctionCompoundCondition1<T, ET> {
    T parent;
    FunctionCompoundCondition1(final Tokens queryTokens,  final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IFunctionWhere1<T, ET> getParent() {
	return new FunctionWhere1<T, ET>(getTokens(), parent);
    }

    @Override
    IFunctionCompoundCondition0<T, ET> getParent2() {
	return new FunctionCompoundCondition0<T, ET>(getTokens(), parent);
    }
}