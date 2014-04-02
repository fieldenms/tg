package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;

class FunctionComparisonOperator1<T, ET extends AbstractEntity<?>> extends AbstractComparisonOperator<IFunctionCompoundCondition1<T, ET>, ET> implements IFunctionComparisonOperator1<T, ET> {
    T parent;

    FunctionComparisonOperator1(final Tokens queryTokens, final T parent) {
        super(queryTokens);
        this.parent = parent;
    }

    @Override
    IFunctionCompoundCondition1<T, ET> getParent1() {
        return new FunctionCompoundCondition1<T, ET>(getTokens(), parent);
    }
}