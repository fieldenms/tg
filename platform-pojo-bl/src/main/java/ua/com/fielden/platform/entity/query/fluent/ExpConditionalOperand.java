package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;

abstract class ExpConditionalOperand<T, ET extends AbstractEntity<?>> //
		extends MultipleOperand<T, ET> //
		implements IComparisonOperand<T, ET> {

    protected ExpConditionalOperand(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	public EntityQueryProgressiveInterfaces.IExprOperand0<T, ET> beginExpr() {
		return createExprOperand0(getTokens().beginExpression());
	}

	private ExprOperand0<T, ET> createExprOperand0(final Tokens tokens) {
		return new ExprOperand0<T, ET>(tokens) {

			@Override
			protected T nextForExprOperand0(final Tokens tokens) {
				return ExpConditionalOperand.this.nextForSingleOperand(tokens);
			}

		};
	}
}