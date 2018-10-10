package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonQuantifiedOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class ExpRightSideConditionalOperand<T, ET extends AbstractEntity<?>> //
		extends RightSideOperand<T, ET> //
		implements IComparisonQuantifiedOperand<T, ET> {
    
    protected ExpRightSideConditionalOperand(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	public IExprOperand0<T, ET> beginExpr() {
		return createExprOperand0(getTokens().beginExpression());
	}

	private ExprOperand0<T, ET> createExprOperand0(final Tokens tokens) {
		return new ExprOperand0<T, ET>(tokens) {

			@Override
			protected T nextForExprOperand0(final Tokens tokens) {
				return ExpRightSideConditionalOperand.this.nextForSingleOperand(tokens);
			}

		};
	}
}