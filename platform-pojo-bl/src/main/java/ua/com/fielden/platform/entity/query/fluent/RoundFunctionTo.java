package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionTo;

abstract class RoundFunctionTo<T> //
		extends AbstractQueryLink //
		implements IRoundFunctionTo<T> {

	protected abstract T nextForRoundFunctionTo();

	@Override
	public T to(final Integer precision) {
		return copy(nextForRoundFunctionTo(), getTokens().to(precision));
	}
}