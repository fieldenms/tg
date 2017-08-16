package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IMultipleOperand;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;

abstract class MultipleOperand<T, ET extends AbstractEntity<?>> //
		extends SingleOperand<T, ET> //
		implements IMultipleOperand<T, ET> {

	@Override
	public T anyOfProps(final String... propertyNames) {
		return copy(nextForSingleOperand(), getTokens().anyOfProps(propertyNames));
	}

	@Override
	public T anyOfValues(final Object... values) {
		return copy(nextForSingleOperand(), getTokens().anyOfValues(values));
	}

	@Override
	public T anyOfParams(final String... paramNames) {
		return copy(nextForSingleOperand(), getTokens().anyOfParams(paramNames));
	}

	@Override
	public T anyOfIParams(final String... paramNames) {
		return copy(nextForSingleOperand(), getTokens().anyOfIParams(paramNames));
	}

	@Override
	public T anyOfModels(final PrimitiveResultQueryModel... models) {
		return copy(nextForSingleOperand(), getTokens().anyOfModels(models));
	}

	@Override
	public T anyOfExpressions(final ExpressionModel... expressions) {
		return copy(nextForSingleOperand(), getTokens().anyOfExpressions(expressions));
	}

	@Override
	public T allOfProps(final String... propertyNames) {
		return copy(nextForSingleOperand(), getTokens().allOfProps(propertyNames));
	}

	@Override
	public T allOfValues(final Object... values) {
		return copy(nextForSingleOperand(), getTokens().allOfValues(values));
	}

	@Override
	public T allOfParams(final String... paramNames) {
		return copy(nextForSingleOperand(), getTokens().allOfParams(paramNames));
	}

	@Override
	public T allOfIParams(final String... paramNames) {
		return copy(nextForSingleOperand(), getTokens().allOfIParams(paramNames));
	}

	@Override
	public T allOfModels(final PrimitiveResultQueryModel... models) {
		return copy(nextForSingleOperand(), getTokens().allOfModels(models));
	}

	@Override
	public T allOfExpressions(final ExpressionModel... expressions) {
		return copy(nextForSingleOperand(), getTokens().allOfExpressions(expressions));
	}
}