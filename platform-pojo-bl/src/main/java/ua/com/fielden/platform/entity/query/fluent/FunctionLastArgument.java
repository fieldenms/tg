package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

abstract class FunctionLastArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<T, IExprOperand0<T, ET>, ET> //
		implements IFunctionLastArgument<T, ET> {

    protected FunctionLastArgument(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForFunctionLastArgument(final Tokens tokens);

	@Override
	protected IExprOperand0<T, ET> nextForExprOperand(final Tokens tokens) {
		return new ExprOperand0<T, ET>(tokens) {

			@Override
			protected T nextForExprOperand0(final Tokens tokens) {
				return FunctionLastArgument.this.nextForFunctionLastArgument(tokens);
			}

		};
	}

	@Override
	protected T nextForSingleOperand(final Tokens tokens) {
		return nextForFunctionLastArgument(tokens);
	}
}