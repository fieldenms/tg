package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionThen;

abstract class IfNullFunctionThen<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IIfNullFunctionThen<T, ET> {

	protected IfNullFunctionThen(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForIfNullFunctionThen(final EqlSentenceBuilder builder);

	@Override
	public IFunctionLastArgument<T, ET> then() {
		return createFunctionLastArgument(builder);
	}

	private FunctionLastArgument<T, ET> createFunctionLastArgument(final EqlSentenceBuilder builder) {
		return new FunctionLastArgument<T, ET>(builder) {

			@Override
			protected T nextForFunctionLastArgument(final EqlSentenceBuilder builder) {
				return IfNullFunctionThen.this.nextForIfNullFunctionThen(builder);
			}

		};
	}

}
