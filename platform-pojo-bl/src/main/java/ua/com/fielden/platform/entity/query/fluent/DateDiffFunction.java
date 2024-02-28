package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;

abstract class DateDiffFunction<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IDateDiffFunction<T, ET> {

	protected DateDiffFunction(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForDateDiffFunction(final EqlSentenceBuilder builder);

	@Override
	public IDateDiffFunctionArgument<T, ET> between() {
		return createDateDiffFunctionArgument(builder);
	}

	private DateDiffFunctionArgument<T, ET> createDateDiffFunctionArgument(final EqlSentenceBuilder builder) {
		return new DateDiffFunctionArgument<T, ET>(builder) {

			@Override
			protected T nextForDateDiffFunctionArgument(final EqlSentenceBuilder builder) {
				return DateDiffFunction.this.nextForDateDiffFunction(builder);
			}

		};
	}

}
