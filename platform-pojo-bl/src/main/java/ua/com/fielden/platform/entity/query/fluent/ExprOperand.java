package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand;

abstract class ExprOperand<T1, T2, ET extends AbstractEntity<?>> //
		extends SingleOperand<T1, ET> //
		implements IExprOperand<T1, T2, ET> {

	protected abstract T2 nextForAbstractExprOperand();

	@Override
	public T2 beginExpr() {
		return copy(nextForAbstractExprOperand(), getTokens().beginExpression());
	}
}