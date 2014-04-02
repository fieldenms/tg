package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;

final class FunctionComparisonOperator0<T, ET extends AbstractEntity<?>> extends AbstractComparisonOperator<IFunctionCompoundCondition0<T, ET>, ET> implements IFunctionComparisonOperator0<T, ET> {
    T parent;

    FunctionComparisonOperator0(final Tokens queryTokens, final T parent) {
        super(queryTokens);
        this.parent = parent;
    }

    @Override
    IFunctionCompoundCondition0<T, ET> getParent1() {
        return new FunctionCompoundCondition0<T, ET>(getTokens(), parent);
    }
}