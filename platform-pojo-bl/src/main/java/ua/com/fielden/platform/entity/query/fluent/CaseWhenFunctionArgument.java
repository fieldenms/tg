package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

final class CaseWhenFunctionArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<ICaseWhenFunctionWhen<T, ET>, IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>, ET> implements ICaseWhenFunctionArgument<T, ET> {
    T parent;

    CaseWhenFunctionArgument(final Tokens queryTokens, final T parent) {
        super(queryTokens);
        this.parent = parent;
    }

    @Override
    IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET> getParent2() {
        return new ExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>(getTokens(), new CaseWhenFunctionWhen<T, ET>(getTokens(), parent));
    }

    @Override
    ICaseWhenFunctionWhen<T, ET> getParent() {
        return new CaseWhenFunctionWhen<T, ET>(getTokens(), parent);
    }
}