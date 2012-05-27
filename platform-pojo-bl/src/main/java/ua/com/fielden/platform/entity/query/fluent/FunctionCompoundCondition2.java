package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;

final class FunctionCompoundCondition2<T, ET extends AbstractEntity<?>> extends AbstractCompoundCondition<IFunctionWhere2<T, ET>, IFunctionCompoundCondition1<T, ET>> implements IFunctionCompoundCondition2<T, ET> {
    T parent;
    FunctionCompoundCondition2(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IFunctionWhere2<T, ET> getParent() {
	return new FunctionWhere2<T, ET>(getTokens(), parent);
    }

    @Override
    IFunctionCompoundCondition1<T, ET> getParent2() {
	return new FunctionCompoundCondition1<T, ET>(getTokens(), parent);
    }
}
