package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonSetOperand;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

abstract class AbstractSetOfOperands<T> extends AbstractSingleOperand<T> implements IComparisonSetOperand<T> {
    protected AbstractSetOfOperands(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public <E extends Object> T values(final E... values) {
	getTokens().setOfValues(values);
	return getParent();
    }

    @Override
    public T props(final String... properties) {
	getTokens().setOfProps(properties);
	return getParent();
    }

    @Override
    public T params(final String... paramNames) {
	getTokens().setOfParams(paramNames);
	return getParent();
    }

    @Override
    public <E extends AbstractEntity> T model(final SingleResultQueryModel model) {
	getTokens().model(model);
	return getParent();
    }
}
