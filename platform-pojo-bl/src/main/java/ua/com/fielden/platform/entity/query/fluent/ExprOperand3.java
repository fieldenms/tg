package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd3;

abstract class ExprOperand3<T, ET extends AbstractEntity<?>> //
		extends SingleOperand<IExprOperationOrEnd3<T, ET>, ET> //
		implements IExprOperand3<T, ET> {

    protected ExprOperand3(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForExprOperand3(final Tokens tokens);

	@Override
	protected IExprOperationOrEnd3<T, ET> nextForSingleOperand(final Tokens tokens) {
		return new ExprOperationOrEnd3<T, ET>(tokens) {

			@Override
			protected T nextForExprOperationOrEnd3(final Tokens tokens) {
				return ExprOperand3.this.nextForExprOperand3(tokens);
			}

		};
	}
}