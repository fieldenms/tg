package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionThen;

abstract class IfNullFunctionThen<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IIfNullFunctionThen<T, ET> {

    protected IfNullFunctionThen(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForIfNullFunctionThen(final Tokens tokens);

	@Override
	public IFunctionLastArgument<T, ET> then() {
		return createFunctionLastArgument(getTokens());
	}

	private FunctionLastArgument<T, ET> createFunctionLastArgument(final Tokens tokens) {
		return new FunctionLastArgument<T, ET>(tokens) {

			@Override
			protected T nextForFunctionLastArgument(final Tokens tokens) {
				return IfNullFunctionThen.this.nextForIfNullFunctionThen(tokens);
			}

		};
	}
}