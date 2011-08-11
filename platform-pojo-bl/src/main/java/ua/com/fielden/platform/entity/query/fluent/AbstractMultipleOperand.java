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
}
