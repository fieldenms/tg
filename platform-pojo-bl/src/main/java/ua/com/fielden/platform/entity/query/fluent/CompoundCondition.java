package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition;

abstract class CompoundCondition<T1, T2> //
		extends LogicalCondition<T1> //
		implements ICompoundCondition<T1, T2> {

	protected abstract T2 nextForAbstractCompoundCondition();

	@Override
	public T2 end() {
		return copy(nextForAbstractCompoundCondition(), getTokens().endCondition());
	}
}