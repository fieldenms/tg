package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd3;

final class ExprOperationOrEnd3<T, ET extends AbstractEntity<?>> extends AbstractExprOperationOrEnd<IExprOperand3<T, ET>, IExprOperationOrEnd2<T, ET>, ET> implements IExprOperationOrEnd3<T, ET> {
    T parent;

    ExprOperationOrEnd3(final Tokens queryTokens, final T parent) {
        super(queryTokens);
        this.parent = parent;
    }

    @Override
    IExprOperationOrEnd2<T, ET> getParent2() {
        return new ExprOperationOrEnd2<T, ET>(getTokens(), parent);
    }

    @Override
    IExprOperand3<T, ET> getParent() {
        return new ExprOperand3<T, ET>(getTokens(), parent);
    }
}