package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd;

abstract class AbstractExprOperationOrEnd<T1, T2, ET extends AbstractEntity<?>> //
		extends AbstractArithmeticalOperator<T1> //
		implements IExprOperationOrEnd<T1, T2, ET> {

	protected abstract T2 nextForAbstractExprOperationOrEnd();

	@Override
	public T2 endExpr() {
		return copy(nextForAbstractExprOperationOrEnd(), getTokens().endExpression());
	}
}