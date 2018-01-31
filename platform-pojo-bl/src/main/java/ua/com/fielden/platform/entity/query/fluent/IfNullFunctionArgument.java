package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionThen;

abstract class IfNullFunctionArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<IIfNullFunctionThen<T, ET>, IExprOperand0<IIfNullFunctionThen<T, ET>, ET>, ET> //
		implements IIfNullFunctionArgument<T, ET> {

    protected IfNullFunctionArgument(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForIfNullFunctionArgument(final Tokens tokens);

	@Override
	protected IExprOperand0<IIfNullFunctionThen<T, ET>, ET> nextForExprOperand(final Tokens tokens) {
		return new ExprOperand0<IIfNullFunctionThen<T, ET>, ET>(tokens) {

			@Override
			protected IIfNullFunctionThen<T, ET> nextForExprOperand0(final Tokens tokens) {
				return new IfNullFunctionThen<T, ET>(tokens) {

					@Override
					protected T nextForIfNullFunctionThen(final Tokens tokens) {
						return IfNullFunctionArgument.this.nextForIfNullFunctionArgument(tokens);
					}

				};
			}

		};
	}

	@Override
	protected IIfNullFunctionThen<T, ET> nextForSingleOperand(final Tokens tokens) {
		return new IfNullFunctionThen<T, ET>(tokens) {

			@Override
			protected T nextForIfNullFunctionThen(final Tokens tokens) {
				return IfNullFunctionArgument.this.nextForIfNullFunctionArgument(tokens);
			}

		};
	}
}