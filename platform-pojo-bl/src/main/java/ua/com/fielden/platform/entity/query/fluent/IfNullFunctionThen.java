package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionThen;

abstract class IfNullFunctionThen<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IIfNullFunctionThen<T, ET> {

	protected abstract T nextForIfNullFunctionThen();

	@Override
	public IFunctionLastArgument<T, ET> then() {
		return copy(new FunctionLastArgument<T, ET>() {

			@Override
			protected T nextForFunctionLastArgument() {
				return IfNullFunctionThen.this.nextForIfNullFunctionThen();
			}

		}, getTokens());
	}
}