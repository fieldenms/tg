package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

final class FunctionWhere3<T, ET extends AbstractEntity<?>> extends AbstractConditionalOperand<IFunctionComparisonOperator3<T, ET>, IFunctionCompoundCondition3<T, ET>, ET> implements IFunctionWhere3<T, ET> {
    T parent;

    FunctionWhere3(final Tokens queryTokens, final T parent) {
        super(queryTokens);
        this.parent = parent;
    }

    @Override
    IFunctionCompoundCondition3<T, ET> getParent2() {
        return new FunctionCompoundCondition3<T, ET>(getTokens(), parent);
    }

    @Override
    IFunctionComparisonOperator3<T, ET> getParent() {
        return new FunctionComparisonOperator3<T, ET>(getTokens(), parent);
    }
}