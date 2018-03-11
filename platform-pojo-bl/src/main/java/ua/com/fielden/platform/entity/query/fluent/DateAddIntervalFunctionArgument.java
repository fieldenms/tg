package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateAddIntervalFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateAddIntervalUnit;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class DateAddIntervalFunctionArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<IDateAddIntervalUnit<T, ET>, IExprOperand0<IDateAddIntervalUnit<T, ET>, ET>, ET> //
		implements IDateAddIntervalFunctionArgument<T, ET> {

    protected DateAddIntervalFunctionArgument(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForDateAddIntervalFunctionArgument(final Tokens tokens);
	
	@Override
	protected IExprOperand0<IDateAddIntervalUnit<T, ET>, ET> nextForExprOperand(final Tokens tokens) {
		return new ExprOperand0<IDateAddIntervalUnit<T, ET>, ET>(tokens) {

			@Override
			protected IDateAddIntervalUnit<T, ET> nextForExprOperand0(final Tokens tokens) {
				return new DateAddIntervalUnit<T, ET>(tokens) {

					@Override
					protected T nextForDateAddIntervalUnit(final Tokens tokens) {
						return DateAddIntervalFunctionArgument.this.nextForDateAddIntervalFunctionArgument(tokens);
					}

				};
			}

		};
	}
	
	@Override
	protected IDateAddIntervalUnit<T, ET> nextForSingleOperand(final Tokens tokens) {
		return new DateAddIntervalUnit<T, ET>(tokens) {

			@Override
			protected T nextForDateAddIntervalUnit(Tokens tokens) {
				return DateAddIntervalFunctionArgument.this.nextForDateAddIntervalFunctionArgument(tokens);
			}
		};
	}

}