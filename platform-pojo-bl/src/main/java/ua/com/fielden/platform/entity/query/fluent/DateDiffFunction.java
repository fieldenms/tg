package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;

abstract class DateDiffFunction<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IDateDiffFunction<T, ET> {

    protected DateDiffFunction(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForDateDiffFunction(final Tokens tokens);

	@Override
	public IDateDiffFunctionArgument<T, ET> between() {
		return createDateDiffFunctionArgument(getTokens());
	}

	private DateDiffFunctionArgument<T, ET> createDateDiffFunctionArgument(final Tokens tokens) {
		return new DateDiffFunctionArgument<T, ET>(tokens) {

			@Override
			protected T nextForDateDiffFunctionArgument(final Tokens tokens) {
				return DateDiffFunction.this.nextForDateDiffFunction(tokens);
			}

		};
	}
}