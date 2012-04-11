package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonSetOperand;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

abstract class AbstractSetOfOperands<T> extends AbstractSingleOperand<T> implements IComparisonSetOperand<T> {
    protected AbstractSetOfOperands(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public <E extends Object> T values(final E... values) {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().setOfValues(values));
	return result;
    }

    @Override
    public T props(final String... properties) {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().setOfProps(properties));
	return result;
    }

    @Override
    public T params(final String... paramNames) {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().setOfParams(paramNames));
	return result;
    }

    @Override
    public T model(final SingleResultQueryModel model) {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().model(model));
	return result;
    }
}