package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

abstract class DateDiffFunctionBetween<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IDateDiffFunctionBetween<T, ET> {

    protected DateDiffFunctionBetween(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForDateDiffFunctionBetween(final Tokens tokens);

	@Override
	public IFunctionLastArgument<T, ET> and() {
		return createFunctionLastArgument(getTokens());
	}

	private FunctionLastArgument<T, ET> createFunctionLastArgument(final Tokens tokens) {
		return new FunctionLastArgument<T, ET>(tokens) {

			@Override
			protected T nextForFunctionLastArgument(final Tokens tokens) {
				return DateDiffFunctionBetween.this.nextForDateDiffFunctionBetween(tokens);
			}

		};
	}
}