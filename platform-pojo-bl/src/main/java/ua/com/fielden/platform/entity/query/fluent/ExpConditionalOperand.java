package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;

abstract class ExpConditionalOperand<T, ET extends AbstractEntity<?>> //
		extends MultipleOperand<T, ET> //
		implements IComparisonOperand<T, ET> {

	@Override
	public EntityQueryProgressiveInterfaces.IExprOperand0<T, ET> beginExpr() {
		return copy(createExprOperand0(), getTokens().beginExpression());
	}

	private ExprOperand0<T, ET> createExprOperand0() {
		return new ExprOperand0<T, ET>() {

			@Override
			protected T nextForExprOperand0() {
				return ExpConditionalOperand.this.nextForAbstractSingleOperand();
			}

		};
	}
}