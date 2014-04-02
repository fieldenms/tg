package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonQuantifiedOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class AbstractExpRightSideConditionalOperand<T, ET extends AbstractEntity<?>> extends AbstractRightSideOperand<T, ET> implements IComparisonQuantifiedOperand<T, ET> {
    protected AbstractExpRightSideConditionalOperand(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    public IExprOperand0<T, ET> beginExpr() {
        return new ExprOperand0<T, ET>(getTokens().beginExpression(), getParent());
    }
}