package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

final class DateDiffFunctionArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IDateDiffFunctionBetween<T, ET>, IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>, ET> implements IDateDiffFunctionArgument<T, ET> {
    T parent;

    DateDiffFunctionArgument(final Tokens queryTokens, final T parent) {
        super(queryTokens);
        this.parent = parent;
    }

    @Override
    IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET> getParent2() {
        return new ExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>(getTokens(), new DateDiffFunctionBetween<T, ET>(getTokens(), parent));
    }

    @Override
    IDateDiffFunctionBetween<T, ET> getParent() {
        return new DateDiffFunctionBetween<T, ET>(getTokens(), parent);
    }
}