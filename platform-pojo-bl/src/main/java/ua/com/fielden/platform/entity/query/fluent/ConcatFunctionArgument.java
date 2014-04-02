package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

final class ConcatFunctionArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IConcatFunctionWith<T, ET>, IExprOperand0<IConcatFunctionWith<T, ET>, ET>, ET> implements IConcatFunctionArgument<T, ET> {
    T parent;

    ConcatFunctionArgument(final Tokens queryTokens, final T parent) {
        super(queryTokens);
        this.parent = parent;
    }

    @Override
    IExprOperand0<IConcatFunctionWith<T, ET>, ET> getParent2() {
        return new ExprOperand0<IConcatFunctionWith<T, ET>, ET>(getTokens(), new ConcatFunctionWith<T, ET>(getTokens(), parent));
    }

    @Override
    IConcatFunctionWith<T, ET> getParent() {
        return new ConcatFunctionWith<T, ET>(getTokens(), parent);
    }
}