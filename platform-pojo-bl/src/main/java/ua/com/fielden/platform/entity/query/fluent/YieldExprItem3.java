package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd3;

abstract class YieldExprItem3<T, ET extends AbstractEntity<?>> //
		extends YieldedItem<IYieldExprOperationOrEnd3<T, ET>, ET> //
		implements IYieldExprItem3<T, ET> {

    protected YieldExprItem3(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForYieldExprItem3(final Tokens tokens);

	@Override
	protected IYieldExprOperationOrEnd3<T, ET> nextForSingleOperand(final Tokens tokens) {
		return new YieldExprOperationOrEnd3<T, ET>(tokens) {

			@Override
			protected T nextForYieldExprOperationOrEnd3(final Tokens tokens) {
				return YieldExprItem3.this.nextForYieldExprItem3(tokens);
			}

		};
	}
}