package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

final class ConcatFunctionArgument<T> extends AbstractExprOperand<IConcatFunctionWith<T>, IExprOperand0<IConcatFunctionWith<T>>> implements IConcatFunctionArgument<T> {
    T parent;
    ConcatFunctionArgument(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand0<IConcatFunctionWith<T>> getParent2() {
	return new ExprOperand0<IConcatFunctionWith<T>>(getTokens(), new ConcatFunctionWith<T>(getTokens(), parent));
    }

    @Override
    IConcatFunctionWith<T> getParent() {
	return new ConcatFunctionWith<T>(getTokens(), parent);
    }
}