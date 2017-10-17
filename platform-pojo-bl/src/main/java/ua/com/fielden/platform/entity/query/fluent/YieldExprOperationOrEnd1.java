package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd1;

abstract class YieldExprOperationOrEnd1<T, ET extends AbstractEntity<?>> //
		extends ExprOperationOrEnd<IYieldExprItem1<T, ET>, IYieldExprOperationOrEnd0<T, ET>, ET> //
		implements IYieldExprOperationOrEnd1<T, ET> {

    protected YieldExprOperationOrEnd1(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForYieldExprOperationOrEnd1(final Tokens tokens);

	@Override
	protected IYieldExprOperationOrEnd0<T, ET> nextForExprOperationOrEnd(final Tokens tokens) {
		return new YieldExprOperationOrEnd0<T, ET>(tokens) {

			@Override
			protected T nextForYieldExprOperationOrEnd0(final Tokens tokens) {
				return YieldExprOperationOrEnd1.this.nextForYieldExprOperationOrEnd1(tokens);
			}

		};
	}

	@Override
	protected IYieldExprItem1<T, ET> nextForArithmeticalOperator(final Tokens tokens) {
		return new YieldExprItem1<T, ET>(tokens) {

			@Override
			protected T nextForYieldExprItem1(final Tokens tokens) {
				return YieldExprOperationOrEnd1.this.nextForYieldExprOperationOrEnd1(tokens);
			}

		};
	}
}