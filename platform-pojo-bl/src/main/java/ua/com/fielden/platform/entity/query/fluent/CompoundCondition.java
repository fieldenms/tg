package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition;

abstract class CompoundCondition<T1, T2> //
		extends LogicalCondition<T1> //
		implements ICompoundCondition<T1, T2> {

	protected CompoundCondition(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T2 nextForCompoundCondition(final EqlSentenceBuilder builder);

	@Override
	public T2 end() {
		return nextForCompoundCondition(builder.endCondition());
	}

}
