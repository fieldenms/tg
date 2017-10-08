package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;

abstract class ExprOperand1<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<IExprOperationOrEnd1<T, ET>, IExprOperand2<T, ET>, ET> //
		implements IExprOperand1<T, ET> {

    protected ExprOperand1(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForExprOperand1(final Tokens tokens);

	@Override
	protected IExprOperand2<T, ET> nextForExprOperand(final Tokens tokens) {
		return new ExprOperand2<T, ET>(tokens) {

			@Override
			protected T nextForExprOperand2(final Tokens tokens) {
				return ExprOperand1.this.nextForExprOperand1(tokens);
			}

		};
	}

	@Override
	protected IExprOperationOrEnd1<T, ET> nextForSingleOperand(final Tokens tokens) {
		return new ExprOperationOrEnd1<T, ET>(tokens) {

			@Override
			protected T nextForExprOperationOrEnd1(final Tokens tokens) {
				return ExprOperand1.this.nextForExprOperand1(tokens);
			}

		};
	}
}