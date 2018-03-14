package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateAddIntervalFunctionTo;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

abstract class DateAddIntervalFunctionTo<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IDateAddIntervalFunctionTo<T, ET> {

    protected DateAddIntervalFunctionTo(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForDateAddIntervalFunctionTo(final Tokens tokens);

	@Override
	public IFunctionLastArgument<T, ET> to() {
		return createFunctionLastArgument(getTokens());
	}

	private FunctionLastArgument<T, ET> createFunctionLastArgument(final Tokens tokens) {
		return new FunctionLastArgument<T, ET>(tokens) {

			@Override
			protected T nextForFunctionLastArgument(final Tokens tokens) {
				return DateAddIntervalFunctionTo.this.nextForDateAddIntervalFunctionTo(tokens);
			}

		};
	}
}