package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

final class DateDiffFunctionArgument<T> extends AbstractExprOperand<IDateDiffFunctionBetween<T>, IExprOperand0<IDateDiffFunctionBetween<T>>> implements IDateDiffFunctionArgument<T> {
    T parent;
    DateDiffFunctionArgument(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand0<IDateDiffFunctionBetween<T>> getParent2() {
	return new ExprOperand0<IDateDiffFunctionBetween<T>>(getTokens(), new DateDiffFunctionBetween<T>(getTokens(), parent));
    }

    @Override
    IDateDiffFunctionBetween<T> getParent() {
	return new DateDiffFunctionBetween<T>(getTokens(), parent);
    }
}
