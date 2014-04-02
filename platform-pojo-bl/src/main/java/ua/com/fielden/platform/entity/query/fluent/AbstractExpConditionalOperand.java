package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;

abstract class AbstractExpConditionalOperand<T, ET extends AbstractEntity<?>> extends AbstractMultipleOperand<T, ET> implements IComparisonOperand<T, ET> {
    protected AbstractExpConditionalOperand(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    public EntityQueryProgressiveInterfaces.IExprOperand0<T, ET> beginExpr() {
        return new ExprOperand0<T, ET>(getTokens().beginExpression(), getParent());
    }
}