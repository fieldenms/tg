package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class DateDiffFunctionArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<IDateDiffFunctionBetween<T, ET>, IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>, ET> //
		implements IDateDiffFunctionArgument<T, ET> {

	protected DateDiffFunctionArgument(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForDateDiffFunctionArgument(final EqlSentenceBuilder builder);

	@Override
	protected IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET> nextForExprOperand(final EqlSentenceBuilder builder) {
		return new ExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>(builder) {

			@Override
			protected IDateDiffFunctionBetween<T, ET> nextForExprOperand0(final EqlSentenceBuilder builder) {
				return new DateDiffFunctionBetween<T, ET>(builder) {

					@Override
					protected T nextForDateDiffFunctionBetween(final EqlSentenceBuilder builder) {
						return DateDiffFunctionArgument.this.nextForDateDiffFunctionArgument(builder);
					}

				};
			}

		};
	}

	@Override
	protected IDateDiffFunctionBetween<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
		return new DateDiffFunctionBetween<T, ET>(builder) {

			@Override
			protected T nextForDateDiffFunctionBetween(final EqlSentenceBuilder builder) {
				return DateDiffFunctionArgument.this.nextForDateDiffFunctionArgument(builder);
			}

		};
	}

}
