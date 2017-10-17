package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;

abstract class YieldExprItem0<T, ET extends AbstractEntity<?>> //
		extends YieldExprOperand<IYieldExprOperationOrEnd0<T, ET>, IYieldExprItem1<T, ET>, ET> //
		implements IYieldExprItem0<T, ET> {

    protected YieldExprItem0(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForYieldExprItem0(final Tokens tokens);

	@Override
	protected IYieldExprItem1<T, ET> nextForYieldExprOperand(final Tokens tokens) {
		return new YieldExprItem1<T, ET>(tokens) {

			@Override
			protected T nextForYieldExprItem1(final Tokens tokens) {
				return YieldExprItem0.this.nextForYieldExprItem0(tokens);
			}

		};
	}

	@Override
	protected IYieldExprOperationOrEnd0<T, ET> nextForSingleOperand(final Tokens tokens) {
		return new YieldExprOperationOrEnd0<T, ET>(tokens) {
			@Override
			protected T nextForYieldExprOperationOrEnd0(final Tokens tokens) {
				return YieldExprItem0.this.nextForYieldExprItem0(tokens);
			}

		};
	}
}