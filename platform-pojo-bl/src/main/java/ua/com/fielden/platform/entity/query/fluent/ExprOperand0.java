package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;

abstract class ExprOperand0<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<IExprOperationOrEnd0<T, ET>, IExprOperand1<T, ET>, ET> //
		implements IExprOperand0<T, ET> {

    protected ExprOperand0(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForExprOperand0(final Tokens tokens);

	@Override
	protected IExprOperationOrEnd0<T, ET> nextForSingleOperand(final Tokens tokens) {
		return new ExprOperationOrEnd0<T, ET>(tokens) {

			@Override
			protected T nextForExprOperationOrEnd0(final Tokens tokens) {
				return ExprOperand0.this.nextForExprOperand0(tokens);
			}

		};
	}

	@Override
	protected IExprOperand1<T, ET> nextForExprOperand(final Tokens tokens) {
		return new ExprOperand1<T, ET>(tokens) {

			@Override
			protected T nextForExprOperand1(final Tokens tokens) {
				return ExprOperand0.this.nextForExprOperand0(tokens);
			}

		};
	}
}