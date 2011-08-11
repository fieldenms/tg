package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;

abstract class AbstractExpConditionalOperand<T> extends AbstractMultipleOperand<T> implements IComparisonOperand<T> {
    protected AbstractExpConditionalOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public EntityQueryProgressiveInterfaces.IExprOperand0<T> beginExpr() {
	return new ExprOperand0<T>(getTokens().beginExpression(), getParent());
    }
}