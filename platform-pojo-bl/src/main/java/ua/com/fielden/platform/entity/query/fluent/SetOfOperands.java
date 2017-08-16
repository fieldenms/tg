package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonSetOperand;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

abstract class SetOfOperands<T, ET extends AbstractEntity<?>> //
		extends SingleOperand<T, ET> //
		implements IComparisonSetOperand<T> {

	@Override
	public <E extends Object> T values(final E... values) {
		if (values.length == 0) {
			throw new EqlException("At least one value is expected when calling [values].");
		} else {
			return copy(nextForAbstractSingleOperand(), getTokens().setOfValues(values));
		}
	}

	@Override
	public T props(final String... properties) {
		return copy(nextForAbstractSingleOperand(), getTokens().setOfProps(properties));
	}

	@Override
	public T params(final String... paramNames) {
		return copy(nextForAbstractSingleOperand(), getTokens().setOfParams(paramNames));
	}

	@Override
	public T iParams(final String... paramNames) {
		return copy(nextForAbstractSingleOperand(), getTokens().setOfIParams(paramNames));
	}

	@Override
	public T model(final SingleResultQueryModel model) {
		return copy(nextForAbstractSingleOperand(), getTokens().model(model));
	}
}