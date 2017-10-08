package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IMultipleOperand;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;

abstract class MultipleOperand<T, ET extends AbstractEntity<?>> //
		extends SingleOperand<T, ET> //
		implements IMultipleOperand<T, ET> {

    protected MultipleOperand(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	public T anyOfProps(final String... propertyNames) {
		return nextForSingleOperand(getTokens().anyOfProps(propertyNames));
	}

	@Override
	public T anyOfValues(final Object... values) {
		return nextForSingleOperand(getTokens().anyOfValues(values));
	}

	@Override
	public T anyOfParams(final String... paramNames) {
		return nextForSingleOperand(getTokens().anyOfParams(paramNames));
	}

	@Override
	public T anyOfIParams(final String... paramNames) {
		return nextForSingleOperand(getTokens().anyOfIParams(paramNames));
	}

	@Override
	public T anyOfModels(final PrimitiveResultQueryModel... models) {
		return nextForSingleOperand(getTokens().anyOfModels(models));
	}

	@Override
	public T anyOfExpressions(final ExpressionModel... expressions) {
		return nextForSingleOperand(getTokens().anyOfExpressions(expressions));
	}

	@Override
	public T allOfProps(final String... propertyNames) {
		return nextForSingleOperand(getTokens().allOfProps(propertyNames));
	}

	@Override
	public T allOfValues(final Object... values) {
		return nextForSingleOperand(getTokens().allOfValues(values));
	}

	@Override
	public T allOfParams(final String... paramNames) {
		return nextForSingleOperand(getTokens().allOfParams(paramNames));
	}

	@Override
	public T allOfIParams(final String... paramNames) {
		return nextForSingleOperand(getTokens().allOfIParams(paramNames));
	}

	@Override
	public T allOfModels(final PrimitiveResultQueryModel... models) {
		return nextForSingleOperand(getTokens().allOfModels(models));
	}

	@Override
	public T allOfExpressions(final ExpressionModel... expressions) {
		return nextForSingleOperand(getTokens().allOfExpressions(expressions));
	}
}