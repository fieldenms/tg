package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;

abstract class ExprOperand2<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<IExprOperationOrEnd2<T, ET>, IExprOperand3<T, ET>, ET> //
		implements IExprOperand2<T, ET> {

    public ExprOperand2(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForExprOperand2(final Tokens tokens);

	@Override
	protected IExprOperand3<T, ET> nextForExprOperand(final Tokens tokens) {
		return new ExprOperand3<T, ET>(tokens) {

			@Override
			protected T nextForExprOperand3(final Tokens tokens) {
				return ExprOperand2.this.nextForExprOperand2(tokens);
			}

		};
	}

	@Override
	protected IExprOperationOrEnd2<T, ET> nextForSingleOperand(final Tokens tokens) {
		return new ExprOperationOrEnd2<T, ET>(tokens) {

			@Override
			protected T nextForExprOperationOrEnd2(final Tokens tokens) {
				return ExprOperand2.this.nextForExprOperand2(tokens);
			}

		};
	}
}