package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IOthers.IExpArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.IExpRightArgument;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;

abstract class AbstractExpRightArgument<T> extends AbstractLeftSideSubject<T> implements IExpRightArgument<T> {

    protected AbstractExpRightArgument(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    //@Override
    @Override
    abstract T createSearchCondition(final QueryTokens queryTokens);

    @Override
    public T val(final Object... values) {
	return createSearchCondition(this.getTokens().rightVal(values));
    }

    @Override
    public T val(final Object value) {
	return createSearchCondition(this.getTokens().rightVal(value));
    }

    @Override
    public <E extends AbstractEntity> T model(final IQueryModel<E> model) {
	return createSearchCondition(this.getTokens().rightModel(model));
    }

    @Override
    public T model(final IQueryModel... models) {
	return createSearchCondition(this.getTokens().rightModel(models));
    }

    @Override
    public T exp(final String expression, final Object... values) {
	return createSearchCondition(this.getTokens().rightExp(expression, values));
    }

    @Override
    public T param(final String paramName) {
	return createSearchCondition(this.getTokens().rightParam(paramName));
    }

    @Override
    public T prop(final String propertyName) {
	return createSearchCondition(this.getTokens().rightProp(propertyName));
    }

    @Override
    public IExpArgument<T> beginExp() {
	return null;//createSearchCondition(this.getTokens().rightProp(propertyName));
    }
}