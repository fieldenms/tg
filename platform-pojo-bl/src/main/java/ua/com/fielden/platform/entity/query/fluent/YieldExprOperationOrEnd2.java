package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;

class YieldExprOperationOrEnd2<T, ET extends AbstractEntity<?>> extends AbstractExprOperationOrEnd<IYieldExprItem2<T, ET>, IYieldExprOperationOrEnd1<T, ET>, ET> implements IYieldExprOperationOrEnd2<T, ET> {
    T parent;

    YieldExprOperationOrEnd2(final Tokens queryTokens, final T parent) {
        super(queryTokens);
        this.parent = parent;
    }

    @Override
    IYieldExprOperationOrEnd1<T, ET> getParent2() {
        return new YieldExprOperationOrEnd1<T, ET>(getTokens(), parent);
    }

    @Override
    IYieldExprItem2<T, ET> getParent() {
        return new YieldExprItem2<T, ET>(getTokens(), parent);
    }
}