package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;

abstract class ExprOperationOrEnd0<T, ET extends AbstractEntity<?>> //
		extends ExprOperationOrEnd<IExprOperand0<T, ET>, T, ET> //
		implements IExprOperationOrEnd0<T, ET> {

    protected ExprOperationOrEnd0(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForExprOperationOrEnd0(final Tokens tokens);

	@Override
	protected IExprOperand0<T, ET> nextForArithmeticalOperator(final Tokens tokens) {
		return new ExprOperand0<T, ET>(tokens) {

			@Override
			protected T nextForExprOperand0(final Tokens tokens) {
				return ExprOperationOrEnd0.this.nextForExprOperationOrEnd0(tokens);
			}

		};
	}

	@Override
	protected T nextForExprOperationOrEnd(final Tokens tokens) {
		return nextForExprOperationOrEnd0(tokens);
	}
}