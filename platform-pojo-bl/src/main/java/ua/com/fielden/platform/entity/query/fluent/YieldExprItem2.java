package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;

final class YieldExprItem2<T, ET extends AbstractEntity<?>> extends AbstractYieldExprOperand<IYieldExprOperationOrEnd2<T, ET>, IYieldExprItem3<T, ET>, ET> implements IYieldExprItem2<T, ET> {
    T parent;

    YieldExprItem2(final Tokens queryTokens, final T parent) {
        super(queryTokens);
        this.parent = parent;
    }

    @Override
    IYieldExprItem3<T, ET> getParent2() {
        return new YieldExprItem3<T, ET>(getTokens(), parent);
    }

    @Override
    IYieldExprOperationOrEnd2<T, ET> getParent() {
        return new YieldExprOperationOrEnd2<T, ET>(getTokens(), parent);
    }
}