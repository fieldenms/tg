package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonQuantifiedOperand;

abstract class AbstractExpRightSideConditionalOperand<T> extends AbstractRightSideOperand<T> implements IComparisonQuantifiedOperand<T> {
    protected AbstractExpRightSideConditionalOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public EntityQueryProgressiveInterfaces.IExprOperand0<T> beginExpr() {
	return new ExprOperand0<T>(getTokens().beginExpression(), getParent());
    }
}