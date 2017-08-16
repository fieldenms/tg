package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonQuantifiedOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class ExpRightSideConditionalOperand<T, ET extends AbstractEntity<?>> //
		extends RightSideOperand<T, ET> //
		implements IComparisonQuantifiedOperand<T, ET> {

	@Override
	public IExprOperand0<T, ET> beginExpr() {
		return copy(createExprOperand0(), getTokens().beginExpression());
	}

	private ExprOperand0<T, ET> createExprOperand0() {
		return new ExprOperand0<T, ET>() {

			@Override
			protected T nextForExprOperand0() {
				return ExpRightSideConditionalOperand.this.nextForAbstractSingleOperand();
			}

		};
	}
}