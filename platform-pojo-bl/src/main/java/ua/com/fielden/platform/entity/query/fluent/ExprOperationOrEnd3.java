package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd3;

abstract class ExprOperationOrEnd3<T, ET extends AbstractEntity<?>> //
		extends ExprOperationOrEnd<IExprOperand3<T, ET>, IExprOperationOrEnd2<T, ET>, ET> //
		implements IExprOperationOrEnd3<T, ET> {

    protected ExprOperationOrEnd3(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForExprOperationOrEnd3(final Tokens tokens);

	@Override
	protected IExprOperationOrEnd2<T, ET> nextForExprOperationOrEnd(final Tokens tokens) {
		return new ExprOperationOrEnd2<T, ET>(tokens) {

			@Override
			protected T nextForExprOperationOrEnd2(final Tokens tokens) {
				return ExprOperationOrEnd3.this.nextForExprOperationOrEnd3(tokens);
			}

		};
	}

	@Override
	protected IExprOperand3<T, ET> nextForArithmeticalOperator(final Tokens tokens) {
		return new ExprOperand3<T, ET>(tokens) {

			@Override
			protected T nextForExprOperand3(final Tokens tokens) {
				return ExprOperationOrEnd3.this.nextForExprOperationOrEnd3(tokens);
			}
		};
	}
}