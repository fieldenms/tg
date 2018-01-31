package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;

abstract class YieldExprOperationOrEnd0<T, ET extends AbstractEntity<?>> //
		extends ExprOperationOrEnd<IYieldExprItem0<T, ET>, T, ET> //
		implements IYieldExprOperationOrEnd0<T, ET> {

    protected YieldExprOperationOrEnd0(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForYieldExprOperationOrEnd0(final Tokens tokens);

	@Override
	protected T nextForExprOperationOrEnd(final Tokens tokens) {
		return nextForYieldExprOperationOrEnd0(tokens);
	}

	@Override
	protected IYieldExprItem0<T, ET> nextForArithmeticalOperator(final Tokens tokens) {
		return new YieldExprItem0<T, ET>(tokens) {

			@Override
			protected T nextForYieldExprItem0(final Tokens tokens) {
				return YieldExprOperationOrEnd0.this.nextForYieldExprOperationOrEnd0(tokens);
			}

		};
	}
}