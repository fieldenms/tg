package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;

abstract class ExprOperationOrEnd2<T, ET extends AbstractEntity<?>> //
		extends ExprOperationOrEnd<IExprOperand2<T, ET>, IExprOperationOrEnd1<T, ET>, ET> //
		implements IExprOperationOrEnd2<T, ET> {

    protected ExprOperationOrEnd2(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForExprOperationOrEnd2(final Tokens tokens);

	@Override
	protected IExprOperationOrEnd1<T, ET> nextForExprOperationOrEnd(final Tokens tokens) {
		return new ExprOperationOrEnd1<T, ET>(tokens) {

			@Override
			protected T nextForExprOperationOrEnd1(final Tokens tokens) {
				return ExprOperationOrEnd2.this.nextForExprOperationOrEnd2(tokens);
			}

		};
	}

	@Override
	protected IExprOperand2<T, ET> nextForArithmeticalOperator(final Tokens tokens) {
		return new ExprOperand2<T, ET>(tokens) {

			@Override
			protected T nextForExprOperand2(final Tokens tokens) {
				return ExprOperationOrEnd2.this.nextForExprOperationOrEnd2(tokens);
			}

		};
	}
}