package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IMultipleOperand;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;

abstract class AbstractMultipleOperand<T> extends AbstractSingleOperand<T> implements IMultipleOperand<T> {

    protected AbstractMultipleOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T anyOfProps(final String... propertyNames) {
	getTokens().anyOfProps(propertyNames);
	return getParent();
    }

    @Override
    public T anyOfValues(final Object... values) {
	getTokens().anyOfValues(values);
	return getParent();
    }

    @Override
    public T anyOfParams(final String... paramNames) {
	getTokens().anyOfParams(paramNames);
	return getParent();
    }

    @Override
    public T anyOfModels(final PrimitiveResultQueryModel... models) {
	getTokens().anyOfModels(models);
	return getParent();
    }

    @Override
    public T anyOfExpressions(final ExpressionModel... expressions) {
	getTokens().anyOfExpressions(expressions);
	return getParent();
    }

    @Override
    public T allOfProps(final String... propertyNames) {
	getTokens().allOfProps(propertyNames);
	return getParent();
    }

    @Override
    public T allOfValues(final Object... values) {
	getTokens().allOfValues(values);
	return getParent();
    }

    @Override
    public T allOfParams(final String... paramNames) {
	getTokens().allOfParams(paramNames);
	return getParent();
    }

    @Override
    public T allOfModels(final PrimitiveResultQueryModel... models) {
	getTokens().allOfModels(models);
	return getParent();
    }

    @Override
    public T allOfExpressions(final ExpressionModel... expressions) {
	getTokens().allOfExpressions(expressions);
	return getParent();
    }

}
