package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateAddIntervalFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateAddIntervalUnit;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class DateAddIntervalFunctionArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<IDateAddIntervalUnit<T, ET>, IExprOperand0<IDateAddIntervalUnit<T, ET>, ET>, ET> //
		implements IDateAddIntervalFunctionArgument<T, ET> {

	protected DateAddIntervalFunctionArgument(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForDateAddIntervalFunctionArgument(final EqlSentenceBuilder builder);

	@Override
	protected IExprOperand0<IDateAddIntervalUnit<T, ET>, ET> nextForExprOperand(final EqlSentenceBuilder builder) {
		return new ExprOperand0<IDateAddIntervalUnit<T, ET>, ET>(builder) {

			@Override
			protected IDateAddIntervalUnit<T, ET> nextForExprOperand0(final EqlSentenceBuilder builder) {
				return new DateAddIntervalUnit<T, ET>(builder) {

					@Override
					protected T nextForDateAddIntervalUnit(final EqlSentenceBuilder builder) {
						return DateAddIntervalFunctionArgument.this.nextForDateAddIntervalFunctionArgument(builder);
					}

				};
			}

		};
	}

	@Override
	protected IDateAddIntervalUnit<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
		return new DateAddIntervalUnit<T, ET>(builder) {

			@Override
			protected T nextForDateAddIntervalUnit(EqlSentenceBuilder builder) {
				return DateAddIntervalFunctionArgument.this.nextForDateAddIntervalFunctionArgument(builder);
			}
		};
	}

}
