package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IMultipleOperand;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;

abstract class AbstractMultipleOperand<T, ET extends AbstractEntity<?>> extends AbstractSingleOperand<T, ET> implements IMultipleOperand<T, ET> {

    protected AbstractMultipleOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T anyOfProps(final String... propertyNames) {
	return copy(getParent(), getTokens().anyOfProps(propertyNames));
    }

    @Override
    public T anyOfValues(final Object... values) {
	return copy(getParent(), getTokens().anyOfValues(values));
    }

    @Override
    public T anyOfParams(final String... paramNames) {
	return copy(getParent(), getTokens().anyOfParams(paramNames));
    }

    @Override
    public T anyOfIParams(final String... paramNames) {
	return copy(getParent(), getTokens().anyOfIParams(paramNames));
    }


    @Override
    public T anyOfModels(final PrimitiveResultQueryModel... models) {
	return copy(getParent(), getTokens().anyOfModels(models));
    }

    @Override
    public T anyOfExpressions(final ExpressionModel... expressions) {
	return copy(getParent(), getTokens().anyOfExpressions(expressions));
    }

    @Override
    public T allOfProps(final String... propertyNames) {
	return copy(getParent(), getTokens().allOfProps(propertyNames));
    }

    @Override
    public T allOfValues(final Object... values) {
	return copy(getParent(), getTokens().allOfValues(values));
    }

    @Override
    public T allOfParams(final String... paramNames) {
	return copy(getParent(), getTokens().allOfParams(paramNames));
    }

    @Override
    public T allOfIParams(final String... paramNames) {
	return copy(getParent(), getTokens().allOfIParams(paramNames));
    }

    @Override
    public T allOfModels(final PrimitiveResultQueryModel... models) {
	return copy(getParent(), getTokens().allOfModels(models));
    }

    @Override
    public T allOfExpressions(final ExpressionModel... expressions) {
	return copy(getParent(), getTokens().allOfExpressions(expressions));
    }
}