package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd1;

abstract class YieldExprItem1<T, ET extends AbstractEntity<?>> //
		extends YieldExprOperand<IYieldExprOperationOrEnd1<T, ET>, IYieldExprItem2<T, ET>, ET> //
		implements IYieldExprItem1<T, ET> {

    protected YieldExprItem1(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForYieldExprItem1(final Tokens tokens);

	@Override
	protected IYieldExprItem2<T, ET> nextForYieldExprOperand(final Tokens tokens) {
		return new YieldExprItem2<T, ET>(tokens) {

			@Override
			protected T nextForYieldExprItem2(final Tokens tokens) {
				return YieldExprItem1.this.nextForYieldExprItem1(tokens);
			}

		};
	}

	@Override
	protected IYieldExprOperationOrEnd1<T, ET> nextForSingleOperand(final Tokens tokens) {
		return new YieldExprOperationOrEnd1<T, ET>(tokens) {

			@Override
			protected T nextForYieldExprOperationOrEnd1(final Tokens tokens) {
				return YieldExprItem1.this.nextForYieldExprItem1(tokens);
			}

		};
	}
}