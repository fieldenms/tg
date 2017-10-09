package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd3;

abstract class YieldExprOperationOrEnd3<T, ET extends AbstractEntity<?>> //
		extends ExprOperationOrEnd<IYieldExprItem3<T, ET>, IYieldExprOperationOrEnd2<T, ET>, ET> //
		implements IYieldExprOperationOrEnd3<T, ET> {

    protected YieldExprOperationOrEnd3(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForYieldExprOperationOrEnd3(final Tokens tokens);

	@Override
	protected IYieldExprOperationOrEnd2<T, ET> nextForExprOperationOrEnd(final Tokens tokens) {
		return new YieldExprOperationOrEnd2<T, ET>(tokens) {

			@Override
			protected T nextForYieldExprOperationOrEnd2(final Tokens tokens) {
				return YieldExprOperationOrEnd3.this.nextForYieldExprOperationOrEnd3(tokens);
			}

		};
	}

	@Override
	protected IYieldExprItem3<T, ET> nextForArithmeticalOperator(final Tokens tokens) {
		return new YieldExprItem3<T, ET>(tokens) {

			@Override
			protected T nextForYieldExprItem3(final Tokens tokens) {
				return YieldExprOperationOrEnd3.this.nextForYieldExprOperationOrEnd3(tokens);
			}

		};
	}
}