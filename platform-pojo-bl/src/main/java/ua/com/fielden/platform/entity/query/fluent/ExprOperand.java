package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand;

abstract class ExprOperand<T1, T2, ET extends AbstractEntity<?>> //
		extends SingleOperand<T1, ET> //
		implements IExprOperand<T1, T2, ET> {

    protected ExprOperand(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T2 nextForExprOperand(final Tokens tokens);

	@Override
	public T2 beginExpr() {
		return nextForExprOperand(getTokens().beginExpression());
	}
}