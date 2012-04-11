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
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().anyOfProps(propertyNames));
	return result;
    }

    @Override
    public T anyOfValues(final Object... values) {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().anyOfValues(values));
	return result;
    }

    @Override
    public T anyOfParams(final String... paramNames) {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().anyOfParams(paramNames));
	return result;
    }

    @Override
    public T anyOfModels(final PrimitiveResultQueryModel... models) {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().anyOfModels(models));
	return result;
    }

    @Override
    public T anyOfExpressions(final ExpressionModel... expressions) {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().anyOfExpressions(expressions));
	return result;
    }

    @Override
    public T allOfProps(final String... propertyNames) {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().allOfProps(propertyNames));
	return result;
    }

    @Override
    public T allOfValues(final Object... values) {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().allOfValues(values));
	return result;
    }

    @Override
    public T allOfParams(final String... paramNames) {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().allOfParams(paramNames));
	return result;
    }

    @Override
    public T allOfModels(final PrimitiveResultQueryModel... models) {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().allOfModels(models));
	return result;
    }

    @Override
    public T allOfExpressions(final ExpressionModel... expressions) {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().allOfExpressions(expressions));
	return result;
    }
}